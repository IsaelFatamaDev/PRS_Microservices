package pe.edu.vallegrande.vgmsnotification.domain.models.valueobjects;

/**
 * Value Object - Prioridad de Notificación
 */
public enum NotificationPriority {
     URGENT, // Envío inmediato (2FA, credenciales)
     HIGH, // Alta prioridad (pagos vencidos, alertas críticas)
     NORMAL, // Prioridad normal (recibos, recordatorios)
     LOW; // Baja prioridad (anuncios, newsletters)

     public int getMaxRetries() {
          return switch (this) {
               case URGENT -> 5;
               case HIGH -> 3;
               case NORMAL -> 2;
               case LOW -> 1;
          };
     }

     public long getRetryDelayMinutes() {
          return switch (this) {
               case URGENT -> 1;
               case HIGH -> 5;
               case NORMAL -> 15;
               case LOW -> 60;
          };
     }
}
