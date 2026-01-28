package pe.edu.vallegrande.ms_infraestructura.infrastructure.rest.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.ms_infraestructura.application.services.IWaterBoxService;
import pe.edu.vallegrande.ms_infraestructura.application.services.IWaterBoxAssignmentService;
import pe.edu.vallegrande.ms_infraestructura.application.services.IWaterBoxTransferService;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.dto.request.WaterBoxRequest;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.dto.request.WaterBoxAssignmentRequest;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.dto.request.WaterBoxTransferRequest;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.dto.response.WaterBoxResponse;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.dto.response.WaterBoxAssignmentResponse;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.dto.response.WaterBoxTransferResponse;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminRest {

    private final IWaterBoxService waterBoxService;
    private final IWaterBoxAssignmentService waterBoxAssignmentService;
    private final IWaterBoxTransferService waterBoxTransferService;

    // ===============================
    // GESTIÓN DE WATER BOXES
    // ===============================
    @GetMapping("/water-boxes")
    public ResponseEntity<List<WaterBoxResponse>> getAllWaterBoxes() {
        List<WaterBoxResponse> allBoxes = new java.util.ArrayList<>();
        allBoxes.addAll(waterBoxService.getAllActive().collectList().block());
        allBoxes.addAll(waterBoxService.getAllInactive().collectList().block());
        return ResponseEntity.ok(allBoxes);
    }

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

    @PostMapping("/water-boxes")
    public ResponseEntity<WaterBoxResponse> createWaterBox(@Valid @RequestBody WaterBoxRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(waterBoxService.save(request).block());
    }

    @PutMapping("/water-boxes/{id}")
    public ResponseEntity<WaterBoxResponse> updateWaterBox(@PathVariable Long id, @Valid @RequestBody WaterBoxRequest request) {
        return ResponseEntity.ok(waterBoxService.update(id, request).block());
    }

    @DeleteMapping("/water-boxes/{id}")
    public ResponseEntity<Void> deleteWaterBox(@PathVariable Long id) {
        waterBoxService.delete(id).block();
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/water-boxes/{id}/restore")
    public ResponseEntity<WaterBoxResponse> restoreWaterBox(@PathVariable Long id) {
        return ResponseEntity.ok(waterBoxService.restore(id).block());
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

    @PostMapping("/water-box-assignments")
    public ResponseEntity<WaterBoxAssignmentResponse> createAssignment(@Valid @RequestBody WaterBoxAssignmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(waterBoxAssignmentService.save(request).block());
    }

    @PutMapping("/water-box-assignments/{id}")
    public ResponseEntity<WaterBoxAssignmentResponse> updateAssignment(@PathVariable Long id, @Valid @RequestBody WaterBoxAssignmentRequest request) {
        return ResponseEntity.ok(waterBoxAssignmentService.update(id, request).block());
    }

    @DeleteMapping("/water-box-assignments/{id}")
    public ResponseEntity<Void> deleteAssignment(@PathVariable Long id) {
        waterBoxAssignmentService.delete(id).block();
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/water-box-assignments/{id}/restore")
    public ResponseEntity<WaterBoxAssignmentResponse> restoreAssignment(@PathVariable Long id) {
        return ResponseEntity.ok(waterBoxAssignmentService.restore(id).block());
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

    @PostMapping("/water-box-transfers")
    public ResponseEntity<WaterBoxTransferResponse> createTransfer(@Valid @RequestBody WaterBoxTransferRequest request) {
        System.out.println("=== TRANSFER REQUEST RECEIVED ===");
        System.out.println("WaterBoxId: " + request.getWaterBoxId());
        System.out.println("OldAssignmentId: " + request.getOldAssignmentId());
        System.out.println("NewAssignmentId: " + request.getNewAssignmentId());
        System.out.println("TransferReason: " + request.getTransferReason());
        System.out.println("Documents: " + request.getDocuments());
        System.out.println("================================");
        return ResponseEntity.status(HttpStatus.CREATED).body(waterBoxTransferService.save(request).block());
    }
}
