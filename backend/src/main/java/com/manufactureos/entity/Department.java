package com.manufactureos.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "departments")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "factory_id")
    private Long factoryId;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getFactoryId() { return factoryId; }
    public void setFactoryId(Long factoryId) { this.factoryId = factoryId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
