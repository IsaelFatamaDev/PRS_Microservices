package pe.edu.vallegrande.vgmsusers.application.service;

import pe.edu.vallegrande.vgmsusers.domain.models.User;
import pe.edu.vallegrande.vgmsusers.infrastructure.dto.ApiResponse;
import pe.edu.vallegrande.vgmsusers.infrastructure.dto.response.CreateAccountResponse;
import reactor.core.publisher.Mono;

/**
 * Servicio para la integración con el microservicio de autenticación
 */
public interface UserAuthIntegrationService {

     Mono<ApiResponse<String>> registerUserInAuthService(User user, String temporaryPassword);

     Mono<CreateAccountResponse> registerUserWithAutoPassword(User user);
}
