package pe.edu.vallegrande.vgmsusers.domain.validators;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vgmsusers.domain.exceptions.DuplicateUserException;
import pe.edu.vallegrande.vgmsusers.domain.models.User;
import pe.edu.vallegrande.vgmsusers.domain.ports.out.IUserRepository;
import reactor.core.publisher.Mono;

// SRP: Responsabilidad única - Validar unicidad de usuarios
@Component
@RequiredArgsConstructor
public class UserUniquenessValidator {

    private final IUserRepository userRepository;

    // Validar que username no exista
    public Mono<Void> validateUsernameDoesNotExist(String username) {
        return userRepository.existsByUsername(username)
                .flatMap(exists -> exists
                        ? Mono.error(new DuplicateUserException("Username already exists: " + username))
                        : Mono.empty());
    }

    // Validar que documentNumber no exista
    public Mono<Void> validateDocumentNumberDoesNotExist(String documentNumber) {
        return userRepository.existsByDocumentNumber(documentNumber)
                .flatMap(exists -> exists
                        ? Mono.error(new DuplicateUserException("Document number already exists: " + documentNumber))
                        : Mono.empty());
    }

    // Validar unicidad completa del usuario
    public Mono<Void> validateUserDoesNotExist(User user) {
        return validateUsernameDoesNotExist(user.getUsername())
                .then(validateDocumentNumberDoesNotExist(user.getDocumentNumber()));
    }
}
