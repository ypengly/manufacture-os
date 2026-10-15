package com.manufactureos.dto.productionOrderItem;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class ProductionOrderItemResponse {

    private Long id;

    private Long productionOrderId;

    private Long materialId;

    private BigDecimal requiredQuantity;

    private BigDecimal reservedQuantity;

    private BigDecimal consumedQuantity;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProductionOrderId() { return productionOrderId; }
    public void setProductionOrderId(Long productionOrderId) { this.productionOrderId = productionOrderId; }
    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }
    public BigDecimal getRequiredQuantity() { return requiredQuantity; }
    public void setRequiredQuantity(BigDecimal requiredQuantity) { this.requiredQuantity = requiredQuantity; }
    public BigDecimal getReservedQuantity() { return reservedQuantity; }
    public void setReservedQuantity(BigDecimal reservedQuantity) { this.reservedQuantity = reservedQuantity; }
    public BigDecimal getConsumedQuantity() { return consumedQuantity; }
    public void setConsumedQuantity(BigDecimal consumedQuantity) { this.consumedQuantity = consumedQuantity; }
}
