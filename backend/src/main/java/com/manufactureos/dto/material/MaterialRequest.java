package com.manufactureos.dto.material;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class MaterialRequest {

    @NotBlank
    @Size(max = 40)
    private String code;

    @NotBlank
    @Size(max = 160)
    private String name;

    private String description;

    @Size(max = 80)
    private String category;

    @Size(max = 20)
    private String unitOfMeasure;

    @DecimalMin(value = "0")
    private BigDecimal minStockLevel;

    @DecimalMin(value = "0")
    private BigDecimal reorderLevel;

    @DecimalMin(value = "0")
    private BigDecimal standardCost;

    private Long supplierId;

    private Boolean active;

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
