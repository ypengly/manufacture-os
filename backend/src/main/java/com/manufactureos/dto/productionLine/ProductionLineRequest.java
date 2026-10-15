package com.manufactureos.dto.productionLine;

import jakarta.validation.constraints.*;

public class ProductionLineRequest {

    @NotNull
    private Long factoryId;

    private Long departmentId;

    @NotBlank
    @Size(max = 30)
    private String code;

    @NotBlank
    @Size(max = 120)
    private String name;

    private Boolean active;

    public Long getFactoryId() { return factoryId; }
    public void setFactoryId(Long factoryId) { this.factoryId = factoryId; }
    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
