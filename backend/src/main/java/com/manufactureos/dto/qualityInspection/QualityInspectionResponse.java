package com.manufactureos.dto.qualityInspection;

import com.manufactureos.entity.InspectionResult;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;

public class QualityInspectionResponse {

    private Long id;

    private String inspectionNumber;

    private Long productionOrderId;

    private Long productId;

    private Long outputId;

    private Long batchId;

    private Long inspectorId;

    private Instant inspectedAt;

    private Integer sampleSize;

    private BigDecimal passedQuantity;

    private BigDecimal failedQuantity;

    private InspectionResult result;

    private String measurements;

    private String notes;

    private Boolean finalized;

    private Boolean rework;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getInspectionNumber() { return inspectionNumber; }
    public void setInspectionNumber(String inspectionNumber) { this.inspectionNumber = inspectionNumber; }
    public Long getProductionOrderId() { return productionOrderId; }
    public void setProductionOrderId(Long productionOrderId) { this.productionOrderId = productionOrderId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Long getOutputId() { return outputId; }
    public void setOutputId(Long outputId) { this.outputId = outputId; }
    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }
    public Long getInspectorId() { return inspectorId; }
    public void setInspectorId(Long inspectorId) { this.inspectorId = inspectorId; }
    public Instant getInspectedAt() { return inspectedAt; }
    public void setInspectedAt(Instant inspectedAt) { this.inspectedAt = inspectedAt; }
    public Integer getSampleSize() { return sampleSize; }
    public void setSampleSize(Integer sampleSize) { this.sampleSize = sampleSize; }
    public BigDecimal getPassedQuantity() { return passedQuantity; }
    public void setPassedQuantity(BigDecimal passedQuantity) { this.passedQuantity = passedQuantity; }
    public BigDecimal getFailedQuantity() { return failedQuantity; }
    public void setFailedQuantity(BigDecimal failedQuantity) { this.failedQuantity = failedQuantity; }
    public InspectionResult getResult() { return result; }
    public void setResult(InspectionResult result) { this.result = result; }
    public String getMeasurements() { return measurements; }
    public void setMeasurements(String measurements) { this.measurements = measurements; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Boolean getFinalized() { return finalized; }
    public void setFinalized(Boolean finalized) { this.finalized = finalized; }
    public Boolean getRework() { return rework; }
    public void setRework(Boolean rework) { this.rework = rework; }
}
