package pe.edu.vallegrande.vgmsorganizations.infrastructure.adapters.out.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vgmsorganizations.domain.exceptions.ZoneNotFoundException;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Zone;
import pe.edu.vallegrande.vgmsorganizations.domain.models.valueobjects.RecordStatus;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IZoneRepository;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.persistence.mappers.ZoneDomainMapper;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.persistence.repositories.ZoneMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ZoneRepositoryImpl implements IZoneRepository {

    private final ZoneMongoRepository mongoRepository;

    @Override
    public Mono<Zone> save(Zone zone) {
        return Mono.just(zone)
                .map(ZoneDomainMapper::toDocument)
                .flatMap(mongoRepository::save)
                .map(ZoneDomainMapper::toDomain);
    }

    @Override
    public Mono<Zone> findById(String id) {
        return mongoRepository.findById(id)
                .map(ZoneDomainMapper::toDomain)
                .switchIfEmpty(Mono.error(new ZoneNotFoundException("Zona no encontrada: " + id)));
    }

    @Override
    public Flux<Zone> findAll() {
        return mongoRepository.findAll()
                .map(ZoneDomainMapper::toDomain);
    }

    @Override
    public Flux<Zone> findByOrganizationId(String organizationId) {
        return mongoRepository.findByOrganizationId(organizationId)
                .map(ZoneDomainMapper::toDomain);
    }

    @Override
    public Flux<Zone> findByStatus(RecordStatus status) {
        return mongoRepository.findByStatus(status.name())
                .map(ZoneDomainMapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsByZoneCode(String zoneCode) {
        return mongoRepository.existsByZoneCode(zoneCode);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return mongoRepository.deleteById(id);
    }
}
