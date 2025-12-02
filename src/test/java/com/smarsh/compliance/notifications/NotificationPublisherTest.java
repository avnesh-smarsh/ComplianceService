package com.smarsh.compliance.notifications;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smarsh.compliance.models.TenantNotification;
import com.smarsh.compliance.notifications.util.NotificationFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.SnsException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class NotificationPublisherTest {

    private SnsClient snsClient;
    private ObjectMapper objectMapper;
    private NotificationFormatter formatter;
    private NotificationPublisher publisher;

    @BeforeEach
    void setUp() {
        snsClient = mock(SnsClient.class);
        objectMapper = mock(ObjectMapper.class);
        formatter = mock(NotificationFormatter.class);
        publisher = new NotificationPublisher(snsClient, objectMapper, formatter);

        // set fields that are @Value injected
        // Reflection used because fields are private; alternatively, create a constructor or setter in production code.
        try {
            var cls = publisher.getClass();
            var baseArnField = cls.getDeclaredField("baseArn");
            baseArnField.setAccessible(true);
            baseArnField.set(publisher, "arn:aws:sns:us-east-1:123456789012:topic");

            var dashField = cls.getDeclaredField("dashboardBaseUrl");
            dashField.setAccessible(true);
            dashField.set(publisher, "https://dashboard.example.com");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void publishNotification_success_callsSnsWithFormattedMessage() throws Exception {
        TenantNotification n = new TenantNotification();
        n.setTenantId("tenantA");
        n.setNetwork("teams");
        n.setMessageId("m-1");
        n.setSeverity("LOW");

        when(objectMapper.writeValueAsString(any())).thenReturn("{json}");
        when(formatter.formatEmail(any())).thenReturn("formatted email");
        when(snsClient.publish(any(PublishRequest.class))).thenReturn(PublishResponse.builder().messageId("mid").build());

        publisher.publishNotification(n);

        ArgumentCaptor<PublishRequest> captor = ArgumentCaptor.forClass(PublishRequest.class);
        verify(snsClient, times(1)).publish(captor.capture());

        PublishRequest req = captor.getValue();
        assertTrue(req.topicArn().endsWith("-tenantA"));
        assertEquals("Flagged Message Notification", req.subject());
        assertEquals("formatted email", req.message());
    }

    @Test
    void publishNotification_nullNotification_logsAndReturnsWithoutThrowing() {
        // Should be handled gracefully (method logs and returns)
        assertDoesNotThrow(() -> publisher.publishNotification(null));
        verifyNoInteractions(snsClient);
    }

}
