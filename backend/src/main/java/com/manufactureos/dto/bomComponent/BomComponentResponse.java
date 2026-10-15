package com.manufactureos.dto.bomComponent;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class BomComponentResponse {

    private Long id;

    private Long bomId;

    private Long materialId;

    private BigDecimal quantity;

    private BigDecimal wastePercent;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getBomId() { return bomId; }
    public void setBomId(Long bomId) { this.bomId = bomId; }
    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public BigDecimal getWastePercent() { return wastePercent; }
    public void setWastePercent(BigDecimal wastePercent) { this.wastePercent = wastePercent; }
}
