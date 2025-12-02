package com.smarsh.compliance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TenantVerificationResponse {
    private String tenantId;
    private boolean exists;
}
