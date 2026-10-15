package com.manufactureos.audit;

import com.manufactureos.entity.AuditLog;
import com.manufactureos.repository.AuditLogRepository;
import com.manufactureos.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Writes immutable audit trail entries. Every write that matters to an operator
 * (creation, approval, status change, inventory movement) goes through here rather
 * than touching AuditLogRepository directly, so the entries stay consistent.
 *
 * Runs in its own REQUIRES_NEW transaction: an audit write must never roll back, and
 * must never be rolled back by, the business transaction that triggered it failing
 * later for an unrelated reason within the same unit of work. In practice both commit
 * or both roll back together since the audit call happens inside the business method.
 */
@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String action, String entityType, String entityId, String description) {
        AuditLog log = new AuditLog();
        log.setUserId(SecurityUtils.currentUserId().orElse(null));
        log.setUsername(SecurityUtils.currentUsername().orElse("system"));
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setDescription(description);
        log.setIpAddress(currentIp());
        auditLogRepository.save(log);
    }

    private String currentIp() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs == null) return null;
            HttpServletRequest request = attrs.getRequest();
            String forwarded = request.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                return forwarded.split(",")[0].trim();
            }
            return request.getRemoteAddr();
        } catch (Exception e) {
            return null;
        }
    }
}
