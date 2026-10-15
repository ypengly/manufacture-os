package com.manufactureos.dto.qualityOps;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class ReworkCompleteRequest {

    @NotNull
    private BigDecimal passedQuantity;

    private String outcome;

    public BigDecimal getPassedQuantity() { return passedQuantity; }
    public void setPassedQuantity(BigDecimal passedQuantity) { this.passedQuantity = passedQuantity; }

    public String getOutcome() { return outcome; }
    public void setOutcome(String outcome) { this.outcome = outcome; }
}
