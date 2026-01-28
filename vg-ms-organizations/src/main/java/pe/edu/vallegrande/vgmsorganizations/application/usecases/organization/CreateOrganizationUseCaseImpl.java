package pe.edu.vallegrande.vgmsorganizations.application.usecases.organization;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsorganizations.domain.event.DomainEvent;
import pe.edu.vallegrande.vgmsorganizations.domain.exception.DuplicateOrganizationException;
import pe.edu.vallegrande.vgmsorganizations.domain.model.Organization;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.in.ICreateOrganizationUseCase;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IDomainEventPublisher;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IOrganizationRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateOrganizationUseCaseImpl implements ICreateOrganizationUseCase {

    private final IOrganizationRepository repository;
    private final IDomainEventPublisher eventPublisher;

    @Override
    public Mono<Organization> execute(Organization organization) {
        return repository.existsByRuc(organization.getRuc())
            .flatMap(exists -> {
                if (exists) {
                    return Mono.error(new DuplicateOrganizationException(
                        "Ya existe una organización con el RUC: " + organization.getRuc()));
                }
                return repository.save(organization)
                    .flatMap(saved -> publishDomainEvents(saved).thenReturn(saved));
            })
            .doOnSuccess(saved -> log.info("Organización creada: {}", saved.getId()))
            .doOnError(error -> log.error("Error creando organización: {}", error.getMessage()));
    }

    private Mono<Void> publishDomainEvents(Organization organization) {
        return Flux.fromIterable(organization.getDomainEvents())
            .flatMap(eventPublisher::publish)
            .doOnComplete(organization::clearDomainEvents)
            .then();
    }
}
