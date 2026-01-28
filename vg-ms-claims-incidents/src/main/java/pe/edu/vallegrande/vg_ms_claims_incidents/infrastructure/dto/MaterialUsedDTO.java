package pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.dto;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class MaterialUsedDTO {
    @JsonProperty("productId")
    private String productId;
    
    @JsonProperty("quantity")
    private Integer quantity;
    
    @JsonProperty("unit")
    private String unit;
    
    @JsonProperty("unitCost")
    private Double unitCost;
    
    // Getters y setters explícitos para compatibilidad con IDE y serialización
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