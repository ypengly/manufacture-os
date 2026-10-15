package com.manufactureos.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

/** Quantities at or above the threshold require an approver (approvedBy) on the scrap record. */
@ConfigurationProperties(prefix = "manufactureos.scrap")
public class ScrapProperties {

    private BigDecimal approvalThreshold = BigDecimal.valueOf(50);

    public BigDecimal getApprovalThreshold() { return approvalThreshold; }
    public void setApprovalThreshold(BigDecimal approvalThreshold) { this.approvalThreshold = approvalThreshold; }
}
