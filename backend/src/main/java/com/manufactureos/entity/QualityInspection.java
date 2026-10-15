package com.manufactureos.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "quality_inspections")
public class QualityInspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "inspection_number")
    private String inspectionNumber;

    @Column(name = "production_order_id")
    private Long productionOrderId;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "output_id")
    private Long outputId;

    @Column(name = "batch_id")
    private Long batchId;

    @Column(name = "inspector_id")
    private Long inspectorId;

    @Column(name = "inspected_at")
    private Instant inspectedAt = Instant.now();

    @Column(name = "sample_size")
    private Integer sampleSize = 0;

    @Column(name = "passed_quantity")
    private BigDecimal passedQuantity = BigDecimal.ZERO;

    @Column(name = "failed_quantity")
    private BigDecimal failedQuantity = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "result")
    private InspectionResult result = InspectionResult.PENDING;

    @Column(name = "measurements")
    private String measurements;

    @Column(name = "notes")
    private String notes;

    @Column(name = "finalized")
    private Boolean finalized = false;

    @Column(name = "rework")
    private Boolean rework = false;

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
