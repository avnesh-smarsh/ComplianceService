package com.smarsh.compliance.evaluators;

import com.smarsh.compliance.entity.Flag;
import com.smarsh.compliance.entity.Policy;
import com.smarsh.compliance.models.CanonicalMessage;

import java.util.Optional;

public interface PolicyEvaluator {
    public boolean supports(String type);

    public Optional<Flag> evaluate(CanonicalMessage canonicalMessage, Policy policy);
}
