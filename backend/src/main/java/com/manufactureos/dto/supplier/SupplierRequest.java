package com.manufactureos.dto.supplier;

import jakarta.validation.constraints.*;

public class SupplierRequest {

    @NotBlank
    @Size(max = 30)
    private String code;

    @NotBlank
    @Size(max = 160)
    private String companyName;

    @Size(max = 120)
    private String contactPerson;

    @Size(max = 160)
    @Email
    private String email;

    @Size(max = 40)
    private String phone;

    @Size(max = 255)
    private String address;

    @Pattern(regexp = "ACTIVE|INACTIVE|BLOCKED")
    private String status;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
