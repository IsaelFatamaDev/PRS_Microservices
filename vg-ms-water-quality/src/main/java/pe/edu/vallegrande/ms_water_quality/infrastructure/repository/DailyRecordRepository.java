package pe.edu.vallegrande.ms_water_quality.infrastructure.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.ms_water_quality.infrastructure.document.DailyRecordDocument;
import reactor.core.publisher.Flux;

/**
 * Repository: DailyRecord
 * Repositorio reactivo para persistencia de registros diarios en MongoDB.
 * Trabaja con DailyRecordDocument (capa de infraestructura).
 */
@Repository
public interface DailyRecordRepository extends ReactiveMongoRepository<DailyRecordDocument, String> {

    Flux<DailyRecordDocument> findAllByOrganizationId(String organizationId);
    Flux<DailyRecordDocument> findByRecordTypeOrderByRecordCodeDesc(String recordType);
    Flux<DailyRecordDocument> findByOrganizationIdAndRecordType(String organizationId, String recordType);
}