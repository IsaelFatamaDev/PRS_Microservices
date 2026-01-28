package pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.Authentication;
import org.springframework.web.reactive.function.client.ClientRequest;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {

     @Value("${microservices.users.base-url}")
     private String usersServiceBaseUrl;

     @Value("${webclient.timeout:5000}")
     private int timeout;

     @Bean(name = "usersWebClient")
     public WebClient usersWebClient() {
          // Pool de conexiones optimizado
          ConnectionProvider connectionProvider = ConnectionProvider.builder("users-pool")
                    .maxConnections(5)
                    .pendingAcquireMaxCount(10)
                    .pendingAcquireTimeout(Duration.ofSeconds(5))
                    .maxIdleTime(Duration.ofSeconds(30))
                    .maxLifeTime(Duration.ofMinutes(5))
                    .evictInBackground(Duration.ofSeconds(60))
                    .build();

          // Cliente HTTP optimizado con timeouts
          HttpClient httpClient = HttpClient.create(connectionProvider)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeout)
                    .responseTimeout(Duration.ofMillis(timeout))
                    .doOnConnected(conn -> conn
                              .addHandlerLast(new ReadTimeoutHandler(timeout, TimeUnit.MILLISECONDS))
                              .addHandlerLast(new WriteTimeoutHandler(timeout, TimeUnit.MILLISECONDS)))
                    .compress(true);

          // Estrategias con buffer reducido (2MB en lugar de 10MB)
          ExchangeStrategies strategies = ExchangeStrategies.builder()
                    .codecs(configurer -> configurer
                              .defaultCodecs()
                              .maxInMemorySize(2 * 1024 * 1024)) // 2MB
                    .build();

          return WebClient.builder()
                    .baseUrl(usersServiceBaseUrl)
                    .clientConnector(new ReactorClientHttpConnector(httpClient))
                    .exchangeStrategies(strategies)
                    .filter((request, next) -> ReactiveSecurityContextHolder.getContext()
                              .map(SecurityContext::getAuthentication)
                              .map(Authentication::getCredentials)
                              .map(credentials -> {
                                   if (credentials instanceof org.springframework.security.oauth2.jwt.Jwt) {
                                        return ((org.springframework.security.oauth2.jwt.Jwt) credentials)
                                                  .getTokenValue();
                                   }
                                   return credentials.toString();
                              })
                              .map(token -> {
                                   return ClientRequest.from(request)
                                             .headers(headers -> headers.setBearerAuth(token))
                                             .build();
                              })
                              .defaultIfEmpty(request)
                              .flatMap(next::exchange))
                    .build();
     }
}
