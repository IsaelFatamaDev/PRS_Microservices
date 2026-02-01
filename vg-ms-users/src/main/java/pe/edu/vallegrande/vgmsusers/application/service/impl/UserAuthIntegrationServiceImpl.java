package pe.edu.vallegrande.vgmsusers.application.service.impl;

import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsusers.application.service.UserAuthIntegrationService;
import pe.edu.vallegrande.vgmsusers.domain.models.User;
import pe.edu.vallegrande.vgmsusers.infrastructure.client.internal.AuthenticationClient;
import pe.edu.vallegrande.vgmsusers.infrastructure.dto.ApiResponse;
import pe.edu.vallegrande.vgmsusers.infrastructure.dto.request.UserCredentialRequest;
import pe.edu.vallegrande.vgmsusers.infrastructure.dto.response.CreateAccountResponse;
import reactor.core.publisher.Mono;

@Service
public class UserAuthIntegrationServiceImpl implements UserAuthIntegrationService {

     private final AuthenticationClient authenticationClient;

     public UserAuthIntegrationServiceImpl(AuthenticationClient authenticationClient) {
          this.authenticationClient = authenticationClient;
     }

     @Override
     public Mono<ApiResponse<String>> registerUserInAuthService(User user, String temporaryPassword) {
          return authenticationClient.isServiceAvailable()
                    .flatMap(available -> {
                         if (!available) {
                              return Mono.just(ApiResponse.<String>builder()
                                        .success(false)
                                        .message("El servicio de autenticación no está disponible, el usuario se creará sin credenciales")
                                        .build());
                         }

                         // MS-AUTHENTICATION genera el username automáticamente desde firstName y
                         // lastName
                         // No necesitamos generarlo aquí
                         UserCredentialRequest request = UserCredentialRequest.builder()
                                   .email(user.getContact().getEmail())
                                   .firstName(user.getPersonalInfo().getFirstName())
                                   .lastName(user.getPersonalInfo().getLastName())
                                   .temporaryPassword(temporaryPassword) // documentNumber
                                   .organizationId(user.getOrganizationId())
                                   .roles(user.getRoles())
                                   .userCode(user.getUserCode())
                                   .userId(user.getId())
                                   .build();

                         return authenticationClient.registerUserInKeycloak(request);
                    });
     }

     @Override
     public Mono<CreateAccountResponse> registerUserWithAutoPassword(User user) {
          return authenticationClient.isServiceAvailable()
                    .flatMap(available -> {
                         if (!available) {
                              return Mono.error(new RuntimeException("MS-AUTHENTICATION no disponible"));
                         }

                         // MS-AUTHENTICATION genera el username automáticamente desde firstName y
                         // lastName
                         // La contraseña temporal es el documentNumber del usuario
                         UserCredentialRequest request = UserCredentialRequest.builder()
                                   .email(user.getContact().getEmail())
                                   .firstName(user.getPersonalInfo().getFirstName())
                                   .lastName(user.getPersonalInfo().getLastName())
                                   .temporaryPassword(user.getPersonalInfo().getDocumentNumber()) // Password =
                                                                                                  // documentNumber
                                   .organizationId(user.getOrganizationId())
                                   .roles(user.getRoles())
                                   .userCode(user.getUserCode())
                                   .userId(user.getId())
                                   .build();

                         return authenticationClient.createAccountWithFullResponse(request)
                                   .map(response -> {
                                        if (response.isSuccess()) {
                                             return response.getData();
                                        } else {
                                             throw new RuntimeException(
                                                       "Error en MS-AUTHENTICATION: " + response.getMessage());
                                        }
                                   });
                    });
     }
}
