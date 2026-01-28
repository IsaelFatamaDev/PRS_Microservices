package pe.edu.vallegrande.vgmsusers.domain.exceptions;

public class OrganizationNotFoundException extends RuntimeException {
     public OrganizationNotFoundException(String message) {
          super(message);
     }
}
