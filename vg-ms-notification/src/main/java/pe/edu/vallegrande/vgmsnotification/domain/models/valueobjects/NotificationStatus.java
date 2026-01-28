package pe.edu.vallegrande.vgmsnotification.domain.models.valueobjects;

/**
 * Value Object - Estado de Notificación
 */
public enum NotificationStatus {
     PENDING, // Esperando ser enviada
     PROCESSING, // En proceso de envío
     SENT, // Enviada exitosamente
     DELIVERED, // Entregada al destinatario
     READ, // Leída por el usuario
     FAILED, // Falló el envío
     CANCELLED; // Cancelada

     public boolean isCompleted() {
          return this == SENT || this == DELIVERED || this == READ;
     }

     public boolean isFinal() {
          return this == DELIVERED || this == READ || this == FAILED || this == CANCELLED;
     }

     public boolean canRetry() {
          return this == FAILED || this == PENDING;
     }
}
