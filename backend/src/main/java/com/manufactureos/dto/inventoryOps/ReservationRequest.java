package com.manufactureos.dto.inventoryOps;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class ReservationRequest {

    @NotNull
    private Long productionOrderId;

    @NotNull
    private Long materialId;

    @NotNull
    private Long factoryId;

    @NotNull
    @DecimalMin(value = "0.0001")
    private BigDecimal quantity;

    private boolean allowPartial = false;

    public Long getProductionOrderId() { return productionOrderId; }
    public void setProductionOrderId(Long productionOrderId) { this.productionOrderId = productionOrderId; }
    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }
    public Long getFactoryId() { return factoryId; }
    public void setFactoryId(Long factoryId) { this.factoryId = factoryId; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public boolean isAllowPartial() { return allowPartial; }
    public void setAllowPartial(boolean allowPartial) { this.allowPartial = allowPartial; }
}
