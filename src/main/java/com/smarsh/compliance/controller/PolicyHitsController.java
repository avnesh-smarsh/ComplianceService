package com.smarsh.compliance.controller;

import com.smarsh.compliance.dto.FlagDto;
import com.smarsh.compliance.service.PolicyHitsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/tenants/{tenantId}/policy-hits")
@AllArgsConstructor
@CrossOrigin(origins = "*")
public class PolicyHitsController {

    private final PolicyHitsService policyHitsService;

    // Allow GET on the base path: GET /tenants/{tenantId}/policy-hits
    @GetMapping
    public ResponseEntity<List<FlagDto>> getPolicyHitsRoot(
            @PathVariable("tenantId") String tenantId,
            @RequestParam(value = "ruleId", required = false) List<String> ruleIds
    ) {
        log.info("GET /tenants/{}/policy-hits called with ruleIds={}", tenantId, ruleIds);
        List<FlagDto> flags = policyHitsService.getFlagsForTenantByRules(tenantId, ruleIds);
        return ResponseEntity.ok(flags == null ? Collections.emptyList() : flags);
    }

    // Keep the explicit /search GET (if you want an explicit path)
    @GetMapping("/search")
    public ResponseEntity<List<FlagDto>> getPolicyHits(
            @PathVariable("tenantId") String tenantId,
            @RequestParam(value = "ruleId", required = false) List<String> ruleIds
    ) {
        log.info("GET /search for tenant {} ruleIds={}", tenantId, ruleIds);
        List<FlagDto> flags = policyHitsService.getFlagsForTenantByRules(tenantId, ruleIds);
        return ResponseEntity.ok(flags == null ? Collections.emptyList() : flags);
    }

    // POST /search with body { "ruleIds": ["r1","r2"] }
    @PostMapping("/search")
    public ResponseEntity<List<FlagDto>> searchPolicyHits(
            @PathVariable("tenantId") String tenantId,
            @RequestBody(required = false) Map<String, List<String>> body
    ) {
        log.info("POST /search for tenant {} bodyKeys={}", tenantId, body == null ? null : body.keySet());
        List<String> ruleIds = body == null ? null : body.get("ruleIds");
        List<FlagDto> flags = policyHitsService.getFlagsForTenantByRules(tenantId, ruleIds);
        return ResponseEntity.ok(flags == null ? Collections.emptyList() : flags);
    }

    // Return distinct rule ids; more descriptive path
    @GetMapping("/ruleId")
    public ResponseEntity<List<String>> getRuleIds(@PathVariable("tenantId") String tenantId) {
        log.info("GET /rules for tenant {}", tenantId);
        List<String> rules = policyHitsService.getDistinctRuleIdsForTenant(tenantId);
        return ResponseEntity.ok(rules == null ? Collections.emptyList() : rules);
    }
}
