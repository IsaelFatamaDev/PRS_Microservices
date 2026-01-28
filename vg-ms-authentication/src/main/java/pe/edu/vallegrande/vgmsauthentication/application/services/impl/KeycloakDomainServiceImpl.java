package pe.edu.vallegrande.vgmsauthentication.application.services.impl;

import jakarta.annotation.PostConstruct;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import pe.edu.vallegrande.vgmsauthentication.application.services.KeycloakDomainService;
import pe.edu.vallegrande.vgmsauthentication.infrastructure.utils.JwtTokenExtractor;
import pe.edu.vallegrande.vgmsauthentication.infrastructure.utils.PasswordGenerator;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.*;

import java.util.Collections;
import java.util.List;

/**
 * Implementación del servicio de dominio para Keycloak
 */
@Service
@Slf4j
public class KeycloakDomainServiceImpl implements KeycloakDomainService {

     private final Keycloak keycloak;

     // Constructor explícito (PRS Standard - No usar @RequiredArgsConstructor)
     public KeycloakDomainServiceImpl(Keycloak keycloak) {
          this.keycloak = keycloak;
     }

     @Value("${keycloak.url}")
     private String serverUrl;

     @Value("${keycloak.realm}")
     private String realm;
     @Value("${keycloak.token-duration:1800}") // 30 minutos por defecto
     private int tokenDuration;

     private RealmResource realmResource;

     @PostConstruct
     public void init() {
          this.realmResource = keycloak.realm(realm);
          log.info("KeycloakDomainService inicializado para realm: {}", realm);
     }

     @Override
     public Mono<Void> initializeKeycloakIfNeeded() {
          return Mono.fromCallable(() -> {
               log.info("🔧 KeycloakDomainService - La configuración inicial la maneja KeycloakSetupService");
               log.info("Configuración delegada al servicio de setup automático");
               return null;
          })
                    .subscribeOn(Schedulers.boundedElastic())
                    .then();
     }

     @Override
     public Mono<KeycloakDomainService.UserCreationResult> createUserAccount(String username, String email,
               String firstName, String lastName,
               String temporaryPassword) {
          return Mono.fromCallable(() -> {
               log.info("Creando usuario en Keycloak: {}", username);

               // Crear representación del usuario
               UserRepresentation user = new UserRepresentation();
               user.setUsername(username);
               user.setEmail(email);
               user.setFirstName(firstName);
               user.setLastName(lastName);
               user.setEnabled(true);
               user.setEmailVerified(true);

               // NUEVO: Agregar atributos personalizados que se incluirán en el JWT
               var attributes = new java.util.HashMap<String, java.util.List<String>>();
               attributes.put("firstName", List.of(firstName));
               attributes.put("lastName", List.of(lastName));
               attributes.put("email", List.of(email));
               // Estos atributos se pueden incluir en el token JWT configurando mappers en
               // Keycloak
               user.setAttributes(attributes);

               // Crear usuario
               UsersResource usersResource = realmResource.users();
               Response response = usersResource.create(user);

               if (response.getStatus() != 201) {
                    // Si el error es por username duplicado, lanzar excepción específica
                    if (response.getStatus() == 409) {
                         throw new RuntimeException("El username '" + username + "' ya existe en el sistema");
                    }
                    throw new RuntimeException("Error creando usuario: " + response.getStatusInfo());
               }

               // Obtener ID del usuario creado
               String location = response.getLocation().getPath();
               String userId = location.substring(location.lastIndexOf('/') + 1);

               // ACTUALIZADO: Usar contraseña permanente (no temporal)
               String passwordToUse = (temporaryPassword != null && !temporaryPassword.trim().isEmpty())
                         ? temporaryPassword
                         : PasswordGenerator.generateTemporaryPassword();

               // Establecer contraseña permanente (no temporal)
               setPermanentPassword(userId, passwordToUse);

               log.info(" Usuario creado exitosamente: {} con ID: {} y contraseña temporal: {}",
                         username, userId, passwordToUse);

               // DEVOLVER tanto el ID como la contraseña temporal
               return new KeycloakDomainService.UserCreationResult(userId, passwordToUse);
          })
                    .subscribeOn(Schedulers.boundedElastic());
     }

