package com.smarsh.compliance.service;

import com.smarsh.compliance.entity.Tenant;
import com.smarsh.compliance.repository.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TenantServiceTest {

    private TenantRepository tenantRepository;
    private TenantService tenantService;

    @BeforeEach
    void setUp() {
        tenantRepository = mock(TenantRepository.class);
        tenantService = new TenantService(tenantRepository);
    }

    @Test
    void addTenant_nullTenant_returnsInvalid() {
        String result = tenantService.addTenant(null);
        assertEquals("Invalid tenant data", result);
        verifyNoInteractions(tenantRepository);
    }

    @Test
    void addTenant_nullTenantId_returnsInvalid() {
        Tenant tenant = new Tenant();
        tenant.setTenantId(null);

        String result = tenantService.addTenant(tenant);
        assertEquals("Invalid tenant data", result);
        verifyNoInteractions(tenantRepository);
    }

    @Test
    void addTenant_newTenant_savesSuccessfully() {
        Tenant tenant = new Tenant();
        tenant.setTenantId("t1");
        tenant.setPolicyIds(new ArrayList<>(List.of("p1")));

        when(tenantRepository.findById("t1")).thenReturn(Optional.empty());

        String result = tenantService.addTenant(tenant);

        assertEquals("Tenant added successfully", result);
        verify(tenantRepository, times(1)).save(tenant);
    }

    @Test
    void addTenant_existingTenant_appendsPolicyIds() {
        Tenant existing = new Tenant();
        existing.setTenantId("t2");
        existing.setPolicyIds(new ArrayList<>(List.of("p1")));

        Tenant incoming = new Tenant();
        incoming.setTenantId("t2");
        incoming.setPolicyIds(new ArrayList<>(List.of("p2", "p1"))); // p1 already exists, p2 new

        when(tenantRepository.findById("t2")).thenReturn(Optional.of(existing));

        String result = tenantService.addTenant(incoming);

        assertEquals("Tenant updated with appended policy ids", result);
        assertTrue(existing.getPolicyIds().contains("p1"));
        assertTrue(existing.getPolicyIds().contains("p2"));
        verify(tenantRepository, times(1)).save(existing);
    }

    @Test
    void addTenant_repositoryThrows_returnsErrorMessage() {
        Tenant tenant = new Tenant();
        tenant.setTenantId("t3");
        tenant.setPolicyIds(new ArrayList<>());

        when(tenantRepository.findById("t3")).thenThrow(new RuntimeException("DB error"));

        String result = tenantService.addTenant(tenant);
        assertTrue(result.startsWith("Failed to save tenant:"));
    }

    @Test
    void verifyTenant_existing_returnsTrue() {
        Tenant tenant = new Tenant();
        tenant.setTenantId("t4");

        when(tenantRepository.findById("t4")).thenReturn(Optional.of(tenant));

        assertTrue(tenantService.verifyTenant("t4"));
    }

    @Test
    void verifyTenant_missing_returnsFalse() {
        when(tenantRepository.findById("missing")).thenReturn(Optional.empty());
        assertFalse(tenantService.verifyTenant("missing"));
    }

    @Test
    void getAllTenant_success_returnsList() {
        Tenant t = new Tenant();
        t.setTenantId("t5");
        when(tenantRepository.findAll()).thenReturn(List.of(t));

        List<Tenant> result = tenantService.getAllTenant();
        assertEquals(1, result.size());
        assertEquals("t5", result.get(0).getTenantId());
    }

    @Test
    void getAllTenant_repositoryThrows_returnsEmptyList() {
        when(tenantRepository.findAll()).thenThrow(new RuntimeException("db fail"));
        List<Tenant> result = tenantService.getAllTenant();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
