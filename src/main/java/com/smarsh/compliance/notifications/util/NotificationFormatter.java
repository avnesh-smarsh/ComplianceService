package com.smarsh.compliance.notifications.util;

import com.smarsh.compliance.models.TenantNotification;
import org.springframework.stereotype.Component;

@Component
public class NotificationFormatter {

    public String formatEmail(TenantNotification notification) {
        StringBuilder emailBody = new StringBuilder();

        emailBody.append("====================================\n");
        emailBody.append("           ALERT NOTIFICATION        \n");
        emailBody.append("====================================\n");

        emailBody.append("Tenant ID : ").append(notification.getTenantId()).append("\n");
        emailBody.append("Network   : ").append(notification.getNetwork()).append("\n");
        emailBody.append("Message ID: ").append(notification.getMessageId()).append("\n");
        emailBody.append("Severity  : ").append(notification.getSeverity()).append("\n");
        emailBody.append("Dashboard : ").append(notification.getDashboardUrl()).append("\n\n");

        emailBody.append("====================================\n");
        emailBody.append("This is an automated notification.\n");
        emailBody.append("====================================\n");
        return emailBody.toString();
    }
}
