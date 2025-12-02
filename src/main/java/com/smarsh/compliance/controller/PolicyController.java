package com.smarsh.compliance.controller;

import com.smarsh.compliance.entity.Policy;
import com.smarsh.compliance.exception.BadRequestException;
import com.smarsh.compliance.service.PolicyService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@AllArgsConstructor
public class PolicyController {
    private final PolicyService policyService;

    @PostMapping("/policy")
    public ResponseEntity<String> createPolicy(@RequestBody Policy policy) {
        if (policy == null) {
            throw new BadRequestException("Policy must not be null");
        }
        if (policy.getRuleId() == null || policy.getRuleId().isBlank()) {
            throw new BadRequestException("ruleId is required");
        }
        if (policy.getType() == null || policy.getType().isBlank()) {
            throw new BadRequestException("type is required (keyword or regex)");
        }
        if (!policy.getType().equalsIgnoreCase("keyword")
                && !policy.getType().equalsIgnoreCase("regex")) {
            throw new BadRequestException("Invalid type. Allowed values: keyword, regex");
        }
        if (policy.getField() == null || policy.getField().isBlank()) {
            throw new BadRequestException("field is required");
        }
        if (policy.getWhen() == null) {
            throw new BadRequestException("policy condition (when) must be defined");
        }

        try {
            log.info("Creating policy {}", policy);
            return policyService.addPolicy(policy);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid policy data: " + ex.getMessage(), ex);
        } catch (Exception ex) {
            log.error("Unexpected error while creating policy {}", policy, ex);
            throw new RuntimeException("Unable to create policy at this time. Please try again later.", ex);
        }
    }

    @GetMapping("/policy")
    public List<Policy> getAllPolicies() {
        try {
            return policyService.getAllPolicies();
        } catch (Exception ex) {
            log.error("Unexpected error while fetching policies", ex);
            throw new RuntimeException("Unable to fetch policies at this time. Please try again later.", ex);
        }
    }
}
