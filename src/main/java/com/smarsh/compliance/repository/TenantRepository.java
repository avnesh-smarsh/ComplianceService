package com.smarsh.compliance.repository;

import com.smarsh.compliance.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;


public interface TenantRepository extends JpaRepository<Tenant,String> {
    Optional<Tenant> findByTenantId(String tenantId);
    @Query("select distinct f.policyIds from Tenant f where f.tenantId = :tenantId")
    List<String> findDistinctRuleIdsByTenantId(String tenantId);
}

