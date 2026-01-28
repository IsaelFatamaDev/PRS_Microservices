package pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.enums.MovementReason;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.enums.MovementType;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.entities.InventoryMovementEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Repository
public interface InventoryMovementRepository extends R2dbcRepository<InventoryMovementEntity, String> {

        /**
         * Buscar movimientos por producto y organización ordenados por fecha
         */
        Flux<InventoryMovementEntity> findByOrganizationIdAndProductIdOrderByMovementDateDesc(String organizationId,
                        String productId);

        /**
         * Buscar movimientos por organización ordenados por fecha (paginado)
         */
        Flux<InventoryMovementEntity> findByOrganizationIdOrderByMovementDateDesc(String organizationId,
                        Pageable pageable);

        /**
         * Buscar movimientos por organización ordenados por fecha (sin paginación)
         */
        Flux<InventoryMovementEntity> findByOrganizationIdOrderByMovementDateDesc(String organizationId);

        /**
         * Buscar movimientos por tipo
         */
        Flux<InventoryMovementEntity> findByOrganizationIdAndMovementTypeOrderByMovementDateDesc(String organizationId,
                        MovementType movementType);

        /**
         * Buscar movimientos por razón
         */
        Flux<InventoryMovementEntity> findByOrganizationIdAndMovementReasonOrderByMovementDateDesc(
                        String organizationId,
                        MovementReason movementReason);

        /**
         * Buscar movimientos por rango de fechas
         */
        @Query("SELECT * FROM inventory_movements WHERE organization_id = :organizationId " +
                        "AND movement_date >= :startDate AND movement_date <= :endDate " +
                        "ORDER BY movement_date DESC")
        Flux<InventoryMovementEntity> findByOrganizationIdAndMovementDateBetween(
                        @Param("organizationId") String organizationId,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        /**
         * Buscar movimientos por documento de referencia
         */
        Flux<InventoryMovementEntity> findByOrganizationIdAndReferenceDocumentOrderByMovementDateDesc(
                        String organizationId,
                        String referenceDocument);

        /**
         * Buscar movimientos por ID de referencia (para purchases)
         */
        Flux<InventoryMovementEntity> findByReferenceIdOrderByMovementDateDesc(String referenceId);

        /**
         * Contar movimientos por organización
         */
        Mono<Long> countByOrganizationId(String organizationId);

        /**
         * Obtener último movimiento de un producto
         */
        @Query("SELECT * FROM inventory_movements WHERE organization_id = :organizationId AND product_id = :productId "
                        +
                        "ORDER BY movement_date DESC LIMIT 1")
        Mono<InventoryMovementEntity> findLastMovementByProductId(@Param("organizationId") String organizationId,
                        @Param("productId") String productId);

        /**
         * Buscar movimientos con filtros múltiples
         */
        @Query("SELECT * FROM inventory_movements WHERE organization_id = :organizationId " +
                        "AND (:productId IS NULL OR product_id = :productId) " +
                        "AND (:movementType IS NULL OR movement_type = :movementType) " +
                        "AND (:movementReason IS NULL OR movement_reason = :movementReason) " +
                        "AND (:startDate IS NULL OR movement_date >= :startDate) " +
                        "AND (:endDate IS NULL OR movement_date <= :endDate) " +
                        "ORDER BY movement_date DESC")
        Flux<InventoryMovementEntity> findWithFilters(
                        @Param("organizationId") String organizationId,
                        @Param("productId") String productId,
                        @Param("movementType") MovementType movementType,
                        @Param("movementReason") MovementReason movementReason,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        Pageable pageable);

        /**
         * Obtener resumen de stock actual por producto
         */
        @Query("SELECT DISTINCT ON (product_id) * FROM inventory_movements " +
                        "WHERE organization_id = :organizationId " +
                        "ORDER BY product_id, movement_date DESC")
        Flux<InventoryMovementEntity> findLatestMovementByProduct(@Param("organizationId") String organizationId);
}
