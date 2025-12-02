package com.smarsh.compliance.config;


//import com.smarsh.compliance.chain.EvaluatorAdapterHandler;
//import com.smarsh.compliance.chain.PolicyHandler;
import com.smarsh.compliance.entity.PolicyHandler;
import com.smarsh.compliance.evaluators.EvaluatorAdapterHandler;
import com.smarsh.compliance.evaluators.PolicyEvaluator;
import com.smarsh.compliance.service.FlagService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


import java.util.List;


@Configuration
public class ChainConfiguration {


    @Bean
    public PolicyHandler policyHandlerChain(List<PolicyEvaluator> evaluators, FlagService flagService) {
        PolicyHandler head = null;
        PolicyHandler prev = null;


        for (PolicyEvaluator e : evaluators) {
            EvaluatorAdapterHandler handler = new EvaluatorAdapterHandler(e, flagService);
            if (head == null) head = handler;
            if (prev != null) prev.setNext(handler);
            prev = handler;
        }


// if you want to append custom handlers, do it here (e.g. new KeywordHandler(flagService))


        return head;
    }
}