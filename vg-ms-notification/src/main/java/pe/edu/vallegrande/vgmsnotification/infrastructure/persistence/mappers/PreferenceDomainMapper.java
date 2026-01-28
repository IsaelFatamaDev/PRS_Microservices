package pe.edu.vallegrande.vgmsnotification.infrastructure.persistence.mappers;

import pe.edu.vallegrande.vgmsnotification.domain.models.NotificationPreference;
import pe.edu.vallegrande.vgmsnotification.domain.valueobjects.NotificationChannel;
import pe.edu.vallegrande.vgmsnotification.infrastructure.persistence.entities.PreferenceDocument;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class PreferenceDomainMapper {
    
    public static PreferenceDocument toDocument(NotificationPreference preference) {
        PreferenceDocument document = new PreferenceDocument();
        document.setId(preference.getId());
        document.setUserId(preference.getUserId());
        
        // Convertir Map<String, ChannelPreference> a Map<String, ChannelPreferenceDoc>
        Map<String, PreferenceDocument.ChannelPreferenceDoc> prefsDoc = new HashMap<>();
        preference.getPreferences().forEach((key, value) -> {
            PreferenceDocument.ChannelPreferenceDoc cpDoc = new PreferenceDocument.ChannelPreferenceDoc();
            cpDoc.setEnabledChannels(
                value.getEnabledChannels().stream()
                    .map(Enum::name)
                    .collect(Collectors.toList())
            );
            cpDoc.setPrimaryChannel(value.getPrimaryChannel().name());
            prefsDoc.put(key, cpDoc);
        });
        document.setPreferences(prefsDoc);
        
        document.setPhoneNumber(preference.getPhoneNumber());
        document.setWhatsappNumber(preference.getWhatsappNumber());
        document.setEmail(preference.getEmail());
        document.setEnableSms(preference.isEnableSms());
        document.setEnableWhatsapp(preference.isEnableWhatsapp());
        document.setEnableEmail(preference.isEnableEmail());
        document.setEnableInApp(preference.isEnableInApp());
        document.setQuietHoursStart(preference.getQuietHoursStart());
        document.setQuietHoursEnd(preference.getQuietHoursEnd());
        document.setCreatedAt(preference.getCreatedAt());
        document.setUpdatedAt(preference.getUpdatedAt());
        return document;
    }
    
    public static NotificationPreference toDomain(PreferenceDocument document) {
        // Convertir Map<String, ChannelPreferenceDoc> a Map<String, ChannelPreference>
        Map<String, NotificationPreference.ChannelPreference> prefsDomain = new HashMap<>();
        document.getPreferences().forEach((key, value) -> {
            NotificationPreference.ChannelPreference cp = new NotificationPreference.ChannelPreference(
                value.getEnabledChannels().stream()
                    .map(NotificationChannel::valueOf)
                    .collect(Collectors.toList()),
                NotificationChannel.valueOf(value.getPrimaryChannel())
            );
            prefsDomain.put(key, cp);
        });
        
        return new NotificationPreference(
            document.getId(),
            document.getUserId(),
            prefsDomain,
            document.getPhoneNumber(),
            document.getWhatsappNumber(),
            document.getEmail(),
            document.isEnableSms(),
            document.isEnableWhatsapp(),
            document.isEnableEmail(),
            document.isEnableInApp(),
            document.getQuietHoursStart(),
            document.getQuietHoursEnd(),
            document.getCreatedAt(),
            document.getUpdatedAt()
        );
    }
}
