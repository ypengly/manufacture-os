package com.manufactureos.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "bom_components")
public class BomComponent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bom_id")
    private Long bomId;

    @Column(name = "material_id")
    private Long materialId;

    @Column(name = "quantity")
    private BigDecimal quantity;

    @Column(name = "waste_percent")
    private BigDecimal wastePercent = BigDecimal.ZERO;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getBomId() { return bomId; }
    public void setBomId(Long bomId) { this.bomId = bomId; }

    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getWastePercent() { return wastePercent; }
    public void setWastePercent(BigDecimal wastePercent) { this.wastePercent = wastePercent; }
}
