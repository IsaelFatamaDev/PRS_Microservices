package pe.edu.vallegrande.vgmsorganizations.application.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuración de Jackson para serialización/deserialización JSON
 */
@Configuration
public class JacksonConfig {

    /**
     * ObjectMapper configurado para:
     * - Soporte de Java 8 Time API (Instant, LocalDateTime, etc.)
     * - Fechas en formato ISO-8601 (no timestamps)
     * - No fallar en beans vacíos
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        return mapper;
    }
}


