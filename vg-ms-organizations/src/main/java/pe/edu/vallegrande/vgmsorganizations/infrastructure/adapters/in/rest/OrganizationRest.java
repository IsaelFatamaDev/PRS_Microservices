package pe.edu.vallegrande.vgmsorganizations.infrastructure.adapters.in.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.vgmsorganizations.application.dto.organization.CreateOrganizationRequest;
import pe.edu.vallegrande.vgmsorganizations.application.dto.organization.OrganizationResponse;
import pe.edu.vallegrande.vgmsorganizations.application.dto.organization.UpdateOrganizationRequest;
import pe.edu.vallegrande.vgmsorganizations.application.dto.shared.ApiResponse;
import pe.edu.vallegrande.vgmsorganizations.application.mapper.OrganizationMapper;
import pe.edu.vallegrande.vgmsorganizations.domain.model.Organization;
import pe.edu.vallegrande.vgmsorganizations.domain.model.valueobject.RecordStatus;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.in.ICreateOrganizationUseCase;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.in.IDeleteOrganizationUseCase;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.in.IGetOrganizationUseCase;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.in.IUpdateOrganizationUseCase;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequestMapping("/api/organizations")
@RequiredArgsConstructor
public class OrganizationRest {

    private final ICreateOrganizationUseCase createUseCase;
    private final IGetOrganizationUseCase getUseCase;
    private final IUpdateOrganizationUseCase updateUseCase;
    private final IDeleteOrganizationUseCase deleteUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<ApiResponse<OrganizationResponse>> create(@Valid @RequestBody CreateOrganizationRequest request) {
        return Mono.just(request)
                .map(OrganizationMapper::toDomain)
                .flatMap(createUseCase::execute)
                .map(OrganizationMapper::toResponse)
                .map(response -> ApiResponse.success(response, "Organización creada exitosamente"))
                .doOnSuccess(response -> log.info("Organización creada: {}", response.getData().getId()));
    }

    @GetMapping("/{id}")
    public Mono<ApiResponse<OrganizationResponse>> findById(@PathVariable String id) {
        return getUseCase.findById(id)
                .map(OrganizationMapper::toResponse)
                .map(ApiResponse::success);
    }

    @GetMapping("/ruc/{ruc}")
    public Mono<ApiResponse<OrganizationResponse>> findByRuc(@PathVariable String ruc) {
        return getUseCase.findByRuc(ruc)
                .map(OrganizationMapper::toResponse)
                .map(ApiResponse::success);
    }

    @GetMapping
    public Mono<ApiResponse<Flux<OrganizationResponse>>> findAll() {
        Flux<OrganizationResponse> organizations = getUseCase.findAll()
                .map(OrganizationMapper::toResponse);
        return Mono.just(ApiResponse.success(organizations));
    }

    @GetMapping("/status/{status}")
    public Mono<ApiResponse<Flux<OrganizationResponse>>> findByStatus(@PathVariable String status) {
        RecordStatus recordStatus = RecordStatus.valueOf(status.toUpperCase());
        Flux<OrganizationResponse> organizations = getUseCase.findByStatus(recordStatus)
                .map(OrganizationMapper::toResponse);
        return Mono.just(ApiResponse.success(organizations));
    }

    @GetMapping("/region/{region}")
    public Mono<ApiResponse<Flux<OrganizationResponse>>> findByRegion(@PathVariable String region) {
        Flux<OrganizationResponse> organizations = getUseCase.findByRegion(region)
                .map(OrganizationMapper::toResponse);
        return Mono.just(ApiResponse.success(organizations));
    }

    @PutMapping("/{id}")
    public Mono<ApiResponse<OrganizationResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateOrganizationRequest request) {
        return getUseCase.findById(id)
                .map(existing -> OrganizationMapper.toDomainForUpdate(id, request, existing))
                .flatMap(updated -> updateUseCase.execute(id, updated))
                .map(OrganizationMapper::toResponse)
                .map(response -> ApiResponse.success(response, "Organización actualizada exitosamente"));
    }

    @DeleteMapping("/{id}")
    public Mono<ApiResponse<OrganizationResponse>> delete(@PathVariable String id) {
        return deleteUseCase.softDelete(id)
                .map(OrganizationMapper::toResponse)
                .map(response -> ApiResponse.success(response, "Organización eliminada exitosamente"));
    }

    @PatchMapping("/{id}/restore")
    public Mono<ApiResponse<OrganizationResponse>> restore(@PathVariable String id) {
        return deleteUseCase.restore(id)
                .map(OrganizationMapper::toResponse)
                .map(response -> ApiResponse.success(response, "Organización restaurada exitosamente"));
    }
}
