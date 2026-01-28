package pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.exception;

public class UserOrganizationMismatchException extends RuntimeException {
     public UserOrganizationMismatchException(String message) {
          super(message);
     }

     public UserOrganizationMismatchException(String message, Throwable cause) {
          super(message, cause);
     }
}
