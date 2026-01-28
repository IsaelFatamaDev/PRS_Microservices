package pe.edu.vallegrande.vgmsnotification.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.vallegrande.vgmsnotification.domain.events.DomainEvent;
import pe.edu.vallegrande.vgmsnotification.domain.events.TemplateCreatedEvent;
import pe.edu.vallegrande.vgmsnotification.domain.events.TemplateUpdatedEvent;
import pe.edu.vallegrande.vgmsnotification.domain.models.valueobjects.NotificationChannel;
import pe.edu.vallegrande.vgmsnotification.domain.models.valueobjects.TemplateStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Aggregate Root - NotificationTemplate
 * Plantilla reutilizable para notificaciones
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationTemplate {

     private String id;
     private String code; // RECEIPT_SMS, PAYMENT_CONFIRMATION_EMAIL, etc.
     private String name;
     private NotificationChannel channel;
     private String subject; // Solo para EMAIL
     private String template; // "Hola {userName}, tu recibo {receiptCode} por S/{amount}..."
     private List<String> variables; // ["userName", "receiptCode", "amount"]
     private TemplateStatus status;
     private LocalDateTime createdAt;
     private String createdBy;
     private LocalDateTime updatedAt;
     private String updatedBy;

     // Domain Events
     @Builder.Default
     private List<DomainEvent> domainEvents = new ArrayList<>();

     // Métodos de negocio

     public static NotificationTemplate createNew(NotificationTemplate template) {
          template.setStatus(TemplateStatus.DRAFT);
          template.setCreatedAt(LocalDateTime.now());
          template.registerEvent(TemplateCreatedEvent.from(template));
          return template;
     }

     public void activate() {
          this.status = TemplateStatus.ACTIVE;
          this.updatedAt = LocalDateTime.now();
     }

     public void deactivate() {
          this.status = TemplateStatus.INACTIVE;
          this.updatedAt = LocalDateTime.now();
     }

     public void updateContent(String template, List<String> variables, String updatedBy) {
          this.template = template;
          this.variables = variables;
          this.updatedBy = updatedBy;
          this.updatedAt = LocalDateTime.now();
          registerEvent(TemplateUpdatedEvent.from(this));
     }

     public boolean isActive() {
          return status == TemplateStatus.ACTIVE;
     }

     public String renderTemplate(java.util.Map<String, String> params) {
          String rendered = template;
          if (params != null && !params.isEmpty()) {
               for (java.util.Map.Entry<String, String> entry : params.entrySet()) {
                    rendered = rendered.replace("{" + entry.getKey() + "}", entry.getValue());
               }
          }
          return rendered;
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
