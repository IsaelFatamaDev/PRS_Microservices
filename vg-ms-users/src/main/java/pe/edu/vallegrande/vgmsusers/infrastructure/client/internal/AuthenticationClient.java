package pe.edu.vallegrande.vgmsusers.infrastructure.client.internal;

import pe.edu.vallegrande.vgmsusers.infrastructure.dto.ApiResponse;
import pe.edu.vallegrande.vgmsusers.infrastructure.dto.request.UserCredentialRequest;
import pe.edu.vallegrande.vgmsusers.infrastructure.dto.response.CreateAccountResponse;
import reactor.core.publisher.Mono;

public interface AuthenticationClient {

     Mono<ApiResponse<String>> registerUserInKeycloak(UserCredentialRequest request);

     Mono<ApiResponse<CreateAccountResponse>> createAccountWithFullResponse(UserCredentialRequest request);

     Mono<Boolean> isServiceAvailable();
}
