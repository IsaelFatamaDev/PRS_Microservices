package pe.edu.vallegrande.vgmsnotification.infrastructure.adapters.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class EmailConfig {

     @Value("${spring.mail.host:smtp.gmail.com}")
     private String mailHost;

     @Value("${spring.mail.port:587}")
     private int mailPort;

     @Value("${spring.mail.username}")
     private String mailUsername;

     @Value("${spring.mail.password}")
     private String mailPassword;

     @Value("${spring.mail.protocol:smtp}")
     private String mailProtocol;

     @Value("${spring.mail.smtp.auth:true}")
     private String smtpAuth;

     @Value("${spring.mail.smtp.starttls.enable:true}")
     private String startTlsEnable;

     @Bean
     public JavaMailSender javaMailSender() {
          JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

          mailSender.setHost(mailHost);
          mailSender.setPort(mailPort);
          mailSender.setUsername(mailUsername);
          mailSender.setPassword(mailPassword);
          mailSender.setProtocol(mailProtocol);

          Properties props = mailSender.getJavaMailProperties();
          props.put("mail.smtp.auth", smtpAuth);
          props.put("mail.smtp.starttls.enable", startTlsEnable);
          props.put("mail.smtp.connectiontimeout", "5000");
          props.put("mail.smtp.timeout", "5000");
          props.put("mail.smtp.writetimeout", "5000");

          return mailSender;
     }
}
