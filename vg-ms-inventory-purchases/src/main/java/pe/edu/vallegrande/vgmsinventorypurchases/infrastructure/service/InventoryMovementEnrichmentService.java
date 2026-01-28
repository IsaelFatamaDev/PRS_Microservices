package pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.common.InventoryMovementDto;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.mapper.MaterialMapper;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.mapper.ProductCategoryMapper;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.models.Material;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.models.ProductCategory;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.response.EnrichedInventoryMovementResponse;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.repository.MaterialRepository;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.repository.ProductCategoryRepository;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Servicio para
 * enriquecer movimientos de inventario con información de
 * productos y usuarios (Optimizado con caché)
 */
@Service
@Slf4j
public class InventoryMovementEnrichmentService {

     private final MaterialRepository materialRepository;
     private final ProductCategoryRepository categoryRepository;
     private final UserCacheService userCacheService;
     private final MaterialMapper materialMapper;
     private final ProductCategoryMapper categoryMapper;

     private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm",
               new Locale("es", "ES"));

     private static final NumberFormat CURRENCY_FORMATTER = NumberFormat.getCurrencyInstance(new Locale("es", "PE"));

     static {
          CURRENCY_FORMATTER.setCurrency(java.util.Currency.getInstance("PEN"));
     }

     public InventoryMovementEnrichmentService(MaterialRepository materialRepository,
               ProductCategoryRepository categoryRepository,
               UserCacheService userCacheService,
               MaterialMapper materialMapper,
               ProductCategoryMapper categoryMapper) {
          this.materialRepository = materialRepository;
          this.categoryRepository = categoryRepository;
          this.userCacheService = userCacheService;
          this.materialMapper = materialMapper;
          this.categoryMapper = categoryMapper;
     }

     /**
      *
      * Enriquece un movimiento de inventario
      * con información de producto y usuario
      *
      * @param movementDto Movimiento de inventario a
      *                    enriquecer
      * @return Movimiento enriquecido con toda la información
      */
     public Mono<EnrichedInventoryMovementResponse> enrichMovement(InventoryMovementDto movementDto) {
          log.debug("🔍 Enriqueciendo movimiento: {}", movementDto.getMovementId());

          // Obtener producto, categoría y usuario en paralelo
          Mono<Material> productMono = materialRepository.findById(movementDto.getProductId())
                    .map(materialMapper::toDomain)
                    .doOnNext(product -> log.debug("✅ Producto obtenido: {}", product.getProductName()))
                    .onErrorResume(error -> {
                         log.error("❌ Error obteniendo producto {}: {}", movementDto.getProductId(),
                                   error.getMessage());
                         return Mono.empty();
                    });

          Mono<EnrichedInventoryMovementResponse.UserInfo> userMono = userCacheService
                    .getUserInfo(movementDto.getUserId());

          Mono<ProductCategory> categoryMono = productMono
                    .flatMap(product -> {
                         if (product.getCategoryId() != null) {
                              return categoryRepository.findById(product.getCategoryId())
                                        .map(categoryMapper::toDomain)
                                        .doOnNext(category -> log.debug("✅ Categoría obtenida: {}",
                                                  category.getCategoryName()));
                         }
                         return Mono.empty();
                    })
                    .defaultIfEmpty(new ProductCategory());

          return Mono.zip(productMono.defaultIfEmpty(new Material()), userMono,
                    categoryMono)
                    .map(tuple -> {
                         Material product = tuple.getT1();
                         EnrichedInventoryMovementResponse.UserInfo userInfo = tuple.getT2();
                         ProductCategory category = tuple.getT3();

                         return buildEnrichedResponse(movementDto, product, category,
                                   userInfo);
                    });
     }

     /**
      * Construye la respuesta
      * enriquecida con toda la información
      */
     private EnrichedInventoryMovementResponse buildEnrichedResponse(
               InventoryMovementDto movementDto,
               Material product,
               ProductCategory category,
               EnrichedInventoryMovementResponse.UserInfo userInfo) {

          BigDecimal totalValue = movementDto.getUnitCost().multiply(BigDecimal.valueOf(movementDto.getQuantity()));

          EnrichedInventoryMovementResponse.ProductInfo productInfo = EnrichedInventoryMovementResponse.ProductInfo
                    .builder()
                    .productId(product.getProductId())
                    .productCode(product.getProductCode() != null ? product.getProductCode() : "PROD???")
                    .productName(product.getProductName() != null ? product.getProductName() : "Producto desconocido")
                    .categoryId(product.getCategoryId())
                    .categoryName(category.getCategoryName() != null ? category.getCategoryName() : "General")
                    .unitOfMeasure(
                              product.getUnitOfMeasure() != null ? product.getUnitOfMeasure().toString() : "unidades")
                    .minimumStock(product.getMinimumStock())
                    .maximumStock(product.getMaximumStock())
                    .currentStock(product.getCurrentStock())
                    .unitCost(product.getUnitCost())
                    .status(product.getStatus() != null
                              ? product.getStatus().toString()
                              : "UNKNOWN")
                    .build();

          return EnrichedInventoryMovementResponse.builder()
                    // Datos del movimiento
                    .movementId(movementDto.getMovementId())
                    .organizationId(movementDto.getOrganizationId())
                    .productId(movementDto.getProductId())
                    .movementType(movementDto.getMovementType())
                    .movementReason(movementDto.getMovementReason())
                    .quantity(movementDto.getQuantity())
                    .unitCost(movementDto.getUnitCost())
                    .totalValue(totalValue)
                    .referenceDocument(movementDto.getReferenceDocument())
                    .referenceId(movementDto.getReferenceId())
                    .previousStock(movementDto.getPreviousStock())
                    .newStock(movementDto.getNewStock())
                    .movementDate(movementDto.getMovementDate())
                    .userId(movementDto.getUserId())
                    .observations(movementDto.getObservations())
                    .createdAt(movementDto.getCreatedAt())

                    // Información enriquecida
                    .productInfo(productInfo)
                    .userInfo(userInfo)

                    // Campos formateados
                    .formattedDate(movementDto.getMovementDate().format(DATE_FORMATTER))
                    .formattedCost(CURRENCY_FORMATTER.format(movementDto.getUnitCost()))
                    .formattedValue(CURRENCY_FORMATTER.format(totalValue))
                    .performedBy(userInfo.getFullName())
                    .productName(productInfo.getProductName())
                    .productCode(productInfo.getProductCode())
                    .build();
     }
}
