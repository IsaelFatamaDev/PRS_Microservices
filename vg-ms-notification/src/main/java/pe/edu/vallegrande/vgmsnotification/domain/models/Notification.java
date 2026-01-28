package pe.edu.vallegrande.vgmsnotification.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.vallegrande.vgmsnotification.domain.events.*;
import pe.edu.vallegrande.vgmsnotification.domain.models.valueobjects.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Aggregate Root - Notification
 * Representa una notificación enviada a un usuario
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

     private String id;
     private String userId;
     private NotificationChannel channel;
     private String recipient; // Teléfono, email, o userId para in-app
     private NotificationType type;
     private String subject; // Solo para EMAIL
     private String message;
     private NotificationStatus status;
     private NotificationPriority priority;

     // Metadata
     private String templateId;
     private Map<String, String> templateParams;

     // Proveedor y tracking
     private String providerName; // "OWN_NUMBER", "LOCAL_GATEWAY", etc.
     private String providerId; // ID del proveedor externo si aplica
     private String errorMessage;
     private Integer retryCount;

     // Timestamps
     private LocalDateTime scheduledAt;
     private LocalDateTime sentAt;
     private LocalDateTime deliveredAt;
     private LocalDateTime readAt;
     private LocalDateTime createdAt;
     private String createdBy;

     // Domain Events
     @Builder.Default
     private List<DomainEvent> domainEvents = new ArrayList<>();

     // Métodos de negocio

     public static Notification createNew(Notification notification) {
          notification.setStatus(NotificationStatus.PENDING);
          notification.setRetryCount(0);
          notification.setCreatedAt(LocalDateTime.now());
          notification.registerEvent(NotificationCreatedEvent.from(notification));
          return notification;
     }

     public void markAsSent() {
          this.status = NotificationStatus.SENT;
          this.sentAt = LocalDateTime.now();
          registerEvent(NotificationSentEvent.from(this));
     }

     public void markAsDelivered() {
          this.status = NotificationStatus.DELIVERED;
          this.deliveredAt = LocalDateTime.now();
          registerEvent(NotificationDeliveredEvent.from(this));
     }

     public void markAsRead() {
          this.status = NotificationStatus.READ;
          this.readAt = LocalDateTime.now();
          registerEvent(NotificationReadEvent.from(this));
     }

     public void markAsFailed(String errorMessage) {
          this.status = NotificationStatus.FAILED;
          this.errorMessage = errorMessage;
          registerEvent(NotificationFailedEvent.from(this, errorMessage));
     }

     public void incrementRetry() {
          this.retryCount = (this.retryCount == null ? 0 : this.retryCount) + 1;
          this.status = NotificationStatus.PENDING;
     }

     public boolean canRetry() {
          if (!status.canRetry())
               return false;
          int maxRetries = priority != null ? priority.getMaxRetries() : 3;
          return retryCount == null || retryCount < maxRetries;
     }

     public boolean isPending() {
          return status == NotificationStatus.PENDING;
     }

     public boolean isUrgent() {
          return priority == NotificationPriority.URGENT ||
                    (type != null && type.isUrgent());
     }

     // Gestión de eventos
     public void registerEvent(DomainEvent event) {
          this.domainEvents.add(event);
     }

     public List<DomainEvent> getDomainEvents() {
          return Collections.unmodifiableList(domainEvents);
     }

     public void clearDomainEvents() {
          this.domainEvents.clear();
     }
}
