package com.manufactureos.dto.inventoryOps;

import java.math.BigDecimal;

/** One line of an MRP check: how much of a material a planned quantity needs versus what's on hand. */
public class MaterialRequirementView {

    private Long materialId;
    private BigDecimal requiredQuantity;
    private BigDecimal availableQuantity;
    private BigDecimal shortageQuantity;

    public MaterialRequirementView() {}

    public MaterialRequirementView(Long materialId, BigDecimal requiredQuantity, BigDecimal availableQuantity) {
        this.materialId = materialId;
        this.requiredQuantity = requiredQuantity;
        this.availableQuantity = availableQuantity;
        BigDecimal shortage = requiredQuantity.subtract(availableQuantity);
        this.shortageQuantity = shortage.signum() > 0 ? shortage : BigDecimal.ZERO;
    }

    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }
    public BigDecimal getRequiredQuantity() { return requiredQuantity; }
    public void setRequiredQuantity(BigDecimal requiredQuantity) { this.requiredQuantity = requiredQuantity; }
    public BigDecimal getAvailableQuantity() { return availableQuantity; }
    public void setAvailableQuantity(BigDecimal availableQuantity) { this.availableQuantity = availableQuantity; }
    public BigDecimal getShortageQuantity() { return shortageQuantity; }
    public void setShortageQuantity(BigDecimal shortageQuantity) { this.shortageQuantity = shortageQuantity; }
}
