package pe.edu.vallegrande.vgmsusers.infrastructure.mapper;

import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vgmsusers.domain.models.UserCodeCounter;
import pe.edu.vallegrande.vgmsusers.infrastructure.document.UserCodeCounterDocument;

@Component
public class UserCodeCounterMapper {

    public UserCodeCounter toDomain(UserCodeCounterDocument document) {
        if (document == null) {
            return null;
        }
        return UserCodeCounter.builder()
                .id(document.getId())
                .organizationId(document.getOrganizationId())
                .lastCode(document.getLastCode())
                .prefix(document.getPrefix())
                .build();
    }

    public UserCodeCounterDocument toDocument(UserCodeCounter domain) {
        if (domain == null) {
            return null;
        }
        return UserCodeCounterDocument.builder()
                .id(domain.getId())
                .organizationId(domain.getOrganizationId())
                .lastCode(domain.getLastCode())
                .prefix(domain.getPrefix())
                .build();
    }
}
