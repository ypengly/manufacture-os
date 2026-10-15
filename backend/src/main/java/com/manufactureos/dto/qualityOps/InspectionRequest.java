package com.manufactureos.dto.qualityOps;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class InspectionRequest {

    @NotNull
    private Long outputId;

    @Min(0)
    private Integer sampleSize;

    @NotNull
    private BigDecimal passedQuantity;

    @NotNull
    private BigDecimal failedQuantity;

    private String measurements;

    private String notes;

    public Long getOutputId() { return outputId; }
    public void setOutputId(Long outputId) { this.outputId = outputId; }

    public Integer getSampleSize() { return sampleSize; }
    public void setSampleSize(Integer sampleSize) { this.sampleSize = sampleSize; }

    public BigDecimal getPassedQuantity() { return passedQuantity; }
    public void setPassedQuantity(BigDecimal passedQuantity) { this.passedQuantity = passedQuantity; }

    public BigDecimal getFailedQuantity() { return failedQuantity; }
    public void setFailedQuantity(BigDecimal failedQuantity) { this.failedQuantity = failedQuantity; }

    public String getMeasurements() { return measurements; }
    public void setMeasurements(String measurements) { this.measurements = measurements; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
