package pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.repository;

import pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.document.ComplaintCategoryDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComplaintCategoryRepository extends ReactiveMongoRepository<ComplaintCategoryDocument, String> {
}