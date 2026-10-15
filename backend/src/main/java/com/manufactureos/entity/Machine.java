package com.manufactureos.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "machines")
public class Machine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, length = 30)
    private String code;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "machine_type", length = 60)
    private String machineType;

    @Column(name = "factory_id")
    private Long factoryId;

    @Column(name = "line_id")
    private Long lineId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private MachineStatus status = MachineStatus.AVAILABLE;

    @Column(name = "capacity_per_hour")
    private BigDecimal capacityPerHour = BigDecimal.ZERO;

    @Column(name = "crew_size")
    private Integer crewSize = 1;

    @Column(name = "required_skill", length = 60)
    private String requiredSkill;

    @Column(name = "operating_hours")
    private BigDecimal operatingHours = BigDecimal.ZERO;

    @Column(name = "maintenance_interval_days")
    private Integer maintenanceIntervalDays = 90;

    @Column(name = "last_maintenance_date")
    private LocalDate lastMaintenanceDate;

    @Column(name = "next_maintenance_date")
    private LocalDate nextMaintenanceDate;

    @Version
    @Column(name = "version")
    private Long version;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMachineType() { return machineType; }
    public void setMachineType(String machineType) { this.machineType = machineType; }

    public Long getFactoryId() { return factoryId; }
    public void setFactoryId(Long factoryId) { this.factoryId = factoryId; }

    public Long getLineId() { return lineId; }
    public void setLineId(Long lineId) { this.lineId = lineId; }

    public MachineStatus getStatus() { return status; }
    public void setStatus(MachineStatus status) { this.status = status; }

    public BigDecimal getCapacityPerHour() { return capacityPerHour; }
    public void setCapacityPerHour(BigDecimal capacityPerHour) { this.capacityPerHour = capacityPerHour; }

    public Integer getCrewSize() { return crewSize; }
    public void setCrewSize(Integer crewSize) { this.crewSize = crewSize; }

    public String getRequiredSkill() { return requiredSkill; }
    public void setRequiredSkill(String requiredSkill) { this.requiredSkill = requiredSkill; }

    public BigDecimal getOperatingHours() { return operatingHours; }
    public void setOperatingHours(BigDecimal operatingHours) { this.operatingHours = operatingHours; }

    public Integer getMaintenanceIntervalDays() { return maintenanceIntervalDays; }
    public void setMaintenanceIntervalDays(Integer maintenanceIntervalDays) { this.maintenanceIntervalDays = maintenanceIntervalDays; }

    public LocalDate getLastMaintenanceDate() { return lastMaintenanceDate; }
    public void setLastMaintenanceDate(LocalDate lastMaintenanceDate) { this.lastMaintenanceDate = lastMaintenanceDate; }

    public LocalDate getNextMaintenanceDate() { return nextMaintenanceDate; }
    public void setNextMaintenanceDate(LocalDate nextMaintenanceDate) { this.nextMaintenanceDate = nextMaintenanceDate; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
