package pe.edu.vallegrande.vgmsorganizations;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@Slf4j
@SpringBootApplication
@EnableAsync
public class VgMsOrganizationsApplication {

    public static void main(String[] args) {
        SpringApplication.run(VgMsOrganizationsApplication.class, args);
    }
}