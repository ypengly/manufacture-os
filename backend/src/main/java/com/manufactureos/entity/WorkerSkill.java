package com.manufactureos.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "worker_skills")
public class WorkerSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "worker_id")
    private Long workerId;

    @Column(name = "skill")
    private String skill;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getWorkerId() { return workerId; }
    public void setWorkerId(Long workerId) { this.workerId = workerId; }

    public String getSkill() { return skill; }
    public void setSkill(String skill) { this.skill = skill; }
}
