package com.smarsh.compliance.entity;

import jakarta.persistence.Access;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@DiscriminatorValue("regex")
@AllArgsConstructor
@NoArgsConstructor
public class RegexPolicy extends Policy {
    private String pattern;
}
