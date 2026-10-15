package com.manufactureos.dto.materialReservation;

import com.manufactureos.entity.ReservationStatus;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;

public class MaterialReservationResponse {

    private Long id;

    private Long productionOrderId;

    private Long inventoryId;

    private Long materialId;

    private BigDecimal quantity;

    private BigDecimal consumedQuantity;

    private ReservationStatus status;

    private Instant createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getProductionOrderId() { return productionOrderId; }
    public void setProductionOrderId(Long productionOrderId) { this.productionOrderId = productionOrderId; }
    public Long getInventoryId() { return inventoryId; }
    public void setInventoryId(Long inventoryId) { this.inventoryId = inventoryId; }
    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public BigDecimal getConsumedQuantity() { return consumedQuantity; }
    public void setConsumedQuantity(BigDecimal consumedQuantity) { this.consumedQuantity = consumedQuantity; }
    public ReservationStatus getStatus() { return status; }
    public void setStatus(ReservationStatus status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
