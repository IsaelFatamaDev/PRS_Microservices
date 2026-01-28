package pe.edu.vallegrande.vgmsorganizations.application.usecases.street;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsorganizations.domain.exception.ZoneNotFoundException;
import pe.edu.vallegrande.vgmsorganizations.domain.model.Street;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.in.ICreateStreetUseCase;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IDomainEventPublisher;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IStreetRepository;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IZoneRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateStreetUseCaseImpl implements ICreateStreetUseCase {

    private final IStreetRepository streetRepository;
    private final IZoneRepository zoneRepository;
    private final IDomainEventPublisher eventPublisher;

    @Override
    public Mono<Street> execute(Street street) {
        return zoneRepository.findById(street.getZoneId())
            .switchIfEmpty(Mono.error(new ZoneNotFoundException("Zona no encontrada: " + street.getZoneId())))
            .flatMap(zone -> streetRepository.save(street)
                .flatMap(saved -> publishDomainEvents(saved).thenReturn(saved)))
            .doOnSuccess(saved -> log.info("Calle creada: {}", saved.getId()))
            .doOnError(error -> log.error("Error creando calle: {}", error.getMessage()));
    }

    private Mono<Void> publishDomainEvents(Street street) {
        return Flux.fromIterable(street.getDomainEvents())
            .flatMap(eventPublisher::publish)
            .doOnComplete(street::clearDomainEvents)
            .then();
    }
}
