package pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.rest.admin;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.vgmsinventorypurchases.application.services.MaterialsService;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.enums.ProductStatus;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.common.ResponseDto;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.request.ProductRequest;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.response.ProductResponse;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/admin/materials")
@Validated
public class MaterialRest {

     private final MaterialsService materialsService;

     public MaterialRest(MaterialsService materialsService) {
          this.materialsService = materialsService;
     }

     @GetMapping
     @PreAuthorize("hasAuthority('ADMIN')")
     public Mono<ResponseEntity<ResponseDto<List<ProductResponse>>>> findAll(@RequestParam String organizationId) {
          return materialsService.findAll(organizationId)
                    .collectList()
                    .map(ResponseDto::success)
                    .map(ResponseEntity::ok);
     }

     @GetMapping("/{id}")
     @PreAuthorize("hasAuthority('ADMIN')")
     public Mono<ResponseEntity<ResponseDto<ProductResponse>>> findById(@PathVariable String id) {
          return materialsService.findById(id)
                    .map(ResponseDto::success)
                    .map(ResponseEntity::ok);
     }

     @PostMapping
     @PreAuthorize("hasAuthority('ADMIN')")
     public Mono<ResponseEntity<ResponseDto<ProductResponse>>> create(@Valid @RequestBody ProductRequest request) {
          return materialsService.create(request)
                    .map(ResponseDto::success)
                    .map(dto -> ResponseEntity.status(HttpStatus.CREATED).body(dto));
     }

     @PutMapping("/{id}")
     @PreAuthorize("hasAuthority('ADMIN')")
     public Mono<ResponseEntity<ResponseDto<ProductResponse>>> update(
               @PathVariable String id,
               @Valid @RequestBody ProductRequest request) {
          return materialsService.update(id, request)
                    .map(ResponseDto::success)
                    .map(ResponseEntity::ok);
     }

     @DeleteMapping("/{id}")
     @PreAuthorize("hasAuthority('ADMIN')")
     public Mono<ResponseEntity<ResponseDto<String>>> delete(@PathVariable String id) {
          return materialsService.delete(id)
                    .then(Mono.just(ResponseDto.success("Material eliminado correctamente")))
                    .map(ResponseEntity::ok);
     }

     @GetMapping("/status/{status}")
     @PreAuthorize("hasAuthority('ADMIN')")
     public Mono<ResponseEntity<ResponseDto<List<ProductResponse>>>> findAllByStatus(
               @RequestParam String organizationId,
               @PathVariable ProductStatus status) {
          return materialsService.findAllByStatus(organizationId, status)
                    .collectList()
                    .map(ResponseDto::success)
                    .map(ResponseEntity::ok);
     }

     @GetMapping("/category/{categoryId}")
     @PreAuthorize("hasAuthority('ADMIN')")
     public Mono<ResponseEntity<ResponseDto<List<ProductResponse>>>> findByCategoryId(
               @RequestParam String organizationId,
               @PathVariable String categoryId) {
          return materialsService.findByCategoryId(categoryId, organizationId)
                    .collectList()
                    .map(ResponseDto::success)
                    .map(ResponseEntity::ok);
     }

     @GetMapping("/category/{categoryId}/status/{status}")
     @PreAuthorize("hasAuthority('ADMIN')")
     public Mono<ResponseEntity<ResponseDto<List<ProductResponse>>>> findByCategoryIdAndStatus(
               @RequestParam String organizationId,
               @PathVariable String categoryId,
               @PathVariable ProductStatus status) {
          return materialsService.findByCategoryIdAndStatus(categoryId, organizationId, status)
                    .collectList()
                    .map(ResponseDto::success)
                    .map(ResponseEntity::ok);
     }

     @PatchMapping("/{id}/restore")
     @PreAuthorize("hasAuthority('ADMIN')")
     public Mono<ResponseEntity<ResponseDto<ProductResponse>>> restore(@PathVariable String id) {
          return materialsService.restore(id)
                    .map(ResponseDto::success)
                    .map(ResponseEntity::ok);
     }
}
