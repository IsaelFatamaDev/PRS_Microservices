package pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
public class JacksonConfig {

     @Bean
     @Primary
     public ObjectMapper objectMapper(Jackson2ObjectMapperBuilder builder) {
          ObjectMapper objectMapper = builder.createXmlMapper(false).build();

          // Módulos necesarios
          objectMapper.registerModule(new JavaTimeModule());

          // Optimizaciones de serialización
          objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
          objectMapper.configure(SerializationFeature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS, false);
          objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

          // Optimizaciones de deserialización
          objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
          objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);

          return objectMapper;
     }
}
