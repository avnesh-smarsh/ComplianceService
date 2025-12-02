package com.smarsh.compliance.entity;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = KeywordPolicy.class, name = "keyword"),
        @JsonSubTypes.Type(value = RegexPolicy.class, name = "regex")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Policy {
    @Id
    private String ruleId;
    private String version;
    @Column(name = "type", insertable = false, updatable = false)
    private String type;
    @Embedded
    private PolicyCondition when;
    private String field;
    private String description;

    public Policy(String rule1, String subject, String desc, String regexPolicy) {
    }
}