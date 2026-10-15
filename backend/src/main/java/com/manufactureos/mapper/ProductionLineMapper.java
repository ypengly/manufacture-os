package com.manufactureos.mapper;

import com.manufactureos.entity.ProductionLine;
import com.manufactureos.dto.productionLine.ProductionLineRequest;
import com.manufactureos.dto.productionLine.ProductionLineResponse;
import org.springframework.stereotype.Component;

@Component
public class ProductionLineMapper {

    public ProductionLineResponse toResponse(ProductionLine e) {
        ProductionLineResponse r = new ProductionLineResponse();
        r.setId(e.getId());
        r.setFactoryId(e.getFactoryId());
        r.setDepartmentId(e.getDepartmentId());
        r.setCode(e.getCode());
        r.setName(e.getName());
        r.setActive(e.getActive());
        return r;
    }

    public void applyToEntity(ProductionLineRequest req, ProductionLine e) {
        e.setFactoryId(req.getFactoryId());
        e.setDepartmentId(req.getDepartmentId());
        e.setCode(req.getCode());
        e.setName(req.getName());
        e.setActive(req.getActive());
    }
}
