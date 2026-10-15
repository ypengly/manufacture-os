package com.manufactureos.dto.qualityOps;

import com.manufactureos.entity.Severity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class DefectRequest {

    @NotBlank
    private String defectType;

    private String description;

    @NotNull
    private Severity severity;

    private Long productionOrderId;

    private Long productId;

    private Long batchId;

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
}
