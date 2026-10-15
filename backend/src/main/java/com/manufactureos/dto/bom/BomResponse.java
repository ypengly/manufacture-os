package com.manufactureos.dto.bom;

import com.manufactureos.entity.BomStatus;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.time.LocalDate;

public class BomResponse {

    private Long id;

    private String bomNumber;

    private Long productId;

    private Integer version;

    private BomStatus status;

    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;

    private Long createdBy;

    private Long approvedBy;

    private Instant approvedAt;

    private Instant createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getBomNumber() { return bomNumber; }
    public void setBomNumber(String bomNumber) { this.bomNumber = bomNumber; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }
    public BomStatus getStatus() { return status; }
    public void setStatus(BomStatus status) { this.status = status; }
    public LocalDate getEffectiveFrom() { return effectiveFrom; }
    public void setEffectiveFrom(LocalDate effectiveFrom) { this.effectiveFrom = effectiveFrom; }
    public LocalDate getEffectiveTo() { return effectiveTo; }
    public void setEffectiveTo(LocalDate effectiveTo) { this.effectiveTo = effectiveTo; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public Long getApprovedBy() { return approvedBy; }
    public void setApprovedBy(Long approvedBy) { this.approvedBy = approvedBy; }
    public Instant getApprovedAt() { return approvedAt; }
    public void setApprovedAt(Instant approvedAt) { this.approvedAt = approvedAt; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
