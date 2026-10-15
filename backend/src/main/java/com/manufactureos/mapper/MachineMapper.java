package com.manufactureos.mapper;

import com.manufactureos.entity.Machine;
import com.manufactureos.dto.machine.MachineRequest;
import com.manufactureos.dto.machine.MachineResponse;
import org.springframework.stereotype.Component;

@Component
public class MachineMapper {

    public MachineResponse toResponse(Machine e) {
        MachineResponse r = new MachineResponse();
        r.setId(e.getId());
        r.setCode(e.getCode());
        r.setName(e.getName());
        r.setMachineType(e.getMachineType());
        r.setFactoryId(e.getFactoryId());
        r.setLineId(e.getLineId());
        r.setStatus(e.getStatus());
        r.setCapacityPerHour(e.getCapacityPerHour());
        r.setCrewSize(e.getCrewSize());
        r.setRequiredSkill(e.getRequiredSkill());
        r.setOperatingHours(e.getOperatingHours());
        r.setMaintenanceIntervalDays(e.getMaintenanceIntervalDays());
        r.setLastMaintenanceDate(e.getLastMaintenanceDate());
        r.setNextMaintenanceDate(e.getNextMaintenanceDate());
        r.setVersion(e.getVersion());
        return r;
    }

    public void applyToEntity(MachineRequest req, Machine e) {
        e.setCode(req.getCode());
        e.setName(req.getName());
        e.setMachineType(req.getMachineType());
        e.setFactoryId(req.getFactoryId());
        e.setLineId(req.getLineId());
        e.setCapacityPerHour(req.getCapacityPerHour());
        e.setCrewSize(req.getCrewSize());
        e.setRequiredSkill(req.getRequiredSkill());
        e.setMaintenanceIntervalDays(req.getMaintenanceIntervalDays());
        e.setLastMaintenanceDate(req.getLastMaintenanceDate());
        e.setNextMaintenanceDate(req.getNextMaintenanceDate());
    }
}
