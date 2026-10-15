package com.manufactureos.dto.department;

import jakarta.validation.constraints.*;

public class DepartmentRequest {

    @NotNull
    private Long factoryId;

    @NotBlank
    @Size(max = 120)
    private String name;

    public Long getFactoryId() { return factoryId; }
    public void setFactoryId(Long factoryId) { this.factoryId = factoryId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
