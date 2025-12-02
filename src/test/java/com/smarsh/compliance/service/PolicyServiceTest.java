package com.smarsh.compliance.service;

import com.smarsh.compliance.entity.Policy;
import com.smarsh.compliance.repository.FlagRepository;
import com.smarsh.compliance.repository.PolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PolicyServiceTest {

    private FlagRepository flagRepository;
    private PolicyRepository policyRepository;
    private PolicyService policyService;

    @BeforeEach
    void setUp() {
        flagRepository = mock(FlagRepository.class);
        policyRepository = mock(PolicyRepository.class);
        policyService = new PolicyService(flagRepository, policyRepository);
    }

    @Test
    void getPoliciesByIds_nullOrEmpty_returnsEmptyList() {
        List<Policy> res1 = policyService.getPoliciesByIds(null);
        assertTrue(res1.isEmpty());

        List<Policy> res2 = policyService.getPoliciesByIds(Collections.emptyList());
        assertTrue(res2.isEmpty());
    }

    @Test
    void getPoliciesByIds_existingIds_returnsPolicies() {
        Policy p1 = new Policy();
        p1.setRuleId("r1");
        Policy p2 = new Policy();
        p2.setRuleId("r2");

        when(policyRepository.findById("r1")).thenReturn(Optional.of(p1));
        when(policyRepository.findById("r2")).thenReturn(Optional.of(p2));

        List<Policy> result = policyService.getPoliciesByIds(Arrays.asList("r1", "r2"));

        assertEquals(2, result.size());
        assertTrue(result.contains(p1));
        assertTrue(result.contains(p2));
    }

    @Test
    void getPoliciesByIds_repositoryThrows_logsAndContinues() {
        Policy p1 = new Policy();
        p1.setRuleId("r1");
        when(policyRepository.findById("r1")).thenReturn(Optional.of(p1));
        when(policyRepository.findById("bad")).thenThrow(new RuntimeException("db"));

        List<Policy> result = policyService.getPoliciesByIds(Arrays.asList("r1", "bad"));

        // Should have gracefully skipped the bad id and returned the good one
        assertEquals(1, result.size());
        assertEquals("r1", result.get(0).getRuleId());
    }

    @Test
    void addPolicy_success_returnsOk() {
        Policy p = new Policy();
        p.setRuleId("newRule");

        when(policyRepository.save(p)).thenReturn(p);

        ResponseEntity<String> resp = policyService.addPolicy(p);
        assertTrue(resp.getStatusCode().is2xxSuccessful());
        assertEquals("Policy added successfully", resp.getBody());

        ArgumentCaptor<Policy> captor = ArgumentCaptor.forClass(Policy.class);
        verify(policyRepository, times(1)).save(captor.capture());
        assertEquals("newRule", captor.getValue().getRuleId());
    }

    @Test
    void addPolicy_saveThrows_returnsBadRequest() {
        Policy p = new Policy();
        p.setRuleId("badRule");

        when(policyRepository.save(p)).thenThrow(new RuntimeException("db error"));

        ResponseEntity<String> resp = policyService.addPolicy(p);
        assertTrue(resp.getStatusCode().is4xxClientError());
        assertTrue(resp.getBody().contains("Failed to add policy"));
    }

    @Test
    void getAllPolicies_success_returnsList() {
        Policy p = new Policy();
        p.setRuleId("rAll");
        when(policyRepository.findAll()).thenReturn(List.of(p));

        List<Policy> res = policyService.getAllPolicies();
        assertEquals(1, res.size());
        assertEquals("rAll", res.get(0).getRuleId());
    }

    @Test
    void getAllPolicies_repositoryThrows_returnsEmptyList() {
        when(policyRepository.findAll()).thenThrow(new RuntimeException("boom"));
        List<Policy> res = policyService.getAllPolicies();
        assertTrue(res.isEmpty());
    }
}
