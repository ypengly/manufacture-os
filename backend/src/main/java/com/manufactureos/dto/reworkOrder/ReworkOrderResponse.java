package com.manufactureos.dto.reworkOrder;

import com.manufactureos.entity.ReworkStatus;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;

public class ReworkOrderResponse {

    private Long id;

    private String reworkNumber;

    private Long outputId;

    private Long productionOrderId;

    private BigDecimal quantity;

    private BigDecimal passedQuantity;

    private ReworkStatus status;

    private Long assignedWorkerId;

    private Long machineId;

    private String notes;

    private String outcome;

    private String additionalMaterials;

    private Long createdBy;

    private Instant createdAt;

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
