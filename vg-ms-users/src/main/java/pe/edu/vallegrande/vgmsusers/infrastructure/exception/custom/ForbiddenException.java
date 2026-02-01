package pe.edu.vallegrande.vgmsusers.infrastructure.exception.custom;

import lombok.Getter;
import pe.edu.vallegrande.vgmsusers.infrastructure.exception.CustomException;

@Getter
public class ForbiddenException extends CustomException {

     public ForbiddenException(String message) {
          super(message, "FORBIDDEN", 403);
     }

     public ForbiddenException(String message, String errorCode) {
          super(message, errorCode, 403);
     }
}
