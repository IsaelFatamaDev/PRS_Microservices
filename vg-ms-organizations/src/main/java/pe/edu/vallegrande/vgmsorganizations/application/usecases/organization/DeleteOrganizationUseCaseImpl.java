package pe.edu.vallegrande.vgmsorganizations.application.usecases.organization;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsorganizations.domain.exception.OrganizationNotFoundException;
import pe.edu.vallegrande.vgmsorganizations.domain.model.Organization;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.in.IDeleteOrganizationUseCase;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IDomainEventPublisher;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IOrganizationRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteOrganizationUseCaseImpl implements IDeleteOrganizationUseCase {

    private final IOrganizationRepository repository;
    private final IDomainEventPublisher eventPublisher;

    @Override
    public Mono<Organization> softDelete(String id) {
        return repository.findById(id)
            .switchIfEmpty(Mono.error(new OrganizationNotFoundException("Organización no encontrada con ID: " + id)))
            .flatMap(organization -> {
                Organization deleted = organization.markAsDeleted();
                return repository.save(deleted)
                    .flatMap(saved -> publishDomainEvents(saved).thenReturn(saved));
            })
            .doOnSuccess(deleted -> log.info("Organización eliminada (soft): {}", deleted.getId()))
            .doOnError(error -> log.error("Error eliminando organización: {}", error.getMessage()));
    }

    @Override
    public Mono<Organization> restore(String id) {
        return repository.findById(id)
            .switchIfEmpty(Mono.error(new OrganizationNotFoundException("Organización no encontrada con ID: " + id)))
            .flatMap(organization -> {
                Organization restored = organization.markAsRestored();
                return repository.save(restored);
            })
            .doOnSuccess(restored -> log.info("Organización restaurada: {}", restored.getId()))
            .doOnError(error -> log.error("Error restaurando organización: {}", error.getMessage()));
    }

    private Mono<Void> publishDomainEvents(Organization organization) {
        return Flux.fromIterable(organization.getDomainEvents())
            .flatMap(eventPublisher::publish)
            .doOnComplete(organization::clearDomainEvents)
            .then();
    }
}
