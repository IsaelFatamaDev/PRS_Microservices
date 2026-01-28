package pe.edu.vallegrande.ms_water_quality.infrastructure.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.ms_water_quality.infrastructure.document.QualityTestDocument;
import reactor.core.publisher.Flux;

/**
 * Repository: QualityTest
 * Repositorio reactivo para persistencia de pruebas de calidad en MongoDB.
 * Trabaja con QualityTestDocument (capa de infraestructura).
 */
@Repository
public interface QualityTestRepository extends ReactiveMongoRepository<QualityTestDocument, String> {

    Flux<QualityTestDocument> findAllByOrganizationId(String organizationId);
    Flux<QualityTestDocument> findAllByTestType(String testType);
    Flux<QualityTestDocument> findAllByStatus(String status);
    Flux<QualityTestDocument> findByOrganizationIdAndStatus(String organizationId, String status);
}