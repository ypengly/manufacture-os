package com.manufactureos.dto.product;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class ProductRequest {

    @NotBlank
    @Size(max = 40)
    private String code;

    @NotBlank
    @Size(max = 40)
    private String sku;

    @NotBlank
    @Size(max = 160)
    private String name;

    private String description;

    @Size(max = 80)
    private String category;

    @Positive
    private Integer version;

    @Size(max = 20)
    private String unitOfMeasure;

    @DecimalMin(value = "0")
    private BigDecimal standardMinutes;

    @DecimalMin(value = "0")
    private BigDecimal standardCost;

    @DecimalMin(value = "0")
    private BigDecimal sellingPrice;

    @DecimalMin(value = "0")
    private BigDecimal targetQuantity;

    private Boolean active;

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
