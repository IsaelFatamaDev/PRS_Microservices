package pe.edu.vallegrande.vgmsnotification.infrastructure.persistence.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notification_preferences")
public class PreferenceDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String userId;

    private Map<String, ChannelPreferenceDoc> preferences;

    private String phoneNumber;
    private String whatsappNumber;
    private String email;

    private boolean enableSms;
    private boolean enableWhatsapp;
    private boolean enableEmail;
    private boolean enableInApp;

    private String quietHoursStart;
    private String quietHoursEnd;

    private LocalDateTime updatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChannelPreferenceDoc {
        private List<String> enabledChannels;
        private String primaryChannel;
    }
}
