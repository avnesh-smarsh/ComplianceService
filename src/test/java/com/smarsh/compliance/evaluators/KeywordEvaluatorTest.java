package com.smarsh.compliance.evaluators;

import com.smarsh.compliance.entity.Policy;
import com.smarsh.compliance.models.CanonicalMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class KeywordEvaluatorTest {

    private KeywordEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new KeywordEvaluator();
    }

    @Test
    void supports_keywordType_true() {
        assertTrue(evaluator.supports("keyword"));
        assertTrue(evaluator.supports("KEYword"));
    }

    @Test
    void supports_nonKeyword_false() {
        assertFalse(evaluator.supports("regex"));
        assertFalse(evaluator.supports("somethingElse"));
        assertFalse(evaluator.supports(null));
    }

    @Test
    void evaluate_nullMessageOrPolicy_returnsEmpty() {
        Policy policy = mock(Policy.class);
        CanonicalMessage message = mock(CanonicalMessage.class);

        assertEquals(Optional.empty(), evaluator.evaluate(null, policy));
        assertEquals(Optional.empty(), evaluator.evaluate(message, null));
    }

}
