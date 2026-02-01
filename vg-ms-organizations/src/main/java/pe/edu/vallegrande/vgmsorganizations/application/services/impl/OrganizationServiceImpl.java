package pe.edu.vallegrande.vgmsorganizations.application.services.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.Duration;
import java.time.Instant;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import pe.edu.vallegrande.vgmsorganizations.application.services.OrganizationService;
import pe.edu.vallegrande.vgmsorganizations.domain.enums.Constants;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Organization;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.client.external.UserAuthClient;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.document.OrganizationDocument;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.request.OrganizationRequest;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.response.*;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.mapper.OrganizationMapper;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.repository.*;

@Service
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final ZoneRepository zoneRepository;
    private final StreetRepository streetRepository;
    private final FareRepository fareRepository;
    private final ParameterRepository parameterRepository;
    private final UserAuthClient userAuthClient;
    private final CloudinaryFileStorageService cloudinaryService;
    private final OrganizationMapper organizationMapper;

    // Constructor explícito (PRS1 Standard - No usar @RequiredArgsConstructor con
    // @Value)
    public OrganizationServiceImpl(
            OrganizationRepository organizationRepository,
            ZoneRepository zoneRepository,
            StreetRepository streetRepository,
            FareRepository fareRepository,
            ParameterRepository parameterRepository,
            UserAuthClient userAuthClient,
            CloudinaryFileStorageService cloudinaryService,
            OrganizationMapper organizationMapper) {
        this.organizationRepository = organizationRepository;
        this.zoneRepository = zoneRepository;
        this.streetRepository = streetRepository;
        this.fareRepository = fareRepository;
        this.parameterRepository = parameterRepository;
        this.userAuthClient = userAuthClient;
        this.cloudinaryService = cloudinaryService;
        this.organizationMapper = organizationMapper;
    }

    @Override
    public Mono<OrganizationResponse> create(OrganizationRequest request) {
        return generateNextCode()
                .flatMap(code -> {
                    // Procesar logo si existe, pero no fallar si hay error
                    Mono<String> logoMono = (request.getLogo() != null && !request.getLogo().trim().isEmpty())
                            ? cloudinaryService.processLogo(request.getLogo()).onErrorReturn("")
                            : Mono.just("");

                    return logoMono.flatMap(logoUrl -> {
                        OrganizationDocument document = OrganizationDocument.builder()
                                .organizationCode(code)
                                .organizationName(request.getOrganizationName())
                                .legalRepresentative(request.getLegalRepresentative())
                                .address(request.getAddress())
                                .phone(request.getPhone())
                                .logo(logoUrl.isEmpty() ? null : logoUrl) // Solo guardar si hay URL
                                .status(Constants.ACTIVE)
                                .build();

                        // Las fechas se manejan automáticamente por @CreatedDate y @LastModifiedDate
                        return organizationRepository.save(document);
                    });
                })
                .map(saved -> OrganizationResponse.builder()
                        .organizationId(saved.getOrganizationId())
                        .organizationCode(saved.getOrganizationCode())
                        .organizationName(saved.getOrganizationName())
                        .legalRepresentative(saved.getLegalRepresentative())
                        .address(saved.getAddress())
                        .phone(saved.getPhone())
                        .logo(saved.getLogo())
                        .status(saved.getStatus())
                        .createdAt(saved.getCreatedAt())
                        .updatedAt(saved.getUpdatedAt())
                        .build());
    }

    @Override
    public Flux<OrganizationResponse> findAll() {
        // Procesamiento completamente reactivo: cada organización se procesa
        // individualmente
        // sin cargar todo en memoria
        return organizationRepository.findAllByStatus(Constants.ACTIVE)
                .flatMap(doc -> {
                    Organization org = organizationMapper.toDomain(doc);
                    // Para cada organización, hacer las consultas necesarias de forma reactiva
                    return mapToOrganizationResponse(org);
                }, 1); // Procesar 1 organización a la vez para minimizar memoria
    }

    @Override
    public Mono<OrganizationResponse> findById(String id) {
        return organizationRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Organization not found")))
                .flatMap(doc -> mapToOrganizationResponse(organizationMapper.toDomain(doc)));
    }

    @Override
    public Mono<OrganizationResponse> update(String id, OrganizationRequest request) {
        return organizationRepository.findById(id)
                .flatMap(existing -> {
                    // Si hay un nuevo logo, procesar y eliminar el anterior
                    if (request.getLogo() != null && !request.getLogo().trim().isEmpty()) {
                        return cloudinaryService.deleteLogo(existing.getLogo()) // Eliminar logo anterior
                                .then(cloudinaryService.processLogo(request.getLogo())) // Procesar nuevo logo
                                .flatMap(newLogoUrl -> {
                                    existing.setOrganizationName(request.getOrganizationName());
                                    existing.setLegalRepresentative(request.getLegalRepresentative());
                                    existing.setAddress(request.getAddress());
                                    existing.setPhone(request.getPhone());
                                    existing.setLogo(newLogoUrl); // Guardar URL completa de Cloudinary
                                    existing.setUpdatedAt(Instant.now());
                                    return organizationRepository.save(existing);
                                });
                    } else {
                        // Actualizar sin cambiar el logo
                        existing.setOrganizationName(request.getOrganizationName());
                        existing.setLegalRepresentative(request.getLegalRepresentative());
                        existing.setAddress(request.getAddress());
                        existing.setPhone(request.getPhone());
                        existing.setUpdatedAt(Instant.now());
                        return organizationRepository.save(existing);
                    }
                })
                .flatMap(doc -> {
                    Organization org = organizationMapper.toDomain(doc);
                    return mapToOrganizationResponse(org);
                });
    }

    @Override
    public Mono<Void> delete(String id) {
        return organizationRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Organización no encontrada")))
                .flatMap(org -> {
                    org.setStatus(Constants.INACTIVE);
                    org.setUpdatedAt(Instant.now());
                    return organizationRepository.save(org);
                })
                .then();
    }

    @Override
    public Mono<Void> restore(String id) {
        return organizationRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Organización no encontrada")))
                .flatMap(org -> {
                    org.setStatus(Constants.ACTIVE);
                    org.setUpdatedAt(Instant.now());
                    return organizationRepository.save(org);
                })
                .then();
    }

    @Override
    public Flux<OrganizationWithAdminsResponse> getOrganizationsWithAdmins() {
        // Optimizado: Procesar en streaming sin cargar todas las organizaciones en
        // memoria
        return organizationRepository.findAllByStatus(Constants.ACTIVE)
                .flatMap(doc -> {
                    Organization organization = organizationMapper.toDomain(doc);
                    return userAuthClient.getAdminsByOrganizationId(organization.getOrganizationId())
                            .map(user -> AdminUserResponse.builder()
                                    .userId(user.getId())
                                    .firstName(user.getFirstName())
                                    .lastName(user.getLastName())
                                    .email(user.getEmail())
                                    .documentType(user.getDocumentType())
                                    .documentNumber(user.getDocumentNumber())
                                    .phone(user.getPhone())
                                    .address(user.getAddress())
                                    .build())
                            .collectList()
                            .map(admins -> OrganizationWithAdminsResponse.builder()
                                    .organizationId(organization.getOrganizationId())
                                    .organizationCode(organization.getOrganizationCode())
                                    .organizationName(organization.getOrganizationName())
                                    .legalRepresentative(organization.getLegalRepresentative())
                                    .address(organization.getAddress())
                                    .phone(organization.getPhone())
                                    .logo(organization.getLogo())
                                    .status(organization.getStatus())
                                    .createdAt(organization.getCreatedAt())
                                    .updatedAt(organization.getUpdatedAt())
                                    .admins(admins)
                                    .build())
                            .onErrorReturn(OrganizationWithAdminsResponse.builder()
                                    .organizationId(organization.getOrganizationId())
                                    .organizationCode(organization.getOrganizationCode())
                                    .organizationName(organization.getOrganizationName())
                                    .legalRepresentative(organization.getLegalRepresentative())
                                    .address(organization.getAddress())
                                    .phone(organization.getPhone())
                                    .logo(organization.getLogo())
                                    .status(organization.getStatus())
                                    .createdAt(organization.getCreatedAt())
                                    .updatedAt(organization.getUpdatedAt())
                                    .admins(Collections.emptyList()) // En caso de error, lista vacía
                                    .build());
                },
                        1) // Procesar 1 organización a la vez para minimizar memoria
                .timeout(Duration.ofSeconds(30)); // Timeout total más razonable
    }

    private Mono<OrganizationResponse> mapToOrganizationResponse(Organization org) {
        // Optimizado: Eliminado .cache() para evitar mantener datos en memoria
        // indefinidamente
        // Obtener parámetros de la organización (en paralelo)
        Mono<List<ParameterResponse>> parametersMono = parameterRepository
                .findAllByOrganizationId(org.getOrganizationId())
                .map(parameter -> ParameterResponse.builder()
                        .id(parameter.getId())
                        .organizationId(parameter.getOrganizationId())
                        .parameterCode(parameter.getParameterCode())
                        .parameterName(parameter.getParameterName())
                        .parameterValue(parameter.getParameterValue())
                        .parameterDescription(parameter.getParameterDescription())
                        .status(parameter.getStatus())
                        .createdAt(parameter.getCreatedAt())
                        .updatedAt(parameter.getUpdatedAt())
                        .build())
                .collectList()
                .onErrorReturn(Collections.emptyList());

        // Obtener todas las zonas primero
        Mono<List<ZoneResponse>> zonesMono = zoneRepository.findAllByOrganizationId(org.getOrganizationId())
                .collectList()
                .flatMap(zones -> {
                    if (zones.isEmpty()) {
                        return Mono.just(Collections.<ZoneResponse>emptyList());
                    }

                    // Obtener todas las calles de todas las zonas en una sola operación
                    List<String> zoneIds = zones.stream()
                            .map(zone -> zone.getZoneId())
                            .collect(Collectors.toList());

                    return Mono.zip(
                            streetRepository.findAllByZoneIdIn(zoneIds).collectList(),
                            fareRepository.findAllByZoneIdIn(zoneIds).collectList()).map(tuple -> {
                                List<pe.edu.vallegrande.vgmsorganizations.infrastructure.document.StreetDocument> allStreets = tuple
                                        .getT1();
                                List<pe.edu.vallegrande.vgmsorganizations.infrastructure.document.FareDocument> allFares = tuple
                                        .getT2();

                                // Agrupar calles por zona
                                Map<String, List<StreetResponse>> streetsByZone = allStreets.stream()
                                        .collect(Collectors.groupingBy(
                                                street -> street.getZoneId(),
                                                Collectors.mapping(
                                                        street -> StreetResponse.builder()
                                                                .streetId(street.getStreetId())
                                                                .zoneId(street.getZoneId())
                                                                .streetCode(street.getStreetCode())
                                                                .streetName(street.getStreetName())
                                                                .streetType(street.getStreetType())
                                                                .status(street.getStatus())
                                                                .createdAt(street.getCreatedAt())
                                                                .build(),
                                                        Collectors.toList())));

                                // Agrupar tarifas por zona
                                Map<String, List<FareResponse>> faresByZone = allFares.stream()
                                        .collect(Collectors.groupingBy(
                                                fare -> fare.getZoneId(),
                                                Collectors.mapping(
                                                        fare -> pe.edu.vallegrande.vgmsorganizations.infrastructure.mapper.FareMapper
                                                                .toResponse(
                                                                        pe.edu.vallegrande.vgmsorganizations.infrastructure.mapper.FareMapper
                                                                                .toDomain(fare)),
                                                        Collectors.toList())));

                                // Mapear zonas con sus calles y tarifas
                                return zones.stream()
                                        .map(zone -> {
                                            ZoneResponse response = ZoneResponse.builder()
                                                    .zoneId(zone.getZoneId())
                                                    .organizationId(zone.getOrganizationId())
                                                    .zoneCode(zone.getZoneCode())
                                                    .zoneName(zone.getZoneName())
                                                    .description(zone.getDescription())
                                                    .status(zone.getStatus())
                                                    .streets(streetsByZone.getOrDefault(zone.getZoneId(),
                                                            Collections.emptyList()))
                                                    .fares(faresByZone.getOrDefault(zone.getZoneId(),
                                                            Collections.emptyList()))
                                                    .build();

                                            // Enriquecer fares con zoneName (ya lo tenemos aquí)
                                            if (response.getFares() != null) {
                                                response.getFares().forEach(f -> f.setZoneName(zone.getZoneName()));
                                            }

                                            return response;
                                        })
                                        .collect(Collectors.toList());
                            });
                })
                .onErrorReturn(Collections.emptyList());

        // Combinar parámetros y zonas en la respuesta
        return Mono.zip(parametersMono, zonesMono)
                .map(tuple -> OrganizationResponse.builder()
                        .organizationId(org.getOrganizationId())
                        .organizationCode(org.getOrganizationCode())
                        .organizationName(org.getOrganizationName())
                        .legalRepresentative(org.getLegalRepresentative())
                        .address(org.getAddress())
                        .phone(org.getPhone())
                        .logo(org.getLogo())
                        .status(org.getStatus())
                        .parameters(tuple.getT1())
                        .createdAt(org.getCreatedAt())
                        .updatedAt(org.getUpdatedAt())
                        .zones(tuple.getT2())
                        .build())
                .timeout(Duration.ofSeconds(10))
                .onErrorReturn(OrganizationResponse.builder()
                        .organizationId(org.getOrganizationId())
                        .organizationCode(org.getOrganizationCode())
                        .organizationName(org.getOrganizationName())
                        .legalRepresentative(org.getLegalRepresentative())
                        .address(org.getAddress())
                        .phone(org.getPhone())
                        .logo(org.getLogo())
                        .status(org.getStatus())
                        .parameters(Collections.emptyList())
                        .zones(Collections.emptyList())
                        .createdAt(org.getCreatedAt())
                        .updatedAt(org.getUpdatedAt())
                        .build());
    }

    // ========================================
    // MÉTODOS OPTIMIZADOS ADICIONALES
    // ========================================

    @Override
    public Flux<OrganizationStatsResponse> findAllLight() {
        return organizationRepository.findAllByStatus(Constants.ACTIVE)
                .map(doc -> mapToOrganizationResponseLight(organizationMapper.toDomain(doc))); // Sin zonas/calles, sin
                                                                                               // consultas adicionales
    }

    @Override
    public Mono<OrganizationResponse> findByIdLight(String id) {
        return organizationRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Organization not found")))
                .flatMap(doc -> {
                    Organization org = organizationMapper.toDomain(doc);
                    // Incluir parámetros pero no zonas/calles
                    return parameterRepository.findAllByOrganizationId(org.getOrganizationId())
                            .map(parameter -> ParameterResponse.builder()
                                    .id(parameter.getId())
                                    .organizationId(parameter.getOrganizationId())
                                    .parameterCode(parameter.getParameterCode())
                                    .parameterName(parameter.getParameterName())
                                    .parameterValue(parameter.getParameterValue())
                                    .parameterDescription(parameter.getParameterDescription())
                                    .status(parameter.getStatus())
                                    .createdAt(parameter.getCreatedAt())
                                    .updatedAt(parameter.getUpdatedAt())
                                    .build())
                            .collectList()
                            .onErrorReturn(Collections.emptyList())
                            .map(parameters -> OrganizationResponse.builder()
                                    .organizationId(org.getOrganizationId())
                                    .organizationCode(org.getOrganizationCode())
                                    .organizationName(org.getOrganizationName())
                                    .legalRepresentative(org.getLegalRepresentative())
                                    .address(org.getAddress())
                                    .phone(org.getPhone())
                                    .logo(org.getLogo())
                                    .status(org.getStatus())
                                    .parameters(parameters)
                                    .createdAt(org.getCreatedAt())
                                    .updatedAt(org.getUpdatedAt())
                                    .zones(Collections.emptyList()) // No carga zonas para ahorrar memoria
                                    .build());
                })
                .timeout(Duration.ofSeconds(10)); // Timeout más corto para consultas individuales
    }

    // Mapeo ligero sin zonas/calles (ahorra mucha memoria)
    private OrganizationStatsResponse mapToOrganizationResponseLight(Organization org) {
        return OrganizationStatsResponse.builder()
                .organizationId(org.getOrganizationId())
                .organizationCode(org.getOrganizationCode() != null ? org.getOrganizationCode() : "")
                .organizationName(org.getOrganizationName())
                .legalRepresentative(org.getLegalRepresentative())
                .address(org.getAddress())
                .phone(org.getPhone())
                .logo(org.getLogo())
                .status(org.getStatus())
                .parameters(Collections.emptyList()) // Lista vacía en lugar de null
                .totalZones(0L) // 0 en lugar de null
                .totalStreets(0L) // 0 en lugar de null
                .build();
    }

    // ========================================
    // MÉTODOS CON ESTADÍSTICAS
    // ========================================

    @Override
    public Flux<OrganizationStatsResponse> findAllWithStats() {
        // Procesamiento completamente reactivo: cada organización se procesa
        // individualmente
        // sin cargar todo en memoria
        return organizationRepository.findAllByStatus(Constants.ACTIVE)
                .flatMap(doc -> {
                    Organization org = organizationMapper.toDomain(doc);
                    // Para cada organización, hacer las consultas necesarias de forma reactiva
                    return mapToOrganizationStatsResponse(org);
                }, 1); // Procesar 1 organización a la vez para minimizar memoria
    }

    @Override
    public Mono<OrganizationStatsResponse> findByIdWithStats(String id) {
        return organizationRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Organization not found")))
                .flatMap(doc -> {
                    Organization org = organizationMapper.toDomain(doc);
                    return mapToOrganizationStatsResponse(org);
                })
                .timeout(Duration.ofSeconds(10)); // Increased timeout
    }

    private Mono<OrganizationStatsResponse> mapToOrganizationStatsResponse(Organization org) {
        // Versión optimizada: usa count() en lugar de cargar datos en memoria cuando
        // solo se necesita contar
        // Obtener parámetros (necesarios para la respuesta)
        Mono<List<ParameterResponse>> parametersMono = parameterRepository
                .findAllByOrganizationId(org.getOrganizationId())
                .map(parameter -> ParameterResponse.builder()
                        .id(parameter.getId())
                        .organizationId(parameter.getOrganizationId())
                        .parameterCode(parameter.getParameterCode())
                        .parameterName(parameter.getParameterName())
                        .parameterValue(parameter.getParameterValue())
                        .parameterDescription(parameter.getParameterDescription())
                        .status(parameter.getStatus())
                        .createdAt(parameter.getCreatedAt())
                        .updatedAt(parameter.getUpdatedAt())
                        .build())
                .collectList()
                .onErrorReturn(Collections.emptyList());

        // Contar zonas directamente sin cargar en memoria
        Mono<Long> zoneCountMono = zoneRepository.findAllByOrganizationId(org.getOrganizationId())
                .count()
                .onErrorReturn(0L);

        // Contar calles: primero obtener IDs de zonas, luego contar calles
        Mono<Long> streetCountMono = zoneRepository.findAllByOrganizationId(org.getOrganizationId())
                .map(zone -> zone.getZoneId())
                .collectList()
                .flatMap(zoneIds -> {
                    if (zoneIds.isEmpty()) {
                        return Mono.just(0L);
                    }
                    return streetRepository.findAllByZoneIdIn(zoneIds)
                            .count()
                            .onErrorReturn(0L);
                })
                .onErrorReturn(0L);

        // Combinar todos los datos
        return Mono.zip(parametersMono, zoneCountMono, streetCountMono)
                .map(tuple -> OrganizationStatsResponse.builder()
                        .organizationId(org.getOrganizationId())
                        .organizationCode(org.getOrganizationCode())
                        .organizationName(org.getOrganizationName())
                        .legalRepresentative(org.getLegalRepresentative())
                        .address(org.getAddress())
                        .phone(org.getPhone())
                        .logo(org.getLogo())
                        .status(org.getStatus())
                        .parameters(tuple.getT1())
                        .totalZones(tuple.getT2())
                        .totalStreets(tuple.getT3())
                        .build());
    }

    private Mono<String> generateNextCode() {
        // Optimizado: Solo obtiene el último registro en lugar de cargar todos
        return organizationRepository.findFirstByOrganizationCodeIsNotNullOrderByOrganizationCodeDesc()
                .map(last -> {
                    String lastCode = last.getOrganizationCode();
                    if (lastCode != null && lastCode.startsWith("JASS")) {
                        try {
                            int number = Integer.parseInt(lastCode.replace("JASS", ""));
                            return String.format("JASS%03d", number + 1);
                        } catch (NumberFormatException e) {
                            return "JASS001";
                        }
                    }
                    return "JASS001";
                })
                .defaultIfEmpty("JASS001");
    }

    @Override
    public Flux<OrganizationResponse> findByOrganizationId(String organizationId) {
        return organizationRepository.findByOrganizationId(organizationId)
                .flatMap(doc -> {
                    Organization org = organizationMapper.toDomain(doc);
                    return mapToOrganizationResponse(org);
                });
    }
}