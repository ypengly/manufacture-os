package com.manufactureos.mapper;

import com.manufactureos.entity.Shift;
import com.manufactureos.dto.shift.ShiftRequest;
import com.manufactureos.dto.shift.ShiftResponse;
import org.springframework.stereotype.Component;

@Component
public class ShiftMapper {

    public ShiftResponse toResponse(Shift e) {
        ShiftResponse r = new ShiftResponse();
        r.setId(e.getId());
        r.setName(e.getName());
        r.setStartTime(e.getStartTime());
        r.setEndTime(e.getEndTime());
        r.setFactoryId(e.getFactoryId());
        r.setDepartment(e.getDepartment());
        r.setStatus(e.getStatus());
        return r;
    }

    public void applyToEntity(ShiftRequest req, Shift e) {
        e.setName(req.getName());
        e.setStartTime(req.getStartTime());
        e.setEndTime(req.getEndTime());
        e.setFactoryId(req.getFactoryId());
        e.setDepartment(req.getDepartment());
        e.setStatus(req.getStatus());
    }
}
