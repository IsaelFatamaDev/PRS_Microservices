package pe.edu.vallegrande.vgmsusers.infrastructure.rest.common;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.vgmsusers.application.service.UserService;
import pe.edu.vallegrande.vgmsusers.infrastructure.dto.ApiResponse;
import pe.edu.vallegrande.vgmsusers.infrastructure.dto.request.UpdateUserPatchRequest;
import pe.edu.vallegrande.vgmsusers.infrastructure.dto.response.UserResponse;
import pe.edu.vallegrande.vgmsusers.infrastructure.exception.custom.NotFoundException;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/common/profile")
public class ProfileRest {

    private final UserService userService;

    public ProfileRest(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public Mono<ResponseEntity<ApiResponse<UserResponse>>> getMyProfile(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaimAsString("userId");
        String organizationId = jwt.getClaimAsString("organizationId");

        if (userId == null || organizationId == null) {
            return Mono.just(ResponseEntity.badRequest()
                    .body(ApiResponse.error("Token incompleto: falta userId o organizationId")));
        }

        return userService.getUserById(userId)
                .map(response -> {
                    if (response.isSuccess() && response.getData() != null) {
                        // Validar que el usuario pertenezca a la organización del token
                        if (!organizationId.equals(response.getData().getOrganizationId())) {
                            return ResponseEntity.status(403)
                                    .body(ApiResponse.<UserResponse>error("Acceso denegado: Organización no coincide"));
                        }
                        return ResponseEntity.ok(response);
                    } else {
                        return ResponseEntity.status(404).body(response);
                    }
                })
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PatchMapping("/me")
    public Mono<ResponseEntity<ApiResponse<UserResponse>>> updateMyProfile(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody UpdateUserPatchRequest request) {

        String userId = jwt.getClaimAsString("userId");
        String organizationId = jwt.getClaimAsString("organizationId");

        if (userId == null || organizationId == null) {
            return Mono.just(ResponseEntity.badRequest()
                    .body(ApiResponse.error("Token incompleto: falta userId o organizationId")));
        }

        return userService.getUserById(userId)
                .flatMap(response -> {
                    if (response.isSuccess() && response.getData() != null) {
                        // Validar que el usuario pertenezca a la organización del token
                        if (!organizationId.equals(response.getData().getOrganizationId())) {
                            return Mono.just(ResponseEntity.status(403).body(
                                    ApiResponse.<UserResponse>error("Acceso denegado: Organización no coincide")));
                        }

                        return userService.patchUser(userId, request)
                                .map(updateResponse -> ResponseEntity.ok(updateResponse));
                    } else {
                        return Mono.error(new NotFoundException("Usuario no encontrado"));
                    }
                });
    }
}
