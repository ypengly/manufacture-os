package com.manufactureos.mapper;

import com.manufactureos.entity.Workstation;
import com.manufactureos.dto.workstation.WorkstationRequest;
import com.manufactureos.dto.workstation.WorkstationResponse;
import org.springframework.stereotype.Component;

@Component
public class WorkstationMapper {

    public WorkstationResponse toResponse(Workstation e) {
        WorkstationResponse r = new WorkstationResponse();
        r.setId(e.getId());
        r.setLineId(e.getLineId());
        r.setName(e.getName());
        return r;
    }

    public void applyToEntity(WorkstationRequest req, Workstation e) {
        e.setLineId(req.getLineId());
        e.setName(req.getName());
    }
}
