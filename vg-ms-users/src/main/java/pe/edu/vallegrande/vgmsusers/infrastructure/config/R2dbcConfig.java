package pe.edu.vallegrande.vgmsusers.infrastructure.config;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import pe.edu.vallegrande.vgmsusers.domain.models.valueobjects.DocumentType;
import pe.edu.vallegrande.vgmsusers.domain.models.valueobjects.RecordStatus;
import pe.edu.vallegrande.vgmsusers.domain.models.valueobjects.Role;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableR2dbcRepositories(basePackages = "pe.edu.vallegrande.vgmsusers.infrastructure.persistence.repositories")
public class R2dbcConfig extends AbstractR2dbcConfiguration {

     private final ConnectionFactory connectionFactory;

     public R2dbcConfig(ConnectionFactory connectionFactory) {
          this.connectionFactory = connectionFactory;
     }

     @Override
     public ConnectionFactory connectionFactory() {
          return connectionFactory;
     }

     @Override
     @Bean
     public R2dbcCustomConversions r2dbcCustomConversions() {
          List<Converter<?, ?>> converters = new ArrayList<>();
          converters.add(new RoleToStringConverter());
          converters.add(new StringToRoleConverter());
          converters.add(new DocumentTypeToStringConverter());
          converters.add(new StringToDocumentTypeConverter());
          converters.add(new RecordStatusToStringConverter());
          converters.add(new StringToRecordStatusConverter());
          return new R2dbcCustomConversions(getStoreConversions(), converters);
     }

     static class RoleToStringConverter implements Converter<Role, String> {
          @Override
          public String convert(Role source) {
               return source.name();
          }
     }

     static class StringToRoleConverter implements Converter<String, Role> {
          @Override
          public Role convert(String source) {
               return Role.valueOf(source);
          }
     }

     static class DocumentTypeToStringConverter implements Converter<DocumentType, String> {
          @Override
          public String convert(DocumentType source) {
               return source.name();
          }
     }

     static class StringToDocumentTypeConverter implements Converter<String, DocumentType> {
          @Override
          public DocumentType convert(String source) {
               return DocumentType.valueOf(source);
          }
     }

     static class RecordStatusToStringConverter implements Converter<RecordStatus, String> {
          @Override
          public String convert(RecordStatus source) {
               return source.name();
          }
     }

     static class StringToRecordStatusConverter implements Converter<String, RecordStatus> {
          @Override
          public RecordStatus convert(String source) {
               return RecordStatus.valueOf(source);
          }
     }
}
