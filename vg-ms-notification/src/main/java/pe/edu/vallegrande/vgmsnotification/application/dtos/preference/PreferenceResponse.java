package pe.edu.vallegrande.vgmsnotification.application.dtos.preference;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PreferenceResponse {
    private String id;
    private String userId;
    private Map<String, ChannelPreferenceDTO> preferences;
    private String phoneNumber;
    private String whatsappNumber;
    private String email;
    private boolean enableSms;
    private boolean enableWhatsapp;
    private boolean enableEmail;
    private boolean enableInApp;
    private LocalTime quietHoursStart;
    private LocalTime quietHoursEnd;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChannelPreferenceDTO {
        private List<String> enabledChannels;
        private String primaryChannel;
    }
}
