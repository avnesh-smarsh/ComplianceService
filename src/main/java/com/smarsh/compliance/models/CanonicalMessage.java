package com.smarsh.compliance.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Getter
@Setter
public class CanonicalMessage {
    @Id
    private String stableMessageId;
    private String tenantId;
    private String sender;
    private String network;
    private Content content;
    private Context context;
    private FlagInfo flagInfo;
    @Field(type = FieldType.Date, format = DateFormat.epoch_millis)
    private Instant policyEvaluationTimestamp;
    private String rawReference;
}



