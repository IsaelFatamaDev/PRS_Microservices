package pe.edu.vallegrande.ms_water_quality.infrastructure.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.ms_water_quality.infrastructure.document.TestingPointDocument;
import reactor.core.publisher.Flux;

/**
 * Repository: TestingPoint
 * Repositorio reactivo para persistencia de puntos de muestreo en MongoDB.
 * Trabaja con TestingPointDocument (capa de infraestructura).
 */
@Repository
public interface TestingPointRepository extends ReactiveMongoRepository<TestingPointDocument, String> {
    Flux<TestingPointDocument> findByStatus(String status);
    Flux<TestingPointDocument> findByOrganizationId(String organizationId);
    Flux<TestingPointDocument> findByOrganizationIdAndStatus(String organizationId, String status);
}