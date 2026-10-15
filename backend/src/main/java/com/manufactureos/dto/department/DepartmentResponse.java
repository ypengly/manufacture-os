package com.manufactureos.dto.department;

import jakarta.validation.constraints.*;

public class DepartmentResponse {

    private Long id;

    private Long factoryId;

    private String name;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getFactoryId() { return factoryId; }
    public void setFactoryId(Long factoryId) { this.factoryId = factoryId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
