package com.smarsh.compliance.evaluators;

import com.smarsh.compliance.entity.Flag;
import com.smarsh.compliance.entity.Policy;
import com.smarsh.compliance.entity.RegexPolicy;
import com.smarsh.compliance.models.CanonicalMessage;
import com.smarsh.compliance.service.PolicyMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class RegexEvaluatorWithUnionTest {

    private RegexEvaluator evaluator;

    @BeforeEach
    void setUp() {
        // Use the no-arg constructor simulated by tests; the real class has @AllArgsConstructor but
        // it doesn't require dependencies for evaluate(), so we can instantiate directly.
        evaluator = new RegexEvaluator();
    }

    @Test
    void supports_recognizesRegex() {
        assertTrue(evaluator.supports("regex"));
        assertTrue(evaluator.supports("REGEX"));
        assertFalse(evaluator.supports("keyword"));
        assertFalse(evaluator.supports(null));
    }


    @Test
    void evaluate_invalidPattern_returnsEmpty() {
        CanonicalMessage msg = mock(CanonicalMessage.class);
        Object messageContent = mock(Object.class, invocation -> {
            String m = invocation.getMethod().getName();
            if ("isString".equals(m)) return true;
            if ("string".equals(m)) return "some text";
            return invocation.callRealMethod();
        });


        Policy policy = mock(Policy.class);
        when(policy.getField()).thenReturn("subject");
        when(policy.getRuleId()).thenReturn("rule-bad");

        RegexPolicy rp = new RegexPolicy();


        try (MockedStatic<PolicyMapper> ms = mockStatic(PolicyMapper.class)) {
            ms.when(() -> PolicyMapper.getRegexPolicy(policy)).thenReturn(rp);

            Optional<Flag> res = evaluator.evaluate(msg, policy);
            assertTrue(res.isEmpty(), "Invalid regex pattern should be handled and return empty Optional");
        }
    }

    @Test
    void evaluate_nullInputs_returnEmpty() {
        Policy policy = mock(Policy.class);
        CanonicalMessage msg = mock(CanonicalMessage.class);

        assertTrue(evaluator.evaluate(null, policy).isEmpty());
        assertTrue(evaluator.evaluate(msg, null).isEmpty());
    }
}
