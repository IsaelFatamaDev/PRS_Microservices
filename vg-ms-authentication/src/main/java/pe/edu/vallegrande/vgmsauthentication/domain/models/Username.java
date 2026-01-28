package pe.edu.vallegrande.vgmsauthentication.domain.models;

import lombok.Getter;

import java.text.Normalizer;

/**
 * Value Object para username
 * Genera automáticamente username en formato email desde firstName y lastName
 */
@Getter
public class Username {
     private final String value;
     private static final String EMAIL_DOMAIN = "@jass.gob.pe";

     private Username(String value) {
          this.value = value;
     }

     /**
      * Genera username desde nombres en formato email
      * Ejemplo: "José María" + "García López" -> "jose.garcia@jass.gob.pe"
      * Toma el PRIMER nombre y el PRIMER apellido
      */
     public static Username fromNames(String firstName, String lastName) {
          // Extraer primer nombre
          String firstNamePart = extractFirstPart(firstName);
          // Extraer primer apellido
          String lastNamePart = extractFirstPart(lastName);

          // Generar email: nombre.apellido@jass.gob.pe
          String email = firstNamePart + "." + lastNamePart + EMAIL_DOMAIN;
          return new Username(email.toLowerCase());
     }

     /**
      * Extrae la primera parte de un nombre completo (primer nombre o primer
      * apellido)
      * Ejemplo: "José María" -> "jose", "García López" -> "garcia"
      */
     private static String extractFirstPart(String fullName) {
          if (fullName == null || fullName.trim().isEmpty()) {
               return "";
          }

          // Dividir por espacios y tomar la primera parte
          String[] parts = fullName.trim().split("\\s+");
          String firstPart = parts[0];

          // Normalizar (eliminar acentos y caracteres especiales)
          return normalize(firstPart);
     }

     /**
      * Normaliza texto eliminando acentos y caracteres especiales
      */
     private static String normalize(String text) {
          if (text == null || text.isEmpty()) {
               return "";
          }

          // Eliminar acentos
          String normalized = Normalizer.normalize(text, Normalizer.Form.NFD);
          normalized = normalized.replaceAll("\\p{M}", "");

          // Eliminar espacios y caracteres especiales
          normalized = normalized.replaceAll("[^a-zA-Z]", "");

          return normalized;
     }
}
