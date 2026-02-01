package pe.edu.vallegrande.vgmsusers.infrastructure.rest.management;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.vgmsusers.application.service.UserService;
import pe.edu.vallegrande.vgmsusers.application.service.UserCodeService;
import pe.edu.vallegrande.vgmsusers.infrastructure.client.external.ReniecClient;
import pe.edu.vallegrande.vgmsusers.infrastructure.util.UsernameGeneratorUtil;
import pe.edu.vallegrande.vgmsusers.infrastructure.validation.OrganizationValidationService;
import pe.edu.vallegrande.vgmsusers.infrastructure.dto.request.CreateUserRequest;
import pe.edu.vallegrande.vgmsusers.infrastructure.dto.response.UserResponse;
import pe.edu.vallegrande.vgmsusers.infrastructure.dto.response.UserCreationResponse;
import pe.edu.vallegrande.vgmsusers.infrastructure.dto.ApiResponse;
import pe.edu.vallegrande.vgmsusers.infrastructure.exception.custom.ValidationException;
import pe.edu.vallegrande.vgmsusers.domain.enums.RolesUsers;
import pe.edu.vallegrande.vgmsusers.domain.enums.UserStatus;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/management")
public class ManagementRest {

    private final UserService userService;
    private final UserCodeService userCodeService;
    private final ReniecClient reniecService;
    private final UsernameGeneratorUtil usernameGenerator;
    private final OrganizationValidationService organizationValidationService;

    public ManagementRest(UserService userService, UserCodeService userCodeService, ReniecClient reniecService,
            UsernameGeneratorUtil usernameGenerator, OrganizationValidationService organizationValidationService) {
        this.userService = userService;
        this.userCodeService = userCodeService;
        this.reniecService = reniecService;
        this.usernameGenerator = usernameGenerator;
        this.organizationValidationService = organizationValidationService;
    }

