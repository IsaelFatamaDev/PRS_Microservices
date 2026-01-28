package pe.edu.vallegrande.msdistribution.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

/**
 * Configuración para la validación de beans (JSR-380).
 * Habilita la validación automática en el contexto de Spring.
 * 
 * @version 1.0
 */
@Configuration
public class ValidationConfig {

    /**
     * Define el validador local de fábrica.
     * 
     * @return LocalValidatorFactoryBean.
     */
    @Bean
    public LocalValidatorFactoryBean validator() {
        return new LocalValidatorFactoryBean();
    }
}