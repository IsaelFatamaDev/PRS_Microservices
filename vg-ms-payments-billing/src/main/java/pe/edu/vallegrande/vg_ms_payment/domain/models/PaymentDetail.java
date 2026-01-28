package pe.edu.vallegrande.vg_ms_payment.domain.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidad de dominio PaymentDetail - Modelo puro sin dependencias de infraestructura
 * Coincide con la estructura real de la base de datos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDetail {

    private String paymentDetailId;
    private boolean isNew = true;
    private String paymentId;
    private String concept;
    private Integer year;
    private Integer month;
    private BigDecimal amount;
    private String description;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    
    /**
     * Valida si el detalle del pago es válido
     */
    public boolean isValid() {
        return paymentDetailId != null && 
               paymentId != null && 
               concept != null && 
               amount != null && 
               amount.compareTo(BigDecimal.ZERO) > 0;
    }
    
    /**
     * Obtiene el período como string
     */
    public String getPeriodDescription() {
        if (year != null && month != null) {
            return String.format("%02d/%d", month, year);
        }
        return "N/A";
    }
    
    /**
     * Valida si el período es válido
     */
    public boolean isValidPeriod() {
        return year != null && year > 2000 && 
               month != null && month >= 1 && month <= 12;
    }
}
