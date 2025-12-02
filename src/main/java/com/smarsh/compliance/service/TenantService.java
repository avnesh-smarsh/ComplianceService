package com.smarsh.compliance.service;

import com.smarsh.compliance.entity.Tenant;
import com.smarsh.compliance.repository.TenantRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class TenantService {

    private final TenantRepository tenantRepository;

    @Transactional
    public String addTenant(Tenant tenant) {
        if (tenant == null || tenant.getTenantId() == null) {
            log.warn("Invalid tenant data: {}", tenant);
            return "Invalid tenant data";
        }
        try {
            return tenantRepository.findById(tenant.getTenantId())
                    .map(existing -> {
                        List<String> existingIds = existing.getPolicyIds();
                        for (String pid : tenant.getPolicyIds()) {
                            if (!existingIds.contains(pid)) {
                                existingIds.add(pid);
                            }
                        }
                        tenantRepository.save(existing);
                        return "Tenant updated with appended policy ids";
                    })
                    .orElseGet(() -> {
                        tenantRepository.save(tenant);
                        return "Tenant added successfully";
                    });
        } catch (Exception ex) {
            log.error("Error saving tenant {}: {}", tenant.getTenantId(), ex.getMessage(), ex);
            return "Failed to save tenant: " + ex.getMessage();
        }
    }

    public boolean verifyTenant(String tenantId) {
        return tenantRepository.findById(tenantId).isPresent();
    }

    public List<Tenant> getAllTenant() {
        try {
            return tenantRepository.findAll();
        } catch (Exception ex) {
            log.error("Error fetching tenants: {}", ex.getMessage(), ex);
            return Collections.emptyList();
        }
    }
}
