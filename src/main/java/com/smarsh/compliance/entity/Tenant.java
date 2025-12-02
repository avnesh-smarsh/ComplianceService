package com.smarsh.compliance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Tenant {
    @Id
    private String tenantId;
    private String email;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "tenant_policy_ids",
            joinColumns = @JoinColumn(name = "tenant_policy_id")
    )
    @Column(name = "policy_id")
    private List<String> policyIds = new ArrayList<>();
}
