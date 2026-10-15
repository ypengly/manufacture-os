package com.manufactureos.dto.purchaseOrderOps;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public class PurchaseOrderCreateRequest {

    @NotNull
    private Long supplierId;

    @NotNull
    private Long factoryId;

    private LocalDate expectedDate;

    @NotEmpty
    @Valid
    private List<PurchaseOrderItemInput> items;

    public Long getSupplierId() { return supplierId; }
    public void setSupplierId(Long supplierId) { this.supplierId = supplierId; }

    public Long getFactoryId() { return factoryId; }
    public void setFactoryId(Long factoryId) { this.factoryId = factoryId; }

    public LocalDate getExpectedDate() { return expectedDate; }
    public void setExpectedDate(LocalDate expectedDate) { this.expectedDate = expectedDate; }

    public List<PurchaseOrderItemInput> getItems() { return items; }
    public void setItems(List<PurchaseOrderItemInput> items) { this.items = items; }
}
