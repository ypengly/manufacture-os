package com.manufactureos.mapper;

import com.manufactureos.entity.StorageLocation;
import com.manufactureos.dto.storageLocation.StorageLocationRequest;
import com.manufactureos.dto.storageLocation.StorageLocationResponse;
import org.springframework.stereotype.Component;

@Component
public class StorageLocationMapper {

    public StorageLocationResponse toResponse(StorageLocation e) {
        StorageLocationResponse r = new StorageLocationResponse();
        r.setId(e.getId());
        r.setFactoryId(e.getFactoryId());
        r.setCode(e.getCode());
        r.setName(e.getName());
        r.setType(e.getType());
        return r;
    }

    public void applyToEntity(StorageLocationRequest req, StorageLocation e) {
        e.setFactoryId(req.getFactoryId());
        e.setCode(req.getCode());
        e.setName(req.getName());
        e.setType(req.getType());
    }
}
