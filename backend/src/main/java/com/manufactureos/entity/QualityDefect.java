package com.manufactureos.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "quality_defects")
public class QualityDefect {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "defect_code")
    private String defectCode;

    @Column(name = "defect_type")
    private String defectType;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity")
    private Severity severity = Severity.LOW;

    @Column(name = "production_order_id")
    private Long productionOrderId;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "batch_id")
    private Long batchId;

    @Column(name = "reported_by")
    private Long reportedBy;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    @Column(name = "corrective_action")
    private String correctiveAction;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private DefectStatus status = DefectStatus.OPEN;

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
