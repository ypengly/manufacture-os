package com.manufactureos.dto.workerSkill;

import jakarta.validation.constraints.*;

public class WorkerSkillResponse {

    private Long id;

    private Long workerId;

    private String skill;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getWorkerId() { return workerId; }
    public void setWorkerId(Long workerId) { this.workerId = workerId; }
    public String getSkill() { return skill; }
    public void setSkill(String skill) { this.skill = skill; }
}
