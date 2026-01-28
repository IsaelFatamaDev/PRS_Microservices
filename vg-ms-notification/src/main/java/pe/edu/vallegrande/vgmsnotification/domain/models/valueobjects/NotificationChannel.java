package pe.edu.vallegrande.vgmsnotification.domain.models.valueobjects;

/**
 * Value Object - Canal de Notificación
 * Prioridad para zonas rurales: SMS > WhatsApp > Email > In-App
 */
public enum NotificationChannel {
     SMS, // PRIORIDAD 1 - Para zonas rurales sin internet
     WHATSAPP, // PRIORIDAD 2 - Si tiene WhatsApp registrado
     EMAIL, // PRIORIDAD 3 - Pocos tienen email
     IN_APP; // PRIORIDAD 4 - Notificaciones dentro de la app

     public boolean requiresPhone() {
          return this == SMS || this == WHATSAPP;
     }

     public boolean requiresEmail() {
          return this == EMAIL;
     }

     public boolean requiresInternet() {
          return this != SMS;
     }
}
