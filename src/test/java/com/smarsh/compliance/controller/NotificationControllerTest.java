package com.smarsh.compliance.controller;

import com.smarsh.compliance.exception.BadRequestException;
import com.smarsh.compliance.models.TenantNotification;
import com.smarsh.compliance.notifications.NotificationPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationControllerTest {

    private NotificationPublisher publisher;
    private NotificationController controller;

    @BeforeEach
    void setUp() {
        publisher = mock(NotificationPublisher.class);
        controller = new NotificationController(publisher);
    }

    @Test
    void notifyTenant_validRequest_callsPublisherAndReturnsMessage() {
        TenantNotification body = new TenantNotification();
        body.setMessageId("m1");

        String response = controller.notifyTenant("tenantX", body);

        assertTrue(response.contains("Notification sent for tenant: tenantX"));
        // ensure tenantId was set on body
        assertEquals("tenantX", body.getTenantId());
        verify(publisher, times(1)).publishNotification(body);
    }

    @Test
    void notifyTenant_nullTenant_throwsBadRequest() {
        TenantNotification body = new TenantNotification();
        BadRequestException ex = assertThrows(BadRequestException.class, () -> controller.notifyTenant("", body));
        assertTrue(ex.getMessage().contains("Tenant ID must not be null or empty"));
        verifyNoInteractions(publisher);
    }

    @Test
    void notifyTenant_nullBody_throwsBadRequest() {
        BadRequestException ex = assertThrows(BadRequestException.class, () -> controller.notifyTenant("t1", null));
        assertTrue(ex.getMessage().contains("Notification body or message must not be empty"));
        verifyNoInteractions(publisher);
    }

    @Test
    void notifyTenant_publisherThrowsIllegalArgument_wrapsAsBadRequest() {
        TenantNotification body = new TenantNotification();
        doThrow(new IllegalArgumentException("invalid")).when(publisher).publishNotification(any());

        BadRequestException ex = assertThrows(BadRequestException.class, () -> controller.notifyTenant("tkk", body));
        assertTrue(ex.getMessage().contains("Failed to publish notification"));
        verify(publisher, times(1)).publishNotification(body);
    }

    @Test
    void notifyTenant_publisherThrowsUnexpected_exceptionIsWrapped() {
        TenantNotification body = new TenantNotification();
        doThrow(new RuntimeException("boom")).when(publisher).publishNotification(any());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> controller.notifyTenant("tenantY", body));
        assertTrue(ex.getMessage().contains("Unable to send notification at this time"));
        verify(publisher, times(1)).publishNotification(body);
    }
}
