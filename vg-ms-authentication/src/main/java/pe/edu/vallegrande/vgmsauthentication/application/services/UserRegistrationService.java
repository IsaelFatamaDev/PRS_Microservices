package pe.edu.vallegrande.vgmsauthentication.application.services;

import pe.edu.vallegrande.vgmsauthentication.infrastructure.dto.request.RegisterUserRequest;
import reactor.core.publisher.Mono;

/**
 * Servicio para gestionar el registro de usuarios en Keycloak
 */
public interface UserRegistrationService {

     /**
      * Registra un usuario en Keycloak
      *
      * @param request Datos del usuario a registrar
      * @return ID del usuario en Keycloak
      */
     Mono<String> registerUser(RegisterUserRequest request);
}
