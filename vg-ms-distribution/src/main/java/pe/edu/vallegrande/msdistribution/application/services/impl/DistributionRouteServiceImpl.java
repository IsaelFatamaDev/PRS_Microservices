package pe.edu.vallegrande.msdistribution.application.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.msdistribution.application.services.DistributionRouteService;
import pe.edu.vallegrande.msdistribution.domain.models.DistributionRoute;
import pe.edu.vallegrande.msdistribution.domain.enums.Constants;
import pe.edu.vallegrande.msdistribution.infrastructure.dto.request.DistributionRouteCreateRequest;
import pe.edu.vallegrande.msdistribution.infrastructure.dto.response.DistributionRouteResponse;
import pe.edu.vallegrande.msdistribution.infrastructure.exception.custom.CustomException;
import pe.edu.vallegrande.msdistribution.infrastructure.repository.DistributionRouteRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.Instant;
import java.util.stream.Collectors;

/**
 * Servicio de implementación para la gestión de Rutas de Distribución.
 * 
 * @version 1.0
 */
@Service
@Slf4j
public class DistributionRouteServiceImpl implements DistributionRouteService {

    private final DistributionRouteRepository repository;

    /**
     * Inyección por constructor.
     * 
     * @param repository Repositorio de rutas.
     */
    public DistributionRouteServiceImpl(DistributionRouteRepository repository) {
        this.repository = repository;
    }

    @Override
    public Flux<DistributionRoute> getAll() {
        return repository.findAll();
    }

    @Override
    public Flux<DistributionRoute> getAllActive() {
        return repository.findAllByStatus(Constants.ACTIVE.name());
    }

    @Override
    public Flux<DistributionRoute> getAllInactive() {
        return repository.findAllByStatus(Constants.INACTIVE.name());
    }

