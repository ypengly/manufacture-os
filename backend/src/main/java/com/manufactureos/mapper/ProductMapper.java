package com.manufactureos.mapper;

import com.manufactureos.entity.Product;
import com.manufactureos.dto.product.ProductRequest;
import com.manufactureos.dto.product.ProductResponse;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductResponse toResponse(Product e) {
        ProductResponse r = new ProductResponse();
        r.setId(e.getId());
        r.setCode(e.getCode());
        r.setSku(e.getSku());
        r.setName(e.getName());
        r.setDescription(e.getDescription());
        r.setCategory(e.getCategory());
        r.setVersion(e.getVersion());
        r.setUnitOfMeasure(e.getUnitOfMeasure());
        r.setStandardMinutes(e.getStandardMinutes());
        r.setStandardCost(e.getStandardCost());
        r.setSellingPrice(e.getSellingPrice());
        r.setTargetQuantity(e.getTargetQuantity());
        r.setActive(e.getActive());
        return r;
    }

    public void applyToEntity(ProductRequest req, Product e) {
        e.setCode(req.getCode());
        e.setSku(req.getSku());
        e.setName(req.getName());
        e.setDescription(req.getDescription());
        e.setCategory(req.getCategory());
        e.setVersion(req.getVersion());
        e.setUnitOfMeasure(req.getUnitOfMeasure());
        e.setStandardMinutes(req.getStandardMinutes());
        e.setStandardCost(req.getStandardCost());
        e.setSellingPrice(req.getSellingPrice());
        e.setTargetQuantity(req.getTargetQuantity());
        e.setActive(req.getActive());
    }
}
