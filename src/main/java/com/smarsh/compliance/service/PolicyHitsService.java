package com.smarsh.compliance.service;

import com.smarsh.compliance.dto.FlagDto;
import com.smarsh.compliance.entity.Flag;
import com.smarsh.compliance.repository.FlagRepository;
import com.smarsh.compliance.repository.TenantRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class PolicyHitsService {

    private final FlagRepository flagRepository;
    private final TenantRepository tenantRepository;

    public List<FlagDto> getFlagsForTenant(String tenantId) {
        try {
            return mapToDtoList(flagRepository.findByTenantId(tenantId));
        } catch (Exception ex) {
            log.error("Error fetching flags for tenant {}: {}", tenantId, ex.getMessage(), ex);
            return Collections.emptyList();
        }
    }

    public List<FlagDto> getFlagsForTenantByRules(String tenantId, List<String> ruleIds) {
        try {
            if (ruleIds == null || ruleIds.isEmpty()) {
                return getFlagsForTenant(tenantId);
            }
            return mapToDtoList(flagRepository.findByTenantIdAndRuleIdIn(tenantId, ruleIds));
        } catch (Exception ex) {
            log.error("Error fetching flags for tenant {} with rules {}: {}", tenantId, ruleIds, ex.getMessage(), ex);
            return Collections.emptyList();
        }
    }

    public List<String> getDistinctRuleIdsForTenant(String tenantId) {
        try {
            return tenantRepository.findDistinctRuleIdsByTenantId(tenantId);
        } catch (Exception ex) {
            log.error("Error fetching distinct ruleIds for tenant {}: {}", tenantId, ex.getMessage(), ex);
            return Collections.emptyList();
        }
    }

    private List<FlagDto> mapToDtoList(List<Flag> flags) {
        if (flags == null) {
            return Collections.emptyList();
        }
        return flags.stream()
                .map(f -> new FlagDto(
                        f.getRuleId(),
                        f.getFlagDescription(),
                        f.getMessageId(),
                        f.getTenantId(),
                        f.getNetwork()))
                .collect(Collectors.toList());
    }
}
