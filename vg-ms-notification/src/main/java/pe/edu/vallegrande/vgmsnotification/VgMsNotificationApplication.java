package pe.edu.vallegrande.vgmsnotification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@SpringBootApplication
@EnableReactiveMongoRepositories(basePackages = "pe.edu.vallegrande.vgmsnotification.infrastructure.persistence.repositories")
public class VgMsNotificationApplication {
     public static void main(String[] args) {
          SpringApplication.run(VgMsNotificationApplication.class, args);
     }
}
