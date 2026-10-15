package com.manufactureos.mapper;

import com.manufactureos.entity.Worker;
import com.manufactureos.dto.worker.WorkerRequest;
import com.manufactureos.dto.worker.WorkerResponse;
import org.springframework.stereotype.Component;

@Component
public class WorkerMapper {

    public WorkerResponse toResponse(Worker e) {
        WorkerResponse r = new WorkerResponse();
        r.setId(e.getId());
        r.setEmployeeId(e.getEmployeeId());
        r.setFullName(e.getFullName());
        r.setEmail(e.getEmail());
        r.setPhone(e.getPhone());
        r.setDepartment(e.getDepartment());
        r.setJobTitle(e.getJobTitle());
        r.setSkillLevel(e.getSkillLevel());
        r.setShiftId(e.getShiftId());
        r.setUserId(e.getUserId());
        r.setEmploymentStatus(e.getEmploymentStatus());
        r.setHireDate(e.getHireDate());
        return r;
    }

    public void applyToEntity(WorkerRequest req, Worker e) {
        e.setEmployeeId(req.getEmployeeId());
        e.setFullName(req.getFullName());
        e.setEmail(req.getEmail());
        e.setPhone(req.getPhone());
        e.setDepartment(req.getDepartment());
        e.setJobTitle(req.getJobTitle());
        e.setSkillLevel(req.getSkillLevel());
        e.setShiftId(req.getShiftId());
        e.setUserId(req.getUserId());
        e.setEmploymentStatus(req.getEmploymentStatus());
        e.setHireDate(req.getHireDate());
    }
}
