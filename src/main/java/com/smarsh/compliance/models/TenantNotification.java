package com.smarsh.compliance.models;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TenantNotification {
//    private String recipient;
    private String tenantId;
    private String network;
    private String messageId;
    private String severity;
    private String dashboardUrl;
    public TenantNotification(String tenantId, String messageId, String severity, String network) {
        this.tenantId = tenantId;
        this.messageId = messageId;
        this.severity = severity;
        this.network = network;
    }

}

