package com.smarsh.compliance.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@Data
@NoArgsConstructor
@Table
public class Flag {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String flagId;
    private String flagDescription;
    private String ruleId;
    private String messageId;
    private String tenantId;
    private String network;

    @Column(nullable = false, updatable = false)
    private Long createdAt;

    public Flag(String ruleId, String messageId, String flagDescription, String network, String tenantId) {
        this.flagDescription = flagDescription;
        this.ruleId = ruleId;
        this.messageId = messageId;
        this.createdAt = Instant.now().toEpochMilli();
        this.tenantId = tenantId;
        this.network = network;
    }
}