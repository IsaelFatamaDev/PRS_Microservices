package pe.edu.vallegrande.vg_ms_payment.infrastructure.rest.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pe.edu.vallegrande.vg_ms_payment.application.services.PaymentService;
import pe.edu.vallegrande.vg_ms_payment.infrastructure.dto.common.ErrorMessage;
import pe.edu.vallegrande.vg_ms_payment.infrastructure.dto.common.ResponseDto;
import pe.edu.vallegrande.vg_ms_payment.infrastructure.dto.request.PaymentCreateRequest;
import pe.edu.vallegrande.vg_ms_payment.infrastructure.dto.response.PaymentResponse;
import pe.edu.vallegrande.vg_ms_payment.infrastructure.dto.response.EnrichedPaymentResponse;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import jakarta.validation.Valid;
import java.util.List;

/**
 * Controlador REST para endpoints administrativos de pagos
 */
@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin Payment Management", description = "Endpoints administrativos para gestión de pagos")
@RequiredArgsConstructor
@Slf4j
public class AdminPaymentRest {

    private final PaymentService paymentService;

    @GetMapping("/payments/enriched")
    @Operation(summary = "Listar todos los pagos enriquecidos", description = "Endpoint para listar los pagos con información de usuario y organización")
    public Mono<ResponseDto<List<EnrichedPaymentResponse>>> getAllEnriched() {
        return paymentService.getAllEnriched()
                .collectList()
                .map(payments -> new ResponseDto<>(true, payments))
                .onErrorResume(e -> Mono.just(
                    new ResponseDto<>(false,
                            new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                    "Error retrieving payments",
                                    e.getMessage()))));
    }

    @GetMapping("/payments/{id}/enriched")
    @Operation(summary = "Obtener pago enriquecido por ID", description = "Endpoint para ver un pago con información de usuario y organización")
    public Mono<ResponseDto<EnrichedPaymentResponse>> getEnrichedById(@PathVariable String id) {
        return paymentService.getEnrichedById(id)
                .map(payment -> new ResponseDto<>(true, payment))
                .onErrorResume(e -> Mono.just(
                    new ResponseDto<>(false,
                            new ErrorMessage(HttpStatus.NOT_FOUND.value(),
                                    "Payment not found",
                                    e.getMessage()))));
    }

    @PostMapping("/payments")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear un nuevo pago", description = "Endpoint administrativo para crear un nuevo pago")
    public Mono<ResponseDto<PaymentResponse>> create(@Valid @RequestBody PaymentCreateRequest request) {
        log.info("Received payment creation request: {}", request);
        
        return paymentService.save(request)
                .map(savedPayment -> {
                    log.info("Payment created successfully: {}", savedPayment.getPaymentId());
                    return new ResponseDto<>(true, savedPayment);
                })
                .onErrorResume(e -> {
                    log.error("Error creating payment: {}", e.getMessage(), e);
                    return Mono.just(
                        new ResponseDto<>(false,
                                new ErrorMessage(HttpStatus.BAD_REQUEST.value(),
                                        "Error creating payment: " + e.getMessage(),
                                        e.getMessage())));
                });
    }

    @PutMapping("/payments/{id}")
    @Operation(summary = "Actualizar un pago existente", description = "Endpoint administrativo para actualizar un pago existente")
    public Mono<ResponseDto<PaymentResponse>> update(@PathVariable String id, @RequestBody PaymentCreateRequest request) {
        return paymentService.update(id, request)
                .map(updatedPayment -> new ResponseDto<>(true, updatedPayment))
                .onErrorResume(e -> Mono.just(
                    new ResponseDto<>(false,
                            new ErrorMessage(HttpStatus.BAD_REQUEST.value(),
                                    "Update error",
                                    e.getMessage()))));
    }

    @DeleteMapping("/payments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar un pago por ID", description = "Endpoint administrativo para eliminar un pago específico")
    public Mono<ResponseDto<Void>> deleteById(@PathVariable String id) {
        log.info("Received request to delete payment with ID: {}", id);
        
        return paymentService.deleteById(id)
                .then(Mono.just(new ResponseDto<Void>(true, (Void) null)))
                .onErrorResume(e -> {
                    log.error("Error deleting payment with ID {}: {}", id, e.getMessage(), e);
                    return Mono.just(
                        new ResponseDto<Void>(false,
                                new ErrorMessage(HttpStatus.BAD_REQUEST.value(),
                                        "Error deleting payment: " + e.getMessage(),
                                        e.getMessage())));
                });
    }

    @DeleteMapping("/payments")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar todos los pagos", description = "Endpoint administrativo para eliminar físicamente todos los pagos y sus detalles")
    public Mono<ResponseDto<Void>> deleteAll() {
        log.info("Received request to delete all payments");
        
        return paymentService.deleteAll()
                .then(Mono.just(new ResponseDto<Void>(true, (Void) null)))
                .doOnSuccess(v -> log.info("All payments deleted successfully"))
                .onErrorResume(e -> {
                    log.error("Error deleting all payments: {}", e.getMessage(), e);
                    return Mono.just(
                        new ResponseDto<Void>(false,
                                new ErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                        "Error deleting all payments: " + e.getMessage(),
                                        e.getMessage())));
                });
    }
}