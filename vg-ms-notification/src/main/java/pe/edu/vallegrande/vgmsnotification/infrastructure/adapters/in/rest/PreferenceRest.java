package pe.edu.vallegrande.vgmsnotification.infrastructure.adapters.in.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.vgmsnotification.application.dtos.preference.PreferenceResponse;
import pe.edu.vallegrande.vgmsnotification.application.dtos.preference.UpdatePreferenceRequest;
import pe.edu.vallegrande.vgmsnotification.application.dtos.shared.ApiResponse;
import pe.edu.vallegrande.vgmsnotification.application.mappers.PreferenceMapper;
import pe.edu.vallegrande.vgmsnotification.domain.models.NotificationPreference;
import pe.edu.vallegrande.vgmsnotification.domain.ports.in.IGetPreferenceUseCase;
import pe.edu.vallegrande.vgmsnotification.domain.ports.in.IUpdatePreferenceUseCase;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/preferences")
@RequiredArgsConstructor
public class PreferenceRest {

    private final IGetPreferenceUseCase getPreferenceUseCase;
    private final IUpdatePreferenceUseCase updatePreferenceUseCase;

    @GetMapping("/user/{userId}")
    public Mono<ResponseEntity<ApiResponse<PreferenceResponse>>> getPreferencesByUserId(
            @PathVariable String userId) {

        return getPreferenceUseCase.findByUserId(userId)
            .map(PreferenceMapper::toResponse)
            .map(response -> ResponseEntity
                .ok(ApiResponse.success(response, "Preferences retrieved successfully")))
            .switchIfEmpty(Mono.just(ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("Preferences not found for user"))));
    }

    @PutMapping("/user/{userId}")
    public Mono<ResponseEntity<ApiResponse<PreferenceResponse>>> updatePreferences(
            @PathVariable String userId,
            @RequestBody UpdatePreferenceRequest request) {

        return getPreferenceUseCase.findByUserId(userId)
            .switchIfEmpty(Mono.defer(() -> {
                // Crear nuevas preferencias si no existen
                NotificationPreference newPref = PreferenceMapper.toDomain(request, userId, null);
                return Mono.just(newPref);
            }))
            .flatMap(existing -> {
                // Actualizar preferencias existentes
                NotificationPreference updated = PreferenceMapper.toDomain(
                    request,
                    userId,
                    existing.getId()
                );
                return updatePreferenceUseCase.execute(updated);
            })
            .map(PreferenceMapper::toResponse)
            .map(response -> ResponseEntity
                .ok(ApiResponse.success(response, "Preferences updated successfully")))
            .onErrorResume(e -> Mono.just(ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(e.getMessage()))));
    }
}
