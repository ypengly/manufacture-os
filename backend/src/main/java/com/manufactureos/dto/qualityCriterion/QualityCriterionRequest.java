package com.manufactureos.dto.qualityCriterion;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class QualityCriterionRequest {

    @NotNull
    private Long productId;

    @NotBlank
    @Size(max = 120)
    private String name;

    private BigDecimal minValue;

    private BigDecimal maxValue;

    @Size(max = 20)
    private String unit;

    private Boolean mandatory;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getMinValue() { return minValue; }
    public void setMinValue(BigDecimal minValue) { this.minValue = minValue; }
    public BigDecimal getMaxValue() { return maxValue; }
    public void setMaxValue(BigDecimal maxValue) { this.maxValue = maxValue; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public Boolean getMandatory() { return mandatory; }
    public void setMandatory(Boolean mandatory) { this.mandatory = mandatory; }
}
