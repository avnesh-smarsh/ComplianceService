package com.smarsh.compliance.evaluators;

import com.smarsh.compliance.entity.Flag;
import com.smarsh.compliance.entity.KeywordPolicy;
import com.smarsh.compliance.entity.Policy;
import com.smarsh.compliance.models.CanonicalMessage;
import com.smarsh.compliance.service.PolicyMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeywordEvaluator implements PolicyEvaluator {

    @Override
    public boolean supports(String type) {
        return "keyword".equalsIgnoreCase(type);
    }

    @Override
    public Optional<Flag> evaluate(CanonicalMessage canonicalMessage, Policy policy) {
        try {
            if (canonicalMessage == null || policy == null) {
                throw new IllegalArgumentException("Message or Policy cannot be null");
            }

            String fieldValue = getFieldValue(canonicalMessage, policy.getField());
            if (fieldValue == null) {
                return Optional.empty();
            }

            KeywordPolicy keywordPolicy = PolicyMapper.getKeywordPolicy(policy);
            List<String> keywords = keywordPolicy.getKeywords();

            for (String keyword : keywords) {
                if (fieldValue.contains(keyword)) {
                    Flag flag = new Flag(
                            policy.getRuleId(),
                            canonicalMessage.getStableMessageId(),
                            policy.getDescription(),
                            canonicalMessage.getNetwork(),
                            canonicalMessage.getTenantId()
                    );
                    return Optional.of(flag);
                }
            }
            log.info("Message is fine: {}", canonicalMessage.getStableMessageId());
            return Optional.empty();
        } catch (IllegalArgumentException ex) {
            log.warn("Invalid input: {}", ex.getMessage());
            return Optional.empty();
        } catch (Exception ex) {
            log.error("Error during keyword evaluation: {}", ex.getMessage(), ex);
            return Optional.empty();
        }
    }

    private String getFieldValue(CanonicalMessage canonicalMessage, String field) {
        if ("subject".equalsIgnoreCase(field)) {
            return canonicalMessage.getContent().getSubject();
        } else if ("body".equalsIgnoreCase(field)) {
            return canonicalMessage.getContent().getBody();
        }
        return null;
    }
}
