package pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.rest.admin;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.vgmsinventorypurchases.application.services.PurchaseService;
import pe.edu.vallegrande.vgmsinventorypurchases.application.services.PurchaseUserIntegrationService;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.enums.PurchaseStatus;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.common.ResponseDto;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.request.PurchaseRequest;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.request.PurchaseStatusUpdateRequest;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.response.PurchaseResponse;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.response.PurchaseWithUserDetailsResponse;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/admin/purchases")
@Validated
public class PurchaseRest {

     private final PurchaseService purchaseService;
     private final PurchaseUserIntegrationService purchaseUserIntegrationService;

     public PurchaseRest(PurchaseService purchaseService, PurchaseUserIntegrationService purchaseUserIntegrationService) {
          this.purchaseService = purchaseService;
          this.purchaseUserIntegrationService = purchaseUserIntegrationService;
     }

     @GetMapping
     @PreAuthorize("hasAuthority('ADMIN')")
     public Mono<ResponseEntity<ResponseDto<List<PurchaseWithUserDetailsResponse>>>> findAll() {
          return purchaseUserIntegrationService.findAllWithUserDetails()
                    .collectList()
                    .map(ResponseDto::success)
                    .map(ResponseEntity::ok);
     }

     @GetMapping("/organization/{organizationId}")
     @PreAuthorize("hasAuthority('ADMIN')")
     public Mono<ResponseEntity<ResponseDto<List<PurchaseWithUserDetailsResponse>>>> findByOrganizationId(
               @PathVariable String organizationId) {
          return purchaseUserIntegrationService.findByOrganizationIdWithUserDetails(organizationId)
                    .collectList()
                    .map(ResponseDto::success)
                    .map(ResponseEntity::ok);
     }

     @GetMapping("/{id}")
     @PreAuthorize("hasAuthority('ADMIN')")
     public Mono<ResponseEntity<ResponseDto<PurchaseWithUserDetailsResponse>>> findById(@PathVariable String id) {
          return purchaseUserIntegrationService.findByIdWithUserDetails(id)
                    .map(ResponseDto::success)
                    .map(ResponseEntity::ok);
     }

     @PostMapping
     @PreAuthorize("hasAuthority('ADMIN')")
     public Mono<ResponseEntity<ResponseDto<PurchaseResponse>>> create(
               @Valid @RequestBody PurchaseRequest request) {
          return purchaseService.create(request)
                    .map(ResponseDto::success)
                    .map(dto -> ResponseEntity.status(HttpStatus.CREATED).body(dto));
     }

     @PutMapping("/{id}")
     @PreAuthorize("hasAuthority('ADMIN')")
     public Mono<ResponseEntity<ResponseDto<PurchaseResponse>>> update(
               @PathVariable String id,
               @Valid @RequestBody PurchaseRequest request) {
          return purchaseService.update(id, request)
                    .map(ResponseDto::success)
                    .map(ResponseEntity::ok);
     }

     @DeleteMapping("/{id}")
     @PreAuthorize("hasAuthority('ADMIN')")
     public Mono<ResponseEntity<ResponseDto<String>>> delete(@PathVariable String id) {
          return purchaseService.delete(id)
                    .then(Mono.just(ResponseDto.success("Compra eliminada correctamente")))
                    .map(ResponseEntity::ok);
     }

     @PostMapping("/{id}/restore")
     @PreAuthorize("hasAuthority('ADMIN')")
     public Mono<ResponseEntity<ResponseDto<PurchaseResponse>>> restore(@PathVariable String id) {
          return purchaseService.restore(id)
                    .map(ResponseDto::success)
                    .map(ResponseEntity::ok);
     }

     @GetMapping("/status/{status}")
     @PreAuthorize("hasAuthority('ADMIN')")
     public Mono<ResponseEntity<ResponseDto<List<PurchaseWithUserDetailsResponse>>>> findByStatus(
               @RequestParam String organizationId,
               @PathVariable PurchaseStatus status) {
          return purchaseUserIntegrationService.findByStatusWithUserDetails(organizationId, status)
                    .collectList()
                    .map(ResponseDto::success)
                    .map(ResponseEntity::ok);
     }

     @PatchMapping("/{id}/status")
     @PreAuthorize("hasAuthority('ADMIN')")
     public Mono<ResponseEntity<ResponseDto<PurchaseResponse>>> updateStatus(
               @PathVariable String id,
               @Valid @RequestBody PurchaseStatusUpdateRequest request) {
          return purchaseService.updateStatus(id, request.getStatus())
                    .map(ResponseDto::success)
                    .map(ResponseEntity::ok);
     }
}
