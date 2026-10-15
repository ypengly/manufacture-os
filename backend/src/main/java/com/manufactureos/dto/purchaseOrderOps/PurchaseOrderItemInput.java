package com.manufactureos.dto.purchaseOrderOps;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class PurchaseOrderItemInput {

    @NotNull
    private Long materialId;

    @NotNull
    @DecimalMin(value = "0.000001")
    private BigDecimal quantity;

    @DecimalMin(value = "0")
    private BigDecimal unitCost;

    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }
}
