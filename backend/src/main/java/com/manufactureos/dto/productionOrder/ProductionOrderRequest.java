package com.manufactureos.dto.productionOrder;

import com.manufactureos.entity.OrderStatus;
import com.manufactureos.entity.Priority;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;

public class ProductionOrderRequest {

    private String orderNumber;

    private Long productId;

    private Long bomId;

    private BigDecimal plannedQuantity;

    private BigDecimal completedQuantity;

    private BigDecimal rejectedQuantity;

    private Long factoryId;

    private Long lineId;

    private Priority priority;

    private Instant plannedStart;

    private Instant plannedEnd;

    private Instant actualStart;

    private Instant actualEnd;

    private OrderStatus status;

    private OrderStatus statusBeforeHold;

    private Boolean allowPartialMaterials;

    private String scheduleOverrideReason;

    private String statusReason;

    private Long createdBy;

    private Long approvedBy;

    private Instant createdAt;

    private Long version;

    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Long getBomId() { return bomId; }
    public void setBomId(Long bomId) { this.bomId = bomId; }
    public BigDecimal getPlannedQuantity() { return plannedQuantity; }
    public void setPlannedQuantity(BigDecimal plannedQuantity) { this.plannedQuantity = plannedQuantity; }
    public BigDecimal getCompletedQuantity() { return completedQuantity; }
    public void setCompletedQuantity(BigDecimal completedQuantity) { this.completedQuantity = completedQuantity; }
    public BigDecimal getRejectedQuantity() { return rejectedQuantity; }
    public void setRejectedQuantity(BigDecimal rejectedQuantity) { this.rejectedQuantity = rejectedQuantity; }
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
    public Instant getActualStart() { return actualStart; }
    public void setActualStart(Instant actualStart) { this.actualStart = actualStart; }
    public Instant getActualEnd() { return actualEnd; }
    public void setActualEnd(Instant actualEnd) { this.actualEnd = actualEnd; }
    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
    public OrderStatus getStatusBeforeHold() { return statusBeforeHold; }
    public void setStatusBeforeHold(OrderStatus statusBeforeHold) { this.statusBeforeHold = statusBeforeHold; }
    public Boolean getAllowPartialMaterials() { return allowPartialMaterials; }
    public void setAllowPartialMaterials(Boolean allowPartialMaterials) { this.allowPartialMaterials = allowPartialMaterials; }
    public String getScheduleOverrideReason() { return scheduleOverrideReason; }
    public void setScheduleOverrideReason(String scheduleOverrideReason) { this.scheduleOverrideReason = scheduleOverrideReason; }
    public String getStatusReason() { return statusReason; }
    public void setStatusReason(String statusReason) { this.statusReason = statusReason; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public Long getApprovedBy() { return approvedBy; }
    public void setApprovedBy(Long approvedBy) { this.approvedBy = approvedBy; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