     /**
      * NUEVO: Actualiza atributos personalizados del usuario en Keycloak
      */
     public Mono<Void> updateUserAttributes(String username, String organizationId, String userId) {
          return Mono.fromCallable(() -> {
               log.info("Actualizando atributos personalizados para usuario: {}", username);

               try {
                    // Buscar usuario por username
                    var users = keycloak.realm(realm).users().search(username, true);
                    if (users.isEmpty()) {
                         throw new RuntimeException("Usuario no encontrado: " + username);
                    }

                    var userResource = keycloak.realm(realm).users().get(users.get(0).getId());
                    var userRepresentation = userResource.toRepresentation();
                    log.info(" [DEBUG] User ID: {}, FederationLink: {}", userRepresentation.getId(),
                              userRepresentation.getFederationLink());

                    // Agregar atributos personalizados
                    var attributes = userRepresentation.getAttributes();
                    if (attributes == null) {
                         attributes = new java.util.HashMap<>();
                    } else {
                         attributes = new java.util.HashMap<>(attributes);
                    }

                    attributes.put("organizationId", new java.util.ArrayList<>(java.util.List.of(organizationId)));
                    attributes.put("userId", new java.util.ArrayList<>(java.util.List.of(userId)));

                    userRepresentation.setAttributes(attributes);

                    // INTENTO: Forzar update usando el recurso de usuarios directamente
                    log.info(" [DEBUG] Enviando update para usuario {}", userRepresentation.getId());
                    keycloak.realm(realm).users().get(userRepresentation.getId()).update(userRepresentation);

                    // VERIFICACIÓN
                    var updatedUser = keycloak.realm(realm).users().get(userRepresentation.getId()).toRepresentation();
                    log.info(" [VERIFICACIÓN] Atributos después de update: {}", updatedUser.getAttributes());

                    log.info(" Atributos actualizados: organizationId={}, userId={}", organizationId, userId);
                    return null;

               } catch (Exception e) {
                    log.error(" Error actualizando atributos para {}: {}", username, e.getMessage());
                    throw new RuntimeException("Error actualizando atributos: " + e.getMessage());
               }
          })
                    .subscribeOn(Schedulers.boundedElastic())
                    .then();
     }

     /**
      * NUEVO: Configura mappers para incluir atributos personalizados en el token
      */
     public Mono<Void> configureTokenMappers() {
          return Mono.fromCallable(() -> {
               log.info("Configurando mappers para atributos personalizados en el token");

               try {
                    var clientsResource = keycloak.realm(realm).clients();
                    var clients = clientsResource.findByClientId("jass-users-service");

                    if (clients.isEmpty()) {
                         throw new RuntimeException("Cliente jass-users-service no encontrado");
                    }

                    var clientResource = clientsResource.get(clients.get(0).getId());

                    // Crear mapper para organizationId
                    createAttributeMapper(clientResource, "organizationId", "organizationId", "String");

                    // Crear mapper para userId (MongoDB ID)
                    createAttributeMapper(clientResource, "userId", "userId", "String");

                    // Crear mapper de debug para verificar que los mappers funcionan
                    createHardcodedMapper(clientResource, "debug-claim", "working");

                    log.info(" Mappers configurados exitosamente");
                    return null;

               } catch (Exception e) {
                    log.error(" Error configurando mappers: {}", e.getMessage());
                    throw new RuntimeException("Error configurando mappers: " + e.getMessage());
               }
          })
                    .subscribeOn(Schedulers.boundedElastic())
                    .then();
     }

     /**
      * Crea un mapper de atributo para el cliente
      */
     private void createAttributeMapper(org.keycloak.admin.client.resource.ClientResource clientResource,
               String mapperName, String attributeName, String claimType) {
          try {
               var existingMappers = clientResource.getProtocolMappers()
                         .getMappers()
                         .stream()
                         .filter(mapper -> mapperName.equals(mapper.getName()))
                         .findFirst();

               var mapper = new org.keycloak.representations.idm.ProtocolMapperRepresentation();
               mapper.setName(mapperName);
               mapper.setProtocol("openid-connect");
               mapper.setProtocolMapper("oidc-usermodel-attribute-mapper");

               var config = new java.util.HashMap<String, String>();
               config.put("user.attribute", attributeName);
               config.put("claim.name", attributeName);
               config.put("jsonType.label", claimType);
               config.put("id.token.claim", "true");
               config.put("access.token.claim", "true");
               config.put("userinfo.token.claim", "true");
               config.put("multivalued", "false");
               config.put("aggregate.attrs", "false");

               mapper.setConfig(config);

               if (existingMappers.isPresent()) {
                    log.info("Mapper {} ya existe, actualizando configuración...", mapperName);
                    mapper.setId(existingMappers.get().getId());
                    clientResource.getProtocolMappers().update(existingMappers.get().getId(), mapper);
               } else {
                    clientResource.getProtocolMappers().createMapper(mapper);
                    log.info(" Mapper {} creado exitosamente", mapperName);
               }

          } catch (Exception e) {
               log.warn("Error configurando mapper {}: {}", mapperName, e.getMessage());
          }
     }

