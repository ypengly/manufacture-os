package com.manufactureos.mapper;

import com.manufactureos.entity.Supplier;
import com.manufactureos.dto.supplier.SupplierRequest;
import com.manufactureos.dto.supplier.SupplierResponse;
import org.springframework.stereotype.Component;

@Component
public class SupplierMapper {

    public SupplierResponse toResponse(Supplier e) {
        SupplierResponse r = new SupplierResponse();
        r.setId(e.getId());
        r.setCode(e.getCode());
        r.setCompanyName(e.getCompanyName());
        r.setContactPerson(e.getContactPerson());
        r.setEmail(e.getEmail());
        r.setPhone(e.getPhone());
        r.setAddress(e.getAddress());
        r.setStatus(e.getStatus());
        return r;
    }

    public void applyToEntity(SupplierRequest req, Supplier e) {
        e.setCode(req.getCode());
        e.setCompanyName(req.getCompanyName());
        e.setContactPerson(req.getContactPerson());
        e.setEmail(req.getEmail());
        e.setPhone(req.getPhone());
        e.setAddress(req.getAddress());
        e.setStatus(req.getStatus());
    }
}
