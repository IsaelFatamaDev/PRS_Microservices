package pe.edu.vallegrande.vgmsorganizations.infrastructure.adapters.in.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.vgmsorganizations.application.dto.shared.ApiResponse;
import pe.edu.vallegrande.vgmsorganizations.application.dto.street.CreateStreetRequest;
import pe.edu.vallegrande.vgmsorganizations.application.dto.street.StreetResponse;
import pe.edu.vallegrande.vgmsorganizations.application.mapper.StreetMapper;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.in.ICreateStreetUseCase;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.in.IGetStreetUseCase;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/streets")
@RequiredArgsConstructor
public class StreetRest {

    private final ICreateStreetUseCase createUseCase;
    private final IGetStreetUseCase getUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ApiResponse<StreetResponse>> create(@Valid @RequestBody CreateStreetRequest request) {
        return Mono.just(request)
                .map(StreetMapper::toDomain)
                .flatMap(createUseCase::execute)
                .map(StreetMapper::toResponse)
                .map(response -> ApiResponse.success(response, "Calle creada exitosamente"))
                .doOnSuccess(response -> log.info("Calle creada: {}", response.getData().getId()));
    }

    @GetMapping("/{id}")
    public Mono<ApiResponse<StreetResponse>> findById(@PathVariable String id) {
        return getUseCase.findById(id)
                .map(StreetMapper::toResponse)
                .map(ApiResponse::success);
    }

    @GetMapping
    public Mono<ApiResponse<Flux<StreetResponse>>> findAll() {
        Flux<StreetResponse> streets = getUseCase.findAll()
                .map(StreetMapper::toResponse);
        return Mono.just(ApiResponse.success(streets));
    }

    @GetMapping("/zone/{zoneId}")
    public Mono<ApiResponse<Flux<StreetResponse>>> findByZoneId(@PathVariable String zoneId) {
        Flux<StreetResponse> streets = getUseCase.findByZoneId(zoneId)
                .map(StreetMapper::toResponse);
        return Mono.just(ApiResponse.success(streets));
    }
}
