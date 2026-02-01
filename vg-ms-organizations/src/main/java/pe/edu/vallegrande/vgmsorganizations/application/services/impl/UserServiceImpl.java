package pe.edu.vallegrande.vgmsorganizations.application.services.impl;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsorganizations.application.services.StreetService;
import pe.edu.vallegrande.vgmsorganizations.application.services.UserService;
import pe.edu.vallegrande.vgmsorganizations.application.services.ZoneService;
import pe.edu.vallegrande.vgmsorganizations.domain.enums.Constants;
import pe.edu.vallegrande.vgmsorganizations.domain.models.User;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.client.external.UserAuthClient;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.external.CreateAdminRequest;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.external.CreateAdminResponse;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.request.UserCreateRequest;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.response.StreetResponse;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.response.UserResponse;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.response.ZoneResponse;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.exception.CustomException;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.repository.UserRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserAuthClient userAuthClient;
    private final ZoneService zoneService;
    private final StreetService streetService;

    // Constructor explícito (PRS1 Standard - No usar @RequiredArgsConstructor)
    public UserServiceImpl(UserRepository userRepository,
                          UserAuthClient userAuthClient,
                          ZoneService zoneService,
                          StreetService streetService) {
        this.userRepository = userRepository;
        this.userAuthClient = userAuthClient;
        this.zoneService = zoneService;
        this.streetService = streetService;
    }

    @Override
    public Mono<CreateAdminResponse> createAdmin(UserCreateRequest userRequest, String organizationId) {
        // Optimizado: Eliminado .cache() para evitar mantener datos en memoria indefinidamente
        // 1. Intentar obtener la primera zona de la organización de forma opcional.
        Mono<ZoneResponse> optionalZoneMono = zoneService.findByOrganizationId(organizationId).next();

        // 2. Basado en la zona (si existe), intentar obtener la primera calle de forma opcional.
        Mono<StreetResponse> optionalStreetMono = optionalZoneMono
                .flatMap(zone -> streetService.findByZoneId(zone.getZoneId()).next());

        // 3. Combinar los resultados opcionales de zona y calle.
        return Mono.zip(
                optionalZoneMono.map(Optional::of).defaultIfEmpty(Optional.empty()),
                optionalStreetMono.map(Optional::of).defaultIfEmpty(Optional.empty())
        ).flatMap(tuple -> {
            Optional<ZoneResponse> zoneOpt = tuple.getT1();
            Optional<StreetResponse> streetOpt = tuple.getT2();

            // 4. Construir la solicitud para ms-users, incluyendo IDs de zona/calle si se encontraron.
            CreateAdminRequest externalRequest = CreateAdminRequest.builder()
                    .firstName(userRequest.getFirstName())
                    .lastName(userRequest.getLastName())
                    .documentType(userRequest.getDocumentType())
                    .documentNumber(userRequest.getDocumentNumber())
                    .email(userRequest.getEmail())
                    .phone(userRequest.getPhone())
                    .address(userRequest.getAddress())
                    .organizationId(organizationId)
                    .zoneId(zoneOpt.map(ZoneResponse::getZoneId).orElse(null))
                    .streetId(streetOpt.map(StreetResponse::getStreetId).orElse(null))
                    .roles(userRequest.getRoles() != null ? userRequest.getRoles() : Collections.singletonList("ADMIN"))
                    .build();

            // 5. Llamar al microservicio de usuarios para crear el admin.
            return userAuthClient.createAdmin(externalRequest, organizationId)
                    .flatMap(response -> {
                        // 6. Si la creación externa fue exitosa, guardar una copia localmente.
                        if (response != null && response.getUserId() != null) {
                            User localUser = new User();
                            localUser.setUserId(response.getUserId());
                            localUser.setOrganizationId(organizationId); // Asignar el ID de la organización
                            localUser.setName(userRequest.getFirstName() + " " + userRequest.getLastName());
                            localUser.setEmail(userRequest.getEmail());
                            localUser.setStatus(Constants.ACTIVE);
                            localUser.setCreatedAt(Instant.now());

                            return userRepository.save(localUser)
                                    .thenReturn(response); // Devolver la respuesta original.
                        } else {
                            return Mono.error(new CustomException(HttpStatus.BAD_REQUEST.value(),
                                    "Admin creation failed in external service",
                                    response != null ? response.getMessage() : "No response from user service"));
                        }
                    });
        });
    }

    @Override
    public Flux<User> getAll() {
        return userRepository.findAll();
    }

    @Override
    public Flux<User> getAllActive() {
        return userRepository.findAllByStatus(Constants.ACTIVE);
    }

    @Override
    public Flux<User> getAllInactive() {
        return userRepository.findAllByStatus(Constants.INACTIVE);
    }

    @Override
    public Mono<User> getById(String id) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        "User not found",
                        "The requested user with id " + id + " was not found")));
    }

    @Override
    public Mono<UserResponse> save(UserCreateRequest request) {
        return userRepository.existsByEmail(request.getEmail())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new CustomException(
                                HttpStatus.BAD_REQUEST.value(),
                                "Email already exists",
                                "The email " + request.getEmail() + " is already registered"));
                    }

                    return userAuthClient.validateUserByEmail(request.getEmail())
                            .flatMap(validateResponse -> {
                                if (!validateResponse.isExists()) {
                                    return Mono.error(new CustomException(
                                            HttpStatus.BAD_REQUEST.value(),
                                            "User not found in authentication service",
                                            "The user " + request.getEmail() + " must exist in MS-USERS first"));
                                }

                                User user = new User();
                                user.setName(request.getFirstName() + " " + request.getLastName());
                                user.setEmail(request.getEmail());
                                user.setPassword("temp_password");
                                user.setCreatedAt(Instant.now());
                                user.setStatus(Constants.ACTIVE);

                                return userRepository.save(user)
                                        .map(savedUser -> {
                                            UserResponse response = new UserResponse();
                                            response.setUserId(savedUser.getUserId());
                                            response.setName(savedUser.getName());
                                            response.setEmail(savedUser.getEmail());
                                            response.setStatus(savedUser.getStatus());
                                            response.setCreatedAt(savedUser.getCreatedAt() != null ? 
                                                LocalDateTime.ofInstant(savedUser.getCreatedAt(), ZoneId.systemDefault()) : null);
                                            response.setUpdatedAt(savedUser.getUpdatedAt() != null ? 
                                                LocalDateTime.ofInstant(savedUser.getUpdatedAt(), ZoneId.systemDefault()) : null);
                                            return response;
                                        });
                            });
                });
    }

    @Override
    public Mono<User> update(String id, User user) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        "User not found",
                        "Cannot update non-existent user with id " + id)))
                .flatMap(existingUser -> {
                    existingUser.setName(user.getName());
                    existingUser.setEmail(user.getEmail());
                    existingUser.setUpdatedAt(Instant.now());
                    return userRepository.save(existingUser);
                });
    }

    @Override
    public Mono<Void> delete(String id) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        "User not found",
                        "Cannot delete non-existent user with id " + id)))
                .flatMap(userRepository::delete);
    }

    @Override
    public Mono<User> activate(String id) {
        return changeStatus(id, Constants.ACTIVE);
    }

    @Override
    public Mono<User> deactivate(String id) {
        return changeStatus(id, Constants.INACTIVE);
    }

    private Mono<User> changeStatus(String id, String status) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new CustomException(
                        HttpStatus.NOT_FOUND.value(),
                        "User not found",
                        "Cannot change status of non-existent user with id " + id)))
                .flatMap(user -> {
                    user.setStatus(status);
                    user.setUpdatedAt(Instant.now());
                    return userRepository.save(user);
                });
    }
}