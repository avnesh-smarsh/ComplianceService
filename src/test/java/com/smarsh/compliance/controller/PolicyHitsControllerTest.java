package com.smarsh.compliance.controller;

import com.smarsh.compliance.dto.FlagDto;
import com.smarsh.compliance.service.PolicyHitsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PolicyHitsControllerTest {

    private PolicyHitsService service;
    private PolicyHitsController controller;

    @BeforeEach
    void setUp() {
        service = mock(PolicyHitsService.class);
        controller = new PolicyHitsController(service);
    }

    @Test
    void getPolicyHitsRoot_returnsFlagsAndHandlesNull() {
        FlagDto dto = new FlagDto("r1", "d", "m1", "t1", "net");
        when(service.getFlagsForTenantByRules("t1", null)).thenReturn(List.of(dto));

        ResponseEntity<List<FlagDto>> resp = controller.getPolicyHitsRoot("t1", null);
        assertEquals(200, resp.getStatusCodeValue());
        List<FlagDto> body = resp.getBody();
        assertNotNull(body);
        assertEquals(1, body.size());
        assertEquals("r1", body.get(0).getRuleId());
    }

    @Test
    void getPolicyHits_searchEndpoint_returnsEmptyListWhenServiceReturnsNull() {
        when(service.getFlagsForTenantByRules("t1", List.of("r1"))).thenReturn(null);
        ResponseEntity<List<FlagDto>> resp = controller.getPolicyHits("t1", List.of("r1"));
        assertEquals(200, resp.getStatusCodeValue());
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody().isEmpty());
    }

    @Test
    void searchPolicyHits_postSearch_withBody_callsService() {
        FlagDto dto = new FlagDto("r2", "desc", "m2", "t2", "net2");
        when(service.getFlagsForTenantByRules("t2", List.of("r2"))).thenReturn(List.of(dto));

        Map<String, List<String>> body = Map.of("ruleIds", List.of("r2"));
        ResponseEntity<List<FlagDto>> resp = controller.searchPolicyHits("t2", body);
        assertEquals(200, resp.getStatusCodeValue());
        List<FlagDto> list = resp.getBody();
        assertEquals(1, list.size());
        assertEquals("r2", list.get(0).getRuleId());
    }

    @Test
    void searchPolicyHits_postSearch_withNullBody_callsServiceWithNullRuleIds() {
        when(service.getFlagsForTenantByRules("t3", null)).thenReturn(Collections.emptyList());
        ResponseEntity<List<FlagDto>> resp = controller.searchPolicyHits("t3", null);
        assertEquals(200, resp.getStatusCodeValue());
        assertNotNull(resp.getBody());
        assertTrue(resp.getBody().isEmpty());
        verify(service, times(1)).getFlagsForTenantByRules("t3", null);
    }

    @Test
    void getRuleIds_returnsListAndHandlesNull() {
        when(service.getDistinctRuleIdsForTenant("tA")).thenReturn(List.of("a", "b"));
        ResponseEntity<List<String>> resp = controller.getRuleIds("tA");
        assertEquals(200, resp.getStatusCodeValue());
        assertEquals(2, resp.getBody().size());

        when(service.getDistinctRuleIdsForTenant("tB")).thenReturn(null);
        ResponseEntity<List<String>> resp2 = controller.getRuleIds("tB");
        assertEquals(200, resp2.getStatusCodeValue());
        assertNotNull(resp2.getBody());
        assertTrue(resp2.getBody().isEmpty());
    }
}
