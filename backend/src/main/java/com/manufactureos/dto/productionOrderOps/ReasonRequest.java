package com.manufactureos.dto.productionOrderOps;

import jakarta.validation.constraints.NotBlank;

/** Generic {reason} body shared by hold / cancel / override endpoints. */
public class ReasonRequest {

    @NotBlank
    private String reason;

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
