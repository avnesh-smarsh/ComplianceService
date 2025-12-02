package com.smarsh.compliance.kafka;

import com.smarsh.compliance.models.CanonicalMessage;
import com.smarsh.compliance.service.ComplianceService;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;




@Slf4j
@Service
@RequiredArgsConstructor
public class MessageConsumer {
    private final ComplianceService complianceService;


    @KafkaListener(topics = "compliance-topic", groupId = "compliance-services")
    @WithSpan
    public void consume(CanonicalMessage canonicalMessage) {
        try {
            MDC.put("stableMessageId",canonicalMessage.getStableMessageId());
            log.info("Received message from Kafka,{}", canonicalMessage);
            complianceService.process(canonicalMessage);
        } catch (Exception e) {
            log.error("Error processing message", e);
        }
        finally {
            MDC.clear();
        }
    }
}
