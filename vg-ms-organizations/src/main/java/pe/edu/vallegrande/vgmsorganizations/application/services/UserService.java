package pe.edu.vallegrande.vgmsorganizations.application.services;

import pe.edu.vallegrande.vgmsorganizations.domain.models.User;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.external.CreateAdminResponse;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.request.UserCreateRequest;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.response.UserResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserService {
    Flux<User> getAll();
    Flux<User> getAllActive();
    Flux<User> getAllInactive();
    Mono<User> getById(String id);
    Mono<UserResponse> save(UserCreateRequest userRequest);
    Mono<User> update(String id, User user);
    Mono<Void> delete(String id);
    Mono<User> activate(String id);
    Mono<User> deactivate(String id);
    
    // Método para crear administradores usando MS-USERS
    Mono<CreateAdminResponse> createAdmin(UserCreateRequest userRequest, String organizationId);
}