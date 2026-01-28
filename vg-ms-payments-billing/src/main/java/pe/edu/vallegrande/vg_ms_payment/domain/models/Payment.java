package pe.edu.vallegrande.vg_ms_payment.domain.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad de dominio Payment - Modelo puro sin dependencias de infraestructura
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    
    private String paymentId;
    private boolean isNew = true;
    private String organizationId;
    private String paymentCode;
    private String userId;
    private String waterBoxId;
    private String paymentType;
    private String paymentMethod;
    private BigDecimal totalAmount;
    private LocalDate paymentDate;
    private String paymentStatus;
    private String externalReference;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    /**
     * Valida si el pago es válido según las reglas de negocio
     */
    public boolean isValid() {
        return paymentId != null && 
               organizationId != null && 
               userId != null && 
               totalAmount != null && 
               totalAmount.compareTo(BigDecimal.ZERO) > 0 &&
               paymentDate != null &&
               paymentStatus != null;
    }
    
    /**
     * Marca el pago como procesado
     */
    public void markAsProcessed() {
        this.paymentStatus = "PROCESSED";
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Marca el pago como cancelado
     */
    public void markAsCancelled() {
        this.paymentStatus = "CANCELLED";
        this.updatedAt = LocalDateTime.now();
    }
}
