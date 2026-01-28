package pe.edu.vallegrande.vgmsorganizations.application.usecases.organization;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsorganizations.domain.model.Organization;
import pe.edu.vallegrande.vgmsorganizations.domain.model.valueobject.RecordStatus;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.in.IGetOrganizationUseCase;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IOrganizationRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetOrganizationUseCaseImpl implements IGetOrganizationUseCase {

    private final IOrganizationRepository repository;

    @Override
    public Mono<Organization> findById(String id) {
        return repository.findById(id)
            .doOnSuccess(org -> log.debug("Organización encontrada: {}", id))
            .doOnError(error -> log.error("Error buscando organización {}: {}", id, error.getMessage()));
    }

    @Override
    public Mono<Organization> findByRuc(String ruc) {
        return repository.findByRuc(ruc)
            .doOnSuccess(org -> log.debug("Organización encontrada por RUC: {}", ruc));
    }

    @Override
    public Flux<Organization> findAll() {
        return repository.findAll()
            .doOnComplete(() -> log.debug("Listado de organizaciones completado"));
    }

    @Override
    public Flux<Organization> findByStatus(RecordStatus status) {
        return repository.findByStatus(status)
            .doOnComplete(() -> log.debug("Búsqueda por status {} completada", status));
    }

    @Override
    public Flux<Organization> findByRegion(String region) {
        return repository.findByRegion(region)
            .doOnComplete(() -> log.debug("Búsqueda por región {} completada", region));
    }
}
