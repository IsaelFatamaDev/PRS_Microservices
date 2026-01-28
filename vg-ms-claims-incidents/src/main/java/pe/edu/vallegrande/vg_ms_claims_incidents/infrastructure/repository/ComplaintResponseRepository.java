package pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.repository;

import pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.document.ComplaintResponseDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComplaintResponseRepository extends ReactiveMongoRepository<ComplaintResponseDocument, String> {
}