package pe.edu.vallegrande.vgmsusers.infrastructure.rest.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.vgmsusers.application.service.UserService;
import pe.edu.vallegrande.vgmsusers.domain.enums.RolesUsers;
import pe.edu.vallegrande.vgmsusers.infrastructure.dto.ApiResponse;
import pe.edu.vallegrande.vgmsusers.infrastructure.dto.ReniecResponseDto;
import pe.edu.vallegrande.vgmsusers.infrastructure.dto.request.CreateFirstUserRequest;
import pe.edu.vallegrande.vgmsusers.infrastructure.dto.request.CreateUserRequest;
import pe.edu.vallegrande.vgmsusers.infrastructure.dto.response.RoleInfoResponse;
import pe.edu.vallegrande.vgmsusers.infrastructure.dto.response.UserBasicInfoResponse;
import pe.edu.vallegrande.vgmsusers.infrastructure.dto.response.UserCreationResponse;
import pe.edu.vallegrande.vgmsusers.infrastructure.dto.response.UserResponse;
import pe.edu.vallegrande.vgmsusers.infrastructure.exception.custom.NotFoundException;
import pe.edu.vallegrande.vgmsusers.infrastructure.client.external.ReniecClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/common")
public class CommonRest {

        private final UserService userService;
        private final ReniecClient reniecClient;

        public CommonRest(UserService userService, ReniecClient reniecClient) {
                this.userService = userService;
                this.reniecClient = reniecClient;
        }

        @GetMapping("/user/code/{userCode}/basic")
        public Mono<ApiResponse<UserBasicInfoResponse>> getUserBasicInfo(@PathVariable String userCode) {

                return userService.getUserByCode(userCode)
                                .flatMap(response -> {
                                        if (response.isSuccess()) {
                                                UserResponse user = response.getData();

                                                UserBasicInfoResponse basicInfo = UserBasicInfoResponse.builder()
                                                                .userCode(user.getUserCode())
                                                                .firstName(user.getFirstName())
                                                                .lastName(user.getLastName())
                                                                .status(user.getStatus().toString())
                                                                .organizationId(user.getOrganizationId())
                                                                .build();

                                                return Mono.just(ApiResponse.success("Información básica obtenida",
                                                                basicInfo));
                                        } else {
                                                return Mono.error(new NotFoundException("Usuario no encontrado"));
                                        }
                                });
        }

        @GetMapping("/user/code/{userCode}/exists")
        public Mono<ApiResponse<Boolean>> checkUserCodeExists(@PathVariable String userCode) {
                return userService.getUserByCode(userCode)
                                .map(response -> ApiResponse.success("Verificación completada", response.isSuccess()))
                                .onErrorReturn(ApiResponse.success("Verificación completada", false));
        }

        @GetMapping("/user/email/{email}/available")
        public Mono<ApiResponse<Boolean>> checkEmailAvailable(@PathVariable String email) {
                return userService.getUserByEmail(email)
                                .map(response -> {
                                        if (response.isSuccess()) {
                                                return ApiResponse.success("Verificación de email completada",
                                                                response.getData());
                                        } else {
                                                return ApiResponse.success("Verificación de email completada", false);
                                        }
                                })
                                .onErrorReturn(ApiResponse.success("Verificación de email completada", false));
        }

        @GetMapping("/user/username/{username}")
        public Mono<ApiResponse<UserResponse>> getUserByUsername(@PathVariable String username) {

                return userService.getUserByUsername(username)
                                .doOnSuccess(response -> {
                                })
                                .doOnError(error -> {
                                });
        }

        @GetMapping("/roles")
        public Mono<ApiResponse<List<RoleInfoResponse>>> getAvailableRoles() {
                List<RoleInfoResponse> roles = Arrays.asList(
                                RoleInfoResponse.builder()
                                                .name("SUPER_ADMIN")
                                                .description("Administrador supremo del sistema")
                                                .permissions(Arrays.asList("manage_admins", "manage_all_users",
                                                                "system_config"))
                                                .build(),
                                RoleInfoResponse.builder()
                                                .name("ADMIN")
                                                .description("Administrador de organización")
                                                .permissions(Arrays.asList("manage_clients", "view_org_users",
                                                                "generate_codes"))
                                                .build(),
                                RoleInfoResponse.builder()
                                                .name("CLIENT")
                                                .description("Usuario final del sistema")
                                                .permissions(Arrays.asList("view_profile", "update_profile"))
                                                .build());

                return Mono.just(ApiResponse.success("Información de roles obtenida", roles));
        }

