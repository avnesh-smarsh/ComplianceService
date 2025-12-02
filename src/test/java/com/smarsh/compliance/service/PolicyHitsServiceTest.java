package com.smarsh.compliance.service;

import com.smarsh.compliance.dto.FlagDto;
import com.smarsh.compliance.entity.Flag;
import com.smarsh.compliance.repository.FlagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PolicyHitsServiceTest {

    private FlagRepository flagRepository;
    private PolicyHitsService service;

    @BeforeEach
    void setUp() {
        flagRepository = mock(FlagRepository.class);
        service = new PolicyHitsService(flagRepository);
    }

    @Test
    void getFlagsForTenant_returnsMappedDtoList() {
        Flag f = new Flag();
        f.setRuleId("r1");
        f.setFlagDescription("desc");
        f.setMessageId("m1");
        f.setTenantId("t1");
        f.setNetwork("whatsapp");

        when(flagRepository.findByTenantId("t1")).thenReturn(List.of(f));

        List<FlagDto> res = service.getFlagsForTenant("t1");
        assertEquals(1, res.size());
        FlagDto dto = res.get(0);
        assertEquals("r1", dto.getRuleId());
        assertEquals("desc", dto.getFlagDescription());
        assertEquals("m1", dto.getMessageId());
        assertEquals("t1", dto.getTenantId());
        assertEquals("whatsapp", dto.getNetwork());
    }

    @Test
    void getFlagsForTenant_repositoryThrows_returnsEmptyList() {
        when(flagRepository.findByTenantId("t2")).thenThrow(new RuntimeException("db"));
        List<FlagDto> res = service.getFlagsForTenant("t2");
        assertNotNull(res);
        assertTrue(res.isEmpty());
    }

    @Test
    void getFlagsForTenantByRules_withNullRuleIds_callsFindByTenantId() {
        Flag f = new Flag();
        f.setRuleId("r2");
        f.setTenantId("tX");
        when(flagRepository.findByTenantId("tX")).thenReturn(List.of(f));

        List<FlagDto> res = service.getFlagsForTenantByRules("tX", null);
        assertEquals(1, res.size());
        assertEquals("r2", res.get(0).getRuleId());
        verify(flagRepository, times(1)).findByTenantId("tX");
        verify(flagRepository, never()).findByTenantIdAndRuleIdIn(anyString(), anyList());
    }

    @Test
    void getFlagsForTenantByRules_withRuleIds_callsFindByTenantIdAndRuleIdIn() {
        Flag f = new Flag();
        f.setRuleId("r3");
        f.setTenantId("tY");
        when(flagRepository.findByTenantIdAndRuleIdIn("tY", List.of("r3"))).thenReturn(List.of(f));

        List<FlagDto> res = service.getFlagsForTenantByRules("tY", List.of("r3"));
        assertEquals(1, res.size());
        assertEquals("r3", res.get(0).getRuleId());
        verify(flagRepository, times(1)).findByTenantIdAndRuleIdIn("tY", List.of("r3"));
    }

    @Test
    void getFlagsForTenantByRules_repositoryThrows_returnsEmptyList() {
        when(flagRepository.findByTenantIdAndRuleIdIn("t1", List.of("bad"))).thenThrow(new RuntimeException("boom"));
        List<FlagDto> res = service.getFlagsForTenantByRules("t1", List.of("bad"));
        assertNotNull(res);
        assertTrue(res.isEmpty());
    }

    @Test
    void getDistinctRuleIdsForTenant_returnsList() {
        when(flagRepository.findDistinctRuleIdsByTenantId("tenant1")).thenReturn(List.of("rA", "rB"));
        List<String> rules = service.getDistinctRuleIdsForTenant("tenant1");
        assertEquals(2, rules.size());
        assertTrue(rules.contains("rA"));
        assertTrue(rules.contains("rB"));
    }

    @Test
    void getDistinctRuleIdsForTenant_repositoryThrows_returnsEmptyList() {
        when(flagRepository.findDistinctRuleIdsByTenantId("tenantX")).thenThrow(new RuntimeException("err"));
        List<String> rules = service.getDistinctRuleIdsForTenant("tenantX");
        assertNotNull(rules);
        assertTrue(rules.isEmpty());
    }

    @Test
    void mapToDtoList_handlesNullFlags_returnsEmptyList() {
        // mapToDtoList is private; ensure public method handles repository returning null
        when(flagRepository.findByTenantId("tNull")).thenReturn(null);
        List<FlagDto> res = service.getFlagsForTenant("tNull");
        assertNotNull(res);
        assertTrue(res.isEmpty());
    }
}
