package pe.edu.vallegrande.vgmsnotification.infrastructure.persistence.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notifications")
@CompoundIndex(def = "{'createdAt': -1}")
public class NotificationDocument {
    
    @Id
    private String id;
    
    @Indexed
    private String userId;
    
    @Indexed
    private String channel;
    
    private String recipient;
    
    @Indexed
    private String type;
    
    private String subject;
    private String message;
    
    @Indexed
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
    
    @Indexed
    private LocalDateTime createdAt;
    private String createdBy;
    
    // TTL: auto-borrar después de 6 meses
    @Indexed(expireAfterSeconds = 15552000)  // 180 días
    private LocalDateTime expiresAt;
}