     private void createHardcodedMapper(org.keycloak.admin.client.resource.ClientResource clientResource,
               String mapperName, String claimValue) {
          try {
               var existingMappers = clientResource.getProtocolMappers()
                         .getMappers()
                         .stream()
                         .filter(mapper -> mapperName.equals(mapper.getName()))
                         .findFirst();

               var mapper = new org.keycloak.representations.idm.ProtocolMapperRepresentation();
               mapper.setName(mapperName);
               mapper.setProtocol("openid-connect");
               mapper.setProtocolMapper("oidc-hardcoded-claim-mapper");

               var config = new java.util.HashMap<String, String>();
               config.put("claim.value", claimValue);
               config.put("claim.name", mapperName);
               config.put("jsonType.label", "String");
               config.put("id.token.claim", "true");
               config.put("access.token.claim", "true");
               config.put("userinfo.token.claim", "true");

               mapper.setConfig(config);

               if (existingMappers.isPresent()) {
                    log.info("Mapper {} ya existe, actualizando configuración...", mapperName);
                    mapper.setId(existingMappers.get().getId());
                    clientResource.getProtocolMappers().update(existingMappers.get().getId(), mapper);
               } else {
                    clientResource.getProtocolMappers().createMapper(mapper);
                    log.info(" Mapper {} creado exitosamente", mapperName);
               }

          } catch (Exception e) {
               log.warn("Error configurando mapper {}: {}", mapperName, e.getMessage());
          }
     }

     @Override
     public Mono<Void> assignRoles(String username, String... roles) {
          return Mono.fromCallable(() -> {
               log.info("Asignando roles {} al usuario: {}", List.of(roles), username);

               // Buscar usuario por username
               List<UserRepresentation> users = realmResource.users().search(username, true);
               if (users.isEmpty()) {
                    throw new RuntimeException("Usuario no encontrado: " + username);
               }

               String userId = users.get(0).getId();
               assignRolesByUserIdSync(userId, roles);
               return null;
          })
                    .subscribeOn(Schedulers.boundedElastic())
                    .then();
     }

     @Override
     public Mono<Void> assignRolesByUserId(String userId, String... roles) {
          return Mono.fromCallable(() -> {
               log.info("Asignando roles {} al usuario con ID: {}", List.of(roles), userId);
               assignRolesByUserIdSync(userId, roles);
               return null;
          })
                    .subscribeOn(Schedulers.boundedElastic())
                    .then();
     }

     /**
      * OPTIMIZADO: Asigna roles de forma síncrona (sin búsqueda)
      */
     private void assignRolesByUserIdSync(String userId, String... roles) {
          var userResource = realmResource.users().get(userId);
          var realmRolesResource = realmResource.roles();

          for (String roleName : roles) {
               try {
                    RoleRepresentation role = realmRolesResource.get(roleName).toRepresentation();
                    userResource.roles().realmLevel().add(Collections.singletonList(role));
                    log.debug("Rol {} asignado al usuario {}", roleName, userId);
               } catch (Exception e) {
                    log.warn("Error asignando rol {} al usuario {}: {}", roleName, userId, e.getMessage());
               }
          }
     }

     @Override
     public Mono<KeycloakDomainService.TokenInfo> authenticateUser(String username, String password) {
          return Mono.fromCallable(() -> {
               log.info("Autenticando usuario en Keycloak: {}", username);

               try {
                    // Usar WebClient para hacer autenticación con Keycloak OAuth2
                    var webClient = org.springframework.web.reactive.function.client.WebClient.builder()
                              .build();

                    // Hacer request a Keycloak token endpoint
                    String tokenUrl = serverUrl + "/realms/" + realm + "/protocol/openid-connect/token";

                    var formData = new org.springframework.util.LinkedMultiValueMap<String, String>();
                    formData.add("grant_type", "password");
                    formData.add("client_id", "jass-users-service");
                    formData.add("client_secret", "zJgI1QiNFXWiinFgAVfxsbqcF8nlGYLy");
                    formData.add("username", username);
                    formData.add("password", password);
                    formData.add("scope", "openid profile email");

                    @SuppressWarnings("unchecked")
                    var response = (java.util.Map<String, Object>) webClient.post()
                              .uri(tokenUrl)
                              .contentType(org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED)
                              .body(org.springframework.web.reactive.function.BodyInserters.fromFormData(formData))
                              .retrieve()
                              .bodyToMono(java.util.Map.class)
                              .block(java.time.Duration.ofSeconds(10));

                    if (response != null && response.containsKey("access_token")) {
                         String accessToken = (String) response.get("access_token");
                         String refreshToken = (String) response.get("refresh_token");
                         String tokenType = (String) response.getOrDefault("token_type", "Bearer");
                         Number expiresIn = (Number) response.getOrDefault("expires_in", 1800);
                         Number refreshExpiresIn = (Number) response.getOrDefault("refresh_expires_in", 3600);

                         log.info(" Autenticación exitosa para usuario: {}", username);
                         return new TokenInfo(
                                   accessToken,
                                   refreshToken,
                                   tokenType,
                                   expiresIn.longValue(),
                                   refreshExpiresIn.longValue());
                    } else {
                         throw new RuntimeException("Respuesta inválida de Keycloak");
                    }
               } catch (Exception e) {
                    log.error(" Error autenticando usuario {}: {}", username, e.getMessage());
                    throw new RuntimeException("Credenciales inválidas: " + e.getMessage());
               }
          })
                    .subscribeOn(Schedulers.boundedElastic());
     }

