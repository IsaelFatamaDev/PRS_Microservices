package pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.client.validator;

import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validador para respuestas de clientes externos.
 * Valida timeouts, formatos y reglas de negocio de servicios externos.
 */
@Component
public class ExternalClientValidator {
    
    private static final Logger log = LoggerFactory.getLogger(ExternalClientValidator.class);
    
    /**
     * Valida que una respuesta externa no sea nula
     */
    public <T> boolean validateNotNull(T response, String serviceName) {
        if (response == null) {
            log.error("Respuesta nula del servicio externo: {}", serviceName);
            return false;
        }
        return true;
    }
    
    /**
     * Valida que una respuesta contenga los campos requeridos
     */
    public boolean validateRequiredFields(Object response, String... fieldNames) {
        if (response == null) {
            log.error("No se puede validar campos de una respuesta nula");
            return false;
        }
        
        // Aquí se implementaría la lógica de validación de campos específicos
        // Por ahora retorna true como placeholder
        log.debug("Validando campos requeridos: {}", (Object[]) fieldNames);
        return true;
    }
    
    /**
     * Valida el formato de una respuesta externa
     */
    public boolean validateResponseFormat(Object response, Class<?> expectedType) {
        if (response == null) {
            log.error("Respuesta nula al validar formato");
            return false;
        }
        
        if (!expectedType.isInstance(response)) {
            log.error("Tipo de respuesta inválido. Esperado: {}, Recibido: {}", 
                expectedType.getSimpleName(), response.getClass().getSimpleName());
            return false;
        }
        
        log.debug("Formato de respuesta válido: {}", expectedType.getSimpleName());
        return true;
    }
    
    /**
     * Valida reglas de negocio específicas en la respuesta
     */
    public boolean validateBusinessRules(Object response, String serviceName) {
        log.debug("Validando reglas de negocio para servicio: {}", serviceName);
        
        // Implementar validaciones de negocio específicas según el servicio
        // Por ahora retorna true como placeholder
        return true;
    }
    
    /**
     * Valida que el tiempo de respuesta esté dentro de límites aceptables
     */
    public boolean validateResponseTime(long responseTimeMs, long maxTimeMs, String serviceName) {
        if (responseTimeMs > maxTimeMs) {
            log.warn("Tiempo de respuesta excedido para {}: {}ms (máximo: {}ms)", 
                serviceName, responseTimeMs, maxTimeMs);
            return false;
        }
        
        log.debug("Tiempo de respuesta aceptable para {}: {}ms", serviceName, responseTimeMs);
        return true;
    }
}
