package pe.edu.vallegrande.vgmsinventorypurchases.application.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.request.CreateInventoryMovementDto;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.common.InventoryMovementDto;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.common.InventoryMovementFilterDto;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.mapper.InventoryMovementMapper;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.mapper.MaterialMapper;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.enums.MovementReason;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.enums.MovementType;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.models.InventoryMovement;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.models.Material;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.entities.InventoryMovementEntity;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.repository.InventoryMovementRepository;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.repository.MaterialRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@Slf4j
public class InventoryMovementService {

     private final InventoryMovementRepository inventoryMovementRepository;
     private final MaterialRepository materialRepository;
     private final InventoryMovementMapper mapper;
     private final MaterialMapper materialMapper;

     public InventoryMovementService(InventoryMovementRepository inventoryMovementRepository,
               MaterialRepository materialRepository,
               InventoryMovementMapper mapper,
               MaterialMapper materialMapper) {
          this.inventoryMovementRepository = inventoryMovementRepository;
          this.materialRepository = materialRepository;
          this.mapper = mapper;
          this.materialMapper = materialMapper;
     }

     /**
      * Obtener stock actual de un producto
      */
     public Mono<Integer> getCurrentStock(String organizationId, String productId) {
          return inventoryMovementRepository.findLastMovementByProductId(organizationId, productId)
                    .map(mapper::toDomain)
                    .map(InventoryMovement::getNewStock)
                    .switchIfEmpty(Mono.just(0))
                    .doOnNext(stock -> log.debug("Current stock for product {}: {}", productId, stock));
     }

     /**
      * Obtener todos los movimientos de una organización con paginación
      */
     public Flux<InventoryMovementDto> getMovementsByOrganization(String organizationId, Integer page, Integer size) {
          log.info("Getting movements for organization: {}, page: {}, size: {}", organizationId, page, size);

          Pageable pageable = PageRequest.of(
                    page != null ? page : 0,
                    size != null ? size : 20,
                    Sort.by(Sort.Direction.DESC, "movementDate"));

          return inventoryMovementRepository.findByOrganizationIdOrderByMovementDateDesc(organizationId, pageable)
                    .map(mapper::toDto)
                    .doOnNext(movement -> log.debug("Retrieved movement: {}", movement.getMovementId()))
                    .doOnError(error -> log.error("Error getting movements for organization {}: {}", organizationId,
                              error.getMessage()));
     }

     /**
      * Obtener todos los movimientos de una organización (sin paginación)
      */
     public Flux<InventoryMovementDto> getAllMovementsByOrganization(String organizationId) {
          log.info("Getting all movements for organization: {}", organizationId);
          return inventoryMovementRepository.findByOrganizationIdOrderByMovementDateDesc(organizationId)
                    .map(mapper::toDto)
                    .doOnNext(movement -> log.debug("Retrieved movement: {}", movement.getMovementId()))
                    .doOnError(error -> log.error("Error getting all movements for organization {}: {}", organizationId,
                              error.getMessage()));
     }

     /**
      * Obtener movimientos por producto específico
      */
     public Flux<InventoryMovementDto> getMovementsByProduct(String organizationId, String productId) {
          log.info("Getting movements for product: {} in organization: {}", productId, organizationId);

          return inventoryMovementRepository
                    .findByOrganizationIdAndProductIdOrderByMovementDateDesc(organizationId, productId)
                    .map(mapper::toDto)
                    .doOnNext(movement -> log.debug("Retrieved product movement: {}", movement.getMovementId()))
                    .doOnError(error -> log.error("Error getting movements for product {}: {}", productId,
                              error.getMessage()));
     }

     /**
      * Obtener movimiento por ID específico
      */
     public Mono<InventoryMovementDto> getMovementById(String movementId) {
          log.info("Getting movement by ID: {}", movementId);

          return inventoryMovementRepository.findById(movementId)
                    .map(mapper::toDto)
                    .doOnNext(movement -> log.debug("Retrieved movement by ID: {}", movementId))
                    .doOnError(error -> log.error("Error getting movement by ID: {}", error.getMessage()));
     }