    /**
     * POST /api/management/admins/initial
     * Endpoint PÚBLICO para crear el primer SUPER_ADMIN del sistema
     * Solo funciona si NO existe ningún SUPER_ADMIN en la base de datos
     */
    @PostMapping("/admins/initial")
    public Mono<ResponseEntity<ApiResponse<UserCreationResponse>>> createInitialSuperAdmin(
            @Valid @RequestBody CreateUserRequest request) {

        // Validar que no existan SUPER_ADMINS en el sistema
        return userService.countSuperAdmins()
                .flatMap(count -> {
                    if (count > 0) {
                        return Mono.error(new ValidationException(
                                "Ya existe un SUPER_ADMIN en el sistema. Use el endpoint protegido /api/management/admins"));
                    }

                    return reniecService.getPersonalDataByDni(request.getDocumentNumber())
                            .doOnNext(reniecData -> {
                                request.setFirstName(reniecData.getFirstName());
                                request.setLastName(
                                        reniecData.getFirstLastName() + " " + reniecData.getSecondLastName());

                                String generatedUsername = usernameGenerator.generateIntelligentUsername(
                                        reniecData.getFirstName(),
                                        reniecData.getFirstLastName(),
                                        reniecData.getSecondLastName());

                                if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                                    request.setEmail(generatedUsername);
                                }
                            })
                            .onErrorMap(error -> new ValidationException(
                                    "DNI no válido según RENIEC: " + error.getMessage()))
                            .flatMap(reniecData -> {
                                // Forzar rol SUPER_ADMIN
                                request.setRoles(java.util.Set.of(RolesUsers.SUPER_ADMIN));

                                return userService.createUserWithCredentials(request);
                            })
                            .flatMap(response -> {
                                if (response.isSuccess()) {
                                    return Mono.just(ResponseEntity
                                            .status(HttpStatus.CREATED)
                                            .body(ApiResponse.success(
                                                    "SUPER_ADMIN inicial creado exitosamente. Guarde las credenciales de forma segura.",
                                                    response.getData())));
                                } else {
                                    return Mono.error(new ValidationException(
                                            "Error creando SUPER_ADMIN: " + response.getMessage()));
                                }
                            });
                });
    }

    /**
     * POST /api/management/admins
     * Endpoint PROTEGIDO para crear ADMIN o SUPER_ADMIN adicionales
     * Requiere autenticación con rol SUPER_ADMIN
     */
    @PostMapping("/admins")
    public Mono<ResponseEntity<ApiResponse<UserCreationResponse>>> createAdmin(
            @Valid @RequestBody CreateUserRequest request) {

        return reniecService.getPersonalDataByDni(request.getDocumentNumber())
                .doOnNext(reniecData -> {

                    request.setFirstName(reniecData.getFirstName());
                    request.setLastName(
                            reniecData.getFirstLastName() + " " + reniecData.getSecondLastName());

                    String generatedUsername = usernameGenerator.generateIntelligentUsername(
                            reniecData.getFirstName(),
                            reniecData.getFirstLastName(),
                            reniecData.getSecondLastName());

                    if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                        request.setEmail(generatedUsername);

                    }

                })
                .onErrorMap(error -> {
                    return new ValidationException(
                            "DNI no válido según RENIEC: " + error.getMessage());
                })
                .flatMap(reniecData -> {
                    if (request.getRoles() == null || request.getRoles().isEmpty()) {
                        request.setRoles(java.util.Set.of(RolesUsers.ADMIN));
                    }

                    boolean hasValidRole = request.getRoles().stream()
                            .allMatch(role -> role == RolesUsers.ADMIN || role == RolesUsers.SUPER_ADMIN);

                    if (!hasValidRole) {
                        throw new ValidationException(
                                "Este endpoint solo permite crear usuarios con rol ADMIN o SUPER_ADMIN");
                    }

                    return userService.createUserWithCredentials(request);
                })
                .flatMap(response -> {
                    if (response.isSuccess()) {

                        return Mono.just(ResponseEntity
                                .status(HttpStatus.CREATED)
                                .body(ApiResponse.success("Administrador creado exitosamente", response.getData())));
                    } else {
                        return Mono.error(new ValidationException(
                                "Error creando administrador: " + response.getMessage()));
                    }
                });
    }

    @GetMapping("/admins")
    public Mono<ApiResponse<Page<UserResponse>>> getAllAdmins(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String organizationId) {

        Pageable pageable = PageRequest.of(page, size);

        if (organizationId != null) {
            return organizationValidationService.validateOrganizationExists(organizationId)
                    .then(userService.getUsersByRole(organizationId, RolesUsers.ADMIN))
                    .flatMap(response -> {
                        if (response.isSuccess()) {
                            Page<UserResponse> pageResponse = new org.springframework.data.domain.PageImpl<>(
                                    response.getData() != null ? response.getData() : Collections.emptyList(),
                                    pageable,
                                    response.getData() != null ? response.getData().size() : 0);
                            return Mono.just(ApiResponse.success("Administradores obtenidos exitosamente",
                                    pageResponse));
                        } else {
                            return Mono.error(new ValidationException(
                                    "Error obteniendo administradores: " + response.getMessage()));
                        }
                    });
        }

        // Si no se proporciona organizationId, devolver todos los administradores
        // (global)
        return userService.getUsersByRoleGlobal(RolesUsers.ADMIN)
                .flatMap(response -> {
                    if (response.isSuccess()) {
                        Page<UserResponse> pageResponse = new org.springframework.data.domain.PageImpl<>(
                                response.getData() != null ? response.getData() : Collections.emptyList(),
                                pageable,
                                response.getData() != null ? response.getData().size() : 0);
                        return Mono.just(ApiResponse.success("Administradores globales obtenidos exitosamente",
                                pageResponse));
                    } else {
                        return Mono.error(new ValidationException(
                                "Error obteniendo administradores globales: " + response.getMessage()));
                    }
                });
    }

    @PatchMapping("/users/{id}/status")
    public Mono<ApiResponse<UserResponse>> changeAnyUserStatus(
            @PathVariable String id,
            @RequestParam UserStatus status) {

        return userService.changeUserStatus(id, status)
                .flatMap(response -> {
                    if (response.isSuccess()) {
                        return Mono.just(ApiResponse.success("Estado del usuario cambiado exitosamente",
                                response.getData()));
                    } else {
                        return Mono.error(new ValidationException(
                                "Error cambiando estado del usuario: " + response.getMessage()));
                    }
                });
    }

    @DeleteMapping("/users/{id}")
    public Mono<ApiResponse<Void>> deleteAnyUser(@PathVariable String id) {

        return userService.deleteUser(id)
                .flatMap(response -> {
                    if (response.isSuccess()) {
                        return Mono.just(ApiResponse.success("Usuario eliminado exitosamente", null));
                    } else {
                        return Mono.error(
                                new ValidationException("Error eliminando usuario: " + response.getMessage()));
                    }
                });
    }

    @DeleteMapping("/user-codes/reset/{organizationId}")
    public Mono<ApiResponse<Void>> resetUserCodeCounter(@PathVariable String organizationId) {

        return organizationValidationService.validateOrganizationExists(organizationId)
                .then(userCodeService.resetCounter(organizationId))
                .then(Mono.just(ApiResponse.success("Contador reiniciado exitosamente", null)));
    }

    @DeleteMapping("/admins/{id}/permanent")

    public Mono<ApiResponse<Void>> deleteAdminPermanently(@PathVariable String id) {

        return userService.deleteUserPermanently(id)
                .flatMap(response -> {
                    if (response.isSuccess()) {
                        return Mono.just(ApiResponse.success("Administrador eliminado permanentemente", null));
                    } else {
                        return Mono.error(new ValidationException(
                                "Error eliminando administrador: " + response.getMessage()));
                    }
                });
    }

    @PutMapping("/admins/{id}/restore")

    public Mono<ApiResponse<UserResponse>> restoreAdmin(@PathVariable String id) {

        return userService.restoreUser(id)
                .flatMap(response -> {
                    if (response.isSuccess()) {
                        return Mono.just(ApiResponse.success("Administrador restaurado exitosamente",
                                response.getData()));
                    } else {
                        return Mono.error(new ValidationException(
                                "Error restaurando administrador: " + response.getMessage()));
                    }
                });
    }

    @GetMapping("/admins/active")

    public Mono<ApiResponse<List<UserResponse>>> getActiveAdmins() {

        return userService.getUsersByRole("", RolesUsers.ADMIN)
                .flatMap(response -> {
                    if (response.isSuccess()) {
                        return Mono.just(ApiResponse.success("Administradores activos obtenidos exitosamente",
                                response.getData()));
                    } else {
                        return Mono.error(new ValidationException(
                                "Error obteniendo administradores activos: " + response.getMessage()));
                    }
                });
    }

    @GetMapping("/admins/inactive")

    public Mono<ApiResponse<List<UserResponse>>> getInactiveAdmins() {

        return Mono.just(ApiResponse.success("Funcionalidad en desarrollo", null));
    }

    @GetMapping("/stats")
    public Mono<ApiResponse<Object>> getSystemStats() {
        return Mono.just(ApiResponse.success("Estadísticas del sistema en desarrollo", null));
    }

}
