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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Listener para eventos de pagos
 * Envía notificaciones de recibos, recordatorios, vencimientos
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentEventListener {
    
    private final ISendNotificationUseCase sendNotificationUseCase;
    private final ObjectMapper objectMapper;
    
    @RabbitListener(queues = "${rabbitmq.queue.payment-completed:payment.completed.queue}")
    public void handlePaymentCompletedEvent(String message) {
        try {
            log.info("Received PaymentCompletedEvent: {}", message);
            
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            
            String userId = (String) event.get("userId");
            String email = (String) event.get("email");
            String phoneNumber = (String) event.get("phoneNumber");
            String receiptNumber = (String) event.get("receiptNumber");
            BigDecimal amount = new BigDecimal(event.get("amount").toString());
            String paymentDate = (String) event.get("paymentDate");
            
            // Notificación de recibo generado por email
            Notification emailNotification = Notification.createNew(
                userId,
                NotificationChannel.EMAIL,
                email,
                NotificationType.RECEIPT_GENERATED,
                "Recibo de Pago Generado - " + receiptNumber,
                buildReceiptMessage(receiptNumber, amount, paymentDate),
                NotificationPriority.NORMAL,
                "RECEIPT_GENERATED_TEMPLATE",
                Map.of(
                    "receiptNumber", receiptNumber,
                    "amount", amount.toString(),
                    "paymentDate", paymentDate,
                    "currency", "PEN"
                ),
                "SYSTEM"
            );
            
            sendNotificationUseCase.execute(emailNotification)
                .doOnSuccess(sent -> log.info("Receipt notification sent to user {}", userId))
                .doOnError(e -> log.error("Failed to send receipt notification: {}", e.getMessage()))
                .subscribe();
            
            // SMS simple para confirmar pago
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                Notification smsNotification = Notification.createNew(
                    userId,
                    NotificationChannel.SMS,
                    phoneNumber,
                    NotificationType.PAYMENT_RECEIVED,
                    null,
                    String.format("Pago recibido: S/ %.2f. Recibo: %s. Gracias!", amount, receiptNumber),
                    NotificationPriority.HIGH,
                    null,
                    null,
                    "SYSTEM"
                );
                
                sendNotificationUseCase.execute(smsNotification).subscribe();
            }
            
        } catch (Exception e) {
            log.error("Error processing PaymentCompletedEvent: {}", e.getMessage(), e);
        }
    }
    
    @RabbitListener(queues = "${rabbitmq.queue.payment-overdue:payment.overdue.queue}")
    public void handlePaymentOverdueEvent(String message) {
        try {
            log.info("Received PaymentOverdueEvent: {}", message);
            
            Map<String, Object> event = objectMapper.readValue(message, Map.class);
            
            String userId = (String) event.get("userId");
            String phoneNumber = (String) event.get("phoneNumber");
            BigDecimal amount = new BigDecimal(event.get("amount").toString());
            String dueDate = (String) event.get("dueDate");
            
            // SMS urgente para pago vencido (zonas rurales)
            Notification smsNotification = Notification.createNew(
                userId,
                NotificationChannel.SMS,
                phoneNumber,
                NotificationType.PAYMENT_OVERDUE,
                null,
                String.format("URGENTE: Pago vencido desde %s. Monto: S/ %.2f. Evita corte de servicio.", dueDate, amount),
                NotificationPriority.URGENT,
                null,
                null,
                "SYSTEM"
            );
            
            sendNotificationUseCase.execute(smsNotification)
                .doOnSuccess(sent -> log.info("Overdue payment notification sent to user {}", userId))
                .doOnError(e -> log.error("Failed to send overdue notification: {}", e.getMessage()))
                .subscribe();
            
        } catch (Exception e) {
            log.error("Error processing PaymentOverdueEvent: {}", e.getMessage(), e);
        }
    }
    
    private String buildReceiptMessage(String receiptNumber, BigDecimal amount, String paymentDate) {
        return String.format("""
            ¡Pago recibido exitosamente!
            
            Recibo N°: %s
            Monto: S/ %.2f
            Fecha de pago: %s
            
            Gracias por tu pago. Puedes descargar el recibo completo desde el sistema.
            
            Sistema JASS
            """, receiptNumber, amount, paymentDate);
    }
}