     /**
      * Crear un nuevo movimiento de inventario
      */
     public Mono<InventoryMovementDto> createMovement(CreateInventoryMovementDto createDto) {
          log.info("Creating inventory movement for product: {}", createDto.getProductId());

          return getCurrentStock(createDto.getOrganizationId(), createDto.getProductId())
                    .flatMap(currentStock -> {
                         log.debug("Current stock for product {}: {}", createDto.getProductId(), currentStock);

                         // Convertir DTO a entidad con el stock actual
                         InventoryMovement movement = mapper.toEntity(createDto, currentStock);

                         // Validar que hay suficiente stock para salidas
                         if (movement.getMovementType() == MovementType.SALIDA &&
                                   currentStock < movement.getQuantity()) {
                              return Mono.error(new RuntimeException(
                                        String.format("Stock insuficiente. Stock actual: %d, Cantidad solicitada: %d",
                                                  currentStock, movement.getQuantity())));
                         }

                         // Guardar el movimiento
                         return inventoryMovementRepository.save(mapper.toPersistence(movement));
                    })
                    .flatMap(savedEntity -> {
                         InventoryMovement savedMovement = mapper.toDomain(savedEntity);
                         // Actualizar el stock del material después de guardar el movimiento
                         return updateMaterialStock(savedMovement).thenReturn(savedMovement);
                    })
                    .map(mapper::toDto)
                    .doOnSuccess(movement -> log.info("Inventory movement created successfully: {}",
                              movement.getMovementId()))
                    .doOnError(error -> log.error("Error creating inventory movement: {}", error.getMessage()));
     }

     /**
      * Método privado para actualizar el stock del material
      */
     private Mono<Material> updateMaterialStock(InventoryMovement movement) {
          return materialRepository.findById(movement.getProductId())
                    .flatMap(material -> {
                         log.debug("Updating stock for material {}: {} -> {}",
                                   material.getProductId(), material.getCurrentStock(), movement.getNewStock());

                         material.setCurrentStock(movement.getNewStock());
                         return materialRepository.save(material);
                    })
                    .map(materialMapper::toDomain)
                    .doOnSuccess(material -> log.debug("Updated material stock: {} -> {}",
                              material.getProductId(), material.getCurrentStock()))
                    .doOnError(error -> log.error("Error updating material stock: {}", error.getMessage()));
     }

     /**
      * Crear movimiento automático desde compra
      */
     public Mono<InventoryMovementDto> createMovementFromPurchase(String organizationId, String productId,
               Integer quantity, BigDecimal unitCost,
               String purchaseCode, String purchaseId, String userId) {

          log.info("Creating automatic inventory movement for purchase: {} - product: {}", purchaseCode, productId);

          CreateInventoryMovementDto createDto = CreateInventoryMovementDto.builder()
                    .organizationId(organizationId)
                    .productId(productId)
                    .movementType(MovementType.ENTRADA)
                    .movementReason(MovementReason.COMPRA)
                    .quantity(quantity)
                    .unitCost(unitCost)
                    .referenceDocument(purchaseCode)
                    .referenceId(purchaseId)
                    .userId(userId)
                    .observations("Entrada automática por compra: " + purchaseCode)
                    .movementDate(LocalDateTime.now())
                    .build();

          return createMovement(createDto);
     }

     /**
      * Obtener movimientos con filtros avanzados
      */
     public Flux<InventoryMovementDto> getMovementsWithFilters(InventoryMovementFilterDto filters) {
          log.info("Getting movements with filters for organization: {}", filters.getOrganizationId());

          Pageable pageable = createPageable(filters);

          return inventoryMovementRepository.findWithFilters(
                    filters.getOrganizationId(),
                    filters.getProductId(),
                    filters.getMovementType(),
                    filters.getMovementReason(),
                    filters.getStartDate(),
                    filters.getEndDate(),
                    pageable)
                    .map(mapper::toDto)
                    .doOnNext(movement -> log.debug("Retrieved filtered movement: {}", movement.getMovementId()))
                    .doOnError(error -> log.error("Error getting filtered movements: {}", error.getMessage()));
     }

