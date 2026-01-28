package pe.edu.vallegrande.vgmsauthentication.infrastructure.mappers;

import pe.edu.vallegrande.vgmsauthentication.domain.models.Username;
import pe.edu.vallegrande.vgmsauthentication.infrastructure.dto.response.CreateAccountResponse;

/**
 * Mapper para conversiones entre DTOs y Domain models
 * Centraliza la lógica de transformación
 */
public class UserMapper {

     private UserMapper() {
          // Constructor privado para clase utilitaria
     }

     /**
      * Convierte firstName y lastName a Value Object Username
      *
      * @param firstName Primer nombre del usuario
      * @param lastName  Primer apellido del usuario
      * @return Username generado en formato email
      */
     public static Username toUsername(String firstName, String lastName) {
          return Username.fromNames(firstName, lastName);
     }

     /**
      * Crea CreateAccountResponse desde datos básicos
      *
      * @param userId            ID del usuario en Keycloak
      * @param username          Username generado
      * @param temporaryPassword Contraseña temporal
      * @param accountEnabled    Estado de la cuenta
      * @param message           Mensaje informativo
      * @return DTO de respuesta
      */
     public static CreateAccountResponse toCreateAccountResponse(
               String userId,
               String username,
               String temporaryPassword,
               boolean accountEnabled,
               String message) {
          return CreateAccountResponse.builder()
                    .userId(userId)
                    .username(username)
                    .temporaryPassword(temporaryPassword)
                    .accountEnabled(accountEnabled)
                    .message(message)
                    .build();
     }

     /**
      * Extrae username como String desde Value Object
      *
      * @param username Value Object Username
      * @return String del username
      */
     public static String getUsernameValue(Username username) {
          return username.getValue();
     }
}