     @Override
     public Mono<KeycloakDomainService.TokenInfo> refreshToken(String refreshToken) {
          return Mono.fromCallable(() -> {
               log.info("Refrescando token");

               // Implementación de refresh token usando Keycloak REST API
               log.warn("Implementar refresh token con Keycloak REST API");

               return new TokenInfo(
                         "new_access_token",
                         "new_refresh_token",
                         "Bearer",
                         tokenDuration,
                         tokenDuration * 2);
          })
                    .subscribeOn(Schedulers.boundedElastic());
     }

     @Override
     public Mono<Boolean> validateToken(String accessToken) {
          return Mono.fromCallable(() -> {
               // Implementación de validación de token
               log.warn("Implementar validación de token con Keycloak introspection endpoint");
               return true;
          })
                    .subscribeOn(Schedulers.boundedElastic());
     }

     @Override
     public Mono<Void> requirePasswordChange(String username) {
          return Mono.fromCallable(() -> {
               log.info("Marcando usuario {} para cambio obligatorio de contraseña", username);

               // Buscar usuario por username
               List<UserRepresentation> users = realmResource.users().search(username, true);
               if (users.isEmpty()) {
                    throw new RuntimeException("Usuario no encontrado: " + username);
               }

               String userId = users.get(0).getId();
               var userResource = realmResource.users().get(userId);

               // Marcar para cambio de contraseña
               UserRepresentation user = userResource.toRepresentation();
               user.getRequiredActions().add("UPDATE_PASSWORD");
               userResource.update(user);

               log.info("Usuario {} marcado para cambio de contraseña", username);
               return null;
          })
                    .subscribeOn(Schedulers.boundedElastic())
                    .then();
     }

     /**
      * Establece contraseña temporal para un usuario con expiración de 15 minutos
      */
     private void setTemporaryPassword(String userId, String temporaryPassword) {
          var userResource = realmResource.users().get(userId);

          CredentialRepresentation passwordCred = new CredentialRepresentation();
          passwordCred.setTemporary(true); // Marcar como temporal
          passwordCred.setType(CredentialRepresentation.PASSWORD);
          passwordCred.setValue(temporaryPassword);

          userResource.resetPassword(passwordCred);

          // NUEVO: Configurar acción requerida para cambio de contraseña
          UserRepresentation user = userResource.toRepresentation();
          if (user.getRequiredActions() == null) {
               user.setRequiredActions(new ArrayList<>());
          }
          user.getRequiredActions().add("UPDATE_PASSWORD");

          // NUEVO: Configurar atributos para expiración de contraseña temporal (15
          // minutos)
          Map<String, List<String>> attributes = user.getAttributes();
          if (attributes == null) {
               attributes = new HashMap<>();
          }

          // Establecer timestamp de expiración (15 minutos desde ahora)
          long expirationTime = System.currentTimeMillis() + (15 * 60 * 1000); // 15 minutos
          attributes.put("temp_password_expires_at", List.of(String.valueOf(expirationTime)));
          attributes.put("temp_password_created_at", List.of(String.valueOf(System.currentTimeMillis())));
          user.setAttributes(attributes);

          userResource.update(user);

          log.info(" Contraseña temporal establecida para usuario: {} - Expira en 15 minutos", userId);
     }

     /**
      * NUEVO: Establece contraseña permanente para un usuario (sin expiración)
      */
     private void setPermanentPassword(String userId, String password) {
          var userResource = realmResource.users().get(userId);

          CredentialRepresentation passwordCred = new CredentialRepresentation();
          passwordCred.setTemporary(false); // NO temporal - contraseña permanente
          passwordCred.setType(CredentialRepresentation.PASSWORD);
          passwordCred.setValue(password);

          userResource.resetPassword(passwordCred);

          // NO agregar acciones requeridas - el usuario puede usar la contraseña
          // inmediatamente

          log.info(" Contraseña permanente establecida para usuario: {}", userId);
     }

