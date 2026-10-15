package com.manufactureos.dto.qualityDefect;

import com.manufactureos.entity.DefectStatus;
import com.manufactureos.entity.Severity;
import jakarta.validation.constraints.*;
import java.time.Instant;

public class QualityDefectResponse {

    private Long id;

    private String defectCode;

    private String defectType;

    private String description;

    private Severity severity;

    private Long productionOrderId;

    private Long productId;

    private Long batchId;

    private Long reportedBy;

    private Instant createdAt;

    private String correctiveAction;

    private DefectStatus status;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDefectCode() { return defectCode; }
    public void setDefectCode(String defectCode) { this.defectCode = defectCode; }
    public String getDefectType() { return defectType; }
    public void setDefectType(String defectType) { this.defectType = defectType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }
    public Long getProductionOrderId() { return productionOrderId; }
    public void setProductionOrderId(Long productionOrderId) { this.productionOrderId = productionOrderId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }
    public Long getReportedBy() { return reportedBy; }
    public void setReportedBy(Long reportedBy) { this.reportedBy = reportedBy; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public String getCorrectiveAction() { return correctiveAction; }
    public void setCorrectiveAction(String correctiveAction) { this.correctiveAction = correctiveAction; }
    public DefectStatus getStatus() { return status; }
    public void setStatus(DefectStatus status) { this.status = status; }
}
