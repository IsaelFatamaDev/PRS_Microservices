package pe.edu.vallegrande.vgmsorganizations.application.usecases.zone;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsorganizations.domain.exception.OrganizationNotFoundException;
import pe.edu.vallegrande.vgmsorganizations.domain.exception.ZoneNotFoundException;
import pe.edu.vallegrande.vgmsorganizations.domain.model.Zone;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.in.ICreateZoneUseCase;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IDomainEventPublisher;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IOrganizationRepository;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IZoneRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateZoneUseCaseImpl implements ICreateZoneUseCase {

    private final IZoneRepository zoneRepository;
    private final IOrganizationRepository organizationRepository;
    private final IDomainEventPublisher eventPublisher;

    @Override
    public Mono<Zone> execute(Zone zone) {
        return organizationRepository.findById(zone.getOrganizationId())
            .switchIfEmpty(Mono.error(new OrganizationNotFoundException(
                "Organización no encontrada: " + zone.getOrganizationId())))
            .flatMap(org -> zoneRepository.existsByZoneCode(zone.getZoneCode())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new IllegalArgumentException(
                            "Ya existe una zona con el código: " + zone.getZoneCode()));
                    }
                    return zoneRepository.save(zone)
                        .flatMap(saved -> publishDomainEvents(saved).thenReturn(saved));
                }))
            .doOnSuccess(saved -> log.info("Zona creada: {}", saved.getId()))
            .doOnError(error -> log.error("Error creando zona: {}", error.getMessage()));
    }

    @Override
    public Mono<Zone> updateZoneFee(String zoneId, BigDecimal newFee, String changedBy, String reason) {
        return zoneRepository.findById(zoneId)
            .switchIfEmpty(Mono.error(new ZoneNotFoundException("Zona no encontrada: " + zoneId)))
            .flatMap(zone -> {
                Zone updated = zone.updateFee(newFee, changedBy, reason);
                return zoneRepository.save(updated)
                    .flatMap(saved -> publishDomainEvents(saved).thenReturn(saved));
            })
            .doOnSuccess(saved -> log.info("Tarifa de zona actualizada: {} -> {}", zoneId, newFee))
            .doOnError(error -> log.error("Error actualizando tarifa: {}", error.getMessage()));
    }

    private Mono<Void> publishDomainEvents(Zone zone) {
        return Flux.fromIterable(zone.getDomainEvents())
            .flatMap(eventPublisher::publish)
            .doOnComplete(zone::clearDomainEvents)
            .then();
    }
}
