package com.manufactureos.dto.bom;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class BomComponentInput {

    @NotNull
    private Long materialId;

    @NotNull
    @DecimalMin(value = "0.0001")
    private BigDecimal quantity;

    @DecimalMin(value = "0")
    private BigDecimal wastePercent = BigDecimal.ZERO;

    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public BigDecimal getWastePercent() { return wastePercent; }
    public void setWastePercent(BigDecimal wastePercent) { this.wastePercent = wastePercent; }
}
