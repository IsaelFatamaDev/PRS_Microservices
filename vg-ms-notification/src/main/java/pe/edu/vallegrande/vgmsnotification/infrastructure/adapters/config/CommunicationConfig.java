package pe.edu.vallegrande.vgmsnotification.infrastructure.adapters.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuración para servicios externos de comunicación
 * WhatsApp y SMS usando gateways propios (NO cloud providers)
 */
@Configuration
public class CommunicationConfig {

     @Value("${whatsapp.api.url:http://localhost:3001}")
     private String whatsappApiUrl;

     @Value("${sms.gateway.url:http://localhost:3002}")
     private String smsGatewayUrl;

     @Bean(name = "whatsappWebClient")
     public WebClient whatsappWebClient() {
          return WebClient.builder()
                    .baseUrl(whatsappApiUrl)
                    .defaultHeader("Content-Type", "application/json")
                    .build();
     }

     @Bean(name = "smsWebClient")
     public WebClient smsWebClient() {
          return WebClient.builder()
                    .baseUrl(smsGatewayUrl)
                    .defaultHeader("Content-Type", "application/json")
                    .build();
     }
}
