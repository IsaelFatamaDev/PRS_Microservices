package pe.edu.vallegrande.vgmsorganizations;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@SpringBootApplication
@EnableReactiveMongoRepositories(basePackages = "pe.edu.vallegrande.vgmsorganizations.infrastructure.persistence.repositories")
public class VgMsOrganizationsApplication {
     public static void main(String[] args) {
          SpringApplication.run(VgMsOrganizationsApplication.class, args);
     }
}
