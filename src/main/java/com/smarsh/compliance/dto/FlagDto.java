package com.smarsh.compliance.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FlagDto {
    private String ruleId;
    private String flagDescription;
    private String messageId;
    private String tenantId;
    private String network;
}
