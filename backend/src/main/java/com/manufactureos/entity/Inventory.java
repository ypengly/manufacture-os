package com.manufactureos.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "inventory")
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "factory_id")
    private Long factoryId;

    @Column(name = "location_id")
    private Long locationId;

    @Column(name = "batch_id")
    private Long batchId;

    @Enumerated(EnumType.STRING)
    @Column(name = "category")
    private InventoryCategory category;

    @Column(name = "quantity_on_hand")
    private BigDecimal quantityOnHand = BigDecimal.ZERO;

    @Column(name = "reserved_quantity")
    private BigDecimal reservedQuantity = BigDecimal.ZERO;

    @Column(name = "damaged_quantity")
    private BigDecimal damagedQuantity = BigDecimal.ZERO;

    @Column(name = "unit_cost")
    private BigDecimal unitCost = BigDecimal.ZERO;

    @Column(name = "updated_at")
    private Instant updatedAt = Instant.now();

    @Version
    @Column(name = "version")
    private Long version;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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
