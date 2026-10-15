package com.manufactureos.dto.notification;

import com.manufactureos.entity.NotificationType;
import com.manufactureos.entity.Role;
import jakarta.validation.constraints.*;
import java.time.Instant;

public class NotificationResponse {

    private Long id;

    private Long recipientUserId;

    private Role recipientRole;

    private String title;

    private String message;

    private NotificationType type;

    private String dedupeKey;

    private Boolean readStatus;

    private Instant createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getRecipientUserId() { return recipientUserId; }
    public void setRecipientUserId(Long recipientUserId) { this.recipientUserId = recipientUserId; }
    public Role getRecipientRole() { return recipientRole; }
    public void setRecipientRole(Role recipientRole) { this.recipientRole = recipientRole; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }
    public String getDedupeKey() { return dedupeKey; }
    public void setDedupeKey(String dedupeKey) { this.dedupeKey = dedupeKey; }
    public Boolean getReadStatus() { return readStatus; }
    public void setReadStatus(Boolean readStatus) { this.readStatus = readStatus; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
