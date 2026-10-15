package com.manufactureos.dto.productionOutput;

import com.manufactureos.entity.OutputStatus;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;

public class ProductionOutputRequest {

    private Long productionOrderId;

    private Long productId;

    private Long batchId;

    private Long reportId;

    private Long factoryId;

    private Long locationId;

    private BigDecimal quantity;

    private BigDecimal releasedQuantity;

    private BigDecimal rejectedQuantity;

    private BigDecimal pendingDispositionQuantity;

    private OutputStatus status;

    private Instant producedAt;

    private Long version;

    public Long getProductionOrderId() { return productionOrderId; }
    public void setProductionOrderId(Long productionOrderId) { this.productionOrderId = productionOrderId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }
    public Long getReportId() { return reportId; }
    public void setReportId(Long reportId) { this.reportId = reportId; }
    public Long getFactoryId() { return factoryId; }
    public void setFactoryId(Long factoryId) { this.factoryId = factoryId; }
    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public BigDecimal getReleasedQuantity() { return releasedQuantity; }
    public void setReleasedQuantity(BigDecimal releasedQuantity) { this.releasedQuantity = releasedQuantity; }
    public BigDecimal getRejectedQuantity() { return rejectedQuantity; }
    public void setRejectedQuantity(BigDecimal rejectedQuantity) { this.rejectedQuantity = rejectedQuantity; }
    public BigDecimal getPendingDispositionQuantity() { return pendingDispositionQuantity; }
    public void setPendingDispositionQuantity(BigDecimal pendingDispositionQuantity) { this.pendingDispositionQuantity = pendingDispositionQuantity; }
    public OutputStatus getStatus() { return status; }
    public void setStatus(OutputStatus status) { this.status = status; }
    public Instant getProducedAt() { return producedAt; }
    public void setProducedAt(Instant producedAt) { this.producedAt = producedAt; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
