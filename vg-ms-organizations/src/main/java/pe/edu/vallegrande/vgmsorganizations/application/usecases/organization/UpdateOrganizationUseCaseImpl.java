package pe.edu.vallegrande.vgmsorganizations.application.usecases.organization;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsorganizations.domain.exception.OrganizationNotFoundException;
import pe.edu.vallegrande.vgmsorganizations.domain.model.Organization;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.in.IUpdateOrganizationUseCase;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IDomainEventPublisher;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IOrganizationRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateOrganizationUseCaseImpl implements IUpdateOrganizationUseCase {

    private final IOrganizationRepository repository;
    private final IDomainEventPublisher eventPublisher;

    @Override
    public Mono<Organization> execute(String id, Organization updatedOrganization) {
        return repository.findById(id)
            .switchIfEmpty(Mono.error(new OrganizationNotFoundException("Organización no encontrada con ID: " + id)))
            .flatMap(existing -> {
                Organization updated = existing.updateWith(
                    updatedOrganization.getName(),
                    updatedOrganization.getAddress(),
                    updatedOrganization.getPhone(),
                    updatedOrganization.getEmail(),
                    updatedOrganization.getRegion(),
                    updatedOrganization.getProvince(),
                    updatedOrganization.getDistrict(),
                    updatedOrganization.getLegalRepresentative(),
                    updatedOrganization.getType(),
                    updatedOrganization.getStatus()
                );
                return repository.save(updated)
                    .flatMap(saved -> publishDomainEvents(saved).thenReturn(saved));
            })
            .doOnSuccess(saved -> log.info("Organización actualizada: {}", saved.getId()))
            .doOnError(error -> log.error("Error actualizando organización: {}", error.getMessage()));
    }

    private Mono<Void> publishDomainEvents(Organization organization) {
        return Flux.fromIterable(organization.getDomainEvents())
            .flatMap(eventPublisher::publish)
            .doOnComplete(organization::clearDomainEvents)
            .then();
    }
}
