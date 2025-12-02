package com.smarsh.compliance.evaluators;

import com.smarsh.compliance.entity.Policy;
import com.smarsh.compliance.models.CanonicalMessage;
import com.smarsh.compliance.service.FlagService;
import com.smarsh.compliance.entity.Flag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EvaluatorAdapterHandlerTest {

    private PolicyEvaluator evaluator;
    private FlagService flagService;
    private EvaluatorAdapterHandler handler;

    @BeforeEach
    void setUp() {
        evaluator = mock(PolicyEvaluator.class);
        flagService = mock(FlagService.class);
        handler = new EvaluatorAdapterHandler(evaluator, flagService);
    }

    @Test
    void process_nullOrEmptyInputs_doNothing() {
        AtomicBoolean flagged = new AtomicBoolean(false);
        AtomicInteger violations = new AtomicInteger(0);

        // null message
        handler.process(null, List.of(), flagged, violations);
        assertFalse(flagged.get());
        assertEquals(0, violations.get());
        verifyNoInteractions(evaluator, flagService);

        // null policies
        handler.process(mock(CanonicalMessage.class), null, flagged, violations);
        assertFalse(flagged.get());
        assertEquals(0, violations.get());
        verifyNoInteractions(evaluator, flagService);

        // empty policies
        handler.process(mock(CanonicalMessage.class), List.of(), flagged, violations);
        assertFalse(flagged.get());
        assertEquals(0, violations.get());
        verifyNoInteractions(evaluator, flagService);
    }

    @Test
    void process_filtersUnsupportedPolicies() {
        CanonicalMessage message = mock(CanonicalMessage.class);

        Policy supported = mock(Policy.class);
        when(supported.getType()).thenReturn("keyword");
        when(supported.getRuleId()).thenReturn("r1");

        Policy unsupported = mock(Policy.class);
        when(unsupported.getType()).thenReturn("regex");
        when(unsupported.getRuleId()).thenReturn("r2");

        when(evaluator.supports("keyword")).thenReturn(true);
        when(evaluator.supports("regex")).thenReturn(false);

        // evaluator returns empty for supported policy
        when(evaluator.evaluate(eq(message), eq(supported))).thenReturn(Optional.empty());

        AtomicBoolean flagged = new AtomicBoolean(false);
        AtomicInteger violations = new AtomicInteger(0);

        handler.process(message, List.of(supported, unsupported), flagged, violations);

        // verify evaluator evaluated only the supported policy
        verify(evaluator, times(1)).evaluate(eq(message), eq(supported));
        verify(evaluator, never()).evaluate(eq(message), eq(unsupported));

        verifyNoInteractions(flagService);
        assertFalse(flagged.get());
        assertEquals(0, violations.get());
    }

    @Test
    void process_whenEvaluatorReturnsFlag_savesFlagAndUpdatesCounters() {
        CanonicalMessage message = mock(CanonicalMessage.class);

        Policy p1 = mock(Policy.class);
        when(p1.getType()).thenReturn("keyword");
        when(p1.getRuleId()).thenReturn("r1");

        Policy p2 = mock(Policy.class);
        when(p2.getType()).thenReturn("keyword");
        when(p2.getRuleId()).thenReturn("r2");

        when(evaluator.supports("keyword")).thenReturn(true);

        Flag flag1 = mock(Flag.class);
        Flag flag2 = mock(Flag.class);

        when(evaluator.evaluate(eq(message), eq(p1))).thenReturn(Optional.of(flag1));
        when(evaluator.evaluate(eq(message), eq(p2))).thenReturn(Optional.of(flag2));

        AtomicBoolean flagged = new AtomicBoolean(false);
        AtomicInteger violations = new AtomicInteger(0);

        handler.process(message, List.of(p1, p2), flagged, violations);

        // flagService.saveFlag called twice with the produced flags
        verify(flagService, times(1)).saveFlag(flag1);
        verify(flagService, times(1)).saveFlag(flag2);

        assertTrue(flagged.get());
        assertEquals(2, violations.get());
    }

    @Test
    void process_whenEvaluatorThrows_exceptionIsSwallowedAndProcessingContinues() {
        CanonicalMessage message = mock(CanonicalMessage.class);

        Policy p1 = mock(Policy.class);
        when(p1.getType()).thenReturn("keyword");
        when(p1.getRuleId()).thenReturn("r1");

        Policy p2 = mock(Policy.class);
        when(p2.getType()).thenReturn("keyword");
        when(p2.getRuleId()).thenReturn("r2");

        when(evaluator.supports("keyword")).thenReturn(true);

        // first policy throws
        try {
            when(evaluator.evaluate(eq(message), eq(p1))).thenThrow(new RuntimeException("boom"));
        } catch (Exception ignored) {}

        // second returns a flag
        Flag flag2 = mock(Flag.class);
        try {
            when(evaluator.evaluate(eq(message), eq(p2))).thenReturn(Optional.of(flag2));
        } catch (Exception ignored) {}

        AtomicBoolean flagged = new AtomicBoolean(false);
        AtomicInteger violations = new AtomicInteger(0);

        // Should not throw despite evaluator throwing for p1
        assertDoesNotThrow(() -> handler.process(message, List.of(p1, p2), flagged, violations));

        // saveFlag should be called once for p2
        verify(flagService, times(1)).saveFlag(flag2);
        assertTrue(flagged.get());
        assertEquals(1, violations.get());
    }
}
