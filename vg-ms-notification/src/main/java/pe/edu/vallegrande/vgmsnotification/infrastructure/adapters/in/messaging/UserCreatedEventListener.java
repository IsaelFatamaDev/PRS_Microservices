package pe.edu.vallegrande.vgmsnotification.infrastructure.adapters.in.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vgmsnotification.domain.models.Notification;
import pe.edu.vallegrande.vgmsnotification.domain.ports.in.ISendNotificationUseCase;
import pe.edu.vallegrande.vgmsnotification.domain.valueobjects.NotificationChannel;
import pe.edu.vallegrande.vgmsnotification.domain.valueobjects.NotificationPriority;
import pe.edu.vallegrande.vgmsnotification.domain.valueobjects.NotificationType;

import java.util.Map;

/**
 * Listener para eventos de creación de usuario
 * Envía notificación con credenciales de acceso
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class UserCreatedEventListener {
    
    private final ISendNotificationUseCase sendNotificationUseCase;
    private final ObjectMapper objectMapper;
    
    @RabbitListener(queues = "${rabbitmq.queue.user-created:user.created.queue}")
    public void handleUserCreatedEvent(String message) {
        try {
            log.info("Received UserCreatedEvent: {}", message);
            
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            
            String userId = (String) event.get("userId");
            String email = (String) event.get("email");
            String phoneNumber = (String) event.get("phoneNumber");
            String username = (String) event.get("username");
            String temporaryPassword = (String) event.get("temporaryPassword");
            
            // Crear notificación con credenciales usando template
            Notification notification = Notification.createNew(
                userId,
                NotificationChannel.EMAIL, // Prioridad: email para credenciales
                email,
                NotificationType.USER_CREDENTIALS,
                "Bienvenido a Sistema JASS - Credenciales de Acceso",
                buildCredentialsMessage(username, temporaryPassword),
                NotificationPriority.HIGH,
                "USER_CREDENTIALS_TEMPLATE",
                Map.of(
                    "username", username,
                    "temporaryPassword", temporaryPassword,
                    "systemName", "Sistema JASS"
                ),
                "SYSTEM"
            );
            
            sendNotificationUseCase.execute(notification)
                .doOnSuccess(sent -> log.info("Credentials notification sent to user {}", userId))
                .doOnError(e -> log.error("Failed to send credentials notification: {}", e.getMessage()))
                .subscribe();
            
            // También enviar por SMS si hay número de teléfono
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                Notification smsNotification = Notification.createNew(
                    userId,
                    NotificationChannel.SMS,
                    phoneNumber,
                    NotificationType.USER_CREDENTIALS,
                    null,
                    "Tu usuario es: " + username + ". Revisa tu email para la contraseña temporal.",
                    NotificationPriority.HIGH,
                    null,
                    null,
                    "SYSTEM"
                );
                
                sendNotificationUseCase.execute(smsNotification).subscribe();
            }
            
        } catch (Exception e) {
            log.error("Error processing UserCreatedEvent: {}", e.getMessage(), e);
        }
    }
    
    private String buildCredentialsMessage(String username, String temporaryPassword) {
        return String.format("""
            Bienvenido al Sistema JASS
            
            Tus credenciales de acceso son:
            Usuario: %s
            Contraseña temporal: %s
            
            IMPORTANTE: Por seguridad, cambia tu contraseña en el primer inicio de sesión.
            
            Si no solicitaste esta cuenta, contacta al administrador.
            """, username, temporaryPassword);
    }
}
