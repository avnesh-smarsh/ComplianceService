package com.smarsh.compliance.service;

import com.smarsh.compliance.entity.Policy;
import com.smarsh.compliance.entity.PolicyHandler;
import com.smarsh.compliance.entity.Tenant;
import com.smarsh.compliance.models.CanonicalMessage;
import com.smarsh.compliance.models.TenantNotification;
import com.smarsh.compliance.notifications.NotificationPublisher;
import com.smarsh.compliance.repository.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ComplianceServiceTest {

    private PolicyHandler policyHandlerChain;
    private PolicyService policyService;
    private TenantRepository tenantRepository;
    private FlagService flagService;
    private NotificationPublisher notificationPublisher;
    private ComplianceService complianceService;

    @BeforeEach
    void setUp() {
        policyHandlerChain = mock(PolicyHandler.class);
        policyService = mock(PolicyService.class);
        tenantRepository = mock(TenantRepository.class);
        flagService = mock(FlagService.class);
        notificationPublisher = mock(NotificationPublisher.class);

        complianceService = new ComplianceService(
                policyHandlerChain,
                policyService,
                tenantRepository,
                flagService,
                notificationPublisher
        );
    }

    @Test
    void process_nullMessage_noInteractions() {
        complianceService.process(null);
        verifyNoInteractions(tenantRepository, policyService, policyHandlerChain, flagService, notificationPublisher);
    }

    @Test
    void process_tenantNotFound_logsAndReturns() {
        CanonicalMessage msg = mock(CanonicalMessage.class);
        when(msg.getTenantId()).thenReturn("t-1");

        when(tenantRepository.findByTenantId("t-1")).thenReturn(Optional.empty());

        complianceService.process(msg);

        verify(tenantRepository, times(1)).findByTenantId("t-1");
        verifyNoMoreInteractions(policyService, policyHandlerChain, flagService, notificationPublisher);
    }

    @Test
    void process_noPolicyIds_returnsEarly() {
        CanonicalMessage msg = mock(CanonicalMessage.class);
        when(msg.getTenantId()).thenReturn("t-2");
        when(msg.getStableMessageId()).thenReturn("stable-2");

        Tenant tenant = mock(Tenant.class);
        when(tenant.getPolicyIds()).thenReturn(null); // null -> treated as empty list in service
        when(tenantRepository.findByTenantId("t-2")).thenReturn(Optional.of(tenant));

        complianceService.process(msg);

        verify(tenantRepository, times(1)).findByTenantId("t-2");
        verifyNoInteractions(policyService, policyHandlerChain, flagService, notificationPublisher);
    }

    @Test
    void process_handlerDoesNotFlag_noNotification() {
        CanonicalMessage msg = mock(CanonicalMessage.class);
        when(msg.getTenantId()).thenReturn("t-3");
        when(msg.getStableMessageId()).thenReturn("stable-3");

        Tenant tenant = mock(Tenant.class);
        when(tenant.getPolicyIds()).thenReturn(List.of("p1"));
        when(tenantRepository.findByTenantId("t-3")).thenReturn(Optional.of(tenant));

        Policy policy = mock(Policy.class);
        when(policyService.getPoliciesByIds(List.of("p1"))).thenReturn(List.of(policy));

        // Make handler leave flagged = false (default)
        doAnswer(invocation -> {
            // invocation.args: message, policies, flagged, violations
            // do nothing (no flags)
            return null;
        }).when(policyHandlerChain).handle(any(), anyList(), any(AtomicBoolean.class), any(AtomicInteger.class));

        complianceService.process(msg);

        // policy handler should be invoked
        verify(policyHandlerChain, times(1)).handle(any(), anyList(), any(AtomicBoolean.class), any(AtomicInteger.class));
        // no notification should be sent
        verifyNoInteractions(notificationPublisher);
    }

    @Test
    void process_whenFlagged_sendsNotificationWithSeverityFromFlagService() {
        CanonicalMessage msg = mock(CanonicalMessage.class);
        when(msg.getTenantId()).thenReturn("tenant-notify");
        when(msg.getStableMessageId()).thenReturn("stable-notify");
        when(msg.getNetwork()).thenReturn("smtp");

        Tenant tenant = mock(Tenant.class);
        when(tenant.getPolicyIds()).thenReturn(List.of("pA", "pB"));
        when(tenantRepository.findByTenantId("tenant-notify")).thenReturn(Optional.of(tenant));

        Policy pA = mock(Policy.class);
        Policy pB = mock(Policy.class);
        when(policyService.getPoliciesByIds(List.of("pA", "pB"))).thenReturn(List.of(pA, pB));

        // When handler.handle(...) is called, set flagged=true and violations=2
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            AtomicBoolean flagged = (AtomicBoolean) args[2];
            AtomicInteger violations = (AtomicInteger) args[3];
            flagged.set(true);
            violations.set(2);
            return null;
        }).when(policyHandlerChain).handle(any(), anyList(), any(AtomicBoolean.class), any(AtomicInteger.class));

        // Mock severity mapping from FlagService
        when(flagService.getFlagSeverity(2)).thenReturn("HIGH");

        // Capture TenantNotification passed to publisher
        ArgumentCaptor<TenantNotification> captor = ArgumentCaptor.forClass(TenantNotification.class);
        doNothing().when(notificationPublisher).publishNotification(captor.capture());

        complianceService.process(msg);

        verify(policyHandlerChain, times(1)).handle(any(), anyList(), any(AtomicBoolean.class), any(AtomicInteger.class));
        verify(flagService, times(1)).getFlagSeverity(2);
        verify(notificationPublisher, times(1)).publishNotification(any());

        TenantNotification sent = captor.getValue();
        assertNotNull(sent);
        assertEquals("tenant-notify", sent.getTenantId());
        assertEquals("stable-notify", sent.getMessageId());
        assertEquals("HIGH", sent.getSeverity());
        assertEquals("smtp", sent.getNetwork());
    }

    @Test
    void process_notificationThrows_doesNotPropagate() {
        CanonicalMessage msg = mock(CanonicalMessage.class);
        when(msg.getTenantId()).thenReturn("tenant-ex");
        when(msg.getStableMessageId()).thenReturn("stable-ex");
        when(msg.getNetwork()).thenReturn("teams");

        Tenant tenant = mock(Tenant.class);
        when(tenant.getPolicyIds()).thenReturn(List.of("pX"));
        when(tenantRepository.findByTenantId("tenant-ex")).thenReturn(Optional.of(tenant));

        Policy p = mock(Policy.class);
        when(policyService.getPoliciesByIds(List.of("pX"))).thenReturn(List.of(p));

        // Handler flags message with 1 violation
        doAnswer(invocation -> {
            AtomicBoolean flagged = (AtomicBoolean) invocation.getArgument(2);
            AtomicInteger violations = (AtomicInteger) invocation.getArgument(3);
            flagged.set(true);
            violations.set(1);
            return null;
        }).when(policyHandlerChain).handle(any(), anyList(), any(AtomicBoolean.class), any(AtomicInteger.class));

        when(flagService.getFlagSeverity(1)).thenReturn("MEDIUM");

        doThrow(new RuntimeException("SNS down")).when(notificationPublisher).publishNotification(any());

        // Should not throw despite publisher throwing
        assertDoesNotThrow(() -> complianceService.process(msg));

        verify(notificationPublisher, times(1)).publishNotification(any());
        verify(flagService, times(1)).getFlagSeverity(1);
    }
}
