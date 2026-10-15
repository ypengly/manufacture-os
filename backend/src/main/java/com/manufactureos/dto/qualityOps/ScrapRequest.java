package com.manufactureos.dto.qualityOps;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class ScrapRequest {

    /** Scrap sourced from a production output's quarantined/rejected quantity (preferred for QC-driven scrap). */
    private Long outputId;

    /** Scrap sourced directly from an inventory row (e.g. damaged raw material found during a count). */
    private Long inventoryId;

    private Long productionOrderId;

    @NotNull
    @DecimalMin(value = "0.000001")
    private BigDecimal quantity;

    @NotBlank
    private String reason;

    public Long getOutputId() { return outputId; }
    public void setOutputId(Long outputId) { this.outputId = outputId; }

    public Long getInventoryId() { return inventoryId; }
    public void setInventoryId(Long inventoryId) { this.inventoryId = inventoryId; }

    public Long getProductionOrderId() { return productionOrderId; }
    public void setProductionOrderId(Long productionOrderId) { this.productionOrderId = productionOrderId; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
