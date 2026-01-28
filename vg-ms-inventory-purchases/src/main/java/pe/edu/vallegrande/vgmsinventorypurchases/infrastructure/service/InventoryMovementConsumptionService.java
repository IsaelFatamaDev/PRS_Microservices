package pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.common.InventoryMovementDto;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.mapper.InventoryMovementMapper;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.enums.MovementType;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.models.InventoryMovement;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.request.InventoryMovementConsumptionRequest;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.entities.InventoryMovementEntity;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.repository.InventoryMovementRepository;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.repository.MaterialRepository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Servicio para gestionar movimientos de inventario por consumo de productos
 */
@Service
@Slf4j
public class InventoryMovementConsumptionService {

     private final InventoryMovementRepository inventoryMovementRepository;
     private final MaterialRepository materialRepository;
     private final InventoryMovementMapper mapper;

     public InventoryMovementConsumptionService(InventoryMovementRepository inventoryMovementRepository,
               MaterialRepository materialRepository,
               InventoryMovementMapper mapper) {
          this.inventoryMovementRepository = inventoryMovementRepository;
          this.materialRepository = materialRepository;
          this.mapper = mapper;
     }

     /**
      * Registra un movimiento de inventario por consumo de producto
      *
      * @param request Datos del consumo del producto
      * @return Mono con el movimiento de inventario creado
      */
     public Mono<InventoryMovement> registerProductConsumption(InventoryMovementConsumptionRequest request) {
          log.info("Registering product consumption movement for product: {} in organization: {}",
                    request.getProductId(), request.getOrganizationId());

          // Validar que la cantidad consumida sea consistente con el cambio de stock
          Integer expectedNewStock = request.getPreviousStock() - request.getQuantity();
          if (!expectedNewStock.equals(request.getNewStock())) {
               log.warn("Stock calculation mismatch. Expected: {}, Received: {}",
                         expectedNewStock, request.getNewStock());
          }

          // Construimos el DTO
          InventoryMovementDto movementDto = InventoryMovementDto.builder()
                    .organizationId(request.getOrganizationId())
                    .productId(request.getProductId())
                    .movementType(MovementType.SALIDA)
                    .movementReason(request.getMovementReason())
                    .quantity(request.getQuantity())
                    .unitCost(request.getUnitCost())
                    .referenceDocument(request.getReferenceDocument())
                    .referenceId(request.getReferenceId())
                    .previousStock(request.getPreviousStock())
                    .newStock(request.getNewStock())
                    .movementDate(LocalDateTime.now())
                    .userId(request.getUserId())
                    .observations(buildObservations(request))
                    .createdAt(LocalDateTime.now())
                    .build();

          // Usamos el mapper para convertir a Entidad
          InventoryMovementEntity entity = mapper.toPersistence(movementDto);

          // Asegurar ID
          entity.setMovementId(UUID.randomUUID().toString());

          log.info("Creating movement with generated ID: {} for product: {}", entity.getMovementId(),
                    request.getProductId());

          return inventoryMovementRepository.save(entity)
                    .flatMap(savedEntity -> {
                         log.info("Successfully registered consumption movement with ID: {} for product: {}",
                                   savedEntity.getMovementId(), savedEntity.getProductId());

                         // CRUCIAL: Actualizar el stock del producto en la tabla products
                         log.info("Updating product stock from {} to {} for product: {}",
                                   request.getPreviousStock(), request.getNewStock(), request.getProductId());

                         return materialRepository.updateCurrentStock(
                                   request.getProductId(),
                                   request.getOrganizationId(),
                                   request.getNewStock())
                                   .doOnSuccess(rowsUpdated -> {
                                        if (rowsUpdated > 0) {
                                             log.info("✅ Product stock updated successfully: {} rows affected for product: {}",
                                                       rowsUpdated, request.getProductId());
                                        } else {
                                             log.warn("⚠️ Product stock update affected 0 rows for product: {} - Product may not exist",
                                                       request.getProductId());
                                        }
                                   })
                                   .doOnError(error -> log.error("❌ Failed to update product stock for product: {}",
                                             request.getProductId(), error))
                                   .onErrorResume(error -> {
                                        log.warn("🔄 Stock update failed, but movement was registered successfully. " +
                                                  "Movement ID: {}, Product: {}", savedEntity.getMovementId(),
                                                  request.getProductId());
                                        return Mono.just(0); // Continue even if stock update fails
                                   })
                                   .thenReturn(mapper.toDomain(savedEntity)); // Return Domain Model using Mapper
                    })
                    .doOnError(error -> log.error("Error registering consumption movement for product: {}",
                              request.getProductId(), error));
     }

     /**
      * Construye las observaciones para el movimiento
      */
     private String buildObservations(InventoryMovementConsumptionRequest request) {
          StringBuilder observations = new StringBuilder();

          // Agregar observación base
          observations.append("Salida por consumo de producto");

          // Agregar observaciones personalizadas si existen
          if (request.getObservations() != null && !request.getObservations().trim().isEmpty()) {
               observations.append(" - ").append(request.getObservations().trim());
          }

          // Agregar información del documento de referencia si existe
          if (request.getReferenceDocument() != null && !request.getReferenceDocument().trim().isEmpty()) {
               observations.append(" (Ref: ").append(request.getReferenceDocument().trim()).append(")");
          }

          return observations.toString();
     }

     /**
      * Valida que los datos de stock sean consistentes
      *
      * @param request Datos del consumo
      * @return true si es válido, false si no
      */
     public boolean validateStockConsistency(InventoryMovementConsumptionRequest request) {
          // Verificar que previous_stock - quantity = new_stock
          int calculatedNewStock = request.getPreviousStock() - request.getQuantity();
          return calculatedNewStock == request.getNewStock();
     }

     /**
      * Obtiene el último movimiento de un producto para verificar el stock actual
      *
      * @param organizationId ID de la organización
      * @param productId      ID del producto
      * @return Mono con el último movimiento
      */
     public Mono<InventoryMovement> getLastMovement(String organizationId, String productId) {
          log.info("Getting last movement for product: {} in organization: {}", productId, organizationId);
          return inventoryMovementRepository.findLastMovementByProductId(organizationId, productId)
                    .map(mapper::toDomain);
     }
}
