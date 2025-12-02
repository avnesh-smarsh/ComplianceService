package com.smarsh.compliance.controller;

import com.smarsh.compliance.dto.TenantVerificationResponse;
import com.smarsh.compliance.entity.Tenant;
import com.smarsh.compliance.exception.BadRequestException;
import com.smarsh.compliance.service.TenantService;
import com.smarsh.compliance.notifications.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TenantController {

    private final TenantService tenantService;
    private final NotificationService notificationService;

    @Value("${aws.sns.topic.arn}")
    private String baseArn;

    @PostMapping("/tenant")
    public String addTenant(@RequestBody Tenant tenant) {
        if (tenant == null || tenant.getTenantId() == null || tenant.getTenantId().isBlank()) {
            throw new BadRequestException("tenantId is required");
        }
        if (tenant.getEmail() == null || tenant.getEmail().isBlank()) {
            throw new BadRequestException("tenant email is required");
        }

        try {
            log.info("addTenant: {}", tenant.getTenantId());
            String topicArn = baseArn + "-" + tenant.getTenantId();
            notificationService.createTopic("TenantNotifications-" + tenant.getTenantId());
            notificationService.subscribe("email", topicArn, tenant.getEmail());

            return tenantService.addTenant(tenant);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid tenant data: " + ex.getMessage(), ex);
        } catch (Exception ex) {
            log.error("Error while adding tenant {}", tenant.getTenantId(), ex);
            throw new RuntimeException("Unable to add tenant right now");
        }
    }

    @GetMapping("/api/tenant/verify/{tenantId}")
    public ResponseEntity<TenantVerificationResponse> verifyTenant(@PathVariable("tenantId") String tenantId) {
        if (tenantId == null || tenantId.isBlank()) {
            throw new BadRequestException("tenantId is required");
        }
        try {
            log.info("Verifying tenant existence for tenantId: {}", tenantId);
            boolean exists = tenantService.verifyTenant(tenantId);
            TenantVerificationResponse response = new TenantVerificationResponse(tenantId, exists);
            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            log.error("Error while verifying tenant {}", tenantId, ex);
            throw new RuntimeException("Unable to verify tenant right now");
        }
    }



    @GetMapping("/tenant")
    public List<Tenant> getAllTenant() {
        try {
            return tenantService.getAllTenant();
        } catch (Exception ex) {
            log.error("Error while fetching tenants", ex);
            throw new RuntimeException("Unable to fetch tenants right now");
        }
    }
}
