package pe.edu.vallegrande.vg_ms_payment.infrastructure.rest.client;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pe.edu.vallegrande.vg_ms_payment.application.services.PaymentService;
import pe.edu.vallegrande.vg_ms_payment.infrastructure.dto.common.ResponseDto;
import pe.edu.vallegrande.vg_ms_payment.infrastructure.dto.response.PaymentResponse;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Controlador REST para endpoints públicos/cliente de pagos
 */
@RestController
@RequestMapping("/api/client")
@Tag(name = "Client Payment Management", description = "Endpoints públicos para gestión de pagos")
@RequiredArgsConstructor
@Slf4j
public class PaymentRest {

    private final PaymentService paymentService;

    @GetMapping("/payments")
    @Operation(summary = "Listar pagos", description = "Endpoint público para listar pagos")
    public Mono<ResponseDto<List<PaymentResponse>>> getAllPayments() {
        return paymentService.getAll()
                .collectList()
                .map(payments -> new ResponseDto<>(true, payments));
    }

    @GetMapping("/payments/{id}")
    @Operation(summary = "Obtener pago por ID", description = "Endpoint público para obtener un pago específico")
    public Mono<ResponseDto<PaymentResponse>> getPaymentById(@PathVariable String id) {
        return paymentService.getById(id)
                .map(payment -> new ResponseDto<>(true, payment));
    }
}