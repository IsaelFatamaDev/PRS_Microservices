package pe.edu.vallegrande.vgmsusers.infrastructure.rest.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;

import pe.edu.vallegrande.vgmsusers.application.service.UserService;
import pe.edu.vallegrande.vgmsusers.application.service.UserCodeService;
import pe.edu.vallegrande.vgmsusers.infrastructure.client.external.OrganizationClient;
import pe.edu.vallegrande.vgmsusers.infrastructure.client.external.ReniecClient;
import pe.edu.vallegrande.vgmsusers.infrastructure.client.external.NotificationClient;
import pe.edu.vallegrande.vgmsusers.infrastructure.dto.request.CreateUserRequest;
import pe.edu.vallegrande.vgmsusers.infrastructure.dto.request.UpdateUserPatchRequest;
import pe.edu.vallegrande.vgmsusers.infrastructure.dto.response.UserResponse;
import pe.edu.vallegrande.vgmsusers.infrastructure.dto.response.UserWithLocationResponse;
import pe.edu.vallegrande.vgmsusers.infrastructure.dto.response.UserCreationResponse;
import pe.edu.vallegrande.vgmsusers.infrastructure.dto.ApiResponse;
import pe.edu.vallegrande.vgmsusers.infrastructure.exception.custom.ForbiddenException;
import pe.edu.vallegrande.vgmsusers.infrastructure.exception.custom.NotFoundException;
import pe.edu.vallegrande.vgmsusers.infrastructure.exception.custom.ValidationException;
import pe.edu.vallegrande.vgmsusers.domain.enums.RolesUsers;
import pe.edu.vallegrande.vgmsusers.domain.enums.UserStatus;
import pe.edu.vallegrande.vgmsusers.infrastructure.util.UsernameGeneratorUtil;
import pe.edu.vallegrande.vgmsusers.infrastructure.validation.OrganizationValidationService;
import pe.edu.vallegrande.vgmsusers.infrastructure.util.UserEnrichmentUtil;

import reactor.core.publisher.Mono;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/admin")
@Validated
@Slf4j
public class AdminRest {

     private final UserService userService;
     private final UserCodeService userCodeService;
     private final OrganizationClient organizationClient;
     private final ReniecClient reniecClient;
     private final UsernameGeneratorUtil usernameGenerator;
     private final UserEnrichmentUtil userEnrichment;
     private final NotificationClient notificationClient;

     public AdminRest(UserService userService, UserCodeService userCodeService,
               OrganizationClient organizationClient, ReniecClient reniecClient,
               UsernameGeneratorUtil usernameGenerator, UserEnrichmentUtil userEnrichment,
               NotificationClient notificationClient) {
          this.userService = userService;
          this.userCodeService = userCodeService;
          this.organizationClient = organizationClient;
          this.reniecClient = reniecClient;
          this.usernameGenerator = usernameGenerator;
          this.userEnrichment = userEnrichment;
          this.notificationClient = notificationClient;
     }

