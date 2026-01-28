package pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.rest.admin;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.request.CreateInventoryMovementDto;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.common.InventoryMovementDto;
import pe.edu.vallegrande.vgmsinventorypurchases.application.services.AuthenticationService;
import pe.edu.vallegrande.vgmsinventorypurchases.application.services.InventoryMovementService;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.common.ResponseDto;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.request.InventoryMovementConsumptionRequest;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.response.EnrichedInventoryMovementResponse;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.response.InventoryMovementConsumptionResponse;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.service.InventoryMovementConsumptionService;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.service.InventoryMovementEnrichmentService;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/admin/inventory-movements")
@Slf4j
@Validated
public class InventoryMovementRest {

     private final InventoryMovementService inventoryMovementService;
     private final InventoryMovementConsumptionService consumptionService;
     private final AuthenticationService authenticationService;
     private final InventoryMovementEnrichmentService enrichmentService;

     public InventoryMovementRest(InventoryMovementService inventoryMovementService,
               InventoryMovementConsumptionService consumptionService,
               AuthenticationService authenticationService,
               InventoryMovementEnrichmentService enrichmentService) {
          this.inventoryMovementService = inventoryMovementService;
          this.consumptionService = consumptionService;
          this.authenticationService = authenticationService;
          this.enrichmentService = enrichmentService;
     }

     /**
      * Obtener todos los movimientos de una organización
      */
     @GetMapping("/organization/{organizationId}")
     @PreAuthorize("hasAuthority('ADMIN')")
     public Mono<ResponseEntity<ResponseDto<List<InventoryMovementDto>>>> getMovementsByOrganization(
               @PathVariable String organizationId,
               @RequestParam(required = false, defaultValue = "0") Integer page,
               @RequestParam(required = false, defaultValue = "20") Integer size,
               Authentication authentication) {
          return authenticationService.getCurrentUserId(authentication)
                    .doOnNext(userId -> log.info("Usuario ADMIN {} consultando movimientos de organización: {}", userId,
                              organizationId))
                    .flatMapMany(
                              userId -> inventoryMovementService.getMovementsByOrganization(organizationId, page, size))
                    .collectList()
                    .map(ResponseDto::success)
                    .map(ResponseEntity::ok);
     }

     /**
      * 🆕 ENDPOINT ENRIQUECIDO: Obtener movimientos con información completa de
      * productos y usuarios
      */
     @GetMapping("/organization/{organizationId}/enriched")
     @PreAuthorize("hasAuthority('ADMIN')")
     public Mono<ResponseEntity<ResponseDto<List<EnrichedInventoryMovementResponse>>>> getEnrichedMovementsByOrganization(
               @PathVariable String organizationId,
               Authentication authentication) {

          return authenticationService.getCurrentUserId(authentication)
                    .doOnNext(userId -> log.info(
                              "🔍 Usuario ADMIN {} consultando movimientos ENRIQUECIDOS de organización: {}",
                              userId, organizationId))
                    .flatMapMany(userId -> inventoryMovementService
                              .getAllMovementsByOrganization(organizationId)
                              .doOnNext(movement -> log.debug("📦 Procesando movimiento: {}", movement.getMovementId()))
                              .flatMap(movement -> enrichmentService.enrichMovement(movement)
                                        .doOnSuccess(enriched -> log.debug(
                                                  "✅ Movimiento {} enriquecido con producto: {} y usuario: {}",
                                                  enriched.getMovementId(),
                                                  enriched.getProductName(),
                                                  enriched.getPerformedBy()))
                                        .onErrorResume(error -> {
                                             log.error("❌ Error enriqueciendo movimiento {}: {}",
                                                       movement.getMovementId(), error.getMessage());
                                             // En caso de error, continuar con el siguiente movimiento
                                             return Mono.empty();
                                        })))
                    .collectList()
                    .map(ResponseDto::success)
                    .map(ResponseEntity::ok)
                    .doOnSuccess(
                              response -> log.info(
                                        "✅ Movimientos enriquecidos obtenidos exitosamente para organización: {}",
                                        organizationId));
     }

     /**
      * Obtener movimientos por producto
      */
     @GetMapping("/organization/{organizationId}/product/{productId}")
     @PreAuthorize("hasAuthority('ADMIN')")
     public Mono<ResponseEntity<ResponseDto<List<InventoryMovementDto>>>> getMovementsByProduct(
               @PathVariable String organizationId,
               @PathVariable String productId) {
          log.info("Getting inventory movements for product: {} in organization: {}", productId, organizationId);
          return inventoryMovementService.getMovementsByProduct(organizationId, productId)
                    .collectList()
                    .map(ResponseDto::success)
                    .map(ResponseEntity::ok);
     }

     /**
      * Obtener movimiento por ID
      */
     @GetMapping("/{movementId}")
     @PreAuthorize("hasAuthority('ADMIN')")
     public Mono<ResponseEntity<ResponseDto<InventoryMovementDto>>> getMovementById(@PathVariable String movementId) {
          log.info("Getting inventory movement by ID: {}", movementId);
          return inventoryMovementService.getMovementById(movementId)
                    .map(ResponseDto::success)
                    .map(ResponseEntity::ok);
     }

