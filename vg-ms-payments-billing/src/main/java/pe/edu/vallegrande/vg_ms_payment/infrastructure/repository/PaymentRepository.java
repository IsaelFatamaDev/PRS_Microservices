package pe.edu.vallegrande.vg_ms_payment.infrastructure.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import pe.edu.vallegrande.vg_ms_payment.infrastructure.entity.PaymentEntity;

/**
 * Repositorio PostgreSQL para la entidad Payment
 */
public interface PaymentRepository extends ReactiveCrudRepository<PaymentEntity, String> {

}
