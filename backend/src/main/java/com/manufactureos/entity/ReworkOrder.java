package com.manufactureos.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "rework_orders")
public class ReworkOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rework_number")
    private String reworkNumber;

    @Column(name = "output_id")
    private Long outputId;

    @Column(name = "production_order_id")
    private Long productionOrderId;

    @Column(name = "quantity")
    private BigDecimal quantity;

    @Column(name = "passed_quantity")
    private BigDecimal passedQuantity = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ReworkStatus status = ReworkStatus.REQUESTED;

    @Column(name = "assigned_worker_id")
    private Long assignedWorkerId;

    @Column(name = "machine_id")
    private Long machineId;

    @Column(name = "notes")
    private String notes;

    @Column(name = "outcome")
    private String outcome;

    @Column(name = "additional_materials")
    private String additionalMaterials;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getReworkNumber() { return reworkNumber; }
    public void setReworkNumber(String reworkNumber) { this.reworkNumber = reworkNumber; }

    public Long getOutputId() { return outputId; }
    public void setOutputId(Long outputId) { this.outputId = outputId; }

    public Long getProductionOrderId() { return productionOrderId; }
    public void setProductionOrderId(Long productionOrderId) { this.productionOrderId = productionOrderId; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getPassedQuantity() { return passedQuantity; }
    public void setPassedQuantity(BigDecimal passedQuantity) { this.passedQuantity = passedQuantity; }

    public ReworkStatus getStatus() { return status; }
    public void setStatus(ReworkStatus status) { this.status = status; }

    public Long getAssignedWorkerId() { return assignedWorkerId; }
    public void setAssignedWorkerId(Long assignedWorkerId) { this.assignedWorkerId = assignedWorkerId; }

    public Long getMachineId() { return machineId; }
    public void setMachineId(Long machineId) { this.machineId = machineId; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getOutcome() { return outcome; }
    public void setOutcome(String outcome) { this.outcome = outcome; }

    public String getAdditionalMaterials() { return additionalMaterials; }
    public void setAdditionalMaterials(String additionalMaterials) { this.additionalMaterials = additionalMaterials; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
