package pe.edu.vallegrande.vgmsorganizations.application.usecases.street;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsorganizations.domain.model.Street;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.in.IGetStreetUseCase;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IStreetRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetStreetUseCaseImpl implements IGetStreetUseCase {

    private final IStreetRepository repository;

    @Override
    public Mono<Street> findById(String id) {
        return repository.findById(id)
            .doOnSuccess(street -> log.debug("Calle encontrada: {}", id))
            .doOnError(error -> log.error("Error buscando calle {}: {}", id, error.getMessage()));
    }

    @Override
    public Flux<Street> findAll() {
        return repository.findAll()
            .doOnComplete(() -> log.debug("Listado de calles completado"));
    }

    @Override
    public Flux<Street> findByZoneId(String zoneId) {
        return repository.findByZoneId(zoneId)
            .doOnComplete(() -> log.debug("Búsqueda de calles por zona {} completada", zoneId));
    }
}
