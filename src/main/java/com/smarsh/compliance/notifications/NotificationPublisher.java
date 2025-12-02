package com.smarsh.compliance.notifications;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarsh.compliance.models.TenantNotification;
import com.smarsh.compliance.notifications.util.NotificationFormatter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.SnsException;

@Slf4j
@Service
public class NotificationPublisher {

    private final SnsClient snsClient;
    private final ObjectMapper objectMapper;
    private NotificationFormatter notificationFormatter;

    @Value("${aws.sns.topic.arn}")
    private String baseArn;
    @Value("${dashboard.baseUrl}")
    private String dashboardBaseUrl;

    public NotificationPublisher(SnsClient snsClient, ObjectMapper objectMapper, NotificationFormatter notificationFormatter) {
        this.snsClient = snsClient;
        this.objectMapper = objectMapper;
        this.notificationFormatter = notificationFormatter;
    }

    public void publishNotification(TenantNotification notification) {
        try {
            if (notification == null || notification.getTenantId() == null) {
                throw new IllegalArgumentException("TenantNotification or tenantId must not be null");
            }

            notification.setDashboardUrl(dashboardBaseUrl + "/tenant/" + notification.getTenantId());
            String messageJson = objectMapper.writeValueAsString(notification);
            String topicArn = baseArn + "-" + notification.getTenantId();

            String finalMessage = notificationFormatter.formatEmail(notification);

            PublishRequest request = PublishRequest.builder()
                    .topicArn(topicArn)
                    .message(finalMessage)
                    .subject("Flagged Message Notification")
                    .build();

            snsClient.publish(request);

            log.info("Notification published to SNS for tenant {}: {}", notification.getTenantId(), messageJson);

        } catch (IllegalArgumentException ex) {
            log.warn("Invalid notification request: {}", ex.getMessage());
        } catch (JsonProcessingException ex) {
            log.error("Failed to serialize notification for tenant {}: {}",
                    notification != null ? notification.getTenantId() : "unknown", ex.getMessage());
        } catch (SnsException ex) {
            log.error("AWS SNS error while publishing notification for tenant {}: {}",
                    notification != null ? notification.getTenantId() : "unknown", ex.awsErrorDetails().errorMessage());
        } catch (Exception ex) {
            log.error("Unexpected error while publishing notification", ex);
        }
    }
}
