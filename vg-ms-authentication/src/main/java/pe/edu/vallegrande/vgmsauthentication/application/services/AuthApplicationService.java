package pe.edu.vallegrande.vgmsauthentication.application.services;

import pe.edu.vallegrande.vgmsauthentication.infrastructure.dto.request.ChangePasswordRequest;
import pe.edu.vallegrande.vgmsauthentication.infrastructure.dto.request.CreateAccountRequest;
import pe.edu.vallegrande.vgmsauthentication.infrastructure.dto.request.FirstPasswordChangeRequest;
import pe.edu.vallegrande.vgmsauthentication.infrastructure.dto.request.LoginRequest;
import pe.edu.vallegrande.vgmsauthentication.infrastructure.dto.response.ApiResponse;
import pe.edu.vallegrande.vgmsauthentication.infrastructure.dto.response.AuthResponse;
import pe.edu.vallegrande.vgmsauthentication.infrastructure.dto.response.CreateAccountResponse;
import reactor.core.publisher.Mono;

/**
 * Application Service - Casos de uso de autenticación
 * Coordina Domain Services y maneja la lógica de aplicación
 */
public interface AuthApplicationService {

    /**
     * Caso de uso: Crear cuenta desde MS-users
     * 1. Genera username (nombre.apellido@jass.gob.pe)
     * 2. Crea usuario en Keycloak con contraseña temporal
     * 3. Asigna roles
     * 4. Retorna username generado y contraseña temporal para que MS-users lo
     * guarde
     */
    Mono<ApiResponse<CreateAccountResponse>> createAccount(CreateAccountRequest request);

    /**
     * Caso de uso: Login de usuario
     * 1. Busca usuario por email en MS-users
     * 2. Genera username si no está cacheado
     * 3. Autentica contra Keycloak
     * 4. Retorna tokens + info de usuario
     */
    Mono<ApiResponse<AuthResponse>> login(LoginRequest request);

    /**
     * Caso de uso: Refresh token
     */
    Mono<ApiResponse<AuthResponse>> refreshToken(String refreshToken);

    /**
     * Caso de uso: Validar token
     */
    Mono<ApiResponse<Boolean>> validateToken(String accessToken);

    /**
     * Caso de uso: Logout
     */
    Mono<ApiResponse<Void>> logout(String accessToken);

    /**
     * Caso de uso: Obtener info de usuario autenticado
     */
    Mono<ApiResponse<AuthResponse.UserInfo>> getCurrentUser(String accessToken);

    /**
     * Caso de uso: Cambiar contraseña
     * 1. Valida la contraseña actual
     * 2. Valida que las nuevas contraseñas coincidan
     * 3. Actualiza en Keycloak
     */
    Mono<ApiResponse<String>> changePassword(String accessToken, ChangePasswordRequest request);

    /**
     * Primer cambio de contraseña (para contraseñas temporales, sin autenticación
     * previa)
     */
    Mono<ApiResponse<String>> firstPasswordChange(FirstPasswordChangeRequest request);

    /**
     * NUEVO: Renovar contraseña temporal cuando haya expirado
     */
    Mono<String> renewTemporaryPassword(String username);
}