        @PostMapping("/setup/first-user")
        public Mono<ResponseEntity<ApiResponse<UserCreationResponse>>> createFirstUser(
                        @RequestBody CreateFirstUserRequest request) {

                return userService.countSuperAdmins()
                                .flatMap(count -> {
                                        if (count > 0) {
                                                return Mono.just(ResponseEntity
                                                                .badRequest()
                                                                .body(ApiResponse.<UserCreationResponse>error(
                                                                                "Ya existe un administrador supremo en el sistema")));
                                        }

                                        CreateUserRequest createRequest = CreateUserRequest.builder()
                                                        .firstName(request.getFirstName())
                                                        .lastName(request.getLastName())
                                                        .documentType(request.getDocumentType())
                                                        .documentNumber(request.getDocumentNumber())
                                                        .email(request.getEmail())
                                                        .phone(request.getPhone())
                                                        .address(request.getAddress())
                                                        .organizationId(request.getOrganizationId())
                                                        .roles(Set.of(RolesUsers.SUPER_ADMIN))
                                                        .build();

                                        return userService.createUserWithCredentials(createRequest)
                                                        .map(response -> {
                                                                if (response.isSuccess()) {
                                                                        UserCreationResponse creationData = response
                                                                                        .getData();

                                                                        return ResponseEntity
                                                                                        .status(HttpStatus.CREATED)
                                                                                        .body(ApiResponse.success(
                                                                                                        "Primer usuario del sistema creado exitosamente. Username: "
                                                                                                                        +
                                                                                                                        creationData.getUsername()
                                                                                                                        + ", Contraseña temporal: "
                                                                                                                        +
                                                                                                                        creationData.getTemporaryPassword(),
                                                                                                        creationData));
                                                                } else {
                                                                        return ResponseEntity
                                                                                        .badRequest()
                                                                                        .body(ApiResponse
                                                                                                        .<UserCreationResponse>error(
                                                                                                                        response.getMessage()));
                                                                }
                                                        });
                                })
                                .onErrorResume(error -> {
                                        return Mono.just(ResponseEntity
                                                        .internalServerError()
                                                        .body(ApiResponse.<UserCreationResponse>error(
                                                                        "Error creando primer usuario: "
                                                                                        + error.getMessage())));
                                });
        }

        @GetMapping("/ping")
        public Mono<String> ping() {
                return Mono.just("pong");
        }

        @GetMapping("/users/reniec/dni/{dni}")
        public Mono<ApiResponse<ReniecResponseDto>> getReniecDataByDni(@PathVariable String dni) {

                if (dni == null || dni.length() != 8 || !dni.matches("\\d{8}")) {
                        return Mono.just(ApiResponse.error("DNI debe tener exactamente 8 dígitos numéricos"));
                }

                return reniecClient.getPersonalDataByDni(dni)
                                .map(personalData -> {
                                        ReniecResponseDto reniecResponse = new ReniecResponseDto();
                                        reniecResponse.setFirstName(personalData.getFirstName());
                                        reniecResponse.setFirstLastName(personalData.getFirstLastName());
                                        reniecResponse.setSecondLastName(personalData.getSecondLastName());
                                        reniecResponse.setFullName(personalData.getFullName());
                                        reniecResponse.setDocumentNumber(personalData.getDocumentNumber());

                                        return ApiResponse.success("Datos de RENIEC obtenidos exitosamente",
                                                        reniecResponse);
                                })
                                .onErrorResume(error -> {

                                        String errorMessage;
                                        if (error.getMessage().contains("DNI inválido")) {
                                                errorMessage = "DNI inválido o con formato incorrecto";
                                        } else if (error.getMessage().contains("No se encontraron datos")) {
                                                errorMessage = "No se encontraron datos para el DNI proporcionado en RENIEC";
                                        } else if (error.getMessage().contains("no disponible")) {
                                                errorMessage = "Servicio de RENIEC no disponible temporalmente";
                                        } else {
                                                errorMessage = "Error al consultar datos de RENIEC: "
                                                                + error.getMessage();
                                        }

                                        return Mono.just(ApiResponse.<ReniecResponseDto>error(errorMessage));
                                });
        }

        @GetMapping("/user/dni/{dni}/exists")
        public Mono<ApiResponse<Boolean>> checkDniExists(@PathVariable String dni) {

                return userService.getUserByDocumentNumber(dni)
                                .map(response -> {
                                        boolean exists = response.getData();
                                        return ApiResponse.success("Verificación de DNI completada", exists);
                                })
                                .onErrorReturn(ApiResponse.success("Verificación de DNI completada", false));
        }

        @GetMapping("/user/phone/{phone}/exists")
        public Mono<ApiResponse<Boolean>> checkPhoneExists(@PathVariable String phone) {

                return userService.getUserByPhone(phone)
                                .map(response -> {
                                        boolean exists = response.getData();
                                        return ApiResponse.success("Verificación de teléfono completada", exists);
                                })
                                .onErrorReturn(ApiResponse.success("Verificación de teléfono completada", false));
        }

        @GetMapping("/user/email/{email}/exists")
        public Mono<ApiResponse<Boolean>> checkEmailExists(@PathVariable String email) {

                return userService.getUserByEmail(email)
                                .map(response -> {
                                        boolean exists = response.getData();
                                        return ApiResponse.success("Verificación de email completada", exists);
                                })
                                .onErrorReturn(ApiResponse.success("Verificación de email completada", false));
        }
}
