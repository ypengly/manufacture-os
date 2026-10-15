package com.manufactureos.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "workers")
public class Worker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false, length = 30)
    private String employeeId;

    @Column(name = "full_name", nullable = false, length = 120)
    private String fullName;

    @Column(name = "email", length = 160)
    private String email;

    @Column(name = "phone", length = 40)
    private String phone;

    @Column(name = "department", length = 80)
    private String department;

    @Column(name = "job_title", length = 80)
    private String jobTitle;

    @Column(name = "skill_level")
    private Integer skillLevel = 1;

    @Column(name = "shift_id")
    private Long shiftId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "employment_status")
    private String employmentStatus = "ACTIVE";

    @Column(name = "hire_date")
    private LocalDate hireDate;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getJobTitle() { return jobTitle; }
    public void setJobTitle(String jobTitle) { this.jobTitle = jobTitle; }

    public Integer getSkillLevel() { return skillLevel; }
    public void setSkillLevel(Integer skillLevel) { this.skillLevel = skillLevel; }

    public Long getShiftId() { return shiftId; }
    public void setShiftId(Long shiftId) { this.shiftId = shiftId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getEmploymentStatus() { return employmentStatus; }
    public void setEmploymentStatus(String employmentStatus) { this.employmentStatus = employmentStatus; }

    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }
}
