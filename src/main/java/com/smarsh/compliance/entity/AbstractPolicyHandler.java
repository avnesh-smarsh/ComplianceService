package com.smarsh.compliance.entity;//package com.smarsh.compliance.chain;


import com.smarsh.compliance.entity.Policy;
import com.smarsh.compliance.entity.PolicyHandler;
import com.smarsh.compliance.models.CanonicalMessage;


import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


public abstract class AbstractPolicyHandler implements PolicyHandler {
    private PolicyHandler next;


    @Override
    public void setNext(PolicyHandler next) {
        this.next = next;
    }


    @Override
    public PolicyHandler getNext() {
        return next;
    }


    @Override
    public void handle(CanonicalMessage message, List<Policy> policies, AtomicBoolean flagged, AtomicInteger violations) {
        try {
            process(message, policies, flagged, violations);
        } catch (Exception ex) {
// each handler should guard itself and not break the chain
// logging is intentionally left for implementations to use the application's logger
        }


        if (next != null) {
            next.handle(message, policies, flagged, violations);
        }
    }


    /**
     * concrete handlers implement this to do their work for matching policies
     */
    protected abstract void process(CanonicalMessage message, List<Policy> policies, AtomicBoolean flagged, AtomicInteger violations);
}