package pe.edu.vallegrande.vg_ms_payment.application.services;

import pe.edu.vallegrande.vg_ms_payment.infrastructure.dto.request.PaymentCreateRequest;
import pe.edu.vallegrande.vg_ms_payment.infrastructure.dto.response.PaymentResponse;
import pe.edu.vallegrande.vg_ms_payment.infrastructure.dto.response.EnrichedPaymentResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PaymentService {
    Flux<PaymentResponse> getAll();
    Mono<PaymentResponse> save(PaymentCreateRequest paymentRequest);
    Mono<PaymentResponse> getById(String paymentId);
    Mono<PaymentResponse> update(String paymentId, PaymentCreateRequest paymentRequest);
    Mono<Void> deleteById(String paymentId);
    Mono<Void> deleteAll(); // Bulk physical delete method
    
    // Nuevos métodos para obtener pagos enriquecidos
    Mono<EnrichedPaymentResponse> getEnrichedById(String paymentId);
    Flux<EnrichedPaymentResponse> getAllEnriched();
}