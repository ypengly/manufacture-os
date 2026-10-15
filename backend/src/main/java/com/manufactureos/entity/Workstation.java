package com.manufactureos.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "workstations")
public class Workstation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "line_id")
    private Long lineId;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getLineId() { return lineId; }
    public void setLineId(Long lineId) { this.lineId = lineId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
