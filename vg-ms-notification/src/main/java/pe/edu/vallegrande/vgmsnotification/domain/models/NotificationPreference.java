package pe.edu.vallegrande.vgmsnotification.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.vallegrande.vgmsnotification.domain.models.valueobjects.NotificationChannel;
import pe.edu.vallegrande.vgmsnotification.domain.models.valueobjects.NotificationType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Entity - User Notification Preferences
 * Preferencias de notificación por usuario
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationPreference {

     private String id;
     private String userId;

     // Canales habilitados por tipo de notificación
     private Map<NotificationType, ChannelPreference> preferences;

     // Contactos
     private String phoneNumber;
     private String whatsappNumber; // Puede ser diferente al teléfono principal
     private String email;

     // Configuración general
     private boolean enableSms;
     private boolean enableWhatsapp;
     private boolean enableEmail;
     private boolean enableInApp;

     // Horarios (para notificaciones no urgentes)
     private String quietHoursStart; // "22:00"
     private String quietHoursEnd; // "07:00"

     private LocalDateTime updatedAt;

     // Métodos de negocio

     public List<NotificationChannel> getPreferredChannels(NotificationType type) {
          ChannelPreference pref = preferences != null ? preferences.get(type) : null;
          if (pref != null && pref.getEnabledChannels() != null) {
               return pref.getEnabledChannels();
          }

          // Default: prioridad SMS > WhatsApp > Email > In-App
          List<NotificationChannel> channels = new ArrayList<>();
          if (enableSms && phoneNumber != null)
               channels.add(NotificationChannel.SMS);
          if (enableWhatsapp && whatsappNumber != null)
               channels.add(NotificationChannel.WHATSAPP);
          if (enableEmail && email != null)
               channels.add(NotificationChannel.EMAIL);
          if (enableInApp)
               channels.add(NotificationChannel.IN_APP);

          return channels;
     }

     public NotificationChannel getPrimaryChannel(NotificationType type) {
          ChannelPreference pref = preferences != null ? preferences.get(type) : null;
          if (pref != null && pref.getPrimaryChannel() != null) {
               return pref.getPrimaryChannel();
          }

          // Default: SMS como canal principal
          if (enableSms && phoneNumber != null)
               return NotificationChannel.SMS;
          if (enableWhatsapp && whatsappNumber != null)
               return NotificationChannel.WHATSAPP;
          if (enableEmail && email != null)
               return NotificationChannel.EMAIL;
          return NotificationChannel.IN_APP;
     }

     public boolean isChannelEnabled(NotificationChannel channel) {
          return switch (channel) {
               case SMS -> enableSms && phoneNumber != null;
               case WHATSAPP -> enableWhatsapp && whatsappNumber != null;
               case EMAIL -> enableEmail && email != null;
               case IN_APP -> enableInApp;
          };
     }

     @Data
     @Builder
     @NoArgsConstructor
     @AllArgsConstructor
     public static class ChannelPreference {
          private List<NotificationChannel> enabledChannels;
          private NotificationChannel primaryChannel;
     }
}
