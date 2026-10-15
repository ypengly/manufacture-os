package com.manufactureos.dto.schedulingOps;

import java.math.BigDecimal;

public class CompleteMaintenanceRequest {

    private String notes;

    private BigDecimal cost;

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public BigDecimal getCost() { return cost; }
    public void setCost(BigDecimal cost) { this.cost = cost; }
}
