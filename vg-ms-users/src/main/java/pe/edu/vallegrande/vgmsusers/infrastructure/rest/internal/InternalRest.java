package pe.edu.vallegrande.vgmsusers.infrastructure.rest.internal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.vgmsusers.application.service.UserService;
import pe.edu.vallegrande.vgmsusers.infrastructure.util.UsernameGeneratorUtil;
import pe.edu.vallegrande.vgmsusers.infrastructure.dto.request.CreateUserRequest;
import pe.edu.vallegrande.vgmsusers.infrastructure.dto.response.CompleteUserResponse;
import pe.edu.vallegrande.vgmsusers.infrastructure.dto.response.UserCreationResponse;
import pe.edu.vallegrande.vgmsusers.infrastructure.dto.ApiResponse;
import pe.edu.vallegrande.vgmsusers.infrastructure.dto.response.UserResponse;
import pe.edu.vallegrande.vgmsusers.infrastructure.dto.response.UserWithLocationResponse;
import pe.edu.vallegrande.vgmsusers.infrastructure.exception.custom.ForbiddenException;
import pe.edu.vallegrande.vgmsusers.infrastructure.exception.custom.NotFoundException;
import pe.edu.vallegrande.vgmsusers.infrastructure.exception.custom.ValidationException;
import pe.edu.vallegrande.vgmsusers.infrastructure.client.external.ReniecClient;
import pe.edu.vallegrande.vgmsusers.domain.enums.RolesUsers;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/internal")
public class InternalRest {

     private final UserService userService;
     private final ReniecClient reniecClient;
     private final UsernameGeneratorUtil usernameGenerator;
     private final pe.edu.vallegrande.vgmsusers.infrastructure.util.UserEnrichmentUtil userEnrichmentUtil;

     public InternalRest(UserService userService, ReniecClient reniecClient,
               UsernameGeneratorUtil usernameGenerator,
               pe.edu.vallegrande.vgmsusers.infrastructure.util.UserEnrichmentUtil userEnrichmentUtil) {
          this.userService = userService;
          this.reniecClient = reniecClient;
          this.usernameGenerator = usernameGenerator;
          this.userEnrichmentUtil = userEnrichmentUtil;
     }

     @GetMapping("/organizations/{organizationId}/users")
     public Mono<ApiResponse<List<CompleteUserResponse>>> getUsersByOrganization(@PathVariable String organizationId) {
          return userService.getCompleteUsersByOrganization(organizationId)
                    .flatMap(response -> {
                         if (response.isSuccess()) {
                              return Mono.just(
                                        ApiResponse.success("Usuarios obtenidos exitosamente con información completa",
                                                  response.getData()));
                         } else {
                              return Mono.just(ApiResponse.<List<CompleteUserResponse>>error(
                                        "Error obteniendo usuarios: " + response.getMessage()));
                         }
                    });
     }

     @GetMapping("/organizations/{organizationId}/clients")
     public Mono<ApiResponse<List<CompleteUserResponse>>> getClientsByOrganization(
               @PathVariable String organizationId) {
          return userService.getCompleteUsersByRole(organizationId, RolesUsers.CLIENT)
                    .flatMap(response -> {
                         if (response.isSuccess()) {
                              return Mono.just(
                                        ApiResponse.success("Clientes obtenidos exitosamente con información completa",
                                                  response.getData()));
                         } else {
                              return Mono.just(ApiResponse.<List<CompleteUserResponse>>error(
                                        "Error obteniendo clientes: " + response.getMessage()));
                         }
                    });
     }

     @GetMapping("/organizations/{organizationId}/admins")
     public Mono<ApiResponse<List<CompleteUserResponse>>> getAdminsByOrganization(@PathVariable String organizationId) {
          return userService.getCompleteUsersByRole(organizationId, RolesUsers.ADMIN)
                    .flatMap(response -> {
                         if (response.isSuccess()) {
                              return Mono.just(ApiResponse.success(
                                        "Administradores obtenidos exitosamente con información completa",
                                        response.getData()));
                         } else {
                              return Mono.just(ApiResponse.<List<CompleteUserResponse>>error(
                                        "Error obteniendo administradores: " + response.getMessage()));
                         }
                    });
     }

