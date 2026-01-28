package pe.edu.vallegrande.vg_ms_payment.infrastructure.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import pe.edu.vallegrande.vg_ms_payment.infrastructure.entity.PaymentDetailEntity;
import reactor.core.publisher.Flux;

/**
 * Repositorio PostgreSQL para la entidad PaymentDetail
 */
public interface PaymentDetailRepository extends ReactiveCrudRepository<PaymentDetailEntity, String> {
    
    /**
     * Busca todos los detalles de un pago específico
     */
    Flux<PaymentDetailEntity> findByPaymentId(String paymentId);
    
    /**
     * Elimina todos los detalles de un pago específico
     */
    Flux<Void> deleteByPaymentId(String paymentId);
}
