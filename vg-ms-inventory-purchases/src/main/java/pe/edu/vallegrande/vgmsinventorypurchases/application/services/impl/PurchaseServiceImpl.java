package pe.edu.vallegrande.vgmsinventorypurchases.application.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.vallegrande.vgmsinventorypurchases.application.services.PurchaseService;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.enums.MovementReason;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.enums.MovementType;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.enums.PurchaseStatus;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.models.InventoryMovement;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.models.Purchase;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.models.PurchaseDetail;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.models.Supplier;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.request.PurchaseDetailRequest;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.request.PurchaseRequest;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.response.PurchaseDetailResponse;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.response.PurchaseResponse;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.exception.CustomException;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.mapper.*;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.repository.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PurchaseServiceImpl implements PurchaseService {

     private final PurchaseRepository purchaseRepository;
     private final PurchaseDetailRepository purchaseDetailRepository;
     private final MaterialRepository materialRepository;
     private final SupplierRepository supplierRepository;
     private final InventoryMovementRepository inventoryMovementRepository;

     private final PurchaseMapper purchaseMapper;
     private final PurchaseDetailMapper purchaseDetailMapper;
     private final MaterialMapper materialMapper;
     private final SupplierMapper supplierMapper;
     private final InventoryMovementMapper inventoryMovementMapper;

     public PurchaseServiceImpl(PurchaseRepository purchaseRepository,
               PurchaseDetailRepository purchaseDetailRepository,
               MaterialRepository materialRepository,
               SupplierRepository supplierRepository,
               InventoryMovementRepository inventoryMovementRepository,
               PurchaseMapper purchaseMapper,
               PurchaseDetailMapper purchaseDetailMapper,
               MaterialMapper materialMapper,
               SupplierMapper supplierMapper,
               InventoryMovementMapper inventoryMovementMapper) {
          this.purchaseRepository = purchaseRepository;
          this.purchaseDetailRepository = purchaseDetailRepository;
          this.materialRepository = materialRepository;
          this.supplierRepository = supplierRepository;
          this.inventoryMovementRepository = inventoryMovementRepository;
          this.purchaseMapper = purchaseMapper;
          this.purchaseDetailMapper = purchaseDetailMapper;
          this.materialMapper = materialMapper;
          this.supplierMapper = supplierMapper;
          this.inventoryMovementMapper = inventoryMovementMapper;
     }

     @Override
     public Flux<PurchaseResponse> findAll() {
          return purchaseRepository.findAll()
                    .map(purchaseMapper::toDomain)
                    .flatMap(this::mapPurchaseToResponse);
     }

     @Override
     public Flux<PurchaseResponse> findByOrganizationId(String organizationId) {
          return purchaseRepository.findByOrganizationId(organizationId)
                    .map(purchaseMapper::toDomain)
                    .flatMap(this::mapPurchaseToResponse);
     }

     @Override
     public Mono<PurchaseResponse> findById(String purchaseId) {
          return purchaseRepository.findById(purchaseId)
                    .map(purchaseMapper::toDomain)
                    .flatMap(this::mapPurchaseToResponse)
                    .switchIfEmpty(Mono.error(new CustomException("Compra no encontrada", "PURCHASE_NOT_FOUND", 404)));
     }

     @Override
     @Transactional
     public Mono<PurchaseResponse> create(PurchaseRequest purchaseRequest) {
          Mono<String> purchaseCodeMono = (purchaseRequest.getPurchaseCode() != null
                    && !purchaseRequest.getPurchaseCode().trim().isEmpty())
                              ? Mono.just(purchaseRequest.getPurchaseCode())
                              : generatePurchaseCode(purchaseRequest.getOrganizationId());

          return purchaseCodeMono.flatMap(purchaseCode -> {
               return supplierRepository.findById(purchaseRequest.getSupplierId())
                         .switchIfEmpty(
                                   Mono.error(new CustomException("El proveedor no existe", "SUPPLIER_NOT_FOUND", 404)))
                         .flatMap(supplierEntity -> {
                              Purchase purchase = Purchase.builder()
                                        .organizationId(purchaseRequest.getOrganizationId())
                                        .purchaseCode(purchaseCode)
                                        .supplierId(purchaseRequest.getSupplierId())
                                        .purchaseDate(purchaseRequest.getPurchaseDate())
                                        .deliveryDate(purchaseRequest.getDeliveryDate())
                                        .totalAmount(BigDecimal.ZERO)
                                        .status(purchaseRequest.getStatus() != null ? purchaseRequest.getStatus()
                                                  : PurchaseStatus.PENDIENTE)
                                        .requestedByUserId(purchaseRequest.getRequestedByUserId())
                                        .invoiceNumber(purchaseRequest.getInvoiceNumber())
                                        .observations(purchaseRequest.getObservations())
                                        .createdAt(LocalDateTime.now())
                                        .build();

                              BigDecimal totalAmount = purchaseRequest.getDetails().stream()
                                        .map(detail -> detail.getUnitCost()
                                                  .multiply(BigDecimal.valueOf(detail.getQuantityOrdered())))
                                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                              purchase.setTotalAmount(totalAmount);

                              return purchaseRepository.save(purchaseMapper.toEntity(purchase))
                                        .map(purchaseMapper::toDomain)
                                        .flatMap(savedPurchase -> {
                                             List<Mono<PurchaseDetail>> detailMonos = purchaseRequest.getDetails()
                                                       .stream()
                                                       .map(detailRequest -> createPurchaseDetail(
                                                                 savedPurchase.getPurchaseId(), detailRequest))
                                                       .collect(Collectors.toList());

                                             return Flux.concat(detailMonos)
                                                       .collectList()
                                                       .flatMap(savedDetails -> mapPurchaseToResponse(savedPurchase));
                                        });
                         });
          });
     }

     @Override
     @Transactional
     public Mono<PurchaseResponse> update(String purchaseId, PurchaseRequest purchaseRequest) {
          return purchaseRepository.findById(purchaseId)
                    .switchIfEmpty(Mono.error(new CustomException("Compra no encontrada", "PURCHASE_NOT_FOUND", 404)))
                    .map(purchaseMapper::toDomain)
                    .flatMap(existingPurchase -> {
                         existingPurchase.setPurchaseCode(purchaseRequest.getPurchaseCode());
                         existingPurchase.setSupplierId(purchaseRequest.getSupplierId());
                         existingPurchase.setPurchaseDate(purchaseRequest.getPurchaseDate());
                         existingPurchase.setDeliveryDate(purchaseRequest.getDeliveryDate());
                         existingPurchase.setRequestedByUserId(purchaseRequest.getRequestedByUserId());
                         existingPurchase.setInvoiceNumber(purchaseRequest.getInvoiceNumber());
                         existingPurchase.setObservations(purchaseRequest.getObservations());

                         return purchaseDetailRepository.deleteByPurchaseId(purchaseId)
                                   .then(purchaseRepository.save(purchaseMapper.toEntity(existingPurchase)))
                                   .map(purchaseMapper::toDomain)
                                   .flatMap(savedPurchase -> {
                                        BigDecimal totalAmount = purchaseRequest.getDetails().stream()
                                                  .map(detail -> detail.getUnitCost()
                                                            .multiply(BigDecimal.valueOf(detail.getQuantityOrdered())))
                                                  .reduce(BigDecimal.ZERO, BigDecimal::add);
                                        savedPurchase.setTotalAmount(totalAmount);

                                        List<Mono<PurchaseDetail>> detailMonos = purchaseRequest.getDetails().stream()
                                                  .map(detailRequest -> createPurchaseDetail(
                                                            savedPurchase.getPurchaseId(), detailRequest))
                                                  .collect(Collectors.toList());

                                        return Flux.concat(detailMonos)
                                                  .collectList()
                                                  .flatMap(savedDetails -> {
                                                       return purchaseRepository
                                                                 .save(purchaseMapper.toEntity(savedPurchase))
                                                                 .map(purchaseMapper::toDomain)
                                                                 .flatMap(this::mapPurchaseToResponse);
                                                  });
                                   });
                    });
     }

     @Override
     @Transactional
     public Mono<Void> delete(String purchaseId) {
          return purchaseRepository.findById(purchaseId)
                    .switchIfEmpty(Mono.error(new CustomException("Compra no encontrada", "PURCHASE_NOT_FOUND", 404)))
                    .map(purchaseMapper::toDomain)
                    .flatMap(purchase -> {
                         purchase.setStatus(PurchaseStatus.CANCELADO);
                         return purchaseRepository.save(purchaseMapper.toEntity(purchase)).then();
                    });
     }

     @Override
     @Transactional
     public Mono<PurchaseResponse> restore(String purchaseId) {
          return purchaseRepository.findById(purchaseId)
                    .switchIfEmpty(Mono.error(new CustomException("Compra no encontrada", "PURCHASE_NOT_FOUND", 404)))
                    .map(purchaseMapper::toDomain)
                    .flatMap(purchase -> {
                         if (purchase.getStatus() != PurchaseStatus.CANCELADO) {
                              return Mono.error(new CustomException("La compra no está eliminada",
                                        "PURCHASE_NOT_DELETED", 400));
                         }

                         purchase.setStatus(PurchaseStatus.PENDIENTE);
                         return purchaseRepository.save(purchaseMapper.toEntity(purchase))
                                   .map(purchaseMapper::toDomain)
                                   .flatMap(this::mapPurchaseToResponse);
                    });
     }

     @Override
     public Flux<PurchaseResponse> findByStatus(String organizationId, PurchaseStatus status) {
          return purchaseRepository.findByOrganizationIdAndStatus(organizationId, status)
                    .map(purchaseMapper::toDomain)
                    .flatMap(this::mapPurchaseToResponse);
     }

     @Override
     @Transactional
     public Mono<PurchaseResponse> updateStatus(String purchaseId, PurchaseStatus status) {
          return purchaseRepository.findById(purchaseId)
                    .switchIfEmpty(Mono.error(new CustomException("Compra no encontrada", "PURCHASE_NOT_FOUND", 404)))
                    .map(purchaseMapper::toDomain)
                    .flatMap(purchase -> {
                         PurchaseStatus previousStatus = purchase.getStatus();
                         purchase.setStatus(status);

                         return purchaseRepository.save(purchaseMapper.toEntity(purchase))
                                   .map(purchaseMapper::toDomain)
                                   .flatMap(savedPurchase -> {
                                        // Si el estado cambió a COMPLETADO, actualizar el stock de los productos
                                        if (status == PurchaseStatus.COMPLETADO
                                                  && previousStatus != PurchaseStatus.COMPLETADO) {
                                             return updateProductStockFromPurchase(purchaseId)
                                                       .then(mapPurchaseToResponse(savedPurchase));
                                        }
                                        return mapPurchaseToResponse(savedPurchase);
                                   });
                    });
     }

     /**
      * Actualiza el stock de los productos basado en los detalles de la compra
      */
     private Mono<Void> updateProductStockFromPurchase(String purchaseId) {
          return purchaseRepository.findById(purchaseId)
                    .map(purchaseMapper::toDomain)
                    .flatMap(purchase -> {
                         return purchaseDetailRepository.findByPurchaseId(purchaseId)
                                   .map(purchaseDetailMapper::toDomain)
                                   .flatMap(detail -> {
                                        return materialRepository.findById(detail.getProductId())
                                                  .map(materialMapper::toDomain)
                                                  .flatMap(product -> {
                                                       // Incrementar el stock actual con la cantidad recibida (o
                                                       // cantidad ordenada si no hay cantidad recibida)
                                                       int quantityToAdd = detail.getQuantityReceived() > 0
                                                                 ? detail.getQuantityReceived()
                                                                 : detail.getQuantityOrdered();

                                                       int previousStock = product.getCurrentStock();
                                                       int newStock = previousStock + quantityToAdd;
                                                       product.setCurrentStock(newStock);

                                                       // Crear movimiento de inventario
                                                       InventoryMovement movement = InventoryMovement.builder()
                                                                 .organizationId(purchase.getOrganizationId())
                                                                 .productId(detail.getProductId())
                                                                 .movementType(MovementType.ENTRADA)
                                                                 .movementReason(MovementReason.COMPRA)
                                                                 .quantity(quantityToAdd)
                                                                 .unitCost(detail.getUnitCost())
                                                                 .referenceDocument(purchase.getPurchaseCode())
                                                                 .referenceId(purchaseId)
                                                                 .previousStock(previousStock)
                                                                 .newStock(newStock)
                                                                 .movementDate(LocalDateTime.now())
                                                                 .userId(purchase.getRequestedByUserId())
                                                                 .observations("Entrada por compra completada: "
                                                                           + purchase.getPurchaseCode())
                                                                 .createdAt(LocalDateTime.now())
                                                                 .build();

                                                       // Actualizar la cantidad recibida si no estaba establecida
                                                       Mono<Void> updateDetailMono = detail.getQuantityReceived() == 0
                                                                 ? Mono.fromRunnable(() -> detail.setQuantityReceived(
                                                                           detail.getQuantityOrdered()))
                                                                           .then(purchaseDetailRepository
                                                                                     .save(purchaseDetailMapper
                                                                                               .toEntity(detail)))
                                                                           .then()
                                                                 : Mono.empty(); // Guardar el producto actualizado y el
                                                                                 // movimiento de inventario
                                                       return materialRepository.save(materialMapper.toEntity(product))
                                                                 .then(inventoryMovementRepository
                                                                           .save(inventoryMovementMapper
                                                                                     .toPersistence(movement)))
                                                                 .then(updateDetailMono);
                                                  });
                                   })
                                   .then();
                    });
     }

     private Mono<PurchaseDetail> createPurchaseDetail(String purchaseId, PurchaseDetailRequest detailRequest) {
          return materialRepository.findById(detailRequest.getProductId())
                    .switchIfEmpty(Mono.error(new CustomException("Producto no encontrado", "PRODUCT_NOT_FOUND", 404)))
                    .flatMap(product -> {
                         PurchaseDetail detail = PurchaseDetail.builder()
                                   .purchaseId(purchaseId)
                                   .productId(detailRequest.getProductId())
                                   .quantityOrdered(detailRequest.getQuantityOrdered())
                                   .quantityReceived(0)
                                   .unitCost(detailRequest.getUnitCost())
                                   .subtotal(detailRequest.getUnitCost()
                                             .multiply(BigDecimal.valueOf(detailRequest.getQuantityOrdered())))
                                   .observations(detailRequest.getObservations())
                                   .build();
                         return purchaseDetailRepository.save(purchaseDetailMapper.toEntity(detail))
                                   .map(purchaseDetailMapper::toDomain);
                    });
     }

     private Mono<PurchaseResponse> mapPurchaseToResponse(Purchase purchase) {
          Mono<Supplier> supplierMono = supplierRepository.findById(purchase.getSupplierId())
                    .map(supplierMapper::toDomain)
                    .switchIfEmpty(Mono.just(Supplier.builder().supplierName("Proveedor no encontrado").build()));

          Mono<List<PurchaseDetailResponse>> detailsResponseMono = purchaseDetailRepository
                    .findByPurchaseId(purchase.getPurchaseId())
                    .map(purchaseDetailMapper::toDomain)
                    .flatMap(detail -> materialRepository.findById(detail.getProductId())
                              .map(materialMapper::toDomain)
                              .map(product -> PurchaseDetailResponse.builder()
                                        .purchaseDetailId(detail.getPurchaseDetailId())
                                        .purchaseId(detail.getPurchaseId())
                                        .productId(detail.getProductId())
                                        .productName(product.getProductName())
                                        .productCode(product.getProductCode())
                                        .quantityOrdered(detail.getQuantityOrdered())
                                        .quantityReceived(detail.getQuantityReceived())
                                        .unitCost(detail.getUnitCost())
                                        .subtotal(detail.getSubtotal())
                                        .observations(detail.getObservations())
                                        .build())
                              .defaultIfEmpty(PurchaseDetailResponse.builder()
                                        .purchaseDetailId(detail.getPurchaseDetailId())
                                        .purchaseId(detail.getPurchaseId())
                                        .productId(detail.getProductId())
                                        .productName("Producto no encontrado")
                                        .quantityOrdered(detail.getQuantityOrdered())
                                        .quantityReceived(detail.getQuantityReceived())
                                        .unitCost(detail.getUnitCost())
                                        .subtotal(detail.getSubtotal())
                                        .observations(detail.getObservations())
                                        .build()))
                    .collectList();

          return Mono.zip(supplierMono, detailsResponseMono)
                    .map(tuple -> {
                         Supplier supplier = tuple.getT1();
                         List<PurchaseDetailResponse> detailsResponse = tuple.getT2();

                         return PurchaseResponse.builder()
                                   .purchaseId(purchase.getPurchaseId())
                                   .organizationId(purchase.getOrganizationId())
                                   .purchaseCode(purchase.getPurchaseCode())
                                   .supplierId(purchase.getSupplierId())
                                   .supplierName(supplier.getSupplierName())
                                   .supplierCode(supplier.getSupplierCode())
                                   .purchaseDate(purchase.getPurchaseDate())
                                   .deliveryDate(purchase.getDeliveryDate())
                                   .totalAmount(purchase.getTotalAmount())
                                   .status(purchase.getStatus())
                                   .requestedByUserId(purchase.getRequestedByUserId())
                                   .approvedByUserId(purchase.getApprovedByUserId())
                                   .invoiceNumber(purchase.getInvoiceNumber())
                                   .observations(purchase.getObservations())
                                   .createdAt(purchase.getCreatedAt())
                                   .details(detailsResponse)
                                   .build();
                    });
     }

     /**
      * Genera automáticamente un código de compra en formato PUR00# para la
      * organización
      */
     private Mono<String> generatePurchaseCode(String organizationId) {
          return purchaseRepository.findByOrganizationId(organizationId)
                    .count()
                    .map(count -> {
                         long nextNumber = count + 1;
                         return String.format("PUR%03d", nextNumber);
                    });
     }
}
