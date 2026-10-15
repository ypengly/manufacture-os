package com.manufactureos.dto.auth;

public class TokenPairResponse {

    private String accessToken;
    private String refreshToken;
    private final String tokenType = "Bearer";
    private long expiresInSeconds;
    private CurrentUserResponse user;

    public TokenPairResponse() {}

    public TokenPairResponse(String accessToken, String refreshToken, long expiresInSeconds, CurrentUserResponse user) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresInSeconds = expiresInSeconds;
        this.user = user;
    }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public String getTokenType() { return tokenType; }

    public long getExpiresInSeconds() { return expiresInSeconds; }
    public void setExpiresInSeconds(long expiresInSeconds) { this.expiresInSeconds = expiresInSeconds; }

    public CurrentUserResponse getUser() { return user; }
    public void setUser(CurrentUserResponse user) { this.user = user; }
}
