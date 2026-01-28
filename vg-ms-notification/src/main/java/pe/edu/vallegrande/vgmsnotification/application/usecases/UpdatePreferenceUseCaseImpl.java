package pe.edu.vallegrande.vgmsnotification.application.usecases;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsnotification.domain.models.NotificationPreference;
import pe.edu.vallegrande.vgmsnotification.domain.ports.in.IUpdatePreferenceUseCase;
import pe.edu.vallegrande.vgmsnotification.domain.ports.out.IPreferenceRepository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UpdatePreferenceUseCaseImpl implements IUpdatePreferenceUseCase {
    
    private final IPreferenceRepository preferenceRepository;
    
    @Override
    public Mono<NotificationPreference> execute(NotificationPreference preference) {
        // Actualizar timestamp
        preference.setUpdatedAt(LocalDateTime.now());
        
        return preferenceRepository.save(preference);
    }
}
