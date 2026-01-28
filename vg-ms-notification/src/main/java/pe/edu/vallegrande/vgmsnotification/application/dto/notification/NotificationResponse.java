package pe.edu.vallegrande.vgmsnotification.application.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {
    private String id;
    private String userId;
    private String channel;
    private String recipient;
    private String type;
    private String subject;
    private String message;
    private String status;
    private String priority;
    private String templateId;
    private Map<String, String> templateParams;
    private String providerName;
    private String providerId;
    private String errorMessage;
    private Integer retryCount;
    private LocalDateTime scheduledAt;
    private LocalDateTime sentAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
    private String createdBy;
}
