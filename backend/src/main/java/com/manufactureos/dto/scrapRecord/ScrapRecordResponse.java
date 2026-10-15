package com.manufactureos.dto.scrapRecord;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;

public class ScrapRecordResponse {

    private Long id;

    private String scrapNumber;

    private Long productionOrderId;

    private Long inventoryId;

    private Long batchId;

    private Long outputId;

    private BigDecimal quantity;

    private String reason;

    private Long approvedBy;

    private Long createdBy;

    private Instant createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getScrapNumber() { return scrapNumber; }
    public void setScrapNumber(String scrapNumber) { this.scrapNumber = scrapNumber; }
    public Long getProductionOrderId() { return productionOrderId; }
    public void setProductionOrderId(Long productionOrderId) { this.productionOrderId = productionOrderId; }
    public Long getInventoryId() { return inventoryId; }
    public void setInventoryId(Long inventoryId) { this.inventoryId = inventoryId; }
    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }
    public Long getOutputId() { return outputId; }
    public void setOutputId(Long outputId) { this.outputId = outputId; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public Long getApprovedBy() { return approvedBy; }
    public void setApprovedBy(Long approvedBy) { this.approvedBy = approvedBy; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