     @GetMapping("/users/{userId}")
     public Mono<ApiResponse<UserWithLocationResponse>> getUserById(@PathVariable String userId) {
          return userService.getUserById(userId)
                    .flatMap(response -> {
                         if (!response.isSuccess()) {
                              return Mono.just(ApiResponse.<UserWithLocationResponse>error(
                                        "Usuario no encontrado: " + response.getMessage()));
                         }

                         UserResponse user = response.getData();

                         // Enriquecer con información de organización, zona y calle
                         return userEnrichmentUtil.enrichUsersWithLocationInfo(List.of(user))
                                   .map(enrichedUsers -> {
                                        if (enrichedUsers.isEmpty()) {
                                             return ApiResponse.<UserWithLocationResponse>error(
                                                       "Error al enriquecer datos del usuario");
                                        }
                                        return ApiResponse.success(
                                                  "Usuario obtenido exitosamente con información completa",
                                                  enrichedUsers.get(0));
                                   });
                    })
                    .onErrorResume(error -> {
                         return Mono.just(ApiResponse.<UserWithLocationResponse>error(
                                   "Error interno: " + error.getMessage()));
                    });
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

                         return userEnrichmentUtil.enrichUsersWithLocationInfo(List.of(user))
                                   .map(enrichedUsers -> {
                                        if (enrichedUsers.isEmpty()) {
                                             throw new NotFoundException("Error al enriquecer datos del cliente");
                                        }
                                        return ApiResponse.success(
                                                  "Cliente obtenido exitosamente con información completa",
                                                  enrichedUsers.get(0));
                                   });
                    });
     }

     @PostMapping("/organizations/{organizationId}/create-admin")
     public Mono<ResponseEntity<ApiResponse<UserCreationResponse>>> createAdmin(
               @PathVariable String organizationId,
               @Valid @RequestBody CreateUserRequest request) {

          if (!organizationId.equals(request.getOrganizationId())) {
               return Mono.error(new ValidationException("OrganizationId en URL no coincide con el del body"));
          }

          request.setRoles(Set.of(RolesUsers.ADMIN));

          return reniecClient.getPersonalDataByDni(request.getDocumentNumber())
                    .doOnNext(reniecData -> {

                         request.setFirstName(reniecData.getFirstName());
                         request.setLastName(reniecData.getFirstLastName() +
                                   (reniecData.getSecondLastName() != null
                                             && !reniecData.getSecondLastName().trim().isEmpty()
                                                       ? " " + reniecData.getSecondLastName()
                                                       : ""));

                         if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                              String generatedUsername = usernameGenerator.generateSimpleUsername(
                                        reniecData.getFirstName(),
                                        reniecData.getFirstLastName());
                              request.setEmail(generatedUsername);
                         }
                    })
                    .onErrorMap(error -> new ValidationException("DNI no válido según RENIEC: " + error.getMessage()))
                    .flatMap(reniecData -> userService.createUserWithCredentials(request))
                    .flatMap(response -> {
                         if (response.isSuccess()) {
                              return Mono.just(ResponseEntity
                                        .status(HttpStatus.CREATED)
                                        .body(ApiResponse.success("Administradores creado exitosamente",
                                                  response.getData())));
                         } else {
                              return Mono.error(new ValidationException(
                                        "Error creando administrador: " + response.getMessage()));
                         }
                    });
     }

     @PostMapping("/create-super-admin")
     public Mono<ResponseEntity<ApiResponse<UserCreationResponse>>> createSuperAdmin(
               @Valid @RequestBody CreateUserRequest request) {

          request.setRoles(Set.of(RolesUsers.SUPER_ADMIN));

          return reniecClient.getPersonalDataByDni(request.getDocumentNumber())
                    .doOnNext(reniecData -> {

                         request.setFirstName(reniecData.getFirstName());
                         request.setLastName(
                                   reniecData.getFirstLastName() +
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
                    .onErrorMap(error -> {
                         return new ValidationException("DNI no válido según RENIEC: " + error.getMessage());
                    })
                    .flatMap(reniecData -> {
                         return userService.createUserWithCredentials(request);
                    })
                    .flatMap(response -> {
                         if (response.isSuccess()) {
                              return Mono.just(ResponseEntity
                                        .status(HttpStatus.CREATED)
                                        .body(ApiResponse.success("SUPER_ADMIN creado exitosamente",
                                                  response.getData())));
                         } else {
                              return Mono.error(new ValidationException(
                                        "Error creando SUPER_ADMIN: " + response.getMessage()));
                         }
                    });
     }

}
