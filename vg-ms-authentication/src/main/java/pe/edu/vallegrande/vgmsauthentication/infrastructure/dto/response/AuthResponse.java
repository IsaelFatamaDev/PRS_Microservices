package pe.edu.vallegrande.vgmsauthentication.infrastructure.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Response estándar para operaciones de autenticación
 */
@Data
@Builder
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;
    private long refreshExpiresIn;
    private UserInfo userInfo;

    /**
     * Información del usuario autenticado
     */
    @Data
    @Builder
    public static class UserInfo {
        private String userId;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String organizationId;
        private Set<String> roles;
        private boolean mustChangePassword;
        private LocalDateTime lastLogin;
    }
}
