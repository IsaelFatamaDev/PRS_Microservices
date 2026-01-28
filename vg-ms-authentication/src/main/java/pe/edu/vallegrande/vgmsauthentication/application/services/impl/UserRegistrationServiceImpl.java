package pe.edu.vallegrande.vgmsauthentication.application.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsauthentication.infrastructure.dto.request.RegisterUserRequest;
import pe.edu.vallegrande.vgmsauthentication.application.services.UserRegistrationService;
import pe.edu.vallegrande.vgmsauthentication.application.services.KeycloakDomainService;
import reactor.core.publisher.Mono;

/**
 * Implementación del servicio para registro de usuarios en Keycloak
 */
@Service
@Slf4j
public class UserRegistrationServiceImpl implements UserRegistrationService {

     private final KeycloakDomainService keycloakDomainService;

     // Constructor explícito (PRS Standard - No usar @RequiredArgsConstructor)
     public UserRegistrationServiceImpl(KeycloakDomainService keycloakDomainService) {
          this.keycloakDomainService = keycloakDomainService;
     }

     @Override
     public Mono<String> registerUser(RegisterUserRequest request) {
          log.info("Procesando solicitud de registro de usuario en Keycloak: {}", request.getUsername());

          // Paso 1: Crear usuario en Keycloak
          return keycloakDomainService.createUserAccount(
                    request.getUsername(),
                    request.getEmail(),
                    request.getFirstName(),
                    request.getLastName(),
                    request.getTemporaryPassword())
                    .flatMap(userCreationResult -> {
                         log.info("Usuario creado en Keycloak con ID: {}", userCreationResult.keycloakUserId());

                         // Paso 2: Asignar roles al usuario
                         if (request.getRoles() != null && !request.getRoles().isEmpty()) {
                              String[] rolesArray = request.getRoles().toArray(new String[0]);
                              return keycloakDomainService.assignRoles(request.getUsername(), rolesArray)
                                        .thenReturn(userCreationResult.keycloakUserId());
                         }
                         return Mono.just(userCreationResult.keycloakUserId());
                    })
                    .flatMap(userId -> {
                         // Paso 3: Requerir cambio de contraseña (COMENTADO para permitir login directo)
                         log.info("Omitiendo requirePasswordChange para permitir login directo con contraseña temporal");
                         return Mono.just(userId);
                    })
                    .doOnSuccess(userId -> log.info("Usuario {} registrado exitosamente en Keycloak",
                              request.getUsername()))
                    .doOnError(error -> log.error("Error registrando usuario en Keycloak: {}", error.getMessage()));
     }
}
