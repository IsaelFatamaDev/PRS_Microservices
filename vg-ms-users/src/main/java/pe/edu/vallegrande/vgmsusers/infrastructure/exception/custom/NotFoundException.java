package pe.edu.vallegrande.vgmsusers.infrastructure.exception.custom;

import lombok.Getter;
import pe.edu.vallegrande.vgmsusers.infrastructure.exception.CustomException;

@Getter
public class NotFoundException extends CustomException {

     public NotFoundException(String message) {
          super(message, "NOT_FOUND", 404);
     }

     public NotFoundException(String message, String errorCode, int httpStatus) {
          super(message, errorCode, httpStatus);
     }

     public static NotFoundException forResource(String resourceType, String identifier) {
          return new NotFoundException(
                    String.format("%s con identificador '%s' no encontrado", resourceType, identifier));
     }
}
