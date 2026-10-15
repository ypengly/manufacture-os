package com.manufactureos.dto.workstation;

import jakarta.validation.constraints.*;

public class WorkstationResponse {

    private Long id;

    private Long lineId;

    private String name;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getLineId() { return lineId; }
    public void setLineId(Long lineId) { this.lineId = lineId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
