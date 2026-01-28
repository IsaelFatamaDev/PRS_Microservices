package pe.edu.vallegrande.vgmsnotification.domain.models.valueobjects;

/**
 * Value Object - Tipo de Notificación
 */
public enum NotificationType {
     // Autenticación y Seguridad
     USER_CREDENTIALS, // Envío de credenciales de acceso (username/password)
     PASSWORD_RESET, // Reseteo de contraseña
     TWO_FACTOR_AUTH, // Código 2FA

     // Recibos y Facturación
     RECEIPT_GENERATED, // Recibo generado
     RECEIPT_REMINDER, // Recordatorio de recibo pendiente
     PAYMENT_RECEIVED, // Confirmación de pago recibido
     PAYMENT_OVERDUE, // Pago vencido

     // Incidentes y Alertas
     INCIDENT_CREATED, // Nuevo incidente reportado
     INCIDENT_UPDATED, // Actualización de incidente
     INCIDENT_RESOLVED, // Incidente resuelto

     // Calidad del Agua
     WATER_QUALITY_ALERT, // Alerta de calidad del agua
     WATER_QUALITY_REPORT, // Reporte de análisis

     // Sistema
     SERVICE_INTERRUPTION, // Interrupción del servicio
     MAINTENANCE_SCHEDULED, // Mantenimiento programado
     SYSTEM_ANNOUNCEMENT, // Anuncio del sistema

     // Inventario
     LOW_STOCK_ALERT, // Alerta de stock bajo
     INVENTORY_UPDATE; // Actualización de inventario

     public boolean isUrgent() {
          return this == PAYMENT_OVERDUE ||
                    this == WATER_QUALITY_ALERT ||
                    this == SERVICE_INTERRUPTION ||
                    this == TWO_FACTOR_AUTH;
     }

     public boolean requiresImmediate() {
          return this == TWO_FACTOR_AUTH || this == USER_CREDENTIALS;
     }
}
