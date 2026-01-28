package pe.edu.vallegrande.ms_infraestructura.infrastructure.rest.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.ms_infraestructura.application.services.IWaterBoxAssignmentService;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.dto.response.WaterBoxAssignmentResponse;

/**
 * REST Controller INTERNO para comunicación entre microservicios - SIN
 * autenticación JWT (solo para uso interno) - Endpoints simples para otros
 * microservicios del ecosistema JASS
 */
@RestController
@RequestMapping("/internal")
@RequiredArgsConstructor
@Component
public class InternalRest {

    private final IWaterBoxAssignmentService waterBoxAssignmentService;

    /**
     * Endpoint de prueba para verificar que el controlador interno funciona
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Internal controller is working - " + System.currentTimeMillis());
    }

    /**
     * Endpoint de prueba simple sin dependencias
     */
    @GetMapping("/test")
    public String simpleTest() {
        return "Controller loaded successfully";
    }

    // ===============================
    // CONSULTAS ADMINISTRATIVAS ESPECIALES
    // ===============================
    /**
     * Obtiene la caja de agua actualmente asignada a un usuario específico Solo
     * para uso interno entre microservicios
     */
    @GetMapping("/users/{userId}/water-box-assignment")
    public ResponseEntity<WaterBoxAssignmentResponse> getActiveWaterBoxAssignmentByUserId(@PathVariable String userId) {
        return ResponseEntity.ok(waterBoxAssignmentService.getActiveAssignmentByUserId(userId).block());
    }
}
