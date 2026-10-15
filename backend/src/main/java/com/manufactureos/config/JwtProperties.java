package com.manufactureos.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "manufactureos.jwt")
public class JwtProperties {

    /** Must be at least 256 bits of entropy for HS256; set via the JWT_SECRET env var. */
    private String secret;
    private long accessTtlMinutes = 15;
    private long refreshTtlDays = 7;

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }

    public long getAccessTtlMinutes() { return accessTtlMinutes; }
    public void setAccessTtlMinutes(long accessTtlMinutes) { this.accessTtlMinutes = accessTtlMinutes; }

    public long getRefreshTtlDays() { return refreshTtlDays; }
    public void setRefreshTtlDays(long refreshTtlDays) { this.refreshTtlDays = refreshTtlDays; }
}
