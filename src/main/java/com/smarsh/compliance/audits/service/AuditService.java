package com.smarsh.compliance.audits.service;

import com.smarsh.compliance.audits.AuditLogRequest;
import com.smarsh.compliance.audits.client.AuditClient;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditClient auditClient;

    public void sendAuditLog(String tenantId, AuditLogRequest request) {
        try {
            auditClient.sendAuditLog(tenantId, request);
        } catch (Exception e) {
            System.err.println("Async audit failed for tenant " + tenantId + ": " + e.getMessage());
        }
    }
}
