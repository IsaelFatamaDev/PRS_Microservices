package pe.edu.vallegrande.ms_infraestructura.infrastructure.rest.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.ms_infraestructura.application.services.IWaterBoxService;
import pe.edu.vallegrande.ms_infraestructura.application.services.IWaterBoxAssignmentService;
import pe.edu.vallegrande.ms_infraestructura.application.services.IWaterBoxTransferService;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.dto.response.WaterBoxResponse;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.dto.response.WaterBoxAssignmentResponse;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.dto.response.WaterBoxTransferResponse;

import java.util.List;

@RestController
@RequestMapping("/api/client")
@RequiredArgsConstructor
public class ClientRest {

    private final IWaterBoxService waterBoxService;
    private final IWaterBoxAssignmentService waterBoxAssignmentService;
    private final IWaterBoxTransferService waterBoxTransferService;

    // ===============================
    // GESTIÓN DE WATER BOXES
    // ===============================

    @GetMapping("/water-boxes/active")
    public ResponseEntity<List<WaterBoxResponse>> getAllActiveWaterBoxes() {
        return ResponseEntity.ok(waterBoxService.getAllActive().collectList().block());
    }

    @GetMapping("/water-boxes/inactive")
    public ResponseEntity<List<WaterBoxResponse>> getAllInactiveWaterBoxes() {
        return ResponseEntity.ok(waterBoxService.getAllInactive().collectList().block());
    }

    @GetMapping("/water-boxes/{id}")
    public ResponseEntity<WaterBoxResponse> getWaterBoxById(@PathVariable Long id) {
        return ResponseEntity.ok(waterBoxService.getById(id).block());
    }

    // ===============================
    // GESTIÓN DE WATER BOX ASSIGNMENTS
    // ===============================

    @GetMapping("/water-box-assignments/active")
    public ResponseEntity<List<WaterBoxAssignmentResponse>> getAllActiveAssignments() {
        return ResponseEntity.ok(waterBoxAssignmentService.getAllActive().collectList().block());
    }

    @GetMapping("/water-box-assignments/inactive")
    public ResponseEntity<List<WaterBoxAssignmentResponse>> getAllInactiveAssignments() {
        return ResponseEntity.ok(waterBoxAssignmentService.getAllInactive().collectList().block());
    }

    @GetMapping("/water-box-assignments/{id}")
    public ResponseEntity<WaterBoxAssignmentResponse> getAssignmentById(@PathVariable Long id) {
        return ResponseEntity.ok(waterBoxAssignmentService.getById(id).block());
    }

    // ===============================
    // GESTIÓN DE WATER BOX TRANSFERS
    // ===============================

    @GetMapping("/water-box-transfers")
    public ResponseEntity<List<WaterBoxTransferResponse>> getAllTransfers() {
        return ResponseEntity.ok(waterBoxTransferService.getAll().collectList().block());
    }

    @GetMapping("/water-box-transfers/{id}")
    public ResponseEntity<WaterBoxTransferResponse> getTransferById(@PathVariable Long id) {
        return ResponseEntity.ok(waterBoxTransferService.getById(id).block());
    }
}
