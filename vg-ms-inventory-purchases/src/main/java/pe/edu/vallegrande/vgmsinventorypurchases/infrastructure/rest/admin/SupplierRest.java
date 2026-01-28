package pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.rest.admin;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.vgmsinventorypurchases.application.services.SupplierService;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.enums.SupplierStatus;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.common.ResponseDto;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.request.SupplierRequest;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.response.SupplierResponse;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/admin/suppliers")
@Validated
public class SupplierRest {

     private final SupplierService supplierService;

     public SupplierRest(SupplierService supplierService) {
          this.supplierService = supplierService;
     }

     @GetMapping
     @PreAuthorize("hasAuthority('ADMIN')")
     public Mono<ResponseEntity<ResponseDto<List<SupplierResponse>>>> findAll(@RequestParam String organizationId) {
          return supplierService.findAll(organizationId)
                    .collectList()
                    .map(ResponseDto::success)
                    .map(ResponseEntity::ok);
     }

     @GetMapping("/{id}")
     @PreAuthorize("hasAuthority('ADMIN')")
     public Mono<ResponseEntity<ResponseDto<SupplierResponse>>> findById(@PathVariable String id) {
          return supplierService.findById(id)
                    .map(ResponseDto::success)
                    .map(ResponseEntity::ok);
     }

     @PostMapping
     @PreAuthorize("hasAuthority('ADMIN')")
     public Mono<ResponseEntity<ResponseDto<SupplierResponse>>> create(@Valid @RequestBody SupplierRequest request) {
          return supplierService.create(request)
                    .map(ResponseDto::success)
                    .map(dto -> ResponseEntity.status(HttpStatus.CREATED).body(dto));
     }

     @PutMapping("/{id}")
     @PreAuthorize("hasAuthority('ADMIN')")
     public Mono<ResponseEntity<ResponseDto<SupplierResponse>>> update(
               @PathVariable String id,
               @Valid @RequestBody SupplierRequest request) {
          return supplierService.update(id, request)
                    .map(ResponseDto::success)
                    .map(ResponseEntity::ok);
     }

     @DeleteMapping("/{id}")
     @PreAuthorize("hasAuthority('ADMIN')")
     public Mono<ResponseEntity<ResponseDto<String>>> delete(@PathVariable String id) {
          return supplierService.delete(id)
                    .then(Mono.just(ResponseDto.success("Proveedor eliminado correctamente")))
                    .map(ResponseEntity::ok);
     }

     @GetMapping("/status/{status}")
     @PreAuthorize("hasAuthority('ADMIN')")
     public Mono<ResponseEntity<ResponseDto<List<SupplierResponse>>>> findAllByStatus(
               @RequestParam String organizationId,
               @PathVariable SupplierStatus status) {
          return supplierService.findAllByStatus(organizationId, status)
                    .collectList()
                    .map(ResponseDto::success)
                    .map(ResponseEntity::ok);
     }

     @PatchMapping("/{id}/restore")
     @PreAuthorize("hasAuthority('ADMIN')")
     public Mono<ResponseEntity<ResponseDto<SupplierResponse>>> restore(@PathVariable String id) {
          return supplierService.restore(id)
                    .map(ResponseDto::success)
                    .map(ResponseEntity::ok);
     }
}
