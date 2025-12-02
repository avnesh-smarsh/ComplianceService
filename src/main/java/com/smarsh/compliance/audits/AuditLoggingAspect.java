package com.smarsh.compliance.audits;


import com.smarsh.compliance.audits.service.AuditService;
import com.smarsh.compliance.entity.Flag;
import com.smarsh.compliance.entity.Policy;
import com.smarsh.compliance.models.CanonicalMessage;
import com.smarsh.compliance.models.TenantNotification;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

// Update the existing AuditLoggingAspect.java
@Aspect
@Component
@RequiredArgsConstructor
public class AuditLoggingAspect {
    private final AuditService auditService;

    // 1. Notification Publisher
    @Around("execution(* com.smarsh.compliance..*NotificationPublisher.publish*(..)) && args(notification)")
    public Object auditNotificationPublishing(ProceedingJoinPoint pjp, TenantNotification notification) throws Throwable {
        try {
            Object result = pjp.proceed();
            auditService.sendAuditLog(
                    notification.getTenantId(),
                    new AuditLogRequest(
                            notification.getMessageId(),
                            notification.getNetwork(), // Network may not be available in notification
                            "NOTIFICATION_SENT",
                            Map.of(
                                    "severity", notification.getSeverity(),
                                    "channel", pjp.getTarget().getClass().getSimpleName()
                            )
                    )
            );
            return result;
        } catch (Exception e) {
            auditService.sendAuditLog(
                    notification.getTenantId(),
                    new AuditLogRequest(
                            notification.getMessageId(),
                            notification.getNetwork(),
                            "NOTIFICATION_FAILED",
                            Map.of(
                                    "channel", pjp.getTarget().getClass().getSimpleName(),
                                    "error", e.getMessage()
                            )
                    )
            );
            throw e;
        }
    }

    // 2. Policy Evaluators (generic for all evaluator types)
    @Around(value = "execution(* com.smarsh.compliance..*Evaluator.evaluate(..)) && args(message, policy)", argNames = "pjp,message,policy")
    public Object auditPolicyEvaluation(ProceedingJoinPoint pjp, CanonicalMessage message, Policy policy) throws Throwable {
        try {
            @SuppressWarnings("unchecked")
            Optional<Flag> result = (Optional<Flag>) pjp.proceed();

            if (result.isPresent()) {
                auditService.sendAuditLog(
                        message.getTenantId(),
                        new AuditLogRequest(
                                message.getStableMessageId(),
                                message.getNetwork(),
                                "POLICY_VIOLATION",
                                Map.of(
                                        "policyId", policy.getRuleId(),
                                        "policyType", policy.getType(),
                                        "evaluator", pjp.getTarget().getClass().getSimpleName()
                                )
                        )
                );
            }
            return result;
        } catch (Exception e) {
            auditService.sendAuditLog(
                    message.getTenantId(),
                    new AuditLogRequest(
                            message.getStableMessageId(),
                            message.getNetwork(),
                            "POLICY_EVALUATION_ERROR",
                            Map.of(
                                    "policyId", policy.getRuleId(),
                                    "error", e.getMessage()
                            )
                    )
            );
            throw e;
        }
    }

    // 3. Compliance Service Processing
    @Around("execution(* com.smarsh.compliance..*ComplianceService.process(..)) && args(message)")
    public Object auditComplianceProcessing(ProceedingJoinPoint pjp, CanonicalMessage message) throws Throwable {
        try {
            Object result = pjp.proceed();
            auditService.sendAuditLog(
                    message.getTenantId(),
                    new AuditLogRequest(
                            message.getStableMessageId(),
                            message.getNetwork(),
                            "COMPLIANCE_PROCESSING_COMPLETE",
                            Map.of(
                                    "service", pjp.getTarget().getClass().getSimpleName(),
                                    "status", "success"
                            )
                    )
            );
            return result;
        } catch (Exception e) {
            auditService.sendAuditLog(
                    message.getTenantId(),
                    new AuditLogRequest(
                            message.getStableMessageId(),
                            message.getNetwork(),
                            "COMPLIANCE_PROCESSING_FAILED",
                            Map.of(
                                    "service", pjp.getTarget().getClass().getSimpleName(),
                                    "error", e.getMessage()
                            )
                    )
            );
            throw e;
        }
    }

    // 4. Flag Creation (already exists but enhanced)
    @Around("execution(* com.smarsh.compliance..*FlagService.saveFlag(..)) && args(flag)")
    public Object auditFlagCreation(ProceedingJoinPoint pjp, Flag flag) throws Throwable {
        try {
            System.out.println(flag+"flag created");
            Object result = pjp.proceed();
            auditService.sendAuditLog(
                    flag.getTenantId(),
                    new AuditLogRequest(
                            flag.getMessageId(),
                            flag.getNetwork(),
                            "FLAG_STORED_TO_DB",
                            Map.of(
                                    "policyId", flag.getRuleId()
                            )
                    )
            );
            return result;
        } catch (Exception e) {
            auditService.sendAuditLog(
                    flag.getTenantId(),
                    new AuditLogRequest(
                            flag.getMessageId(),
                            flag.getNetwork(),
                            "FLAG_STORE_FAILED",
                            Map.of("error", e.getMessage())
                    )
            );
            throw e;
        }
    }
}