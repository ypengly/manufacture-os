package com.manufactureos.dto.purchaseOrderOps;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class ReceivePurchaseOrderRequest {

    @NotEmpty
    @Valid
    private List<ReceiveLineRequest> lines;

    public List<ReceiveLineRequest> getLines() { return lines; }
    public void setLines(List<ReceiveLineRequest> lines) { this.lines = lines; }
}