     @PostMapping("/clients")
     public Mono<ResponseEntity<ApiResponse<UserCreationResponse>>> createClient(
               @Valid @RequestBody CreateUserRequest request) {

          request.setRoles(Set.of(RolesUsers.CLIENT));

          if (request.getRoles() == null || !request.getRoles().contains(RolesUsers.CLIENT) ||
                    request.getRoles().size() != 1) {
               throw new ValidationException("Los administradores solo pueden crear usuarios con rol CLIENT");
          }

          return reniecClient.getPersonalDataByDni(request.getDocumentNumber())
                    .doOnNext(reniecData -> {

                         request.setFirstName(reniecData.getFirstName());
                         request.setLastName(reniecData.getFirstLastName() +
                                   (reniecData.getSecondLastName() != null
                                             && !reniecData.getSecondLastName().trim().isEmpty()
                                                       ? " " + reniecData.getSecondLastName()
                                                       : ""));

                         String generatedUsername = usernameGenerator.generateIntelligentUsername(
                                   reniecData.getFirstName(),
                                   reniecData.getFirstLastName(),
                                   reniecData.getSecondLastName());

                         if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                              request.setEmail(generatedUsername);
                         }
                    })
                    .onErrorResume(error -> {

                         if (request.getFirstName() == null || request.getFirstName().trim().isEmpty() ||
                                   request.getLastName() == null || request.getLastName().trim().isEmpty()) {
                              return Mono.error(new ValidationException(
                                        "RENIEC no disponible. Debe proporcionar firstName y lastName manualmente."));
                         }

                         String generatedUsername = usernameGenerator.generateIntelligentUsername(
                                   request.getFirstName(),
                                   request.getLastName(),
                                   null);

                         if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                              request.setEmail(generatedUsername);
                         }

                         return Mono.empty();
                    })
                    .then(Mono.defer(() -> {
                         return organizationClient.getOrganizationById(request.getOrganizationId());
                    }))
                    .flatMap(orgResponse -> {
                         if (!orgResponse.isStatus()) {
                              return Mono.error(new NotFoundException("Organización no encontrada"));
                         }

                         OrganizationClient.OrganizationData orgData = orgResponse.getData();

                         if (request.getStreetId() != null) {
                              boolean streetValid = orgData.getZones().stream()
                                        .anyMatch(zone -> zone.getZoneId().equals(request.getZoneId()) &&
                                                  zone.getStreets().stream()
                                                            .anyMatch(street -> street.getStreetId()
                                                                      .equals(request.getStreetId())));

                              if (!streetValid) {
                                   return Mono.error(new ValidationException(
                                             "La calle especificada no pertenece a la organización o zona"));
                              }
                         }

                         return userService.createUserWithCredentials(request);
                    })
                    .flatMap(response -> {
                         if (response.isSuccess()) {
                              // Send WhatsApp notification asynchronously
                              UserCreationResponse data = response.getData();
                              String phoneNumber = request.getPhone();

                              if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
                                   notificationClient.sendWelcomeMessage(
                                             phoneNumber,
                                             data.getUsername(),
                                             data.getTemporaryPassword(),
                                             request.getFirstName()).subscribe(); // Fire and forget
                              }

                              return Mono.just(ResponseEntity
                                        .status(HttpStatus.CREATED)
                                        .body(ApiResponse.success("Cliente creado exitosamente", response.getData())));
                         } else {
                              return Mono.error(
                                        new ValidationException("Error creando cliente: " + response.getMessage()));
                         }
                    })
                    .doOnError(ex -> {
                    });
     }

     @GetMapping("/clients")
     public Mono<ApiResponse<Page<UserWithLocationResponse>>> getMyOrganizationClients(
               @RequestParam(defaultValue = "0") int page,
               @RequestParam(defaultValue = "10") int size) {
          return Mono.error(new ForbiddenException("Esta funcionalidad requiere acceso a través del Gateway"));
     }

     @GetMapping("/clients/{id}")
     public Mono<ApiResponse<UserWithLocationResponse>> getClient(@PathVariable String id) {

          return userService.getUserById(id)
                    .flatMap(response -> {
                         if (!response.isSuccess()) {
                              return Mono.error(new NotFoundException("Cliente no encontrado"));
                         }

                         UserResponse user = response.getData();
                         if (!user.isClient()) {
                              return Mono.error(new ForbiddenException("El usuario no es un cliente"));
                         }

                         return userEnrichment.enrichUsersWithLocationInfo(List.of(user))
                                   .map(enrichedUsers -> {
                                        UserWithLocationResponse enrichedUser;
                                        if (enrichedUsers.isEmpty()) {
                                             enrichedUser = userEnrichment
                                                       .createUserWithLocationResponseWithoutOrg(user, null);
                                        } else {
                                             enrichedUser = enrichedUsers.get(0);
                                        }
                                        return ApiResponse.success(
                                                  "Cliente obtenido exitosamente con información completa",
                                                  enrichedUser);
                                   });
                    });
     }

     @PatchMapping("/clients/{id}")
     public Mono<ApiResponse<UserWithLocationResponse>> patchClient(
               @PathVariable String id,
               @Valid @RequestBody UpdateUserPatchRequest request) {

          return userService.getUserById(id)
                    .flatMap(userResponse -> {
                         if (!userResponse.isSuccess()) {
                              return Mono.error(new NotFoundException("Cliente no encontrado"));
                         }

                         UserResponse user = userResponse.getData();

                         if (!user.isClient()) {
                              return Mono.error(new ForbiddenException("El usuario no es un cliente"));
                         }

                         if (request.getStreetId() != null && request.getZoneId() != null) {
                              return organizationClient.getOrganizationById(user.getOrganizationId())
                                        .flatMap(orgResponse -> {
                                             if (!orgResponse.isStatus()) {
                                                  return Mono
                                                            .error(new NotFoundException("Organización no encontrada"));
                                             }

                                             OrganizationClient.OrganizationData orgData = orgResponse.getData();
                                             boolean streetValid = orgData.getZones().stream()
                                                       .anyMatch(zone -> zone.getZoneId().equals(request.getZoneId()) &&
                                                                 zone.getStreets().stream()
                                                                           .anyMatch(street -> street.getStreetId()
                                                                                     .equals(request.getStreetId())));

                                             if (!streetValid) {
                                                  return Mono.error(new ValidationException(
                                                            "La calle especificada no pertenece a la organización o zona"));
                                             }

                                             return userService.patchUser(id, request);
                                        });
                         } else {
                              return userService.patchUser(id, request);
                         }
                    })
                    .flatMap(response -> {
                         if (response.isSuccess()) {

                              return userEnrichment.enrichUsersWithLocationInfo(List.of(response.getData()))
                                        .onErrorResume(enrichmentError -> {

                                             UserWithLocationResponse directResponse = userEnrichment
                                                       .createUserWithLocationResponseWithoutOrg(
                                                                 response.getData(), null);
                                             return Mono.just(List.of(directResponse));
                                        })
                                        .map(enrichedUsers -> {
                                             if (!enrichedUsers.isEmpty()) {
                                                  return ApiResponse.success("Cliente actualizado exitosamente",
                                                            enrichedUsers.get(0));
                                             } else {
                                                  return ApiResponse.success("Cliente actualizado exitosamente",
                                                            userEnrichment.createUserWithLocationResponseWithoutOrg(
                                                                      response.getData(), null));
                                             }
                                        });
                         } else {
                              return Mono.error(new ValidationException(
                                        "Error actualizando cliente: " + response.getMessage()));
                         }
                    });
     }

     @PatchMapping("/clients/{id}/status")
     public Mono<ApiResponse<UserWithLocationResponse>> changeClientStatus(
               @PathVariable String id,
               @RequestParam UserStatus status) {
          return Mono.error(new ForbiddenException("Esta funcionalidad requiere acceso a través del Gateway"));
     }

     @DeleteMapping("/clients/{id}")
     public Mono<ApiResponse<Void>> deleteClient(
               @PathVariable String id) {

          return userService.deleteUser(id);
     }

     @PostMapping("/user-codes/generate")
     public Mono<ApiResponse<String>> generateUserCodeForMyOrg() {
          return Mono.error(new ForbiddenException("Esta funcionalidad requiere acceso a través del Gateway"));
     }

     @GetMapping("/clients/active")
     public Mono<ApiResponse<List<UserWithLocationResponse>>> getActiveClients(@RequestParam String organizationId) {

          return userService.getActiveUsersByOrganization(organizationId)
                    .flatMap(response -> {
                         if (response.isSuccess()) {
                              List<UserResponse> clientUsers = response.getData().stream()
                                        .filter(user -> user.hasRole(RolesUsers.CLIENT))
                                        .filter(user -> !user.hasRole(RolesUsers.ADMIN)
                                                  && !user.hasRole(RolesUsers.SUPER_ADMIN))
                                        .toList();

                              return userEnrichment.enrichUsersWithLocationInfo(clientUsers)
                                        .map(enrichedUsers -> ApiResponse.success(
                                                  "Clientes activos obtenidos exitosamente",
                                                  enrichedUsers));
                         } else {
                              return Mono.error(new ValidationException(
                                        "Error obteniendo clientes activos: " + response.getMessage()));
                         }
                    });
     }

     @GetMapping("/clients/inactive")
     public Mono<ApiResponse<List<UserWithLocationResponse>>> getInactiveClients(@RequestParam String organizationId) {

          return userService.getInactiveUsersByOrganization(organizationId)
                    .flatMap(response -> {
                         if (response.isSuccess()) {
                              List<UserResponse> clientUsers = response.getData().stream()
                                        .filter(user -> user.hasRole(RolesUsers.CLIENT))
                                        .filter(user -> !user.hasRole(RolesUsers.ADMIN)
                                                  && !user.hasRole(RolesUsers.SUPER_ADMIN))
                                        .toList();

                              return userEnrichment.enrichUsersWithLocationInfo(clientUsers)
                                        .map(enrichedUsers -> ApiResponse.success(
                                                  "Clientes inactivos obtenidos exitosamente",
                                                  enrichedUsers));
                         } else {
                              return Mono.error(new ValidationException(
                                        "Error obteniendo clientes inactivos: " + response.getMessage()));
                         }
                    });
     }

     @GetMapping("/clients/all")
     public Mono<ApiResponse<List<UserWithLocationResponse>>> getAllClients(@RequestParam String organizationId) {

          return userService.getAllUsersByOrganization(organizationId)
                    .flatMap(response -> {
                         if (response.isSuccess()) {
                              List<UserResponse> clientUsers = response.getData().stream()
                                        .filter(user -> user.hasRole(RolesUsers.CLIENT))
                                        .filter(user -> !user.hasRole(RolesUsers.ADMIN)
                                                  && !user.hasRole(RolesUsers.SUPER_ADMIN))
                                        .toList();

                              return userEnrichment.enrichUsersWithLocationInfo(clientUsers)
                                        .map(enrichedUsers -> ApiResponse.success(
                                                  "Todos los clientes obtenidos exitosamente",
                                                  enrichedUsers));
                         } else {
                              return Mono.error(new ValidationException(
                                        "Error obteniendo todos los clientes: " + response.getMessage()));
                         }
                    });
     }

     @PutMapping("/clients/{id}/restore")
     public Mono<ApiResponse<UserWithLocationResponse>> restoreClient(
               @PathVariable String id) {
          return userService.restoreUser(id)
                    .flatMap(response -> {
                         if (response.isSuccess()) {
                              return userEnrichment.enrichUsersWithLocationInfo(List.of(response.getData()))
                                        .map(enrichedUsers -> {
                                             if (!enrichedUsers.isEmpty()) {
                                                  return ApiResponse.success("Cliente restaurado exitosamente",
                                                            enrichedUsers.get(0));
                                             } else {
                                                  return ApiResponse.success("Cliente restaurado exitosamente",
                                                            userEnrichment.createUserWithLocationResponseWithoutOrg(
                                                                      response.getData(), null));
                                             }
                                        });
                         } else {
                              return Mono.error(new ValidationException(
                                        "Error restaurando cliente: " + response.getMessage()));
                         }
                    });
     }

     @GetMapping("/user-codes/next")
     public Mono<ApiResponse<String>> getNextUserCodeForMyOrg() {
          return Mono.error(new ForbiddenException("Esta funcionalidad requiere acceso a través del Gateway"));
     }

     // ==========================================
     // ENDPOINTS PARA GESTIÓN DE OPERARIOS
     // ==========================================

     /**
      * Crear operario (ADMIN puede crear OPERATOR)
      * POST /api/admin/operators
      * Siempre asigna rol OPERATOR por defecto
      * Usa datos de RENIEC para nombres y genera username inteligente
      */
     @PostMapping("/operators")
     public Mono<ApiResponse<UserCreationResponse>> createOperator(@Valid @RequestBody CreateUserRequest request) {

          // Asignar rol OPERATOR por defecto y único permitido
          request.setRoles(java.util.Set.of(RolesUsers.OPERATOR));

          // Validación adicional para asegurar que solo se crea OPERATOR
          if (request.getRoles() == null || !request.getRoles().contains(RolesUsers.OPERATOR) ||
                    request.getRoles().size() != 1) {
               throw new ValidationException("Los administradores solo pueden crear usuarios con rol OPERATOR");
          }

          // 1. VALIDAR Y OBTENER DATOS DE RENIEC
          return reniecClient.getPersonalDataByDni(request.getDocumentNumber())
                    .onErrorResume(error -> {
                         return Mono
                                   .error(new ValidationException("DNI no válido según RENIEC: " + error.getMessage()));
                    })
                    .flatMap(reniecData -> {

                         // 2. GENERAR USERNAME INTELIGENTE USANDO DATOS DE RENIEC
                         String generatedUsername = usernameGenerator.generateIntelligentUsername(
                                   reniecData.getFirstName(),
                                   reniecData.getFirstLastName(),
                                   reniecData.getSecondLastName());

                         // 3. CONFIGURAR REQUEST CON DATOS DE RENIEC
                         request.setFirstName(reniecData.getFirstName());
                         request.setLastName(reniecData.getFirstLastName() +
                                   (reniecData.getSecondLastName() != null
                                             && !reniecData.getSecondLastName().trim().isEmpty()
                                                       ? " " + reniecData.getSecondLastName()
                                                       : ""));

                         // Email es obligatorio para Keycloak - usar username generado si no se
                         // proporciona
                         if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                              request.setEmail(generatedUsername); // Usar username como email para Keycloak
                              log.info("[ADMIN] 📧 Email no proporcionado, usando username como email: {}",
                                        generatedUsername);
                         }

                         // 4. VALIDAR ORGANIZACIÓN
                         return organizationClient.getOrganizationById(request.getOrganizationId());
                    })
                    .flatMap(orgResponse -> {
                         if (!orgResponse.isStatus()) {
                              return Mono.error(new NotFoundException("Organización no encontrada"));
                         }

                         OrganizationClient.OrganizationData orgData = orgResponse.getData();
                         log.info("[ADMIN] Validando organización: {} - {}",
                                   orgData.getOrganizationCode(), orgData.getOrganizationName());

                         if (request.getStreetId() != null) {
                              boolean streetValid = orgData.getZones().stream()
                                        .anyMatch(zone -> zone.getZoneId().equals(request.getZoneId()) &&
                                                  zone.getStreets().stream()
                                                            .anyMatch(street -> street.getStreetId()
                                                                      .equals(request.getStreetId())));

                              if (!streetValid) {
                                   return Mono.error(new ValidationException(
                                             "La calle especificada no pertenece a la organización o zona"));
                              }
                         }

                         return userService.createUserWithCredentials(request);
                    })
                    .flatMap(response -> {
                         if (response.isSuccess()) {
                              UserCreationResponse data = response.getData();
                              log.info("[ADMIN] Operario creado exitosamente: {} - Username: {} - Password: {}",
                                        data.getUserInfo().getUserCode(), data.getUsername(),
                                        data.getTemporaryPassword());
                              return Mono.just(ApiResponse.success("Operario creado exitosamente", response.getData()));
                         } else {
                              log.error("[ADMIN] Error creando operario: {}", response.getMessage());
                              return Mono.error(
                                        new ValidationException("Error creando operario: " + response.getMessage()));
                         }
                    })
                    .doOnError(ex -> log.error("[ADMIN] Error en creación de operario: {}", ex.getMessage()));
     }

     /**
      * Listar todos los operarios de la organización específica (activos e
      * inactivos)
      * GET /api/admin/operators
      */
     @GetMapping("/operators")
     public Mono<ApiResponse<List<UserWithLocationResponse>>> getAllOperators(@RequestParam String organizationId) {
          log.info("[ADMIN] Listando todos los operarios de organización: {}", organizationId);

          // Validar que la organización existe y mostrar información completa
          return organizationClient.getOrganizationById(organizationId)
                    .flatMap(orgResponse -> {
                         if (!orgResponse.isStatus()) {
                              return Mono.error(new NotFoundException("Organización no encontrada: " + organizationId));
                         }

                         OrganizationClient.OrganizationData orgData = orgResponse.getData();
                         log.info("[ADMIN] Organización: {} - {} ({})",
                                   orgData.getOrganizationCode(),
                                   orgData.getOrganizationName(),
                                   orgData.getStatus());

                         if (orgData.getZones() != null && !orgData.getZones().isEmpty()) {
                              log.info("[ADMIN] Zonas de la organización:");
                              orgData.getZones().forEach(zone -> {
                                   log.info("  - Zona: {} - {} ({})",
                                             zone.getZoneCode(),
                                             zone.getZoneName(),
                                             zone.getStatus());
                                   if (zone.getStreets() != null) {
                                        zone.getStreets().forEach(street -> {
                                             log.info("    - Calle: {} - {} ({})",
                                                       street.getStreetCode(),
                                                       street.getStreetName(),
                                                       street.getStatus());
                                        });
                                   }
                              });
                         }

                         return userService.getAllUsersByOrganization(organizationId);
                    })
                    .flatMap(response -> {
                         if (response.isSuccess()) {
                              // Filtrar solo usuarios con rol OPERATOR
                              List<UserResponse> operatorUsers = response.getData().stream()
                                        .filter(user -> user.hasRole(RolesUsers.OPERATOR))
                                        .filter(user -> !user.hasRole(RolesUsers.ADMIN)
                                                  && !user.hasRole(RolesUsers.SUPER_ADMIN))
                                        .toList();

                              log.info("[ADMIN] Todos los operarios OPERATOR filtrados: {}", operatorUsers.size());

                              return userEnrichment.enrichUsersWithLocationInfo(operatorUsers)
                                        .map(enrichedUsers -> ApiResponse.success(
                                                  "Todos los operarios obtenidos exitosamente",
                                                  enrichedUsers));
                         } else {
                              return Mono.error(new ValidationException(
                                        "Error obteniendo todos los operarios: " + response.getMessage()));
                         }
                    });
     }

     /**
      * Obtener operario específico (solo de su organización)
      * GET /api/admin/operators/{id}
      */
     @GetMapping("/operators/{id}")
     public Mono<ApiResponse<UserWithLocationResponse>> getOperator(@PathVariable String id) {

          log.info("[ADMIN] Obteniendo operario con información completa: {}", id);

          return userService.getUserById(id)
                    .flatMap(response -> {
                         if (!response.isSuccess()) {
                              return Mono.error(new NotFoundException("Operario no encontrado"));
                         }

                         UserResponse user = response.getData();
                         if (!user.hasRole(RolesUsers.OPERATOR)) {
                              return Mono.error(new ForbiddenException("El usuario no es un operario"));
                         }

                         log.info("[ADMIN] Operario encontrado: {} - Organización: {}", user.getUserCode(),
                                   user.getOrganizationId());

                         return organizationClient.getOrganizationById(user.getOrganizationId())
                                   .map(orgResponse -> {
                                        if (!orgResponse.isStatus()) {
                                             throw new NotFoundException("Organización no encontrada");
                                        }

                                        OrganizationClient.OrganizationData orgData = orgResponse.getData();

                                        OrganizationClient.Zone zone = null;
                                        OrganizationClient.Street street = null;

                                        if (user.getZoneId() != null && orgData.getZones() != null) {
                                             zone = orgData.getZones().stream()
                                                       .filter(z -> z.getZoneId().equals(user.getZoneId()))
                                                       .findFirst()
                                                       .orElse(null);

                                             if (zone != null && user.getStreetId() != null
                                                       && zone.getStreets() != null) {
                                                  street = zone.getStreets().stream()
                                                            .filter(s -> s.getStreetId().equals(user.getStreetId()))
                                                            .findFirst()
                                                            .orElse(null);
                                             }
                                        }

                                        UserWithLocationResponse.OrganizationInfo orgInfo = UserWithLocationResponse.OrganizationInfo
                                                  .builder()
                                                  .organizationId(orgData.getOrganizationId())
                                                  .organizationCode(orgData.getOrganizationCode())
                                                  .organizationName(orgData.getOrganizationName())
                                                  .legalRepresentative(orgData.getLegalRepresentative())
                                                  .address(orgData.getAddress())
                                                  .phone(orgData.getPhone())
                                                  .status(orgData.getStatus())
                                                  .build();

                                        UserWithLocationResponse.ZoneInfo zoneInfo = null;
                                        if (zone != null) {
                                             zoneInfo = UserWithLocationResponse.ZoneInfo.builder()
                                                       .zoneId(zone.getZoneId())
                                                       .zoneCode(zone.getZoneCode())
                                                       .zoneName(zone.getZoneName())
                                                       .description(zone.getDescription())
                                                       .status(zone.getStatus())
                                                       .build();
                                        }

                                        UserWithLocationResponse.StreetInfo streetInfo = null;
                                        if (street != null) {
                                             streetInfo = UserWithLocationResponse.StreetInfo.builder()
                                                       .streetId(street.getStreetId())
                                                       .streetCode(street.getStreetCode())
                                                       .streetName(street.getStreetName())
                                                       .streetType(street.getStreetType())
                                                       .status(street.getStatus())
                                                       .build();
                                        }

                                        UserWithLocationResponse enrichedUser = UserWithLocationResponse.builder()
                                                  .id(user.getId())
                                                  .userCode(user.getUserCode())
                                                  .firstName(user.getFirstName())
                                                  .lastName(user.getLastName())
                                                  .documentType(user.getDocumentType())
                                                  .documentNumber(user.getDocumentNumber())
                                                  .email(user.getEmail())
                                                  .phone(user.getPhone())
                                                  .address(user.getAddress())
                                                  .roles(user.getRoles())
                                                  .status(user.getStatus())
                                                  .createdAt(user.getCreatedAt())
                                                  .updatedAt(user.getUpdatedAt())
                                                  .organization(orgInfo)
                                                  .zone(zoneInfo)
                                                  .street(streetInfo)
                                                  .build();

                                        log.info("[ADMIN] Operario enriquecido - Organización: {}, Zona: {}, Calle: {}",
                                                  orgData.getOrganizationName(),
                                                  zone != null ? zone.getZoneName() : "N/A",
                                                  street != null ? street.getStreetName() : "N/A");

                                        return ApiResponse.success(
                                                  "Operario obtenido exitosamente con información completa",
                                                  enrichedUser);
                                   });
                    });
     }

     /**
      * Actualización parcial de operario (solo campos permitidos)
      * PATCH /api/admin/operators/{id}
      */
     @PatchMapping("/operators/{id}")
     public Mono<ApiResponse<UserWithLocationResponse>> patchOperator(
               @PathVariable String id,
               @Valid @RequestBody UpdateUserPatchRequest request) {

          log.info("[ADMIN] Actualizando parcialmente operario: {}", id);

          return userService.getUserById(id)
                    .flatMap(userResponse -> {
                         if (!userResponse.isSuccess()) {
                              return Mono.error(new NotFoundException("Operario no encontrado"));
                         }

                         UserResponse user = userResponse.getData();

                         if (!user.hasRole(RolesUsers.OPERATOR)) {
                              return Mono.error(new ForbiddenException("El usuario no es un operario"));
                         }

                         if (request.getStreetId() != null && request.getZoneId() != null) {
                              return organizationClient.getOrganizationById(user.getOrganizationId())
                                        .flatMap(orgResponse -> {
                                             if (!orgResponse.isStatus()) {
                                                  return Mono
                                                            .error(new NotFoundException("Organización no encontrada"));
                                             }

                                             OrganizationClient.OrganizationData orgData = orgResponse.getData();
                                             boolean streetValid = orgData.getZones().stream()
                                                       .anyMatch(zone -> zone.getZoneId().equals(request.getZoneId()) &&
                                                                 zone.getStreets().stream()
                                                                           .anyMatch(street -> street.getStreetId()
                                                                                     .equals(request.getStreetId())));

                                             if (!streetValid) {
                                                  return Mono.error(new ValidationException(
                                                            "La calle especificada no pertenece a la organización o zona"));
                                             }

                                             return userService.patchUser(id, request);
                                        });
                         } else {
                              return userService.patchUser(id, request);
                         }
                    })
                    .flatMap(response -> {
                         if (response.isSuccess()) {
                              log.info("[ADMIN] Usuario actualizado correctamente, iniciando enriquecimiento...");

                              // NUEVO: Intentar enriquecimiento, pero con fallback directo si falla
                              return userEnrichment.enrichUsersWithLocationInfo(List.of(response.getData()))
                                        .onErrorResume(enrichmentError -> {
                                             log.warn("[ADMIN] Error en enriquecimiento, usando datos actualizados directamente: {}, {}",
                                                       response.getData().getUserCode(), enrichmentError.getMessage());

                                             // Crear respuesta directamente con los datos actualizados
                                             UserWithLocationResponse directResponse = userEnrichment
                                                       .createUserWithLocationResponseWithoutOrg(
                                                                 response.getData(), null);
                                             return Mono.just(List.of(directResponse));
                                        })
                                        .map(enrichedUsers -> {
                                             if (!enrichedUsers.isEmpty()) {
                                                  log.info("[ADMIN] Enriquecimiento completado para usuario: {}",
                                                            enrichedUsers.get(0).getUserCode());
                                                  return ApiResponse.success("Operario actualizado exitosamente",
                                                            enrichedUsers.get(0));
                                             } else {
                                                  log.info("[ADMIN] Usando respuesta sin enriquecimiento para usuario: {}",
                                                            response.getData().getUserCode());
                                                  return ApiResponse.success("Operario actualizado exitosamente",
                                                            userEnrichment.createUserWithLocationResponseWithoutOrg(
                                                                      response.getData(), null));
                                             }
                                        });
                         } else {
                              return Mono.error(new ValidationException(
                                        "Error actualizando operario: " + response.getMessage()));
                         }
                    });
     }

     /**
      * Cambiar estado de operario (solo de su organización)
      * PATCH /api/admin/operators/{id}/status
      */
     @PatchMapping("/operators/{id}/status")
     public Mono<ApiResponse<UserWithLocationResponse>> changeOperatorStatus(
               @PathVariable String id,
               @RequestParam UserStatus status,
               ServerWebExchange exchange) {

          log.info("[ADMIN] Cambiando estado del operario {} a {}", id, status);

          return userService.getUserById(id)
                    .flatMap(userResponse -> {
                         if (!userResponse.isSuccess()) {
                              return Mono.error(new NotFoundException("Operario no encontrado"));
                         }
                         UserResponse user = userResponse.getData();
                         if (!user.hasRole(RolesUsers.OPERATOR)) {
                              return Mono.error(new ForbiddenException("El usuario no es un operario"));
                         }
                         return userService.changeUserStatus(id, status);
                    })
                    .flatMap(response -> {
                         if (response.isSuccess()) {
                              return userEnrichment.enrichUsersWithLocationInfo(List.of(response.getData()))
                                        .map(enrichedUsers -> {
                                             if (!enrichedUsers.isEmpty()) {
                                                  return ApiResponse.success(
                                                            "Estado del operario actualizado exitosamente",
                                                            enrichedUsers.get(0));
                                             } else {
                                                  return ApiResponse.success(
                                                            "Estado del operario actualizado exitosamente",
                                                            userEnrichment.createUserWithLocationResponseWithoutOrg(
                                                                      response.getData(), null));
                                             }
                                        });
                         } else {
                              return Mono.error(new ValidationException(
                                        "Error cambiando estado del operario: " + response.getMessage()));
                         }
                    });
     }

     /**
      * Eliminar operario (solo de su organización)
      * DELETE /api/admin/operators/{id}
      */
     @DeleteMapping("/operators/{id}")
     public Mono<ApiResponse<Void>> deleteOperator(
               @PathVariable String id) {

          log.warn("[ADMIN] Eliminando operario: {}", id);

          return userService.deleteUser(id);
     }

     /**
      * Restaurar operario eliminado (solo de su organización)
      * PUT /api/admin/operators/{id}/restore
      */
     @PutMapping("/operators/{id}/restore")
     public Mono<ApiResponse<UserWithLocationResponse>> restoreOperator(
               @PathVariable String id) {
          log.info("[ADMIN] Restaurando operario: {}", id);
          return userService.restoreUser(id)
                    .flatMap(response -> {
                         if (response.isSuccess()) {
                              // Enriquecer usuario restaurado con información de ubicación
                              return userEnrichment.enrichUsersWithLocationInfo(List.of(response.getData()))
                                        .map(enrichedUsers -> {
                                             if (!enrichedUsers.isEmpty()) {
                                                  return ApiResponse.success("Operario restaurado exitosamente",
                                                            enrichedUsers.get(0));
                                             } else {
                                                  return ApiResponse.success("Operario restaurado exitosamente",
                                                            userEnrichment.createUserWithLocationResponseWithoutOrg(
                                                                      response.getData(), null));
                                             }
                                        });
                         } else {
                              return Mono.error(new ValidationException(
                                        "Error restaurando operario: " + response.getMessage()));
                         }
                    });
     }

     /**
      * Listar operarios activos de la organización específica
      * GET /api/admin/operators/active
      */
     /**
      * Listar operarios activos de la organización específica
      * GET /api/admin/operators/active
      * OPTIMIZADO: Sin validación preliminar de org (se valida en enrichment)
      */
     @GetMapping("/operators/active")
     public Mono<ApiResponse<List<UserWithLocationResponse>>> getActiveOperators(@RequestParam String organizationId) {
          log.info("[ADMIN] Listando operarios activos de organización: {}", organizationId);

          return userService.getActiveUsersByOrganization(organizationId).flatMap(response ->

          {
               if (response.isSuccess()) {
                    // Filtrar solo usuarios con rol OPERATOR
                    List<UserResponse> operatorUsers = response.getData().stream()
                              .filter(user -> user.hasRole(RolesUsers.OPERATOR))
                              .filter(user -> !user.hasRole(RolesUsers.ADMIN)
                                        && !user.hasRole(RolesUsers.SUPER_ADMIN))
                              .toList();

                    log.info("[ADMIN] Operarios activos OPERATOR filtrados: {}", operatorUsers.size());

                    return userEnrichment.enrichUsersWithLocationInfo(operatorUsers)
                              .map(enrichedUsers -> ApiResponse.success(
                                        "Operarios activos obtenidos exitosamente",
                                        enrichedUsers));
               } else {
                    return Mono.error(new ValidationException(
                              "Error obteniendo operarios activos: " + response.getMessage()));
               }
          });
     }

     /**
      * Listar operarios inactivos de la organización específica
      * GET /api/admin/operators/inactive
      * OPTIMIZADO: Sin validación preliminar de org (se valida en enrichment)
      */
     @GetMapping("/operators/inactive")
     public Mono<ApiResponse<List<UserWithLocationResponse>>> getInactiveOperators(
               @RequestParam String organizationId) {
          log.info("[ADMIN] Listando operarios inactivos de organización: {}", organizationId);

          return userService.getInactiveUsersByOrganization(organizationId)
                    .flatMap(response -> {
                         if (response.isSuccess()) {
                              // Filtrar solo usuarios con rol OPERATOR
                              List<UserResponse> operatorUsers = response.getData().stream()
                                        .filter(user -> user.hasRole(RolesUsers.OPERATOR))
                                        .filter(user -> !user.hasRole(RolesUsers.ADMIN)
                                                  && !user.hasRole(RolesUsers.SUPER_ADMIN))
                                        .toList();

                              log.info("[ADMIN] Operarios inactivos OPERATOR filtrados: {}", operatorUsers.size());

                              return userEnrichment.enrichUsersWithLocationInfo(operatorUsers)
                                        .map(enrichedUsers -> ApiResponse.success(
                                                  "Operarios inactivos obtenidos exitosamente",
                                                  enrichedUsers));
                         } else {
                              return Mono.error(new ValidationException(
                                        "Error obteniendo operarios inactivos: " + response.getMessage()));
                         }
                    });
     }

}
