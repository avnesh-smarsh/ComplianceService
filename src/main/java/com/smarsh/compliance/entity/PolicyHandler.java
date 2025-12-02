package com.smarsh.compliance.entity;


import com.smarsh.compliance.models.CanonicalMessage;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


public interface PolicyHandler {
    PolicyHandler getNext();

    void setNext(PolicyHandler next);

    /**
     * Process the incoming message against given policies. Update flagged and violations counters when flags are produced.
     */
    void handle(CanonicalMessage message, List<Policy> policies, AtomicBoolean flagged, AtomicInteger violations);
}