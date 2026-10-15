package com.manufactureos.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "materials")
public class Material {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, length = 40)
    private String code;

    @Column(name = "name", nullable = false, length = 160)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "category", length = 80)
    private String category;

    @Column(name = "unit_of_measure", length = 20)
    private String unitOfMeasure = "EA";

    @Column(name = "min_stock_level")
    private BigDecimal minStockLevel = BigDecimal.ZERO;

    @Column(name = "reorder_level")
    private BigDecimal reorderLevel = BigDecimal.ZERO;

    @Column(name = "standard_cost")
    private BigDecimal standardCost = BigDecimal.ZERO;

    @Column(name = "supplier_id")
    private Long supplierId;

    @Column(name = "active")
    private Boolean active = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getUnitOfMeasure() { return unitOfMeasure; }
    public void setUnitOfMeasure(String unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; }

    public BigDecimal getMinStockLevel() { return minStockLevel; }
    public void setMinStockLevel(BigDecimal minStockLevel) { this.minStockLevel = minStockLevel; }

    public BigDecimal getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(BigDecimal reorderLevel) { this.reorderLevel = reorderLevel; }

    public BigDecimal getStandardCost() { return standardCost; }
    public void setStandardCost(BigDecimal standardCost) { this.standardCost = standardCost; }

    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
