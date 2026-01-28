package pe.edu.vallegrande.vgmsnotification.application.mapper;

import pe.edu.vallegrande.vgmsnotification.application.dto.template.CreateTemplateRequest;
import pe.edu.vallegrande.vgmsnotification.application.dto.template.TemplateResponse;
import pe.edu.vallegrande.vgmsnotification.domain.models.NotificationTemplate;
import pe.edu.vallegrande.vgmsnotification.domain.models.valueobjects.NotificationChannel;
import pe.edu.vallegrande.vgmsnotification.domain.models.valueobjects.TemplateStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public class TemplateMapper {

    public static NotificationTemplate toDomain(CreateTemplateRequest request) {
        return NotificationTemplate.builder()
                .id(UUID.randomUUID().toString())
                .code(request.getCode())
                .name(request.getName())
                .channel(NotificationChannel.valueOf(request.getChannel()))
                .subject(request.getSubject())
                .template(request.getTemplate())
                .variables(request.getVariables())
                .status(TemplateStatus.DRAFT)
                .createdAt(LocalDateTime.now())
                .createdBy(request.getCreatedBy())
                .build();
    }

    public static TemplateResponse toResponse(NotificationTemplate template) {
        return TemplateResponse.builder()
                .id(template.getId())
                .code(template.getCode())
                .name(template.getName())
                .channel(template.getChannel().name())
                .subject(template.getSubject())
                .template(template.getTemplate())
                .variables(template.getVariables())
                .status(template.getStatus().name())
                .createdAt(template.getCreatedAt())
                .createdBy(template.getCreatedBy())
                .updatedAt(template.getUpdatedAt())
                .updatedBy(template.getUpdatedBy())
                .build();
    }
}
