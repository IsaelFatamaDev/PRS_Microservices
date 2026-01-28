package pe.edu.vallegrande.vgmsnotification.application.usecases;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsnotification.domain.models.NotificationPreference;
import pe.edu.vallegrande.vgmsnotification.domain.ports.in.IGetPreferenceUseCase;
import pe.edu.vallegrande.vgmsnotification.domain.ports.out.IPreferenceRepository;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class GetPreferenceUseCaseImpl implements IGetPreferenceUseCase {

    private final IPreferenceRepository preferenceRepository;

    @Override
    public Mono<NotificationPreference> findByUserId(String userId) {
        return preferenceRepository.findByUserId(userId);
    }
}
