package pe.edu.vallegrande.ms_infraestructura.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JacksonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Registrar el módulo JavaTime para manejar fechas ISO automáticamente
        mapper.registerModule(new JavaTimeModule());
        
        // Configurar para que maneje diferentes formatos de fecha ISO automáticamente
        mapper.findAndRegisterModules();
        
        return mapper;
    }
}
