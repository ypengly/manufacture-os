package com.manufactureos.dto.inventoryOps;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class AdjustmentRequest {

    @NotNull
    private Long inventoryId;

    /** Positive to add stock, negative to remove it. */
    @NotNull
    private BigDecimal deltaQuantity;

    @NotBlank
    private String reason;

    public Long getInventoryId() { return inventoryId; }
    public void setInventoryId(Long inventoryId) { this.inventoryId = inventoryId; }
    public BigDecimal getDeltaQuantity() { return deltaQuantity; }
    public void setDeltaQuantity(BigDecimal deltaQuantity) { this.deltaQuantity = deltaQuantity; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
