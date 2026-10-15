package com.manufactureos.dto.inventoryOps;

import com.manufactureos.entity.InventoryCategory;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/** Inventory row enriched with its batch's material/product and expiry, for list views. */
public class InventoryView {

    private Long id;
    private Long factoryId;
    private Long locationId;
    private Long batchId;
    private String batchNumber;
    private Long materialId;
    private Long productId;
    private InventoryCategory category;
    private BigDecimal quantityOnHand;
    private BigDecimal reservedQuantity;
    private BigDecimal availableQuantity;
    private BigDecimal unitCost;
    private LocalDate expirationDate;
    private Instant updatedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getFactoryId() { return factoryId; }
    public void setFactoryId(Long factoryId) { this.factoryId = factoryId; }
    public Long getLocationId() { return locationId; }
    public void setLocationId(Long locationId) { this.locationId = locationId; }
    public Long getBatchId() { return batchId; }
    public void setBatchId(Long batchId) { this.batchId = batchId; }
    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }
    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public InventoryCategory getCategory() { return category; }
    public void setCategory(InventoryCategory category) { this.category = category; }
    public BigDecimal getQuantityOnHand() { return quantityOnHand; }
    public void setQuantityOnHand(BigDecimal quantityOnHand) { this.quantityOnHand = quantityOnHand; }
    public BigDecimal getReservedQuantity() { return reservedQuantity; }
    public void setReservedQuantity(BigDecimal reservedQuantity) { this.reservedQuantity = reservedQuantity; }
    public BigDecimal getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(BigDecimal availableQuantity) { this.availableQuantity = availableQuantity; }
    public BigDecimal getUnitCost() { return unitCost; }
    public void setUnitCost(BigDecimal unitCost) { this.unitCost = unitCost; }
    public LocalDate getExpirationDate() { return expirationDate; }
    public void setExpirationDate(LocalDate expirationDate) { this.expirationDate = expirationDate; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
