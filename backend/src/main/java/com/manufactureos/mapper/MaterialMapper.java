package com.manufactureos.mapper;

import com.manufactureos.entity.Material;
import com.manufactureos.dto.material.MaterialRequest;
import com.manufactureos.dto.material.MaterialResponse;
import org.springframework.stereotype.Component;

@Component
public class MaterialMapper {

    public MaterialResponse toResponse(Material e) {
        MaterialResponse r = new MaterialResponse();
        r.setId(e.getId());
        r.setCode(e.getCode());
        r.setName(e.getName());
        r.setDescription(e.getDescription());
        r.setCategory(e.getCategory());
        r.setUnitOfMeasure(e.getUnitOfMeasure());
        r.setMinStockLevel(e.getMinStockLevel());
        r.setReorderLevel(e.getReorderLevel());
        r.setStandardCost(e.getStandardCost());
        r.setSupplierId(e.getSupplierId());
        r.setActive(e.getActive());
        return r;
    }

    public void applyToEntity(MaterialRequest req, Material e) {
        e.setCode(req.getCode());
        e.setName(req.getName());
        e.setDescription(req.getDescription());
        e.setCategory(req.getCategory());
        e.setUnitOfMeasure(req.getUnitOfMeasure());
        e.setMinStockLevel(req.getMinStockLevel());
        e.setReorderLevel(req.getReorderLevel());
        e.setStandardCost(req.getStandardCost());
        e.setSupplierId(req.getSupplierId());
        e.setActive(req.getActive());
    }
}
