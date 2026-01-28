package pe.edu.vallegrande.vgmsauthentication.infrastructure.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.Base64;

/**
 * Utilidad para extracción de información desde tokens JWT
 * Responsabilidad: Decodificación y extracción de campos del payload JWT
 */
@Slf4j
public final class JwtTokenExtractor {

     private JwtTokenExtractor() {
          // Clase de utilidad - no instanciable
     }

     /**
      * Decodifica el payload de un token JWT
      */
     public static String decodePayload(String token) {
          String[] tokenParts = token.split("\\.");
          if (tokenParts.length != 3) {
               throw new IllegalArgumentException("Token JWT inválido");
          }

          byte[] payload = Base64.getUrlDecoder().decode(tokenParts[1]);
          return new String(payload);
     }

     /**
      * Extrae un campo específico del payload JWT
      */
     public static String extractField(String payload, String fieldName) {
          try {
               String searchPattern = "\"" + fieldName + "\":\"";
               int start = payload.indexOf(searchPattern);
               if (start != -1) {
                    start += searchPattern.length();
                    int end = payload.indexOf("\"", start);
                    if (end > start) {
                         return payload.substring(start, end);
                    }
               }
               return null;
          } catch (Exception e) {
               log.warn("Error extrayendo campo {} del payload: {}", fieldName, e.getMessage());
               return null;
          }
     }

     /**
      * Extrae un campo específico directamente del token
      */
     public static String extractFieldFromToken(String token, String fieldName) {
          String payload = decodePayload(token);
          return extractField(payload, fieldName);
     }
}
