package com.smarsh.compliance.service;

import com.smarsh.compliance.entity.Policy;
import com.smarsh.compliance.repository.FlagRepository;
import com.smarsh.compliance.repository.PolicyRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class PolicyService {

    private final FlagRepository flagRepository;
    private final PolicyRepository policyRepository;

    List<Policy> getPoliciesByIds(List<String> policyIds) {
        List<Policy> policies = new ArrayList<>();
        try {
            if (policyIds == null || policyIds.isEmpty()) {
                return Collections.emptyList();
            }
            policyIds.forEach(policyId -> {
                try {
                    Optional<Policy> policy = policyRepository.findById(policyId);
                    policy.ifPresent(policies::add);
                } catch (Exception ex) {
                    log.warn("Error fetching policy {}: {}", policyId, ex.getMessage());
                }
            });
        } catch (Exception ex) {
            log.error("Error fetching policies for IDs {}: {}", policyIds, ex.getMessage(), ex);
            return Collections.emptyList();
        }
        return policies;
    }

    public ResponseEntity<String> addPolicy(Policy policy) {
        try {
            policyRepository.save(policy);
            return ResponseEntity.ok("Policy added successfully");
        } catch (Exception ex) {
            log.error("Error saving policy {}: {}", policy != null ? policy.getRuleId() : "null", ex.getMessage(), ex);
            return ResponseEntity.badRequest().body("Failed to add policy: " + ex.getMessage());
        }
    }

    public List<Policy> getAllPolicies() {
        try {
            return policyRepository.findAll();
        } catch (Exception ex) {
            log.error("Error fetching all policies: {}", ex.getMessage(), ex);
            return Collections.emptyList();
        }
    }
}
