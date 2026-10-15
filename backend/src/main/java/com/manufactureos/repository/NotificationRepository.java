package com.manufactureos.repository;

import com.manufactureos.entity.Notification;
import com.manufactureos.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface NotificationRepository extends JpaRepository<Notification, Long>, JpaSpecificationExecutor<Notification> {

    Page<Notification> findByRecipientUserIdOrderByCreatedAtDesc(Long recipientUserId, Pageable pageable);

    Page<Notification> findByRecipientRoleOrderByCreatedAtDesc(Role recipientRole, Pageable pageable);

    boolean existsByDedupeKey(String dedupeKey);

    long countByRecipientUserIdAndReadStatusFalse(Long recipientUserId);

    long countByRecipientRoleAndReadStatusFalse(Role recipientRole);
}
