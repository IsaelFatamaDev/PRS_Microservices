package pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.rest.admin;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.vgmsinventorypurchases.application.services.ProductCategoryService;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.enums.GeneralStatus;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.common.ResponseDto;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.request.ProductCategoryRequest;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.response.ProductCategoryResponse;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/admin/product-categories")
@Validated
public class ProductCategoryRest {

     private final ProductCategoryService productCategoryService;

     public ProductCategoryRest(ProductCategoryService productCategoryService) {
          this.productCategoryService = productCategoryService;
     }

     @GetMapping
     @PreAuthorize("hasAuthority('ADMIN')")
     public Mono<ResponseEntity<ResponseDto<List<ProductCategoryResponse>>>> findAll(@RequestParam String organizationId) {
          return productCategoryService.findAll(organizationId)
                    .collectList()
                    .map(ResponseDto::success)
                    .map(ResponseEntity::ok);
     }

     @GetMapping("/{id}")
     @PreAuthorize("hasAuthority('ADMIN')")
     public Mono<ResponseEntity<ResponseDto<ProductCategoryResponse>>> findById(@PathVariable String id) {
          return productCategoryService.findById(id)
                    .map(ResponseDto::success)
                    .map(ResponseEntity::ok);
     }

     @PostMapping
     @PreAuthorize("hasAuthority('ADMIN')")
     public Mono<ResponseEntity<ResponseDto<ProductCategoryResponse>>> create(@Valid @RequestBody ProductCategoryRequest request) {
          return productCategoryService.create(request)
                    .map(ResponseDto::success)
                    .map(dto -> ResponseEntity.status(HttpStatus.CREATED).body(dto));
     }

     @PutMapping("/{id}")
     @PreAuthorize("hasAuthority('ADMIN')")
     public Mono<ResponseEntity<ResponseDto<ProductCategoryResponse>>> update(
               @PathVariable String id,
               @Valid @RequestBody ProductCategoryRequest request) {
          return productCategoryService.update(id, request)
                    .map(ResponseDto::success)
                    .map(ResponseEntity::ok);
     }

     @DeleteMapping("/{id}")
     @PreAuthorize("hasAuthority('ADMIN')")
     public Mono<ResponseEntity<ResponseDto<String>>> delete(@PathVariable String id) {
          return productCategoryService.delete(id)
                    .then(Mono.just(ResponseDto.success("Categoría eliminada correctamente")))
                    .map(ResponseEntity::ok);
     }

     @GetMapping("/status/{status}")
     @PreAuthorize("hasAuthority('ADMIN')")
     public Mono<ResponseEntity<ResponseDto<List<ProductCategoryResponse>>>> findAllByStatus(
               @RequestParam String organizationId,
               @PathVariable GeneralStatus status) {
          return productCategoryService.findAllByStatus(organizationId, status)
                    .collectList()
                    .map(ResponseDto::success)
                    .map(ResponseEntity::ok);
     }

     @PatchMapping("/{id}/restore")
     @PreAuthorize("hasAuthority('ADMIN')")
     public Mono<ResponseEntity<ResponseDto<ProductCategoryResponse>>> restore(@PathVariable String id) {
          return productCategoryService.restore(id)
                    .map(ResponseDto::success)
                    .map(ResponseEntity::ok);
     }
}
