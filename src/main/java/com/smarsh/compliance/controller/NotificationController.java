package com.smarsh.compliance.controller;

import com.smarsh.compliance.exception.BadRequestException;
import com.smarsh.compliance.exception.NotFoundException;
import com.smarsh.compliance.models.TenantNotification;
import com.smarsh.compliance.notifications.NotificationPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/compliance/notify")
public class NotificationController {

    private final NotificationPublisher publisher;

    public NotificationController(NotificationPublisher publisher) {
        this.publisher = publisher;
    }

    @PostMapping("/{tenantId}")
    public String notifyTenant(@PathVariable String tenantId, @RequestBody TenantNotification body) {
        if (tenantId == null || tenantId.isBlank()) {
            throw new BadRequestException("Tenant ID must not be null or empty");
        }
        if (body == null ) {
            throw new BadRequestException("Notification body or message must not be empty");
        }

        try {
            body.setTenantId(tenantId);
            publisher.publishNotification(body);
            log.info("Notification sent for tenant: {}", tenantId);
            return "Notification sent for tenant: " + tenantId;
        } catch (IllegalArgumentException ex) {
            // Example: if publisher rejects invalid data
            throw new BadRequestException("Failed to publish notification: " + ex.getMessage(), ex);
        } catch (Exception ex) {
            // Catch-all to wrap in runtime exception handled by GlobalExceptionHandler
            log.error("Unexpected error while publishing notification for tenant {}", tenantId, ex);
            throw new RuntimeException("Unable to send notification at this time. Please try again later.");
        }
    }
}
