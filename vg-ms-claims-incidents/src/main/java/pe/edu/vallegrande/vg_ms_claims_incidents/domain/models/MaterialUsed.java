package pe.edu.vallegrande.vg_ms_claims_incidents.domain.models;

import lombok.Data;

/**
 * Value Object de dominio que representa materiales utilizados.
 * No contiene anotaciones de infraestructura (MongoDB, JPA, etc.)
 */
@Data
public class MaterialUsed {
    private String productId;
    private Integer quantity;
    private String unit;
    private Double unitCost;

    // Getters y setters explícitos para compatibilidad con IDE
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Double getUnitCost() {
        return unitCost;
    }

    public void setUnitCost(Double unitCost) {
        this.unitCost = unitCost;
    }
}