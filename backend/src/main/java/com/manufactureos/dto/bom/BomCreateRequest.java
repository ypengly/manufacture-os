package com.manufactureos.dto.bom;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public class BomCreateRequest {

    @NotNull
    private Long productId;

    private LocalDate effectiveFrom;
    private LocalDate effectiveTo;

    @NotEmpty
    @Valid
    private List<BomComponentInput> components;

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public LocalDate getEffectiveFrom() { return effectiveFrom; }
    public void setEffectiveFrom(LocalDate effectiveFrom) { this.effectiveFrom = effectiveFrom; }
    public LocalDate getEffectiveTo() { return effectiveTo; }
    public void setEffectiveTo(LocalDate effectiveTo) { this.effectiveTo = effectiveTo; }
    public List<BomComponentInput> getComponents() { return components; }
    public void setComponents(List<BomComponentInput> components) { this.components = components; }
}
