package pe.edu.vallegrande.vgmsusers.domain.exceptions;

public class UserNotFoundException extends RuntimeException {
     public UserNotFoundException(String message) {
          super(message);
     }
}
