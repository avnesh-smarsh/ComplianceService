package com.smarsh.compliance.evaluators;//package com.smarsh.compliance.chain;


import com.smarsh.compliance.entity.AbstractPolicyHandler;
import com.smarsh.compliance.entity.Policy;
import com.smarsh.compliance.models.CanonicalMessage;
import com.smarsh.compliance.service.FlagService;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


/**
 * Adapter to turn existing PolicyEvaluator implementations into chain handlers.
 * Keeps evaluator implementations unchanged while enabling chain-of-responsibility wiring.
 */
public class EvaluatorAdapterHandler extends AbstractPolicyHandler {


    private final PolicyEvaluator evaluator;
    private final FlagService flagService;


    public EvaluatorAdapterHandler(PolicyEvaluator evaluator, FlagService flagService) {
        this.evaluator = evaluator;
        this.flagService = flagService;
    }


    @Override
    protected void process(CanonicalMessage message, List<Policy> policies, AtomicBoolean flagged, AtomicInteger violations) {
        if (message == null || policies == null || policies.isEmpty()) return;


// filter policies supported by this evaluator
        List<Policy> supported = policies.stream()
                .filter(p -> evaluator.supports(p.getType()))
                .collect(Collectors.toList());


        for (Policy p : supported) {
            try {
                evaluator.evaluate(message, p).ifPresent(flag -> {
                    flagService.saveFlag(flag);
                    flagged.set(true);
                    violations.getAndIncrement();
                });
            } catch (Exception ex) {
// swallow to keep chain running; real implementation should log
            }
        }
    }
}