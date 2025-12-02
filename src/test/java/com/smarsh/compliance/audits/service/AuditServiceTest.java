package com.smarsh.compliance.audits.service;

import com.smarsh.compliance.audits.AuditLogRequest;
import com.smarsh.compliance.audits.client.AuditClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class AuditServiceTest {

    private AuditClient auditClient;
    private AuditService auditService;

    @BeforeEach
    void setUp() {
        auditClient = mock(AuditClient.class);
        auditService = new AuditService(auditClient);
    }

    @Test
    void sendAuditLog_success_callsClient() {
        String tenantId = "tenant-1";
        AuditLogRequest req = new AuditLogRequest("msg-1", "net", "EVENT", Map.of());

        auditService.sendAuditLog(tenantId, req);

        ArgumentCaptor<String> tidCap = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<AuditLogRequest> reqCap = ArgumentCaptor.forClass(AuditLogRequest.class);
        verify(auditClient, times(1)).sendAuditLog(tidCap.capture(), reqCap.capture());

        assertEquals(tenantId, tidCap.getValue());
        assertSame(req, reqCap.getValue());
    }

    @Test
    void sendAuditLog_clientThrows_logsAndDoesNotPropagate() {
        String tenantId = "tenant-2";
        AuditLogRequest req = new AuditLogRequest("msg-2", "net", "EVENT", Map.of());
        doThrow(new RuntimeException("client down")).when(auditClient).sendAuditLog(anyString(), any());

        // Should not throw
        assertDoesNotThrow(() -> auditService.sendAuditLog(tenantId, req));

        // Still attempted to call client
        verify(auditClient, times(1)).sendAuditLog(eq(tenantId), eq(req));
    }
}
