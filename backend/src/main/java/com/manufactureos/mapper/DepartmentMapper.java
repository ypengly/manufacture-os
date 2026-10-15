package com.manufactureos.mapper;

import com.manufactureos.entity.Department;
import com.manufactureos.dto.department.DepartmentRequest;
import com.manufactureos.dto.department.DepartmentResponse;
import org.springframework.stereotype.Component;

@Component
public class DepartmentMapper {

    public DepartmentResponse toResponse(Department e) {
        DepartmentResponse r = new DepartmentResponse();
        r.setId(e.getId());
        r.setFactoryId(e.getFactoryId());
        r.setName(e.getName());
        return r;
    }

    public void applyToEntity(DepartmentRequest req, Department e) {
        e.setFactoryId(req.getFactoryId());
        e.setName(req.getName());
    }
}
