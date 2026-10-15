package com.manufactureos.dto.qualityOps;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class ReworkRequest {

    @NotNull
    private Long outputId;

    @NotNull
    @DecimalMin(value = "0.000001")
    private BigDecimal quantity;

    private Long assignedWorkerId;

    private Long machineId;

    private String notes;

    private String additionalMaterials;

    public Long getOutputId() { return outputId; }
    public void setOutputId(Long outputId) { this.outputId = outputId; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public Long getAssignedWorkerId() { return assignedWorkerId; }
    public void setAssignedWorkerId(Long assignedWorkerId) { this.assignedWorkerId = assignedWorkerId; }

    public Long getMachineId() { return machineId; }
    public void setMachineId(Long machineId) { this.machineId = machineId; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getAdditionalMaterials() { return additionalMaterials; }
    public void setAdditionalMaterials(String additionalMaterials) { this.additionalMaterials = additionalMaterials; }
}
