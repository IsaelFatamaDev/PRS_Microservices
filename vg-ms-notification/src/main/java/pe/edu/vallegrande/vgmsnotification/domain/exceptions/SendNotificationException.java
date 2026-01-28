package pe.edu.vallegrande.vgmsnotification.domain.exceptions;

public class SendNotificationException extends RuntimeException {
     public SendNotificationException(String message) {
          super(message);
     }

     public SendNotificationException(String message, Throwable cause) {
          super(message, cause);
     }
}
