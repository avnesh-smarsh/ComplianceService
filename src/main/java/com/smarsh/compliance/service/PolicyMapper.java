package com.smarsh.compliance.service;

import com.smarsh.compliance.entity.KeywordPolicy;
import com.smarsh.compliance.entity.Policy;
import com.smarsh.compliance.entity.RegexPolicy;

public class PolicyMapper {
    public static  KeywordPolicy getKeywordPolicy(Policy policy) {
        return (KeywordPolicy) policy;
    }

    public static RegexPolicy getRegexPolicy(Policy policy) {
        return (RegexPolicy) policy;
    }

    public static void setRegexPolicy(Policy policy, RegexPolicy regexPolicy) {
    }
}
