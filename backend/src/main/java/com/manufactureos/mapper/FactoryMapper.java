package com.manufactureos.mapper;

import com.manufactureos.entity.Factory;
import com.manufactureos.dto.factory.FactoryRequest;
import com.manufactureos.dto.factory.FactoryResponse;
import org.springframework.stereotype.Component;

@Component
public class FactoryMapper {

    public FactoryResponse toResponse(Factory e) {
        FactoryResponse r = new FactoryResponse();
        r.setId(e.getId());
        r.setCode(e.getCode());
        r.setName(e.getName());
        r.setAddress(e.getAddress());
        r.setContactDetails(e.getContactDetails());
        r.setManagerName(e.getManagerName());
        r.setStatus(e.getStatus());
        r.setTimeZone(e.getTimeZone());
        r.setCreatedAt(e.getCreatedAt());
        return r;
    }

    public void applyToEntity(FactoryRequest req, Factory e) {
        e.setCode(req.getCode());
        e.setName(req.getName());
        e.setAddress(req.getAddress());
        e.setContactDetails(req.getContactDetails());
        e.setManagerName(req.getManagerName());
        e.setStatus(req.getStatus());
        e.setTimeZone(req.getTimeZone());
    }
}
