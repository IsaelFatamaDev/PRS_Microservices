package pe.edu.vallegrande.vgmsusers.domain.exceptions;

public class DuplicateUserException extends RuntimeException {
     public DuplicateUserException(String message) {
          super(message);
     }
}
