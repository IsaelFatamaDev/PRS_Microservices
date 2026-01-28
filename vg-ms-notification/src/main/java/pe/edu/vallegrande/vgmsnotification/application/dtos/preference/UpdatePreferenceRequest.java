package pe.edu.vallegrande.vgmsnotification.application.dtos.preference;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePreferenceRequest {
    private Map<String, ChannelPreferenceDTO> preferences;
    private String phoneNumber;
    private String whatsappNumber;
    private String email;
    private Boolean enableSms;
    private Boolean enableWhatsapp;
    private Boolean enableEmail;
    private Boolean enableInApp;
    private LocalTime quietHoursStart;
    private LocalTime quietHoursEnd;
    private String updatedBy;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ChannelPreferenceDTO {
        private List<String> enabledChannels;
        private String primaryChannel;
    }
}
