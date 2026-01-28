package pe.edu.vallegrande.vgmsauthentication.infrastructure.utils;

/**
 * Utilidad para extracción de tokens desde headers de autorización HTTP
 * Responsabilidad: Validar y extraer tokens Bearer del header Authorization
 */
public final class AuthorizationHeaderExtractor {

     private static final String BEARER_PREFIX = "Bearer ";
     private static final int BEARER_PREFIX_LENGTH = 7;

     private AuthorizationHeaderExtractor() {
          // Clase de utilidad - no instanciable
     }

     /**
      * Extrae el token del header Authorization
      * 
      * @throws IllegalArgumentException si el header es inválido
      */
     public static String extractToken(String authHeader) {
          if (authHeader == null || authHeader.isBlank()) {
               throw new IllegalArgumentException("Header de autorización no puede estar vacío");
          }

          if (!authHeader.startsWith(BEARER_PREFIX)) {
               throw new IllegalArgumentException("Header de autorización debe comenzar con 'Bearer '");
          }

          String token = authHeader.substring(BEARER_PREFIX_LENGTH);
          if (token.isBlank()) {
               throw new IllegalArgumentException("Token no puede estar vacío");
          }

          return token;
     }

     /**
      * Valida si el header contiene un token Bearer válido
      */
     public static boolean isValidBearerToken(String authHeader) {
          try {
               extractToken(authHeader);
               return true;
          } catch (IllegalArgumentException e) {
               return false;
          }
     }
}
