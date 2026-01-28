package pe.edu.vallegrande.vgmsnotification.infrastructure.persistence.mappers;

import pe.edu.vallegrande.vgmsnotification.domain.models.NotificationTemplate;
import pe.edu.vallegrande.vgmsnotification.domain.valueobjects.NotificationChannel;
import pe.edu.vallegrande.vgmsnotification.domain.valueobjects.TemplateStatus;
import pe.edu.vallegrande.vgmsnotification.infrastructure.persistence.entities.TemplateDocument;

public class TemplateDomainMapper {

    public static TemplateDocument toDocument(NotificationTemplate template) {
        TemplateDocument document = new TemplateDocument();
        document.setId(template.getId());
        document.setCode(template.getCode());
        document.setName(template.getName());
        document.setChannel(template.getChannel().name());
        document.setSubject(template.getSubject());
        document.setTemplate(template.getTemplate());
        document.setVariables(template.getVariables());
        document.setStatus(template.getStatus().name());
        document.setCreatedAt(template.getCreatedAt());
        document.setCreatedBy(template.getCreatedBy());
        document.setUpdatedAt(template.getUpdatedAt());
        document.setUpdatedBy(template.getUpdatedBy());
        return document;
    }

    public static NotificationTemplate toDomain(TemplateDocument document) {
        return new NotificationTemplate(
            document.getId(),
            document.getCode(),
            document.getName(),
            NotificationChannel.valueOf(document.getChannel()),
            document.getSubject(),
            document.getTemplate(),
            document.getVariables(),
            TemplateStatus.valueOf(document.getStatus()),
            document.getCreatedAt(),
            document.getCreatedBy(),
            document.getUpdatedAt(),
            document.getUpdatedBy()
        );
    }
}
