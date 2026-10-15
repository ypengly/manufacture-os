package com.manufactureos.dto.inventoryOps;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class ConsumptionRequest {

    @NotNull
    private Long productionOrderId;

    @NotNull
    private Long materialId;

    @NotNull
    @DecimalMin(value = "0.0001")
    private BigDecimal quantity;

    /** Supplied by the caller so a retried request never double-consumes stock. */
    @NotBlank
    private String idempotencyKey;

    public Long getProductionOrderId() { return productionOrderId; }
    public void setProductionOrderId(Long productionOrderId) { this.productionOrderId = productionOrderId; }
    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
}
