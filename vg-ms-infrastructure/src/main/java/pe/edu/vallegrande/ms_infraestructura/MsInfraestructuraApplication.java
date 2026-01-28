package pe.edu.vallegrande.ms_infraestructura;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing // Habilita la auditoría para @CreatedDate
public class MsInfraestructuraApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsInfraestructuraApplication.class, args);
    }

}
