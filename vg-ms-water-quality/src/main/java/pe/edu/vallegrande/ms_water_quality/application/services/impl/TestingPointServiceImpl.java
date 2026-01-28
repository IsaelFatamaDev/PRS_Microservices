package pe.edu.vallegrande.ms_water_quality.application.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.ms_water_quality.application.services.TestingPointService;
import pe.edu.vallegrande.ms_water_quality.domain.models.TestingPoint;
import pe.edu.vallegrande.ms_water_quality.infrastructure.client.dto.ExternalOrganization;
import pe.edu.vallegrande.ms_water_quality.infrastructure.dto.request.TestingPointCreateRequest;
import pe.edu.vallegrande.ms_water_quality.infrastructure.dto.response.TestingPointResponse;
import pe.edu.vallegrande.ms_water_quality.infrastructure.dto.response.enriched.TestingPointEnrichedResponse;
import pe.edu.vallegrande.ms_water_quality.infrastructure.exception.CustomException;
import pe.edu.vallegrande.ms_water_quality.infrastructure.repository.TestingPointRepository;
import pe.edu.vallegrande.ms_water_quality.infrastructure.service.ExternalServiceClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestingPointServiceImpl implements TestingPointService {

    private final TestingPointRepository testingPointRepository;
    private final ExternalServiceClient externalServiceClient;
    private final pe.edu.vallegrande.ms_water_quality.infrastructure.mapper.TestingPointMapper testingPointMapper;
    private final pe.edu.vallegrande.ms_water_quality.infrastructure.security.JwtService jwtService;

    @Override
    public Flux<TestingPointEnrichedResponse> getAll() {
        log.debug("Getting all testing points for the current organization");
        return getCurrentUserOrganizationId()
                .flatMapMany(this::getAllByOrganization);
    }

    @Override
    public Flux<TestingPointEnrichedResponse> getAllActive() {
        return getCurrentUserOrganizationId()
            .flatMapMany(this::getAllActiveByOrganization);
    }

    @Override
    public Flux<TestingPointEnrichedResponse> getAllInactive() {
        return getCurrentUserOrganizationId()
            .flatMapMany(this::getAllInactiveByOrganization);
    }

    @Override
    public Mono<TestingPointEnrichedResponse> getById(String id) {
        return getCurrentUserOrganizationId()
            .flatMap(orgId -> getByIdAndOrganization(id, orgId));
    }

    @Override
    public Mono<TestingPointResponse> save(TestingPointCreateRequest request) {
        log.info("Creating testing point: pointName={}, pointType={}, organizationId={}", 
            request.getPointName(), request.getPointType(), request.getOrganizationId());
        
        TestingPoint testingPoint = new TestingPoint();
        testingPoint.setOrganizationId(request.getOrganizationId());
        
        if (request.getPointCode() != null && !request.getPointCode().trim().isEmpty()) {
            testingPoint.setPointCode(request.getPointCode());
            return saveTestingPoint(testingPoint, request);
        } else {
            return generateNextPointCode(request.getPointType())
                .flatMap(pointCode -> {
                    testingPoint.setPointCode(pointCode);
                    return saveTestingPoint(testingPoint, request);
                })
                .doOnSuccess(saved -> log.info("Testing point created successfully: id={}, pointCode={}", 
                    saved.getId(), saved.getPointCode()))
                .doOnError(error -> log.error("Failed to create testing point: pointName={}, error={}", 
                    request.getPointName(), error.getMessage(), error));
        }
    }

    @Override
    public Mono<TestingPoint> update(String id, TestingPoint point) {
        log.info("Updating testing point: id={}", id);
        return testingPointRepository.findById(id)
            .switchIfEmpty(Mono.error(CustomException.notFound("TestingPoint", id)))
            .map(testingPointMapper::toDomain)
            .flatMap(existing -> {
                existing.setOrganizationId(point.getOrganizationId());
                existing.setPointCode(point.getPointCode());
                existing.setPointName(point.getPointName());
                existing.setPointType(point.getPointType());
                existing.setZoneId(point.getZoneId());
                existing.setLocationDescription(point.getLocationDescription());
                existing.setStreet(point.getStreet());
                if (point.getCoordinates() != null) {
                    existing.setCoordinates(TestingPoint.Coordinates.builder()
                        .latitude(point.getCoordinates().getLatitude())
                        .longitude(point.getCoordinates().getLongitude())
                        .build());
                }
                existing.setStatus(point.getStatus());
                existing.setUpdatedAt(LocalDateTime.now());
                return testingPointRepository.save(testingPointMapper.toDocument(existing))
                    .map(testingPointMapper::toDomain);
            })
            .doOnSuccess(updated -> log.info("Testing point updated successfully: id={}", id))
            .doOnError(error -> log.error("Failed to update testing point: id={}, error={}", id, error.getMessage()));
    }

    @Override
    public Mono<Void> delete(String id) {
        return testingPointRepository.deleteById(id);
    }

    @Override
    public Mono<TestingPointEnrichedResponse> activate(String id) {
        log.info("Activating testing point: id={}", id);
        return testingPointRepository.findById(id)
            .switchIfEmpty(Mono.error(CustomException.notFound("TestingPoint", id)))
            .map(testingPointMapper::toDomain)
            .flatMap(point -> {
                point.setStatus("ACTIVE");
                return testingPointRepository.save(testingPointMapper.toDocument(point))
                    .map(testingPointMapper::toDomain);
            })
            .flatMap(this::enrichTestingPoint);
    }

    @Override
    public Mono<TestingPointEnrichedResponse> deactivate(String id) {
        log.info("Deactivating testing point: id={}", id);
        return testingPointRepository.findById(id)
            .switchIfEmpty(Mono.error(CustomException.notFound("TestingPoint", id)))
            .map(testingPointMapper::toDomain)
            .flatMap(point -> {
                point.setStatus("INACTIVE");
                return testingPointRepository.save(testingPointMapper.toDocument(point))
                    .map(testingPointMapper::toDomain);
            })
            .flatMap(this::enrichTestingPoint);
    }

    @Override
    public Flux<TestingPointEnrichedResponse> getAllByOrganization(String organizationId) {
        log.debug("Getting testing points by organization: organizationId={}", organizationId);
        return testingPointRepository.findByOrganizationId(organizationId)
            .map(testingPointMapper::toDomain)
            .flatMap(this::enrichTestingPoint);
    }

    @Override
    public Flux<TestingPointEnrichedResponse> getAllActiveByOrganization(String organizationId) {
        log.debug("Getting active testing points by organization: organizationId={}", organizationId);
        return testingPointRepository.findByOrganizationIdAndStatus(organizationId, "ACTIVE")
            .map(testingPointMapper::toDomain)
            .flatMap(this::enrichTestingPoint);
    }

    @Override
    public Flux<TestingPointEnrichedResponse> getAllInactiveByOrganization(String organizationId) {
        log.debug("Getting inactive testing points by organization: organizationId={}", organizationId);
        return testingPointRepository.findByOrganizationIdAndStatus(organizationId, "INACTIVE")
            .map(testingPointMapper::toDomain)
            .flatMap(this::enrichTestingPoint);
    }

    @Override
    public Mono<TestingPointEnrichedResponse> getByIdAndOrganization(String id, String organizationId) {
        log.debug("Getting testing point by id and organization: id={}, organizationId={}", id, organizationId);
        return testingPointRepository.findById(id)
            .map(testingPointMapper::toDomain)
            .filter(point -> point.getOrganizationId().equals(organizationId))
            .flatMap(this::enrichTestingPoint)
            .switchIfEmpty(Mono.error(CustomException.notFound("TestingPoint", id)));
    }

    /**
     * Obtiene el organizationId del JWT token del usuario autenticado
     * Según estándar PRS01
     */
    private Mono<String> getCurrentUserOrganizationId() {
        return jwtService.getOrganizationIdFromToken()
            .doOnSuccess(orgId -> log.debug("Using organizationId from JWT: {}", orgId))
            .onErrorResume(error -> {
                log.warn("No se pudo obtener organizationId del JWT, usando fallback: {}", error.getMessage());
                // Fallback temporal para desarrollo/testing
                return testingPointRepository.findAll()
                    .map(testingPointMapper::toDomain)
                    .map(TestingPoint::getOrganizationId)
                    .distinct()
                    .next()
                    .switchIfEmpty(Mono.just("default-org-id"));
            });
    }

    private Mono<TestingPointEnrichedResponse> enrichTestingPoint(TestingPoint point) {
        Mono<ExternalOrganization> orgMono = externalServiceClient
            .getOrganizationById(point.getOrganizationId());

        return orgMono.map(org -> TestingPointEnrichedResponse.builder()
                .id(point.getId())
                .pointCode(point.getPointCode())
                .pointName(point.getPointName())
                .pointType(point.getPointType())
                .zoneId(point.getZoneId())
                .locationDescription(point.getLocationDescription())
                .street(point.getStreet())
                .coordinates(point.getCoordinates())
                .status(point.getStatus())
                .createdAt(point.getCreatedAt())
                .updatedAt(point.getUpdatedAt())
                .organizationId(org)
                .build())
            .switchIfEmpty(Mono.just(TestingPointEnrichedResponse.builder()
                .id(point.getId())
                .pointCode(point.getPointCode())
                .pointName(point.getPointName())
                .pointType(point.getPointType())
                .zoneId(point.getZoneId())
                .locationDescription(point.getLocationDescription())
                .street(point.getStreet())
                .coordinates(point.getCoordinates())
                .status(point.getStatus())
                .createdAt(point.getCreatedAt())
                .updatedAt(point.getUpdatedAt())
                .organizationId(null)
                .build()));
    }

    private Mono<String> generateNextPointCode(String pointType) {
        String prefix = getPointCodePrefix(pointType);
        log.debug("Generating next point code for type: {}, prefix: {}", pointType, prefix);

        return testingPointRepository.findAll()
            .map(testingPointMapper::toDomain)
            .filter(tp -> tp.getPointCode() != null && tp.getPointCode().startsWith(prefix))
            .sort((tp1, tp2) -> tp2.getPointCode().compareTo(tp1.getPointCode()))
            .next()
            .map(last -> {
                try {
                    String numberPart = last.getPointCode().substring(2);
                    int number = Integer.parseInt(numberPart);
                    String newCode = String.format("%s%03d", prefix, number + 1);
                    log.debug("Generated point code: {}", newCode);
                    return newCode;
                } catch (Exception e) {
                    log.warn("Error parsing point code, using default: {}", e.getMessage());
                    return String.format("%s%03d", prefix, 1);
                }
            })
            .defaultIfEmpty(String.format("%s%03d", prefix, 1));
    }
    
    private String getPointCodePrefix(String pointType) {
        if (pointType != null) {
            switch (pointType.toUpperCase()) {
                case "RESERVORIO": return "PR";
                case "RED_DISTRIBUCION": return "PD";
                case "DOMICILIO": return "PM";
                default: return "PT";
            }
        }
        return "PT";
    }

    private Mono<TestingPointResponse> saveTestingPoint(TestingPoint testingPoint, TestingPointCreateRequest request) {
        testingPoint.setPointName(request.getPointName());
        testingPoint.setPointType(request.getPointType());
        testingPoint.setZoneId(request.getZoneId());
        testingPoint.setLocationDescription(request.getLocationDescription());
        testingPoint.setStreet(request.getStreet());
        testingPoint.setCoordinates(TestingPoint.Coordinates.builder()
            .latitude(request.getCoordinates().getLatitude())
            .longitude(request.getCoordinates().getLongitude())
            .build());
        testingPoint.setCreatedAt(LocalDateTime.now());
        testingPoint.setUpdatedAt(LocalDateTime.now());
        testingPoint.setStatus("ACTIVE");

        return testingPointRepository.save(testingPointMapper.toDocument(testingPoint))
            .map(testingPointMapper::toDomain)
            .map(saved -> {
                TestingPointResponse response = new TestingPointResponse();
                response.setId(saved.getId());
                response.setOrganizationId(saved.getOrganizationId());
                response.setPointCode(saved.getPointCode());
                response.setPointName(saved.getPointName());
                response.setPointType(saved.getPointType());
                response.setZoneId(saved.getZoneId());
                response.setLocationDescription(saved.getLocationDescription());
                response.setStreet(saved.getStreet());
                if (saved.getCoordinates() != null) {
                    response.setCoordinates(new pe.edu.vallegrande.ms_water_quality.infrastructure.dto.response.TestingPointResponse.Coordinates(
                        saved.getCoordinates().getLatitude(), 
                        saved.getCoordinates().getLongitude()
                    ));
                }
                response.setStatus(saved.getStatus());
                response.setCreatedAt(saved.getCreatedAt());
                return response;
            });
    }

    @Override
    public Mono<Object> debugGetAll() {
        return testingPointRepository.findAll()
                .map(testingPointMapper::toDomain)
                .collectList()
                .map(allPoints -> {
                    String currentOrgId = "6896b2ecf3e398570ffd99d3";
                    return java.util.Map.of(
                        "totalPointsInDB", allPoints.size(),
                        "currentUserOrgId", currentOrgId,
                        "pointsForCurrentOrg", allPoints.stream()
                            .filter(p -> currentOrgId.equals(p.getOrganizationId()))
                            .count(),
                        "allOrganizationIds", allPoints.stream()
                            .map(TestingPoint::getOrganizationId)
                            .distinct()
                            .toList(),
                        "samplePoints", allPoints.stream()
                            .limit(3)
                            .map(p -> java.util.Map.of(
                                "id", p.getId(),
                                "organizationId", p.getOrganizationId(),
                                "pointName", p.getPointName(),
                                "status", p.getStatus()
                            ))
                            .toList()
                    );
                });
    }
}