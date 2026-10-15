package com.manufactureos.dto.factory;

import jakarta.validation.constraints.*;
import java.time.Instant;

public class FactoryRequest {

    @NotBlank
    @Size(max = 30)
    private String code;

    @NotBlank
    @Size(max = 120)
    private String name;

    @Size(max = 255)
    private String address;

    @Size(max = 255)
    private String contactDetails;

    @Size(max = 120)
    private String managerName;

    @Pattern(regexp = "ACTIVE|INACTIVE|MAINTENANCE")
    private String status;

    @Size(max = 60)
    private String timeZone;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getContactDetails() { return contactDetails; }
    public void setContactDetails(String contactDetails) { this.contactDetails = contactDetails; }
    public String getManagerName() { return managerName; }
    public void setManagerName(String managerName) { this.managerName = managerName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getTimeZone() { return timeZone; }
    public void setTimeZone(String timeZone) { this.timeZone = timeZone; }
}
