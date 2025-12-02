package com.smarsh.compliance.repository;

import com.smarsh.compliance.entity.Policy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PolicyRepository extends JpaRepository<Policy, String> {
}
