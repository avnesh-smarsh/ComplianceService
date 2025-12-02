package com.smarsh.compliance.controller;

import com.smarsh.compliance.entity.Policy;
import com.smarsh.compliance.exception.BadRequestException;
import com.smarsh.compliance.service.PolicyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PolicyControllerTest {

    private PolicyService policyService;
    private PolicyController controller;

    @BeforeEach
    void setUp() {
        policyService = mock(PolicyService.class);
        controller = new PolicyController(policyService);
    }

    @Test
    void createPolicy_nullPolicy_throwsBadRequest() {
        BadRequestException ex = assertThrows(BadRequestException.class, () -> controller.createPolicy(null));
        assertTrue(ex.getMessage().contains("Policy must not be null"));
        verifyNoInteractions(policyService);
    }

    @Test
    void createPolicy_missingRuleId_throwsBadRequest() {
        Policy p = new Policy();
        p.setRuleId("");
        p.setType("keyword");
        p.setField("body");

        BadRequestException ex = assertThrows(BadRequestException.class, () -> controller.createPolicy(p));
        assertTrue(ex.getMessage().contains("ruleId is required"));
        verifyNoInteractions(policyService);
    }

    @Test
    void createPolicy_invalidType_throwsBadRequest() {
        Policy p = new Policy();
        p.setRuleId("r1");
        p.setType("invalidType");
        p.setField("body");


        BadRequestException ex = assertThrows(BadRequestException.class, () -> controller.createPolicy(p));
        assertTrue(ex.getMessage().contains("Invalid type"));
        verifyNoInteractions(policyService);
    }

    @Test
    void createPolicy_missingField_throwsBadRequest() {
        Policy p = new Policy();
        p.setRuleId("r1");
        p.setType("keyword");
        p.setField("");

        BadRequestException ex = assertThrows(BadRequestException.class, () -> controller.createPolicy(p));
        assertTrue(ex.getMessage().contains("field is required"));
        verifyNoInteractions(policyService);
    }

    @Test
    void createPolicy_missingWhen_throwsBadRequest() {
        Policy p = new Policy();
        p.setRuleId("r1");
        p.setType("regex");
        p.setField("subject");
        p.setWhen(null);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> controller.createPolicy(p));
        assertTrue(ex.getMessage().contains("policy condition (when) must be defined"));
        verifyNoInteractions(policyService);
    }


    @Test
    void getAllPolicies_success_invokesServiceAndReturnsList() {
        Policy p = new Policy();
        p.setRuleId("rX");
        when(policyService.getAllPolicies()).thenReturn(List.of(p));

        List<Policy> res = controller.getAllPolicies();
        assertEquals(1, res.size());
        assertEquals("rX", res.get(0).getRuleId());
        verify(policyService, times(1)).getAllPolicies();
    }
}
