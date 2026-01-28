package pe.edu.vallegrande.vgmsnotification.application.dto.template;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTemplateRequest {

    @NotBlank(message = "Template code is required")
    private String code;

    @NotBlank(message = "Template name is required")
    private String name;

    @NotNull(message = "Channel is required")
    private String channel;  // SMS, EMAIL, WHATSAPP, IN_APP

    private String subject;  // Solo para EMAIL

    @NotBlank(message = "Template content is required")
    private String template;

    private List<String> variables;

    private String createdBy;
}