    @Override
    public Mono<DistributionRoute> getById(String id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(CustomException.notFound("DistributionRoute", id)));
    }

    @Override
    public Mono<DistributionRouteResponse> save(DistributionRouteCreateRequest request) {
        return generateNextRouteCode()
                .flatMap(generatedCode -> {
                    // Usar el código generado si no se proporciona uno
                    String routeCode = (request.getRouteCode() != null && !request.getRouteCode().isEmpty())
                            ? request.getRouteCode()
                            : generatedCode;

                    // Convertir ZoneEntry a ZoneOrder
                    var zones = request.getZones() != null ? request.getZones().stream()
                            .map(ze -> DistributionRoute.ZoneOrder.builder()
                                    .zoneId(ze.getZoneId())
                                    .order(ze.getOrder())
                                    .estimatedDuration(ze.getEstimatedDuration())
                                    .build())
                            .collect(Collectors.toList()) : null;

                    DistributionRoute route = DistributionRoute.builder()
                            .organizationId(request.getOrganizationId())
                            .routeCode(routeCode)
                            .routeName(request.getRouteName())
                            .zones(zones)
                            .totalEstimatedDuration(
                                    request.getTotalEstimatedDuration() != null ? request.getTotalEstimatedDuration()
                                            : 0)
                            .responsibleUserId(request.getResponsibleUserId())
                            .status(Constants.ACTIVE.name())
                            .createdAt(Instant.now())
                            .build();

                    return repository.save(route)
                            .map(this::toResponse);
                });
    }

    /**
     * Genera el siguiente código de ruta (ROT###).
     * 
     * @return Mono con el código generado.
     */
    private Mono<String> generateNextRouteCode() {
        return repository.findAll()
                .map(DistributionRoute::getRouteCode)
                .filter(code -> code != null && code.startsWith("ROT"))
                .map(code -> {
                    try {
                        return Integer.parseInt(code.substring(3));
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .reduce(0, Integer::max)
                .map(maxNumber -> String.format("ROT%03d", maxNumber + 1))
                .defaultIfEmpty("ROT001");
    }

    @Override
    public Mono<DistributionRoute> update(String id, DistributionRoute route) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(CustomException.notFound("DistributionRoute", id)))
                .flatMap(existing -> {
                    existing.setOrganizationId(route.getOrganizationId());
                    existing.setRouteCode(route.getRouteCode());
                    existing.setRouteName(route.getRouteName());
                    existing.setZones(route.getZones());
                    existing.setTotalEstimatedDuration(route.getTotalEstimatedDuration());
                    existing.setResponsibleUserId(route.getResponsibleUserId());
                    return repository.save(existing);
                });
    }

    @Override
    public Mono<DistributionRouteResponse> update(String id, DistributionRouteCreateRequest request) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(CustomException.notFound("DistributionRoute", id)))
                .flatMap(existing -> {
                    // Convertir ZoneEntry a ZoneOrder
                    var zones = request.getZones() != null ? request.getZones().stream()
                            .map(ze -> DistributionRoute.ZoneOrder.builder()
                                    .zoneId(ze.getZoneId())
                                    .order(ze.getOrder())
                                    .estimatedDuration(ze.getEstimatedDuration())
                                    .build())
                            .collect(Collectors.toList()) : null;

                    existing.setOrganizationId(request.getOrganizationId());
                    existing.setRouteCode(request.getRouteCode());
                    existing.setRouteName(request.getRouteName());
                    existing.setZones(zones);
                    existing.setTotalEstimatedDuration(
                            request.getTotalEstimatedDuration() != null ? request.getTotalEstimatedDuration() : 0);
                    existing.setResponsibleUserId(request.getResponsibleUserId());
                    return repository.save(existing);
                })
                .map(this::toResponse);
    }

    @Override
    public Mono<DistributionRouteResponse> updatePartial(String id,
            pe.edu.vallegrande.msdistribution.infrastructure.dto.request.DistributionRouteUpdateRequest request) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(CustomException.notFound("DistributionRoute", id)))
                .flatMap(existing -> {
                    if (request.getOrganizationId() != null) {
                        existing.setOrganizationId(request.getOrganizationId());
                    }
                    if (request.getRouteName() != null) {
                        existing.setRouteName(request.getRouteName());
                    }
                    if (request.getZones() != null) {
                        var zones = request.getZones().stream()
                                .map(ze -> DistributionRoute.ZoneOrder.builder()
                                        .zoneId(ze.getZoneId())
                                        .order(ze.getOrder())
                                        .estimatedDuration(ze.getEstimatedDuration())
                                        .build())
                                .collect(Collectors.toList());
                        existing.setZones(zones);
                    }
                    if (request.getTotalEstimatedDuration() != null) {
                        existing.setTotalEstimatedDuration(request.getTotalEstimatedDuration());
                    }
                    if (request.getResponsibleUserId() != null) {
                        existing.setResponsibleUserId(request.getResponsibleUserId());
                    }
                    return repository.save(existing);
                })
                .map(this::toResponse);
    }

    @Override
    public Mono<Void> delete(String id) {
        return repository.deleteById(id);
    }

    @Override
    public Mono<DistributionRouteResponse> activate(String id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(CustomException.notFound("DistributionRoute", id)))
                .flatMap(route -> {
                    route.setStatus(Constants.ACTIVE.name());
                    return repository.save(route);
                })
                .map(this::toResponse);
    }

    @Override
    public Mono<DistributionRouteResponse> deactivate(String id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(CustomException.notFound("DistributionRoute", id)))
                .flatMap(route -> {
                    route.setStatus(Constants.INACTIVE.name());
                    return repository.save(route);
                })
                .map(this::toResponse);
    }

    /**
     * Mapea entidad a DTO de respuesta.
     * 
     * @param route Entidad ruta.
     * @return DTO ruta.
     */
    private DistributionRouteResponse toResponse(DistributionRoute route) {
        return DistributionRouteResponse.builder()
                .id(route.getId())
                .organizationId(route.getOrganizationId())
                .routeCode(route.getRouteCode())
                .routeName(route.getRouteName())
                .status(route.getStatus())
                .createdAt(route.getCreatedAt())
                .build();
    }
}
