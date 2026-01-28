package pe.edu.vallegrande.vgmsnotification.domain.exceptions;

public class NotificationNotFoundException extends RuntimeException {
     public NotificationNotFoundException(String notificationId) {
          super("Notification not found with id: " + notificationId);
     }
}