     /**
      * Crear un nuevo movimiento
      */
     @PostMapping
     @PreAuthorize("hasAuthority('ADMIN')")
     public Mono<ResponseEntity<ResponseDto<InventoryMovementDto>>> createMovement(
               @Valid @RequestBody CreateInventoryMovementDto createDto) {
          log.info("Creating new inventory movement for product: {}", createDto.getProductId());
          return inventoryMovementService.createMovement(createDto)
                    .map(ResponseDto::success)
                    .map(dto -> ResponseEntity.status(HttpStatus.CREATED).body(dto));
     }

     /**
      * Obtener stock actual de un producto
      */
     @GetMapping("/stock/{organizationId}/{productId}")
     @PreAuthorize("hasAuthority('ADMIN')")
     public Mono<ResponseEntity<ResponseDto<Integer>>> getCurrentStock(
               @PathVariable String organizationId,
               @PathVariable String productId,
               Authentication authentication) {
          return authenticationService.getCurrentUserId(authentication)
                    .doOnNext(userId -> log.info(
                              "Usuario ADMIN {} consultando stock de producto: {} en organización: {}", userId,
                              productId, organizationId))
                    .flatMap(userId -> inventoryMovementService.getCurrentStock(organizationId, productId))
                    .map(ResponseDto::success)
                    .map(ResponseEntity::ok);
     }

     /**
      * Contar movimientos por organización
      */
     @GetMapping("/count/{organizationId}")
     @PreAuthorize("hasAuthority('ADMIN')")
     public Mono<ResponseEntity<ResponseDto<Long>>> countMovements(@PathVariable String organizationId) {
          log.info("Counting movements for organization: {}", organizationId);
          return inventoryMovementService.countMovementsByOrganization(organizationId)
                    .map(ResponseDto::success)
                    .map(ResponseEntity::ok);
     }

     /**
      * 🆕 ENDPOINT PARA REGISTRAR CONSUMO DE PRODUCTOS
      */
     @PostMapping("/consumption")
     @PreAuthorize("hasAuthority('ADMIN')")
     public Mono<ResponseEntity<ResponseDto<InventoryMovementConsumptionResponse>>> registerProductConsumption(
               @Valid @RequestBody InventoryMovementConsumptionRequest request,
               Authentication authentication) {

          return authenticationService.getCurrentUserId(authentication)
                    .doOnNext(userId -> log.info(
                              "🍽️ Usuario ADMIN {} registrando consumo de producto: {} en organización: {}",
                              userId, request.getProductId(), request.getOrganizationId()))
                    .flatMap(userId -> {
                         // Validar consistencia del stock
                         if (!consumptionService.validateStockConsistency(request)) {
                              log.warn("⚠️  Stock consistency validation failed for product: {}",
                                        request.getProductId());
                              return Mono.just(
                                        ResponseDto.<InventoryMovementConsumptionResponse>error(
                                                  "Error de consistencia: El cálculo de stock no coincide"))
                                        .map(dto -> ResponseEntity.status(HttpStatus.BAD_REQUEST).body(dto));
                         }

                         return consumptionService.registerProductConsumption(request)
                                   .map(this::mapToConsumptionResponse)
                                   .map(ResponseDto::success)
                                   .map(dto -> ResponseEntity.status(HttpStatus.CREATED).body(dto))
                                   .onErrorResume(error -> {
                                        log.error("❌ Error registering product consumption", error);
                                        return Mono.just(ResponseDto.<InventoryMovementConsumptionResponse>error(
                                                  "Error al registrar el movimiento: " + error.getMessage()))
                                                  .map(dto -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                                            .body(dto));
                                   });
                    });
     }

     /**
      * Mapea InventoryMovement a InventoryMovementConsumptionResponse
      * Note: consumptionService returns InventoryMovement (Domain).
      * I need to ensure consumptionService is compatible or update this map.
      * For now, I'll assume consumptionService returns Domain Model (which I haven't
      * changed yet).
      */
     private InventoryMovementConsumptionResponse mapToConsumptionResponse(
               pe.edu.vallegrande.vgmsinventorypurchases.domain.models.InventoryMovement movement) {
          java.math.BigDecimal totalValue = movement.getUnitCost()
                    .multiply(java.math.BigDecimal.valueOf(movement.getQuantity()));

          return InventoryMovementConsumptionResponse.builder()
                    .movementId(movement.getMovementId())
                    .organizationId(movement.getOrganizationId())
                    .productId(movement.getProductId())
                    .movementType(movement.getMovementType())
                    .movementReason(movement.getMovementReason())
                    .quantity(movement.getQuantity())
                    .unitCost(movement.getUnitCost())
                    .totalValue(totalValue)
                    .previousStock(movement.getPreviousStock())
                    .newStock(movement.getNewStock())
                    .movementDate(movement.getMovementDate())
                    .userId(movement.getUserId())
                    .observations(movement.getObservations())
                    .referenceDocument(movement.getReferenceDocument())
                    .referenceId(movement.getReferenceId())
                    .createdAt(movement.getCreatedAt())
                    .success(true)
                    .message("Movimiento registrado correctamente")
                    .build();
     }
}
