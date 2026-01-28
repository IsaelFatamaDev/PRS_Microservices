package pe.edu.vallegrande.vgmsauthentication.infrastructure.utils;

import lombok.extern.slf4j.Slf4j;

import java.security.SecureRandom;

/**
 * Utilidad para generación de contraseñas seguras
 * Responsabilidad: Generación de contraseñas temporales y aleatorias
 */
@Slf4j
public final class PasswordGenerator {

     private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
     private static final int DEFAULT_LENGTH = 12;
     private static final SecureRandom RANDOM = new SecureRandom();

     private PasswordGenerator() {
          // Clase de utilidad - no instanciable
     }

     /**
      * Genera una contraseña temporal aleatoria de 12 caracteres
      */
     public static String generateTemporaryPassword() {
          return generatePassword(DEFAULT_LENGTH);
     }

     /**
      * Genera una contraseña de longitud específica
      */
     public static String generatePassword(int length) {
          if (length < 8) {
               throw new IllegalArgumentException("La longitud mínima de contraseña es 8 caracteres");
          }

          StringBuilder password = new StringBuilder(length);
          for (int i = 0; i < length; i++) {
               password.append(CHARS.charAt(RANDOM.nextInt(CHARS.length())));
          }

          String generatedPassword = password.toString();
          log.debug("🔑 Contraseña temporal generada");
          return generatedPassword;
     }
}
