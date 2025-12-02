package com.smarsh.compliance.notifications;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.SnsException;
import software.amazon.awssdk.services.sns.model.SubscribeRequest;

@Slf4j
@Component("email")
@RequiredArgsConstructor
public class EmailNotificationSubscriber implements NotificationSubscriber {

    private final SnsClient snsClient;

    @Override
    public void subscribe(String topicArn, String endpoint) {
        try {
            if (topicArn == null || topicArn.isBlank()) {
                throw new IllegalArgumentException("topicArn must not be null or empty");
            }
            if (endpoint == null || endpoint.isBlank()) {
                throw new IllegalArgumentException("endpoint must not be null or empty");
            }

            snsClient.subscribe(
                    SubscribeRequest.builder()
                            .topicArn(topicArn)
                            .protocol("email")
                            .endpoint(endpoint)
                            .returnSubscriptionArn(true)
                            .build()
            );

            log.info("Subscribed {} to topic {}", endpoint, topicArn);
        } catch (IllegalArgumentException ex) {
            log.warn("Invalid subscription request: {}", ex.getMessage());
        } catch (SnsException ex) {
            log.error("AWS SNS error while subscribing {} to {}: {}", endpoint, topicArn, ex.awsErrorDetails().errorMessage());
        } catch (Exception ex) {
            log.error("Unexpected error while subscribing {} to {}", endpoint, topicArn, ex);
        }
    }
}
