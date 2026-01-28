package pe.edu.vallegrande.vgmsorganizations.infrastructure.adapters.out.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vgmsorganizations.domain.exceptions.OrganizationNotFoundException;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Organization;
import pe.edu.vallegrande.vgmsorganizations.domain.models.valueobjects.RecordStatus;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IOrganizationRepository;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.persistence.mappers.OrganizationDomainMapper;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.persistence.repositories.OrganizationMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class OrganizationRepositoryImpl implements IOrganizationRepository {

    private final OrganizationMongoRepository mongoRepository;

    @Override
    public Mono<Organization> save(Organization organization) {
        return Mono.just(organization)
                .map(OrganizationDomainMapper::toDocument)
                .flatMap(mongoRepository::save)
                .map(OrganizationDomainMapper::toDomain);
    }

    @Override
    public Mono<Organization> findById(String id) {
        return mongoRepository.findById(id)
                .map(OrganizationDomainMapper::toDomain)
                .switchIfEmpty(Mono.error(new OrganizationNotFoundException("Organización no encontrada: " + id)));
    }

    @Override
    public Mono<Organization> findByRuc(String ruc) {
        return mongoRepository.findByRuc(ruc)
                .map(OrganizationDomainMapper::toDomain)
                .switchIfEmpty(
                        Mono.error(new OrganizationNotFoundException("Organización no encontrada con RUC: " + ruc)));
    }

    @Override
    public Flux<Organization> findAll() {
        return mongoRepository.findAll()
                .map(OrganizationDomainMapper::toDomain);
    }

    @Override
    public Flux<Organization> findByStatus(RecordStatus status) {
        return mongoRepository.findByStatus(status.name())
                .map(OrganizationDomainMapper::toDomain);
    }

    @Override
    public Flux<Organization> findByRegion(String region) {
        return mongoRepository.findByRegion(region)
                .map(OrganizationDomainMapper::toDomain);
    }

    @Override
    public Mono<Boolean> existsByRuc(String ruc) {
        return mongoRepository.existsByRuc(ruc);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return mongoRepository.deleteById(id);
    }
}
