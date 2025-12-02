package com.smarsh.compliance.evaluators;//package com.smarsh.compliance.chain;


import com.smarsh.compliance.entity.AbstractPolicyHandler;
import com.smarsh.compliance.entity.Flag;
import com.smarsh.compliance.entity.KeywordPolicy;
import com.smarsh.compliance.entity.Policy;
import com.smarsh.compliance.models.CanonicalMessage;
import com.smarsh.compliance.service.PolicyMapper;
import com.smarsh.compliance.service.FlagService;


import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


public class KeywordHandler extends AbstractPolicyHandler {
    private final FlagService flagService;


    public KeywordHandler(FlagService flagService) {
        this.flagService = flagService;
    }


    @Override
    protected void process(CanonicalMessage message, List<Policy> policies, AtomicBoolean flagged, AtomicInteger violations) {
        for (Policy p : policies) {
            if (p == null || p.getType() == null) continue;
            if (!"keyword".equalsIgnoreCase(p.getType())) continue;


            try {
                KeywordPolicy kp = PolicyMapper.getKeywordPolicy(p);
                String fieldValue = null;
                if ("subject".equalsIgnoreCase(p.getField())) fieldValue = message.getContent().getSubject();
                else if ("body".equalsIgnoreCase(p.getField())) fieldValue = message.getContent().getBody();


                if (fieldValue == null) continue;


                for (String keyword : kp.getKeywords()) {
                    if (fieldValue.contains(keyword)) {
                        Flag flag = new Flag(p.getRuleId(), message.getStableMessageId(), p.getDescription(), message.getNetwork(), message.getTenantId());
                        flagService.saveFlag(flag);
                        flagged.set(true);
                        violations.getAndIncrement();
                        break; // once this policy flags, move to next policy
                    }
                }
            } catch (Exception ex) {
// swallow and continue
            }
        }
    }
}