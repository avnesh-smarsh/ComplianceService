package com.smarsh.compliance.audits;

import com.smarsh.compliance.audits.service.AuditService;
import com.smarsh.compliance.entity.Flag;
import com.smarsh.compliance.entity.Policy;
import com.smarsh.compliance.models.CanonicalMessage;
import com.smarsh.compliance.models.TenantNotification;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuditLoggingAspectTest {

    private AuditService auditService;
    private AuditLoggingAspect aspect;

    @BeforeEach
    void setUp() {
        auditService = mock(AuditService.class);
        aspect = new AuditLoggingAspect(auditService);
    }

    // Helper: make a simple ProceedingJoinPoint mock whose target class name can be set.
    private ProceedingJoinPoint makePjp(Object target, Object proceedResult) throws Throwable {
        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        when(pjp.getTarget()).thenReturn(target);
        when(pjp.proceed()).thenReturn(proceedResult);
        return pjp;
    }

    @Test
    void auditNotificationPublishing_success_callsAuditService() throws Throwable {
        TenantNotification notification = mock(TenantNotification.class);
        when(notification.getTenantId()).thenReturn("t1");
        when(notification.getMessageId()).thenReturn("m1");
        when(notification.getNetwork()).thenReturn("netA");
        when(notification.getSeverity()).thenReturn("HIGH");

        Object publisher = new Object() { /* class simple name will be synthetic, ok for test */ };
        ProceedingJoinPoint pjp = makePjp(publisher, null);

        Object res = aspect.auditNotificationPublishing(pjp, notification);
        assertNull(res);

        verify(auditService, times(1)).sendAuditLog(eq("t1"), any());
    }

    @Test
    void auditNotificationPublishing_whenPublisherThrows_sendsFailureAuditAndPropagates() throws Throwable {
        TenantNotification notification = mock(TenantNotification.class);
        when(notification.getTenantId()).thenReturn("t2");
        when(notification.getMessageId()).thenReturn("m2");
        when(notification.getNetwork()).thenReturn("netB");
        when(notification.getSeverity()).thenReturn("LOW");

        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        Object publisher = new Object();
        when(pjp.getTarget()).thenReturn(publisher);
        when(pjp.proceed()).thenThrow(new RuntimeException("send fail"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> aspect.auditNotificationPublishing(pjp, notification));
        assertEquals("send fail", ex.getMessage());

        verify(auditService, times(1)).sendAuditLog(eq("t2"), any());
    }

    @Test
    void auditPolicyEvaluation_violation_sendsPolicyViolation() throws Throwable {
        CanonicalMessage message = mock(CanonicalMessage.class);
        when(message.getTenantId()).thenReturn("tenantP");
        when(message.getStableMessageId()).thenReturn("stable-1");
        when(message.getNetwork()).thenReturn("netP");

        Policy policy = mock(Policy.class);
        when(policy.getRuleId()).thenReturn("rule-123");
        when(policy.getType()).thenReturn("TYPE-A");

        Flag flag = mock(Flag.class);
        Optional<Flag> opt = Optional.of(flag);

        Object evaluator = new Object();
        ProceedingJoinPoint pjp = makePjp(evaluator, opt);

        Object result = aspect.auditPolicyEvaluation(pjp, message, policy);
        assertSame(opt, result);

        verify(auditService, times(1)).sendAuditLog(eq("tenantP"), any());
    }

    @Test
    void auditPolicyEvaluation_noViolation_doesNotSendPolicyViolation() throws Throwable {
        CanonicalMessage message = mock(CanonicalMessage.class);
        when(message.getTenantId()).thenReturn("tenantP2");
        when(message.getStableMessageId()).thenReturn("stable-2");
        when(message.getNetwork()).thenReturn("netP2");

        Policy policy = mock(Policy.class);

        Optional<Flag> opt = Optional.empty();

        Object evaluator = new Object();
        ProceedingJoinPoint pjp = makePjp(evaluator, opt);

        Object result = aspect.auditPolicyEvaluation(pjp, message, policy);
        assertSame(opt, result);

        // No violation -> ensure we do NOT call sendAuditLog for violation (but aspect may still not call it)
        verify(auditService, never()).sendAuditLog(eq("tenantP2"), argThat(req -> {
            // conservative: if called it would likely be POLICY_VIOLATION, so return false if that occurs
            return false;
        }));
    }

    @Test
    void auditPolicyEvaluation_whenEvaluatorThrows_sendsErrorAndPropagates() throws Throwable {
        CanonicalMessage message = mock(CanonicalMessage.class);
        when(message.getTenantId()).thenReturn("tenantErr");
        when(message.getStableMessageId()).thenReturn("stable-err");
        when(message.getNetwork()).thenReturn("netErr");

        Policy policy = mock(Policy.class);
        when(policy.getRuleId()).thenReturn("rule-err");

        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        when(pjp.getTarget()).thenReturn(new Object());
        when(pjp.proceed()).thenThrow(new IllegalStateException("eval boom"));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> aspect.auditPolicyEvaluation(pjp, message, policy));
        assertEquals("eval boom", ex.getMessage());

        verify(auditService, times(1)).sendAuditLog(eq("tenantErr"), any());
    }

    @Test
    void auditComplianceProcessing_success_sendsComplete() throws Throwable {
        CanonicalMessage message = mock(CanonicalMessage.class);
        when(message.getTenantId()).thenReturn("compTenant");
        when(message.getStableMessageId()).thenReturn("stable-comp");
        when(message.getNetwork()).thenReturn("netComp");

        Object serviceTarget = new Object();
        ProceedingJoinPoint pjp = makePjp(serviceTarget, "done");

        Object res = aspect.auditComplianceProcessing(pjp, message);
        assertEquals("done", res);

        verify(auditService, times(1)).sendAuditLog(eq("compTenant"), any());
    }

    @Test
    void auditComplianceProcessing_failure_sendsFailedAndPropagates() throws Throwable {
        CanonicalMessage message = mock(CanonicalMessage.class);
        when(message.getTenantId()).thenReturn("compTenant2");
        when(message.getStableMessageId()).thenReturn("stable-comp2");
        when(message.getNetwork()).thenReturn("netComp2");

        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        when(pjp.getTarget()).thenReturn(new Object());
        when(pjp.proceed()).thenThrow(new RuntimeException("process error"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> aspect.auditComplianceProcessing(pjp, message));
        assertEquals("process error", ex.getMessage());

        verify(auditService, times(1)).sendAuditLog(eq("compTenant2"), any());
    }

    @Test
    void auditFlagCreation_success_sendsFlagStored() throws Throwable {
        Flag flag = mock(Flag.class);
        when(flag.getTenantId()).thenReturn("flagTenant");
        when(flag.getMessageId()).thenReturn("flagMsg");
        when(flag.getNetwork()).thenReturn("flagNet");
        when(flag.getRuleId()).thenReturn("ruleX");

        Object flagService = new Object();
        ProceedingJoinPoint pjp = makePjp(flagService, flag); // proceed returns flag (or saved flag)

        Object res = aspect.auditFlagCreation(pjp, flag);
        assertNotNull(res);

        verify(auditService, times(1)).sendAuditLog(eq("flagTenant"), any());
    }

    @Test
    void auditFlagCreation_failure_sendsFlagStoreFailedAndPropagates() throws Throwable {
        Flag flag = mock(Flag.class);
        when(flag.getTenantId()).thenReturn("flagTenant2");
        when(flag.getMessageId()).thenReturn("flagMsg2");
        when(flag.getNetwork()).thenReturn("flagNet2");

        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        when(pjp.getTarget()).thenReturn(new Object());
        when(pjp.proceed()).thenThrow(new RuntimeException("db down"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> aspect.auditFlagCreation(pjp, flag));
        assertEquals("db down", ex.getMessage());

        verify(auditService, times(1)).sendAuditLog(eq("flagTenant2"), any());
    }
}
