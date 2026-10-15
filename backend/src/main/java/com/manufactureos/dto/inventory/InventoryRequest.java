package com.manufactureos.dto.inventory;

import com.manufactureos.entity.InventoryCategory;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.Instant;

public class InventoryRequest {

    private Long factoryId;

    private Long locationId;

    private Long batchId;

    private InventoryCategory category;

    private BigDecimal quantityOnHand;

    private BigDecimal reservedQuantity;

    private BigDecimal damagedQuantity;

    private BigDecimal unitCost;

    private Instant updatedAt;

    private Long version;

    public Long getFactoryId() { return factoryId; }
    public void setFactoryId(Long factoryId) { this.factoryId = factoryId; }
    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }
    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }
    public InventoryCategory getCategory() { return category; }
    public void setCategory(InventoryCategory category) { this.category = category; }
    public BigDecimal getQuantityOnHand() { return quantityOnHand; }
    public void setQuantityOnHand(BigDecimal quantityOnHand) { this.quantityOnHand = quantityOnHand; }
    public BigDecimal getReservedQuantity() { return reservedQuantity; }
    public void setReservedQuantity(BigDecimal reservedQuantity) { this.reservedQuantity = reservedQuantity; }
    public BigDecimal getDamagedQuantity() { return damagedQuantity; }
    public void setDamagedQuantity(BigDecimal damagedQuantity) { this.damagedQuantity = damagedQuantity; }
    public BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
