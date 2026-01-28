package pe.edu.vallegrande.vgmsnotification.domain.exceptions;

public class TemplateNotFoundException extends RuntimeException {
     public TemplateNotFoundException(String templateCode) {
          super("Template not found with code: " + templateCode);
     }
}
