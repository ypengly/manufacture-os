package com.manufactureos.dto.productionOrderOps;

import com.manufactureos.entity.Priority;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

public class ProductionOrderCreateRequest {

    @NotNull
    private Long productId;

    /** Optional - when omitted, the product's current ACTIVE BOM is used. */
    private Long bomId;

    @NotNull
    @DecimalMin(value = "0.0001")
    private BigDecimal plannedQuantity;

    @NotNull
    private Long factoryId;

    private Long lineId;

    private Priority priority = Priority.NORMAL;

    @NotNull
    private Instant plannedStart;

    @NotNull
    private Instant plannedEnd;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Long getBomId() { return bomId; }
    public void setBomId(Long bomId) { this.bomId = bomId; }
    public BigDecimal getPlannedQuantity() { return plannedQuantity; }
    public void setPlannedQuantity(BigDecimal plannedQuantity) { this.plannedQuantity = plannedQuantity; }
    public Long getFactoryId() { return factoryId; }
    public void setFactoryId(Long factoryId) { this.factoryId = factoryId; }
    public Long getLineId() { return lineId; }
    public void setLineId(Long lineId) { this.lineId = lineId; }
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public Instant getPlannedStart() { return plannedStart; }
    public void setPlannedStart(Instant plannedStart) { this.plannedStart = plannedStart; }
    public Instant getPlannedEnd() { return plannedEnd; }
    public void setPlannedEnd(Instant plannedEnd) { this.plannedEnd = plannedEnd; }
}
