package com.smarsh.compliance.notifications;

public interface NotificationSubscriber {
    void subscribe(String topicArn, String endpoint);
}
