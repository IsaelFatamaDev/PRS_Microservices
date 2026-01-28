package pe.edu.vallegrande.vgmsnotification.application.dto.notification;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendNotificationRequest {

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotNull(message = "Channel is required")
    private String channel;  // SMS, WHATSAPP, EMAIL, IN_APP

    @NotBlank(message = "Recipient is required")
    private String recipient;  // Phone number, email, or userId

    @NotNull(message = "Type is required")
    private String type;  // USER_CREDENTIALS, RECEIPT_GENERATED, etc.

    private String subject;  // Solo para EMAIL

    private String message;  // Mensaje directo (si no usa plantilla)

    private String templateCode;  // Código de plantilla (alternativa a message)

    private Map<String, String> templateParams;  // Parámetros para la plantilla

    private String priority;  // URGENT, HIGH, NORMAL, LOW

    private String createdBy;
}
