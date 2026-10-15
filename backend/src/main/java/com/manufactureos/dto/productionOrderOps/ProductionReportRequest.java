package com.manufactureos.dto.productionOrderOps;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public class ProductionReportRequest {

    /** Storage location the completed quantity is placed into, pending inspection. */
    private Long locationId;

    @DecimalMin(value = "0")
    private BigDecimal completedQuantity = BigDecimal.ZERO;

    @DecimalMin(value = "0")
    private BigDecimal rejectedQuantity = BigDecimal.ZERO;

    @PositiveOrZero
    private int downtimeMinutes = 0;

    private String notes;

    @NotBlank
    private String idempotencyKey;

    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }
    public BigDecimal getCompletedQuantity() { return completedQuantity; }
    public void setCompletedQuantity(BigDecimal completedQuantity) { this.completedQuantity = completedQuantity; }
    public BigDecimal getRejectedQuantity() { return rejectedQuantity; }
    public void setRejectedQuantity(BigDecimal rejectedQuantity) { this.rejectedQuantity = rejectedQuantity; }
    public int getDowntimeMinutes() { return downtimeMinutes; }
    public void setDowntimeMinutes(int downtimeMinutes) { this.downtimeMinutes = downtimeMinutes; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public void setIdempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; }
}
