package pe.edu.vallegrande.vgmsorganizations.application.usecases.zone;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsorganizations.domain.model.Zone;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.in.IGetZoneUseCase;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IZoneRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetZoneUseCaseImpl implements IGetZoneUseCase {

    private final IZoneRepository repository;

    @Override
    public Mono<Zone> findById(String id) {
        return repository.findById(id)
            .doOnSuccess(zone -> log.debug("Zona encontrada: {}", id))
            .doOnError(error -> log.error("Error buscando zona {}: {}", id, error.getMessage()));
    }

    @Override
    public Flux<Zone> findAll() {
        return repository.findAll()
            .doOnComplete(() -> log.debug("Listado de zonas completado"));
    }

    @Override
    public Flux<Zone> findByOrganizationId(String organizationId) {
        return repository.findByOrganizationId(organizationId)
            .doOnComplete(() -> log.debug("Búsqueda de zonas por organización {} completada", organizationId));
    }
}
