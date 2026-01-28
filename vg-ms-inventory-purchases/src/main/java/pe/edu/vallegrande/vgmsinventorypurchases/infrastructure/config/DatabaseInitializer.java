package pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.config;

import io.r2dbc.spi.ConnectionFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.r2dbc.connection.init.CompositeDatabasePopulator;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DatabaseInitializer {

     @Value("${spring.application.name}")
     private String applicationName;

     @Bean
     @ConditionalOnProperty(name = "app.database.initialize", havingValue = "true", matchIfMissing = false)
     public ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {
          log.info("Inicializando base de datos para la aplicación: {}", applicationName);

          ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
          initializer.setConnectionFactory(connectionFactory);

          CompositeDatabasePopulator populator = new CompositeDatabasePopulator();
          populator.addPopulators(new ResourceDatabasePopulator(new ClassPathResource("database/script.sql")));
          initializer.setDatabasePopulator(populator);

          log.info("Se ejecutará el script SQL para inicializar las tablas");

          return initializer;
     }
}
