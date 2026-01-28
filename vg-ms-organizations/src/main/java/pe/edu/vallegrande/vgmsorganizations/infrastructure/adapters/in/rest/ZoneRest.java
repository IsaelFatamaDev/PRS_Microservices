package pe.edu.vallegrande.vgmsorganizations.infrastructure.adapters.in.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.vgmsorganizations.application.dto.shared.ApiResponse;
import pe.edu.vallegrande.vgmsorganizations.application.dto.zone.CreateZoneRequest;
import pe.edu.vallegrande.vgmsorganizations.application.dto.zone.UpdateZoneFeeRequest;
import pe.edu.vallegrande.vgmsorganizations.application.dto.zone.ZoneResponse;
import pe.edu.vallegrande.vgmsorganizations.application.mapper.ZoneMapper;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.in.ICreateZoneUseCase;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.in.IGetZoneUseCase;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/zones")
@RequiredArgsConstructor
public class ZoneRest {

    private final ICreateZoneUseCase createUseCase;
    private final IGetZoneUseCase getUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ApiResponse<ZoneResponse>> create(@Valid @RequestBody CreateZoneRequest request) {
        return Mono.just(request)
                .map(ZoneMapper::toDomain)
                .flatMap(createUseCase::execute)
                .map(ZoneMapper::toResponse)
                .map(response -> ApiResponse.success(response, "Zona creada exitosamente"))
                .doOnSuccess(response -> log.info("Zona creada: {}", response.getData().getId()));
    }

    @GetMapping("/{id}")
    public Mono<ApiResponse<ZoneResponse>> findById(@PathVariable String id) {
        return getUseCase.findById(id)
                .map(ZoneMapper::toResponse)
                .map(ApiResponse::success);
    }

    @GetMapping
    public Mono<ApiResponse<Flux<ZoneResponse>>> findAll() {
        Flux<ZoneResponse> zones = getUseCase.findAll()
                .map(ZoneMapper::toResponse);
        return Mono.just(ApiResponse.success(zones));
    }

    @GetMapping("/organization/{organizationId}")
    public Mono<ApiResponse<Flux<ZoneResponse>>> findByOrganizationId(@PathVariable String organizationId) {
        Flux<ZoneResponse> zones = getUseCase.findByOrganizationId(organizationId)
                .map(ZoneMapper::toResponse);
        return Mono.just(ApiResponse.success(zones));
    }

    @PatchMapping("/{zoneId}/update-fee")
    public Mono<ApiResponse<ZoneResponse>> updateFee(
            @PathVariable String zoneId,
            @Valid @RequestBody UpdateZoneFeeRequest request) {
        return createUseCase.updateZoneFee(
                zoneId,
                request.getNewFee(),
                request.getChangedBy(),
                request.getReason())
                .map(ZoneMapper::toResponse)
                .map(response -> ApiResponse.success(response, "Tarifa actualizada exitosamente"));
    }
}
