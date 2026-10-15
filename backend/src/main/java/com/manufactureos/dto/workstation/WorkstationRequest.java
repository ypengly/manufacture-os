package com.manufactureos.dto.workstation;

import jakarta.validation.constraints.*;

public class WorkstationRequest {

    @NotNull
    private Long lineId;

    @NotBlank
    @Size(max = 120)
    private String name;

    public Long getLineId() { return lineId; }
    public void setLineId(Long lineId) { this.lineId = lineId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