     /**
      * Contar movimientos por organización
      */
     public Mono<Long> countMovementsByOrganization(String organizationId) {
          return inventoryMovementRepository.countByOrganizationId(organizationId)
                    .doOnNext(count -> log.debug("Total movements for organization {}: {}", organizationId, count));
     }

     /**
      * Obtener movimientos por rango de fechas
      */
     public Flux<InventoryMovementDto> getMovementsByDateRange(String organizationId,
               LocalDateTime startDate,
               LocalDateTime endDate) {
          log.info("Getting movements by date range: {} to {} for organization: {}",
                    startDate, endDate, organizationId);

          return inventoryMovementRepository.findByOrganizationIdAndMovementDateBetween(
                    organizationId, startDate, endDate)
                    .map(mapper::toDto)
                    .doOnError(error -> log.error("Error getting movements by date range: {}", error.getMessage()));
     }

     /**
      * Método privado para crear paginación
      */
     private Pageable createPageable(InventoryMovementFilterDto filters) {
          Sort.Direction direction = "ASC".equalsIgnoreCase(filters.getSortDirection())
                    ? Sort.Direction.ASC
                    : Sort.Direction.DESC;
          Sort sort = Sort.by(direction, filters.getSortBy());
          return PageRequest.of(filters.getPage(), filters.getSize(), sort);
     }

     /**
      * Obtener resumen de stock por productos (método adicional útil)
      */
     public Flux<Map<String, Object>> getStockSummary(String organizationId) {
          log.info("Getting stock summary for organization: {}", organizationId);

          return inventoryMovementRepository.findLatestMovementByProduct(organizationId)
                    .collectMap(
                              InventoryMovementEntity::getProductId,
                              movement -> {
                                   Map<String, Object> movementMap = new java.util.HashMap<>();
                                   movementMap.put("productId", movement.getProductId());
                                   movementMap.put("currentStock", movement.getNewStock());
                                   movementMap.put("lastMovementDate", movement.getMovementDate());
                                   movementMap.put("lastMovementType", movement.getMovementType());
                                   return movementMap;
                              })
                    .flatMapMany(stockMap -> materialRepository.findByOrganizationId(organizationId)
                              .map(material -> {
                                   @SuppressWarnings("unchecked")
                                   Map<String, Object> stockInfo = (Map<String, Object>) stockMap
                                             .get(material.getProductId());

                                   Map<String, Object> result = new java.util.HashMap<>();
                                   result.put("productId", material.getProductId());
                                   result.put("productCode", material.getProductCode());
                                   result.put("productName", material.getProductName());
                                   result.put("currentStock", stockInfo != null ? stockInfo.get("currentStock") : 0);
                                   result.put("minimumStock", material.getMinimumStock());
                                   result.put("maximumStock", material.getMaximumStock());
                                   result.put("lastMovementDate",
                                             stockInfo != null ? stockInfo.get("lastMovementDate") : null);
                                   result.put("lastMovementType",
                                             stockInfo != null ? stockInfo.get("lastMovementType") : null);
                                   result.put("stockStatus", getStockStatus(
                                             stockInfo != null ? (Integer) stockInfo.get("currentStock") : 0,
                                             material.getMinimumStock()));
                                   return result;
                              }))
                    .doOnError(error -> log.error("Error getting stock summary: {}", error.getMessage()));
     }

     /**
      * Método privado para determinar estado del stock
      */
     private String getStockStatus(Integer currentStock, Integer minimumStock) {
          if (currentStock == null || currentStock == 0)
               return "AGOTADO";
          if (minimumStock != null && currentStock <= minimumStock)
               return "CRITICO";
          return "NORMAL";
     }

}
