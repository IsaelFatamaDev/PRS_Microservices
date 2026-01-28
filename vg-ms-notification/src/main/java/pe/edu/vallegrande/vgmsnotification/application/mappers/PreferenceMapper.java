package pe.edu.vallegrande.vgmsnotification.application.mappers;

import pe.edu.vallegrande.vgmsnotification.application.dtos.preference.PreferenceResponse;
import pe.edu.vallegrande.vgmsnotification.application.dtos.preference.UpdatePreferenceRequest;
import pe.edu.vallegrande.vgmsnotification.domain.models.NotificationPreference;
import pe.edu.vallegrande.vgmsnotification.domain.valueobjects.NotificationChannel;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class PreferenceMapper {
    
    public static NotificationPreference toDomain(UpdatePreferenceRequest request, String userId, String id) {
        Map<String, NotificationPreference.ChannelPreference> preferences = new HashMap<>();
        
        if (request.getPreferences() != null) {
            request.getPreferences().forEach((key, value) -> {
                NotificationPreference.ChannelPreference cp = new NotificationPreference.ChannelPreference(
                    value.getEnabledChannels().stream()
                        .map(NotificationChannel::valueOf)
                        .collect(Collectors.toList()),
                    NotificationChannel.valueOf(value.getPrimaryChannel())
                );
                preferences.put(key, cp);
            });
        }
        
        return new NotificationPreference(
            id,
            userId,
            preferences,
            request.getPhoneNumber(),
            request.getWhatsappNumber(),
            request.getEmail(),
            request.getEnableSms() != null ? request.getEnableSms() : true,
            request.getEnableWhatsapp() != null ? request.getEnableWhatsapp() : true,
            request.getEnableEmail() != null ? request.getEnableEmail() : true,
            request.getEnableInApp() != null ? request.getEnableInApp() : true,
            request.getQuietHoursStart(),
            request.getQuietHoursEnd(),
            null, // createdAt se establece automáticamente
            null  // updatedAt se establece en el use case
        );
    }
    
    public static PreferenceResponse toResponse(NotificationPreference preference) {
        Map<String, PreferenceResponse.ChannelPreferenceDTO> preferencesDTO = new HashMap<>();
        
        preference.getPreferences().forEach((key, value) -> {
            PreferenceResponse.ChannelPreferenceDTO cpDTO = new PreferenceResponse.ChannelPreferenceDTO(
                value.getEnabledChannels().stream()
                    .map(Enum::name)
                    .collect(Collectors.toList()),
                value.getPrimaryChannel().name()
            );
            preferencesDTO.put(key, cpDTO);
        });
        
        return new PreferenceResponse(
            preference.getId(),
            preference.getUserId(),
            preferencesDTO,
            preference.getPhoneNumber(),
            preference.getWhatsappNumber(),
            preference.getEmail(),
            preference.isEnableSms(),
            preference.isEnableWhatsapp(),
            preference.isEnableEmail(),
            preference.isEnableInApp(),
            preference.getQuietHoursStart(),
            preference.getQuietHoursEnd(),
            preference.getCreatedAt(),
            preference.getUpdatedAt()
        );
    }
}
