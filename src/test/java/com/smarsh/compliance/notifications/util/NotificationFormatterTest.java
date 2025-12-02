package com.smarsh.compliance.notifications.util;

import com.smarsh.compliance.models.TenantNotification;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NotificationFormatterTest {

    private final NotificationFormatter formatter = new NotificationFormatter();

    @Test
    void formatEmail_containsAllFieldsAndDashboardUrl() {
        TenantNotification n = new TenantNotification();
        n.setTenantId("tenant-1");
        n.setNetwork("whatsapp");
        n.setMessageId("msg-123");
        n.setSeverity("HIGH");
        n.setDashboardUrl("https://app.example.com/tenant/tenant-1");

        String body = formatter.formatEmail(n);

        assertNotNull(body);
        assertTrue(body.contains("ALERT NOTIFICATION"));
        assertTrue(body.contains("Tenant ID : tenant-1"));
        assertTrue(body.contains("Network   : whatsapp"));
        assertTrue(body.contains("Message ID: msg-123"));
        assertTrue(body.contains("Severity  : HIGH"));
        assertTrue(body.contains("Dashboard : https://app.example.com/tenant/tenant-1"));
    }
}
