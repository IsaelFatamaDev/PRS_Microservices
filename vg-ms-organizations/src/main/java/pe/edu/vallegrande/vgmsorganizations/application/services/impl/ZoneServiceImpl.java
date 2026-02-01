package pe.edu.vallegrande.vgmsorganizations.application.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsorganizations.application.services.ZoneService;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Zone;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.request.ZoneRequest;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.response.ZoneResponse;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.exception.CustomException;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.mapper.ZoneMapper;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.repository.ZoneRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@Slf4j
public class ZoneServiceImpl implements ZoneService {

    private final ZoneRepository zoneRepository;
    private final ZoneMapper zoneMapper;

    // Constructor explícito (PRS1 Standard - No usar @RequiredArgsConstructor)
    public ZoneServiceImpl(ZoneRepository zoneRepository, ZoneMapper zoneMapper) {
        this.zoneRepository = zoneRepository;
        this.zoneMapper = zoneMapper;
    }

    @Override
    public Mono<ZoneResponse> create(ZoneRequest request) {
        return zoneRepository.findFirstByOrderByZoneCodeDesc()
                .map(zone -> generateZoneCode(zone.getZoneCode()))
                .defaultIfEmpty("ZN0001")
                .flatMap(zoneCode -> {
                    Zone zone = Zone.builder()
                            .organizationId(request.getOrganizationId())
                            .zoneCode(zoneCode)
                            .zoneName(request.getZoneName())
                            .description(request.getDescription())
                            .status("ACTIVE")
                            .createdAt(Instant.now())
                            .build();

                    return zoneRepository.save(zoneMapper.toEntity(zone))
                            .map(zoneMapper::toDomain)
                            .map(this::mapToResponse);
                });
    }

    @Override
    public Flux<ZoneResponse> findAll() {
        return zoneRepository.findAll()
                .map(zoneMapper::toDomain)
                .map(this::mapToResponse);
    }

    @Override
    public Mono<ZoneResponse> findById(String id) {
        return zoneRepository.findById(id)
                .map(zoneMapper::toDomain)
                .map(this::mapToResponse)
                .switchIfEmpty(Mono.error(new CustomException(404, "Zona no encontrada", "La zona con ID " + id + " no existe")));
    }

    @Override
    public Mono<ZoneResponse> update(String id, ZoneRequest request) {
        return zoneRepository.findById(id)
                .switchIfEmpty(Mono.error(new CustomException(404, "Zona no encontrada", "La zona con ID " + id + " no existe")))
                .map(zoneMapper::toDomain)
                .flatMap(existingZone -> {
                    // Mantener el código existente, solo actualizar los otros campos
                    existingZone.setOrganizationId(request.getOrganizationId());
                    existingZone.setZoneName(request.getZoneName());
                    existingZone.setDescription(request.getDescription());
                    // NO actualizar el zoneCode, mantener el original
                    return zoneRepository.save(zoneMapper.toEntity(existingZone));
                })
                .map(zoneMapper::toDomain)
                .map(this::mapToResponse);
    }

    @Override
    public Mono<Void> delete(String id) {
        return zoneRepository.findById(id)
                .switchIfEmpty(Mono.error(new CustomException(404, "Zona no encontrada", "La zona con ID " + id + " no existe")))
                .map(zoneMapper::toDomain)
                .flatMap(zone -> {
                    zone.setStatus("INACTIVE");
                    return zoneRepository.save(zoneMapper.toEntity(zone));
                })
                .then();
    }

    @Override
    public Mono<Void> restore(String id) {
        return zoneRepository.findById(id)
                .switchIfEmpty(Mono.error(new CustomException(404, "Zona no encontrada", "La zona con ID " + id + " no existe")))
                .map(zoneMapper::toDomain)
                .flatMap(zone -> {
                    zone.setStatus("ACTIVE");
                    return zoneRepository.save(zoneMapper.toEntity(zone));
                })
                .then();
    }

    @Override
    public Flux<ZoneResponse> findByOrganizationId(String organizationId) {
        return zoneRepository.findAllByOrganizationId(organizationId)
                .map(zoneMapper::toDomain)
                .map(this::mapToResponse);
    }

    @Override
    public Mono<ZoneResponse> findByZoneIdAndOrganizationId(String zoneId, String organizationId) {
        return zoneRepository.findByZoneIdAndOrganizationId(zoneId, organizationId)
                .map(zoneMapper::toDomain)
                .map(this::mapToResponse)
                .switchIfEmpty(Mono.error(new CustomException(404, "Zona no encontrada", "La zona con ID " + zoneId + " no pertenece a su organización")));
    }

    private String generateZoneCode(String lastCode) {
        if (lastCode == null || lastCode.isEmpty()) {
            return "ZN0001";
        }
        
        // Extraer el número del código anterior
        String numberPart = lastCode.substring(2); // Quitar "ZN"
        int nextNumber = Integer.parseInt(numberPart) + 1;
        
        // Formatear con ceros a la izquierda
        return String.format("ZN%04d", nextNumber);
    }

    private ZoneResponse mapToResponse(Zone zone) {
        return ZoneResponse.builder()
                .zoneId(zone.getZoneId())
                .organizationId(zone.getOrganizationId())
                .zoneCode(zone.getZoneCode())
                .zoneName(zone.getZoneName())
                .description(zone.getDescription())
                .status(zone.getStatus())
                .build();
    }
}