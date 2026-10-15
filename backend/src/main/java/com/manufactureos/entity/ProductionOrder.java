package com.manufactureos.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "production_orders")
public class ProductionOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number")
    private String orderNumber;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "bom_id")
    private Long bomId;

    @Column(name = "planned_quantity")
    private BigDecimal plannedQuantity;

    @Column(name = "completed_quantity")
    private BigDecimal completedQuantity = BigDecimal.ZERO;

    @Column(name = "rejected_quantity")
    private BigDecimal rejectedQuantity = BigDecimal.ZERO;

    @Column(name = "factory_id")
    private Long factoryId;

    @Column(name = "line_id")
    private Long lineId;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority")
    private Priority priority = Priority.NORMAL;

    @Column(name = "planned_start")
    private Instant plannedStart;

    @Column(name = "planned_end")
    private Instant plannedEnd;

    @Column(name = "actual_start")
    private Instant actualStart;

    @Column(name = "actual_end")
    private Instant actualEnd;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private OrderStatus status = OrderStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_before_hold")
    private OrderStatus statusBeforeHold;

    @Column(name = "allow_partial_materials")
    private Boolean allowPartialMaterials = false;

    @Column(name = "schedule_override_reason")
    private String scheduleOverrideReason;

    @Column(name = "status_reason")
    private String statusReason;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    @Version
    @Column(name = "version")
    private Long version;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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