     @Override
     public Mono<Void> changePassword(String username, String currentPassword, String newPassword) {
          return Mono.fromCallable(() -> {
               log.info("Cambiando contraseña para usuario: {}", username);

               // Primero autenticar al usuario con la contraseña actual
               try {
                    Keycloak userKeycloak = Keycloak.getInstance(
                              serverUrl,
                              realm,
                              username,
                              currentPassword,
                              "jass-users-service",
                              "zJgI1QiNFXWiinFgAVfxsbqcF8nlGYLy"); // Usar secret para cliente confidencial
                    // Probar la autenticación
                    userKeycloak.tokenManager().getAccessToken();
                    userKeycloak.close();
               } catch (Exception e) {
                    log.error("Error autenticando usuario {} para cambio de contraseña: {}", username, e.getMessage());
                    throw new RuntimeException("Contraseña actual incorrecta");
               }

               // Buscar usuario en Keycloak
               List<UserRepresentation> users = realmResource.users().search(username);
               if (users.isEmpty()) {
                    throw new RuntimeException("Usuario no encontrado: " + username);
               }

               UserRepresentation user = users.get(0);
               var userResource = realmResource.users().get(user.getId());

               // Cambiar contraseña
               CredentialRepresentation passwordCred = new CredentialRepresentation();
               passwordCred.setTemporary(false); // No temporal
               passwordCred.setType(CredentialRepresentation.PASSWORD);
               passwordCred.setValue(newPassword);

               userResource.resetPassword(passwordCred);
               log.info("Contraseña cambiada exitosamente para usuario: {}", username);

               return null;
          }).subscribeOn(Schedulers.boundedElastic()).then();
     }

     @Override
     public Mono<KeycloakDomainService.UserInfo> getUserInfoFromToken(String accessToken) {
          return Mono.fromCallable(() -> {
               try {
                    // Decodificar el token JWT usando utilidad
                    String payloadStr = JwtTokenExtractor.decodePayload(accessToken);
                    log.debug("JWT Payload: {}", payloadStr);

                    // Extraer información del token usando utilidad
                    String username = JwtTokenExtractor.extractField(payloadStr, "preferred_username");
                    String email = JwtTokenExtractor.extractField(payloadStr, "email");
                    String firstName = JwtTokenExtractor.extractField(payloadStr, "given_name");
                    String lastName = JwtTokenExtractor.extractField(payloadStr, "family_name");
                    String sub = JwtTokenExtractor.extractField(payloadStr, "sub");

                    return new UserInfo(
                              sub != null ? sub : "unknown-sub",
                              username != null ? username : "unknown-user",
                              email != null ? email : "unknown-email",
                              firstName != null ? firstName : "unknown-firstName",
                              lastName != null ? lastName : "unknown-lastName");
               } catch (Exception e) {
                    log.error("Error decodificando token JWT: {}", e.getMessage());
                    throw new RuntimeException("Error obteniendo información del usuario: " + e.getMessage());
               }
          }).subscribeOn(Schedulers.boundedElastic());
     }

     @Override
     public Mono<Boolean> isUsernameAvailable(String username) {
          return Mono.fromCallable(() -> {
               log.debug("Verificando disponibilidad del username: {}", username);
               List<UserRepresentation> users = realmResource.users().search(username, true);
               boolean available = users.isEmpty();
               log.debug("Username '{}' está {}", username, available ? "disponible" : "ocupado");
               return available;
          }).subscribeOn(Schedulers.boundedElastic());
     }

     @Override
     public Mono<String> renewTemporaryPassword(String username) {
          return Mono.fromCallable(() -> {
               log.info("🔄 Renovando contraseña temporal para usuario: {}", username);

               // Buscar usuario
               List<UserRepresentation> users = realmResource.users().search(username, true);
               if (users.isEmpty()) {
                    throw new RuntimeException("Usuario no encontrado: " + username);
               }

               UserRepresentation user = users.get(0);
               String userId = user.getId();

               // Generar nueva contraseña temporal
               String newTemporaryPassword = PasswordGenerator.generateTemporaryPassword();

               // Establecer nueva contraseña temporal
               setTemporaryPassword(userId, newTemporaryPassword);

               log.info(" Contraseña temporal renovada exitosamente para: {}", username);
               return newTemporaryPassword;

          }).subscribeOn(Schedulers.boundedElastic());
     }
}
