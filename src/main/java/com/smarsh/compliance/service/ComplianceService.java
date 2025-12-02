package com.smarsh.compliance.service;


import com.smarsh.compliance.entity.Policy;
import com.smarsh.compliance.entity.PolicyHandler;
import com.smarsh.compliance.entity.Tenant;
import com.smarsh.compliance.models.CanonicalMessage;
import com.smarsh.compliance.models.TenantNotification;
import com.smarsh.compliance.notifications.NotificationPublisher;
import com.smarsh.compliance.repository.TenantRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@AllArgsConstructor
public class ComplianceService {


    private final PolicyHandler policyHandlerChain;
    private final PolicyService policyService;
    private final TenantRepository tenantRepository;
    private final FlagService flagService;
    private final NotificationPublisher notificationPublisher;


    public void process(CanonicalMessage canonicalMessage) {
        if (canonicalMessage == null) {
            log.warn("Null message received, skipping");
            return;
        }
        try {
            Optional<Tenant> tenant = tenantRepository.findByTenantId(canonicalMessage.getTenantId());
            if (tenant.isEmpty()) {
                log.warn("Tenant not found: {}", canonicalMessage.getTenantId());
                return;
            }


            List<String> policyIds = tenant.get().getPolicyIds() == null
                    ? new ArrayList<>()
                    : new ArrayList<>(tenant.get().getPolicyIds());


            if (policyIds.isEmpty()) {
                log.info("No policies for tenant {}", tenant.get().getTenantId());
                return;
            }


            List<Policy> policies = policyService.getPoliciesByIds(policyIds);
            log.info("Processing message {}", canonicalMessage.getStableMessageId());


            AtomicBoolean flagged = new AtomicBoolean(false);
            AtomicInteger violations = new AtomicInteger();


            if (policyHandlerChain != null) {
                policyHandlerChain.handle(canonicalMessage, policies, flagged, violations);
            } else {
                log.warn("No policy handler chain configured");
            }


            if (!flagged.get()) {
                log.info("Message {} not flagged", canonicalMessage.getStableMessageId());
                return;
            }


            try {
                notificationPublisher.publishNotification(
                        new TenantNotification(canonicalMessage.getTenantId(),
                                canonicalMessage.getStableMessageId(),
                                flagService.getFlagSeverity(violations.get()), canonicalMessage.getNetwork()));
            } catch (Exception ex) {
                log.warn("Notification failed for message {}: {}", canonicalMessage.getStableMessageId(), ex.getMessage());
            }


            log.info("Message {} flagged with {} violations", canonicalMessage.getStableMessageId(), violations.get());
        } catch (Exception ex) {
            log.error("Error processing message {}: {}", canonicalMessage.getStableMessageId(), ex.getMessage(), ex);
        }
    }
}