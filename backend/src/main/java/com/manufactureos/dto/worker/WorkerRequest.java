package com.manufactureos.dto.worker;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class WorkerRequest {

    @NotBlank
    @Size(max = 30)
    private String employeeId;

    @NotBlank
    @Size(max = 120)
    private String fullName;

    @Size(max = 160)
    @Email
    private String email;

    @Size(max = 40)
    private String phone;

    @Size(max = 80)
    private String department;

    @Size(max = 80)
    private String jobTitle;

    @Positive
    private Integer skillLevel;

    private Long shiftId;

    private Long userId;

    @Pattern(regexp = "ACTIVE|INACTIVE|ON_LEAVE")
    private String employmentStatus;

    private LocalDate hireDate;

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
