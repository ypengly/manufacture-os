package com.manufactureos.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "production_order_items")
public class ProductionOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "production_order_id")
    private Long productionOrderId;

    @Column(name = "material_id")
    private Long materialId;

    @Column(name = "required_quantity")
    private BigDecimal requiredQuantity;

    @Column(name = "reserved_quantity")
    private BigDecimal reservedQuantity = BigDecimal.ZERO;

    @Column(name = "consumed_quantity")
    private BigDecimal consumedQuantity = BigDecimal.ZERO;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getProductionOrderId() { return productionOrderId; }
    public void setProductionOrderId(Long productionOrderId) { this.productionOrderId = productionOrderId; }

    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }

    public BigDecimal getRequiredQuantity() { return requiredQuantity; }
    public void setRequiredQuantity(BigDecimal requiredQuantity) { this.requiredQuantity = requiredQuantity; }

    public BigDecimal getReservedQuantity() { return reservedQuantity; }
    public void setReservedQuantity(BigDecimal reservedQuantity) { this.reservedQuantity = reservedQuantity; }

    public BigDecimal getConsumedQuantity() { return consumedQuantity; }
    public void setConsumedQuantity(BigDecimal consumedQuantity) { this.consumedQuantity = consumedQuantity; }
}
