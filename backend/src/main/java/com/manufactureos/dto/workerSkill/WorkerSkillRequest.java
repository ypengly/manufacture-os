package com.manufactureos.dto.workerSkill;

import jakarta.validation.constraints.*;

public class WorkerSkillRequest {

    private Long workerId;

    private String skill;

    public Long getWorkerId() { return workerId; }
    public void setWorkerId(Long workerId) { this.workerId = workerId; }
    public String getSkill() { return skill; }
    public void setSkill(String skill) { this.skill = skill; }
}
