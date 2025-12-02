package com.smarsh.compliance.repository;

import com.smarsh.compliance.entity.Flag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FlagRepository extends JpaRepository<Flag, String> {
    List<Flag> findByTenantId(String tenantId);
    List<Flag> findByTenantIdAndRuleIdIn(String tenantId, List<String> ruleIds);

    @Query("select distinct f.ruleId from Flag f where f.tenantId = :tenantId")
    List<String> findDistinctRuleIdsByTenantId(String tenantId);
}
