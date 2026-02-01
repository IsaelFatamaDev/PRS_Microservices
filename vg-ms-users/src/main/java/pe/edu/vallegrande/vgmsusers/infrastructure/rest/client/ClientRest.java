package pe.edu.vallegrande.vgmsusers.infrastructure.rest.client;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.vgmsusers.application.service.UserService;
import pe.edu.vallegrande.vgmsusers.infrastructure.dto.request.UpdateUserRequest;
import pe.edu.vallegrande.vgmsusers.infrastructure.dto.response.UserResponse;
import pe.edu.vallegrande.vgmsusers.infrastructure.dto.ApiResponse;
import pe.edu.vallegrande.vgmsusers.infrastructure.exception.custom.ForbiddenException;
import pe.edu.vallegrande.vgmsusers.infrastructure.exception.custom.NotFoundException;
import pe.edu.vallegrande.vgmsusers.infrastructure.exception.custom.ValidationException;
import pe.edu.vallegrande.vgmsusers.infrastructure.validation.OrganizationValidationService;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/client")

@Validated
public class ClientRest {

     private static final String USER_NOT_FOUND_MESSAGE = "Usuario no encontrado";

     private final UserService userService;
     private final OrganizationValidationService organizationValidationService;

     public ClientRest(UserService userService, OrganizationValidationService organizationValidationService) {
          this.userService = userService;
          this.organizationValidationService = organizationValidationService;
     }

     @GetMapping("/profile")
     public Mono<ApiResponse<UserResponse>> getMyProfile() {
          return Mono.error(new ForbiddenException("Esta funcionalidad requiere acceso a través del Gateway"));
     }

     @GetMapping("/profile/code/{userCode}")
     public Mono<ApiResponse<UserResponse>> getMyProfileByCode(@PathVariable String userCode) {

          return userService.getUserByCode(userCode)
                    .flatMap(response -> {
                         if (response.isSuccess()) {
                              UserResponse user = response.getData();
                              return organizationValidationService.validateOrganizationExists(user.getOrganizationId())
                                        .then(Mono.just(ApiResponse.success("Perfil obtenido exitosamente", user)));
                         } else {
                              return Mono.error(new NotFoundException(USER_NOT_FOUND_MESSAGE));
                         }
                    });
     }

     @PutMapping("/profile")
     public Mono<ApiResponse<UserResponse>> updateMyProfile(@Valid @RequestBody UpdateUserRequest request) {
          return Mono.error(new ForbiddenException("Esta funcionalidad requiere acceso a través del Gateway"));
     }

     @PatchMapping("/profile/status")
     public Mono<ApiResponse<String>> requestStatusChange(@RequestParam String statusChangeReason) {

          return Mono.just(ApiResponse.success(
                    "Solicitud de cambio de estado enviada. Será procesada por un administrador.",
                    "Solicitud registrada con razón: " + statusChangeReason));
     }

     @GetMapping("/user-code")
     public Mono<ApiResponse<String>> getMyUserCode() {
          return Mono.error(new ForbiddenException("Esta funcionalidad requiere acceso a través del Gateway"));
     }
}
