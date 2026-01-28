package pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.config;

import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
@EnableReactiveMongoRepositories(basePackages = "pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.repository")
public class MongoConfig extends AbstractReactiveMongoConfiguration {
    
    private static final Logger log = LoggerFactory.getLogger(MongoConfig.class);

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Value("${spring.data.mongodb.database}")
    private String databaseName;

    @Override
    public MongoClient reactiveMongoClient() {
        log.info("Conectando a MongoDB con URI: {}", mongoUri);
        return MongoClients.create(mongoUri);
    }

    @Override
    protected String getDatabaseName() {
        log.info("Usando base de datos: {}", databaseName);
        return databaseName;
    }

    @Bean
    public ReactiveMongoTemplate reactiveMongoTemplate(ReactiveMongoDatabaseFactory reactiveMongoDatabaseFactory) {
        return new ReactiveMongoTemplate(reactiveMongoDatabaseFactory);
    }
}
