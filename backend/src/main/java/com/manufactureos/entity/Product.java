package com.manufactureos.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, length = 40)
    private String code;

    @Column(name = "sku", nullable = false, length = 40)
    private String sku;

    @Column(name = "name", nullable = false, length = 160)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "category", length = 80)
    private String category;

    @Column(name = "version")
    private Integer version = 1;

    @Column(name = "unit_of_measure", length = 20)
    private String unitOfMeasure = "EA";

    @Column(name = "standard_minutes")
    private BigDecimal standardMinutes = BigDecimal.ZERO;

    @Column(name = "standard_cost")
    private BigDecimal standardCost = BigDecimal.ZERO;

    @Column(name = "selling_price")
    private BigDecimal sellingPrice = BigDecimal.ZERO;

    @Column(name = "target_quantity")
    private BigDecimal targetQuantity = BigDecimal.ZERO;

    @Column(name = "active")
    private Boolean active = true;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public String getUnitOfMeasure() { return unitOfMeasure; }
    public void setUnitOfMeasure(String unitOfMeasure) { this.unitOfMeasure = unitOfMeasure; }

    public BigDecimal getStandardMinutes() { return standardMinutes; }
    public void setStandardMinutes(BigDecimal standardMinutes) { this.standardMinutes = standardMinutes; }

    public BigDecimal getStandardCost() { return standardCost; }
    public void setStandardCost(BigDecimal standardCost) { this.standardCost = standardCost; }

    public BigDecimal getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(BigDecimal sellingPrice) { this.sellingPrice = sellingPrice; }

    public BigDecimal getTargetQuantity() { return targetQuantity; }
    public void setTargetQuantity(BigDecimal targetQuantity) { this.targetQuantity = targetQuantity; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
