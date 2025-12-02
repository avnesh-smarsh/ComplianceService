package com.smarsh.compliance.notifications;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.CreateTopicRequest;
import software.amazon.awssdk.services.sns.model.SnsException;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final SnsClient snsClient;
    private final Map<String, NotificationSubscriber> subscriberStrategies;

    public void subscribe(String protocol, String topicArn, String endpoint) {
        try {
            NotificationSubscriber subscriber = subscriberStrategies.get(protocol);
            if (subscriber == null) {
                throw new IllegalArgumentException("Unsupported protocol: " + protocol);
            }
            subscriber.subscribe(topicArn, endpoint);
            log.info("Subscribed {} to topic {}", endpoint, topicArn);
        } catch (IllegalArgumentException ex) {
            log.warn("Subscription failed: {}", ex.getMessage());
        } catch (SnsException ex) {
            log.error("AWS SNS error during subscription: {}", ex.awsErrorDetails().errorMessage());
        } catch (Exception ex) {
            log.error("Unexpected error during subscription to {} with protocol {}", topicArn, protocol, ex);
        }
    }

    public void createTopic(String topicName) {
        try {
            snsClient.createTopic(CreateTopicRequest.builder()
                    .name(topicName)
                    .build());
            log.info("Created topic: {}", topicName);
        } catch (SnsException ex) {
            log.error("AWS SNS error while creating topic {}: {}", topicName, ex.awsErrorDetails().errorMessage());
        } catch (Exception ex) {
            log.error("Unexpected error while creating topic {}", topicName, ex);
        }
    }
}
