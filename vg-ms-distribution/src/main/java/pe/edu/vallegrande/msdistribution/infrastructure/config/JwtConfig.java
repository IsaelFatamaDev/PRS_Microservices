package pe.edu.vallegrande.msdistribution.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders;

/**
 * Configuración para la decodificación de tokens JWT.
 * Define el decodificador reactivo basado en el issuer.
 * 
 * @version 1.0
 */
@Configuration
public class JwtConfig {

    /** URI del emisor de tokens (ej. Keycloak). */
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    /**
     * Crea un decodificador JWT reactivo.
     * Valida la firma del token contra el issuer configurado.
     * 
     * @return ReactiveJwtDecoder configurado.
     */
    @Bean
    public ReactiveJwtDecoder reactiveJwtDecoder() {
        return ReactiveJwtDecoders.fromIssuerLocation(issuerUri);
    }

}