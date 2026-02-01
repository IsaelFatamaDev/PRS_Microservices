package pe.edu.vallegrande.vgmsusers.infrastructure.util;

import org.springframework.stereotype.Component;

@Component
public class UsernameGeneratorUtil {

    private static final String DEFAULT_USER = "usuario";

    public String generateIntelligentUsername(String firstName, String firstLastName, String secondLastName) {
        String cleanFirstName = cleanAndNormalize(firstName);
        String cleanFirstLastName = cleanAndNormalize(firstLastName);
        String cleanSecondLastName = cleanAndNormalize(secondLastName);

        String firstNamePart = getFirstWord(cleanFirstName).toLowerCase();
        String lastNamePart = processFirstLastName(cleanFirstLastName).toLowerCase();

        StringBuilder username = new StringBuilder();
        username.append(firstNamePart).append(".").append(lastNamePart);

        if (cleanSecondLastName != null && !cleanSecondLastName.trim().isEmpty()) {
            String secondInitial = String.valueOf(cleanSecondLastName.trim().charAt(0)).toLowerCase();
            username.append(".").append(secondInitial);
        }

        username.append("@jass.gob.pe");
        return username.toString();
    }

    public String generateSimpleUsername(String firstName, String firstLastName) {
        String name = firstName != null ? firstName.trim().split("\\s+")[0].toLowerCase() : DEFAULT_USER;
        String lastname = firstLastName != null ? firstLastName.trim().split("\\s+")[0].toLowerCase() : "temporal";
        return name + "." + lastname + "@jass.gob.pe";
    }

    private String processFirstLastName(String firstLastName) {
        if (firstLastName == null || firstLastName.trim().isEmpty()) {
            return DEFAULT_USER;
        }

        String[] words = firstLastName.trim().split("\\s+");

        for (String word : words) {
            if (word.length() > 2) {
                return word;
            }
        }

        if (words.length > 0) {
            return words[words.length - 1];
        }

        return DEFAULT_USER;
    }

    private String getFirstWord(String text) {
        if (text == null || text.trim().isEmpty()) {
            return DEFAULT_USER;
        }

        String[] words = text.trim().split("\\s+");
        return words[0];
    }

    public String cleanAndNormalize(String text) {
        if (text == null) {
            return null;
        }

        return text.trim()
                .replace("Á", "A").replace("É", "E").replace("Í", "I")
                .replace("Ó", "O").replace("Ú", "U").replace("Ñ", "N")
                .replace("á", "a").replace("é", "e").replace("í", "i")
                .replace("ó", "o").replace("ú", "u").replace("ñ", "n")
                .replaceAll("[^A-Za-z0-9\\s]", "");
    }
}
