package com.manufactureos.service;

import com.manufactureos.dto.notification.NotificationResponse;
import com.manufactureos.entity.Notification;
import com.manufactureos.entity.NotificationType;
import com.manufactureos.entity.Role;
import com.manufactureos.exception.NotFoundException;
import com.manufactureos.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Creates and serves in-app notifications. {@link #notify} is idempotent when a
 * dedupeKey is supplied (e.g. "LOW_STOCK:materialId:date") so a scheduled scan that
 * runs every few minutes doesn't flood a recipient with the same alert.
 */
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Page<NotificationResponse> listForUser(Long userId, Pageable pageable) {
        return notificationRepository.findByRecipientUserIdOrderByCreatedAtDesc(userId, pageable).map(this::toResponse);
    }

    public Page<NotificationResponse> listForRole(Role role, Pageable pageable) {
        return notificationRepository.findByRecipientRoleOrderByCreatedAtDesc(role, pageable).map(this::toResponse);
    }

    @Transactional
    public NotificationResponse markRead(Long id) {
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Notification not found: " + id));
        n.setReadStatus(true);
        return toResponse(n);
    }

    @Transactional
    public void notifyUser(Long recipientUserId, String title, String message, NotificationType type, String dedupeKey) {
        notify(recipientUserId, null, title, message, type, dedupeKey);
    }

    @Transactional
    public void notifyRole(Role recipientRole, String title, String message, NotificationType type, String dedupeKey) {
        notify(null, recipientRole, title, message, type, dedupeKey);
    }

    private void notify(Long recipientUserId, Role recipientRole, String title, String message,
                         NotificationType type, String dedupeKey) {
        if (dedupeKey != null && notificationRepository.existsByDedupeKey(dedupeKey)) {
            return;
        }
        Notification n = new Notification();
        n.setRecipientUserId(recipientUserId);
        n.setRecipientRole(recipientRole);
        n.setTitle(title);
        n.setMessage(message);
        n.setType(type);
        n.setDedupeKey(dedupeKey);
        n.setReadStatus(false);
        n.setCreatedAt(Instant.now());
        notificationRepository.save(n);
    }

    private NotificationResponse toResponse(Notification n) {
        NotificationResponse r = new NotificationResponse();
        r.setId(n.getId());
        r.setRecipientUserId(n.getRecipientUserId());
        r.setRecipientRole(n.getRecipientRole());
        r.setTitle(n.getTitle());
        r.setMessage(n.getMessage());
        r.setType(n.getType());
        r.setDedupeKey(n.getDedupeKey());
        r.setReadStatus(n.getReadStatus());
        r.setCreatedAt(n.getCreatedAt());
        return r;
    }
}
