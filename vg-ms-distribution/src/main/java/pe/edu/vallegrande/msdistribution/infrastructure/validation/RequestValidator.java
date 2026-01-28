package pe.edu.vallegrande.msdistribution.infrastructure.validation;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.regex.Pattern;

/**
 * Componente validador de utilidades para requests.
 * Provee validaciones reactivas comunes para emails, teléfonos, etc.
 * 
 * @version 1.0
 */
@Component
public class RequestValidator {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[1-9]\\d{1,14}$");

    /**
     * Valida formato de email.
     * 
     * @param email Email a validar.
     * @return Mono<Boolean> true si es válido.
     */
    public Mono<Boolean> validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return Mono.just(false);
        }
        return Mono.just(EMAIL_PATTERN.matcher(email).matches());
    }

    /**
     * Valida formato de teléfono.
     * 
     * @param phone Teléfono a validar.
     * @return Mono<Boolean> true si es válido.
     */
    public Mono<Boolean> validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return Mono.just(false);
        }
        return Mono.just(PHONE_PATTERN.matcher(phone).matches());
    }

    /**
     * Valida que no sea vacío.
     * 
     * @param value Texto.
     * @return Mono<Boolean> true si no es vacío.
     */
    public Mono<Boolean> validateNotEmpty(String value) {
        return Mono.just(value != null && !value.trim().isEmpty());
    }

    /**
     * Valida longitud de texto.
     * 
     * @param value Texto.
     * @param min   Mínimo.
     * @param max   Máximo.
     * @return Mono<Boolean>.
     */
    public Mono<Boolean> validateLength(String value, int min, int max) {
        if (value == null) {
            return Mono.just(false);
        }
        int length = value.length();
        return Mono.just(length >= min && length <= max);
    }
}
