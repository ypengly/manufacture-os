package com.manufactureos.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "production_outputs")
public class ProductionOutput {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "production_order_id")
    private Long productionOrderId;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "batch_id")
    private Long batchId;

    @Column(name = "report_id")
    private Long reportId;

    @Column(name = "factory_id")
    private Long factoryId;

    @Column(name = "location_id")
    private Long locationId;

    @Column(name = "quantity")
    private BigDecimal quantity;

    @Column(name = "released_quantity")
    private BigDecimal releasedQuantity = BigDecimal.ZERO;

    @Column(name = "rejected_quantity")
    private BigDecimal rejectedQuantity = BigDecimal.ZERO;

    @Column(name = "pending_disposition_quantity")
    private BigDecimal pendingDispositionQuantity = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private OutputStatus status = OutputStatus.PENDING_INSPECTION;

    @Column(name = "produced_at")
    private Instant producedAt = Instant.now();

    @Version
    @Column(name = "version")
    private Long version;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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
