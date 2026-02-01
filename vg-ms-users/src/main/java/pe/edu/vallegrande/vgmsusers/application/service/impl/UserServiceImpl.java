package pe.edu.vallegrande.vgmsusers.application.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsusers.application.service.UserAuthIntegrationService;
import pe.edu.vallegrande.vgmsusers.application.service.UserCodeService;
import pe.edu.vallegrande.vgmsusers.application.service.UserService;
import pe.edu.vallegrande.vgmsusers.domain.enums.RolesUsers;
import pe.edu.vallegrande.vgmsusers.domain.enums.UserStatus;
import pe.edu.vallegrande.vgmsusers.domain.models.AddressUsers;
import pe.edu.vallegrande.vgmsusers.domain.models.Contact;
import pe.edu.vallegrande.vgmsusers.domain.models.PersonalInfo;
import pe.edu.vallegrande.vgmsusers.domain.models.User;

import pe.edu.vallegrande.vgmsusers.infrastructure.dto.ApiResponse;
import pe.edu.vallegrande.vgmsusers.infrastructure.dto.request.CreateUserRequest;
import pe.edu.vallegrande.vgmsusers.infrastructure.dto.request.UpdateUserPatchRequest;
import pe.edu.vallegrande.vgmsusers.infrastructure.dto.request.UpdateUserRequest;
import pe.edu.vallegrande.vgmsusers.infrastructure.dto.response.CompleteUserResponse;
import pe.edu.vallegrande.vgmsusers.infrastructure.dto.response.UserCreationResponse;
import pe.edu.vallegrande.vgmsusers.infrastructure.dto.response.UserResponse;
import pe.edu.vallegrande.vgmsusers.infrastructure.mapper.UserMapper;
import pe.edu.vallegrande.vgmsusers.infrastructure.repository.UserRepository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {

     private final UserRepository userRepository;
     private final UserCodeService userCodeService;
     private final UserAuthIntegrationService userAuthIntegrationService;
     private final UserMapper userMapper;

     public UserServiceImpl(UserRepository userRepository, UserCodeService userCodeService,
               UserAuthIntegrationService userAuthIntegrationService, UserMapper userMapper) {
          this.userRepository = userRepository;
          this.userCodeService = userCodeService;
          this.userAuthIntegrationService = userAuthIntegrationService;
          this.userMapper = userMapper;
     }

     @Override
     public Mono<ApiResponse<UserResponse>> createUser(CreateUserRequest request) {
          return validateCreateRequest(request)
                    .then(userCodeService.generateUserCode(request.getOrganizationId()))
                    .flatMap(userCode -> {
                         PersonalInfo personalInfo = PersonalInfo.builder()
                                   .firstName(request.getFirstName())
                                   .lastName(request.getLastName())
                                   .documentType(request.getDocumentType())
                                   .documentNumber(request.getDocumentNumber())
                                   .build();

                         Contact contact = Contact.builder()
                                   .email(request.getEmail())
                                   .phone(request.getPhone())
                                   .address(AddressUsers.builder()
                                             .fullAddress(request.getAddress())
                                             .streetId(request.getStreetId())
                                             .zoneId(request.getZoneId())
                                             .build())
                                   .build();

                         User user = User.builder()
                                   .userCode(userCode)
                                   .username("")
                                   .organizationId(request.getOrganizationId())
                                   .personalInfo(personalInfo)
                                   .contact(contact)
                                   .roles(request.getRoles())
                                   .status(UserStatus.ACTIVE)
                                   .registrationDate(LocalDateTime.now())
                                   .createdAt(LocalDateTime.now())
                                   .updatedAt(LocalDateTime.now())
                                   .build();

                         return userRepository.save(userMapper.toDocument(user))
                                   .flatMap(savedDoc -> {
                                        User savedUser = userMapper.toDomain(savedDoc);
                                        return userAuthIntegrationService
                                                  .registerUserWithAutoPassword(savedUser)
                                                  .flatMap(authResponse -> {
                                                       savedUser.setUsername(authResponse.username());

                                                       return userRepository.save(userMapper.toDocument(savedUser))
                                                                 .map(userMapper::toDomain);
                                                  })
                                                  .onErrorResume(error -> {
                                                       return Mono.just(savedUser);
                                                  });
                                   });
                    })
                    .map(this::mapToUserResponse)
                    .map(userResponse -> ApiResponse.<UserResponse>builder()
                              .data(userResponse)
                              .message("Usuario creado exitosamente")
                              .success(true)
                              .build())
                    .onErrorResume(error -> {
                         return Mono.just(ApiResponse.<UserResponse>builder()
                                   .success(false)
                                   .message("Error creando usuario: " + error.getMessage())
                                   .build());
                    });
     }

     @Override
     public Mono<ApiResponse<UserCreationResponse>> createUserWithCredentials(CreateUserRequest request) {
          return validateCreateRequest(request)
                    .then(userCodeService.generateUserCode(request.getOrganizationId()))
                    .flatMap(userCode -> {
                         PersonalInfo personalInfo = PersonalInfo.builder()
                                   .firstName(request.getFirstName())
                                   .lastName(request.getLastName())
                                   .documentType(request.getDocumentType())
                                   .documentNumber(request.getDocumentNumber())
                                   .build();

                         Contact contact = Contact.builder()
                                   .email(request.getEmail())
                                   .phone(request.getPhone())
                                   .address(AddressUsers.builder()
                                             .fullAddress(request.getAddress())
                                             .streetId(request.getStreetId())
                                             .zoneId(request.getZoneId())
                                             .build())
                                   .build();

                         User user = User.builder()
                                   .userCode(userCode)
                                   .username("")
                                   .organizationId(request.getOrganizationId())
                                   .personalInfo(personalInfo)
                                   .contact(contact)
                                   .roles(request.getRoles())
                                   .status(UserStatus.ACTIVE)
                                   .registrationDate(LocalDateTime.now())
                                   .createdAt(LocalDateTime.now())
                                   .updatedAt(LocalDateTime.now())
                                   .build();

                         return userRepository.save(userMapper.toDocument(user))
                                   .flatMap(savedDoc -> {
                                        User savedUser = userMapper.toDomain(savedDoc);
                                        return userAuthIntegrationService
                                                  .registerUserWithAutoPassword(savedUser)
                                                  .flatMap(authResponse -> {
                                                       savedUser.setUsername(authResponse.username());

                                                       return userRepository.save(userMapper.toDocument(savedUser))
                                                                 .map(userMapper::toDomain)
                                                                 .map(userUpdated -> {
                                                                      UserResponse userResponse = mapToUserResponse(
                                                                                userUpdated);
                                                                      return UserCreationResponse.success(
                                                                                userResponse,
                                                                                authResponse.username(),
                                                                                authResponse.temporaryPassword());
                                                                 });
                                                  })
                                                  .onErrorResume(error -> {
                                                       UserResponse userResponse = mapToUserResponse(savedUser);
                                                       UserCreationResponse creationResponse = UserCreationResponse
                                                                 .builder()
                                                                 .userInfo(userResponse)
                                                                 .username(savedUser.getUsername())
                                                                 .temporaryPassword("ERROR_MS_AUTH")
                                                                 .message("Usuario creado en BD local, pero falló registro en MS-AUTHENTICATION: "
                                                                           + error.getMessage())
                                                                 .requiresPasswordChange(true)
                                                                 .build();

                                                       return Mono.just(creationResponse);
                                                  });
                                   });
                    })
                    .map(creationResponse -> ApiResponse.<UserCreationResponse>builder()
                              .data(creationResponse)
                              .message("Usuario creado exitosamente con credenciales")
                              .success(true)
                              .build())
                    .onErrorResume(error -> {
                         return Mono.just(ApiResponse.<UserCreationResponse>builder()
                                   .success(false)
                                   .message("Error creando usuario: " + error.getMessage())
                                   .data(UserCreationResponse.error("Error creando usuario: " + error.getMessage()))
                                   .build());
                    });
     }

     @Override
     public Mono<ApiResponse<UserResponse>> getUserById(String id) {
          return userRepository.findById(id)
                    .map(userMapper::toDomain)
                    .map(this::mapToUserResponse)
                    .map(userResponse -> ApiResponse.<UserResponse>builder()
                              .data(userResponse)
                              .message("Usuario encontrado")
                              .success(true)
                              .build())
                    .switchIfEmpty(Mono.just(ApiResponse.<UserResponse>builder()
                              .success(false)
                              .message("Usuario no encontrado")
                              .build()));
     }

     @Override
     public Mono<ApiResponse<UserResponse>> getUserByCode(String userCode) {
          return userRepository.findByUserCodeAndDeletedAtIsNull(userCode)
                    .map(userMapper::toDomain)
                    .map(this::mapToUserResponse)
                    .map(userResponse -> ApiResponse.<UserResponse>builder()
                              .data(userResponse)
                              .message("Usuario encontrado")
                              .success(true)
                              .build())
                    .switchIfEmpty(Mono.just(ApiResponse.<UserResponse>builder()
                              .success(false)
                              .message("Usuario no encontrado")
                              .build()));
     }

     @Override
     public Mono<ApiResponse<UserResponse>> getUserByUsername(String username) {
          return userRepository.findByUsernameAndDeletedAtIsNull(username)
                    .map(userMapper::toDomain)
                    .map(this::mapToUserResponse)
                    .map(userResponse -> {
                         return ApiResponse.<UserResponse>builder()
                                   .data(userResponse)
                                   .message("Usuario encontrado por username")
                                   .success(true)
                                   .build();
                    })
                    .switchIfEmpty(Mono.fromCallable(() -> {
                         return ApiResponse.<UserResponse>builder()
                                   .success(false)
                                   .message("Usuario no encontrado por username: " + username)
                                   .build();
                    }));
     }

     @Override
     public Mono<ApiResponse<Page<UserResponse>>> getUsersByOrganization(String organizationId, Pageable pageable) {
          return userRepository.findByOrganizationIdAndDeletedAtIsNull(organizationId, pageable)
                    .map(userMapper::toDomain)
                    .collectList()
                    .zipWith(userRepository.countByOrganizationIdAndDeletedAtIsNull(organizationId))
                    .map(tuple -> {
                         List<UserResponse> users = tuple.getT1().stream()
                                   .map(this::mapToUserResponse)
                                   .toList();

                         Page<UserResponse> page = new PageImpl<>(users, pageable, tuple.getT2());

                         return ApiResponse.<Page<UserResponse>>builder()
                                   .data(page)
                                   .message("Usuarios obtenidos exitosamente")
                                   .success(true)
                                   .build();
                    });
     }

     @Override
     public Mono<ApiResponse<List<UserResponse>>> getUsersByRole(String organizationId, RolesUsers role) {
          return userRepository.findByOrganizationIdAndRoleAndDeletedAtIsNull(organizationId, role)
                    .map(userMapper::toDomain)
                    .map(this::mapToUserResponse)
                    .collectList()
                    .map(users -> ApiResponse.<List<UserResponse>>builder()
                              .data(users)
                              .message("Usuarios obtenidos exitosamente")
                              .success(true)
                              .build());
     }

     @Override
     public Mono<ApiResponse<List<UserResponse>>> getUsersByRoleGlobal(RolesUsers role) {
          return userRepository.findByRoleAndDeletedAtIsNull(role)
                    .map(userMapper::toDomain)
                    .map(this::mapToUserResponse)
                    .collectList()
                    .map(users -> ApiResponse.<List<UserResponse>>builder()
                              .data(users)
                              .message("Usuarios globales obtenidos exitosamente")
                              .success(true)
                              .build());
     }

     @Override
     public Mono<ApiResponse<UserResponse>> updateUser(String id, UpdateUserRequest request) {
          return userRepository.findByIdAndDeletedAtIsNull(id)
                    .flatMap(userDoc -> {
                         User user = userMapper.toDomain(userDoc);
                         updateUserFields(user, request);
                         return userRepository.save(userMapper.toDocument(user));
                    })
                    .map(userMapper::toDomain)
                    .map(this::mapToUserResponse)
                    .map(userResponse -> ApiResponse.<UserResponse>builder()
                              .data(userResponse)
                              .message("Usuario actualizado exitosamente")
                              .success(true)
                              .build())
                    .switchIfEmpty(Mono.just(ApiResponse.<UserResponse>builder()
                              .success(false)
                              .message("Usuario no encontrado")
                              .build()));
     }

     @Override
     public Mono<ApiResponse<UserResponse>> patchUser(String id, UpdateUserPatchRequest request) {
          return userRepository.findByIdAndDeletedAtIsNull(id)
                    .flatMap(userDoc -> {
                         User user = userMapper.toDomain(userDoc);
                         updateUserPatchFields(user, request);
                         return userRepository.save(userMapper.toDocument(user));
                    })
                    .map(userMapper::toDomain)
                    .map(this::mapToUserResponse)
                    .map(userResponse -> ApiResponse.<UserResponse>builder()
                              .data(userResponse)
                              .message("Usuario actualizado parcialmente exitosamente")
                              .success(true)
                              .build())
                    .switchIfEmpty(Mono.just(ApiResponse.<UserResponse>builder()
                              .success(false)
                              .message("Usuario no encontrado")
                              .build()));
     }

     @Override
     public Mono<ApiResponse<Void>> deleteUser(String id) {
          return userRepository.findByIdAndDeletedAtIsNull(id)
                    .flatMap(userDoc -> {
                         User user = userMapper.toDomain(userDoc);
                         user.setDeletedAt(LocalDateTime.now());
                         user.setUpdatedAt(LocalDateTime.now());
                         user.setStatus(UserStatus.INACTIVE);
                         return userRepository.save(userMapper.toDocument(user));
                    })
                    .then(Mono.just(ApiResponse.<Void>builder()
                              .message("Usuario eliminado exitosamente")
                              .success(true)
                              .build()))
                    .switchIfEmpty(Mono.just(ApiResponse.<Void>builder()
                              .success(false)
                              .message("Usuario no encontrado")
                              .build()));
     }

     @Override
     public Mono<ApiResponse<UserResponse>> changeUserStatus(String id, UserStatus status) {
          return userRepository.findById(id)
                    .flatMap(userDoc -> {
                         User user = userMapper.toDomain(userDoc);
                         user.setStatus(status);
                         user.setUpdatedAt(LocalDateTime.now());

                         if (status == UserStatus.INACTIVE) {
                              user.setDeletedAt(LocalDateTime.now());
                         } else if (status == UserStatus.ACTIVE) {
                              user.setDeletedAt(null);
                         }

                         return userRepository.save(userMapper.toDocument(user));
                    })
                    .map(userMapper::toDomain)
                    .map(this::mapToUserResponse)
                    .map(userResponse -> ApiResponse.<UserResponse>builder()
                              .data(userResponse)
                              .message("Estado de usuario actualizado exitosamente")
                              .success(true)
                              .build())
                    .switchIfEmpty(Mono.just(ApiResponse.<UserResponse>builder()
                              .success(false)
                              .message("Usuario no encontrado")
                              .build()));
     }

     @Override
     public Mono<ApiResponse<Void>> deleteUserPermanently(String id) {
          return userRepository.findById(id)
                    .switchIfEmpty(Mono.error(new RuntimeException("Usuario no encontrado")))
                    .flatMap(user -> userRepository.deleteById(id))
                    .then(Mono.just(ApiResponse.<Void>builder()
                              .success(true)
                              .message("Usuario eliminado permanentemente")
                              .build()))
                    .onErrorReturn(ApiResponse.<Void>builder()
                              .success(false)
                              .message("Error al eliminar usuario permanentemente")
                              .build());
     }

     @Override
     public Mono<ApiResponse<UserResponse>> restoreUser(String id) {
          return userRepository.findById(id)
                    .switchIfEmpty(Mono.error(new RuntimeException("Usuario no encontrado")))
                    .flatMap(userDoc -> {
                         User user = userMapper.toDomain(userDoc);
                         user.setStatus(UserStatus.ACTIVE);
                         user.setUpdatedAt(LocalDateTime.now());
                         user.setDeletedAt(null);

                         return userRepository.save(userMapper.toDocument(user));
                    })
                    .map(userMapper::toDomain)
                    .map(this::mapToUserResponse)
                    .map(userResponse -> ApiResponse.<UserResponse>builder()
                              .success(true)
                              .message("Usuario restaurado correctamente")
                              .data(userResponse)
                              .build())
                    .onErrorReturn(ApiResponse.<UserResponse>builder()
                              .success(false)
                              .message("Error al restaurar usuario")
                              .build());
     }

     @Override
     public Mono<ApiResponse<List<UserResponse>>> getActiveUsersByOrganization(String organizationId) {
          return userRepository.findByOrganizationIdAndStatusAndDeletedAtIsNull(organizationId, UserStatus.ACTIVE)
                    .map(userMapper::toDomain)
                    .map(this::mapToUserResponse)
                    .collectList()
                    .map(users -> ApiResponse.<List<UserResponse>>builder()
                              .success(true)
                              .message("Usuarios activos obtenidos correctamente")
                              .data(users)
                              .build())
                    .onErrorReturn(ApiResponse.<List<UserResponse>>builder()
                              .success(false)
                              .message("Error al obtener usuarios activos")
                              .build());
     }

     @Override
     public Mono<ApiResponse<List<UserResponse>>> getInactiveUsersByOrganization(String organizationId) {
          return userRepository.findByOrganizationIdAndStatusAndDeletedAtIsNull(organizationId, UserStatus.INACTIVE)
                    .map(userMapper::toDomain)
                    .map(this::mapToUserResponse)
                    .collectList()
                    .map(users -> ApiResponse.<List<UserResponse>>builder()
                              .success(true)
                              .message("Usuarios inactivos obtenidos correctamente")
                              .data(users)
                              .build())
                    .onErrorReturn(ApiResponse.<List<UserResponse>>builder()
                              .success(false)
                              .message("Error al obtener usuarios inactivos")
                              .build());
     }

     @Override
     public Mono<ApiResponse<List<UserResponse>>> getAllUsersByOrganization(String organizationId) {
          return userRepository.findByOrganizationId(organizationId)
                    .map(userMapper::toDomain)
                    .map(this::mapToUserResponse)
                    .collectList()
                    .map(users -> {
                         return ApiResponse.<List<UserResponse>>builder()
                                   .success(true)
                                   .message("Todos los usuarios (activos e inactivos) obtenidos correctamente")
                                   .data(users)
                                   .build();
                    })
                    .onErrorResume(error -> {
                         return Mono.just(ApiResponse.<List<UserResponse>>builder()
                                   .success(false)
                                   .message("Error al obtener usuarios: " + error.getMessage())
                                   .build());
                    });
     }

     @Override
     public Mono<ApiResponse<Boolean>> getUserByEmail(String email) {
          return userRepository.existsByContactEmailAndDeletedAtIsNull(email)
                    .map(exists -> ApiResponse.<Boolean>builder()
                              .data(exists)
                              .message("Verificación de email completada")
                              .success(true)
                              .build())
                    .onErrorReturn(ApiResponse.<Boolean>builder()
                              .data(false)
                              .message("Error al verificar email")
                              .success(false)
                              .build());
     }

     @Override
     public Mono<ApiResponse<UserResponse>> findUserByEmail(String email) {
          return userRepository.findByContactEmailAndDeletedAtIsNull(email)
                    .map(userMapper::toDomain)
                    .map(this::mapToUserResponse)
                    .map(userResponse -> ApiResponse.<UserResponse>builder()
                              .data(userResponse)
                              .message("Usuario encontrado por email")
                              .success(true)
                              .build())
                    .switchIfEmpty(Mono.just(ApiResponse.<UserResponse>builder()
                              .success(false)
                              .message("Usuario no encontrado por email")
                              .build()));
     }

     @Override
     public Mono<ApiResponse<Boolean>> getUserByDocumentNumber(String documentNumber) {
          return userRepository.existsByPersonalInfoDocumentNumberAndDeletedAtIsNull(documentNumber)
                    .map(exists -> ApiResponse.<Boolean>builder()
                              .data(exists)
                              .message("Verificación de DNI completada")
                              .success(true)
                              .build())
                    .onErrorReturn(ApiResponse.<Boolean>builder()
                              .data(false)
                              .message("Error al verificar DNI")
                              .success(false)
                              .build());
     }

     @Override
     public Mono<ApiResponse<Boolean>> getUserByPhone(String phone) {
          return userRepository.existsByContactPhoneAndDeletedAtIsNull(phone)
                    .map(exists -> ApiResponse.<Boolean>builder()
                              .data(exists)
                              .message("Verificación de teléfono completada")
                              .success(true)
                              .build())
                    .onErrorReturn(ApiResponse.<Boolean>builder()
                              .data(false)
                              .message("Error al verificar teléfono")
                              .success(false)
                              .build());
     }

     private Mono<Void> validateCreateRequest(CreateUserRequest request) {
          return userRepository.existsByPersonalInfoDocumentNumberAndDeletedAtIsNull(request.getDocumentNumber())
                    .flatMap(exists -> {
                         if (Boolean.TRUE.equals(exists)) {
                              return Mono.error(new IllegalArgumentException(
                                        "Ya existe un usuario con este número de documento"));
                         }
                         return Mono.empty();
                    })
                    .then(userRepository.existsByContactEmailAndDeletedAtIsNull(request.getEmail()))
                    .flatMap(exists -> {
                         if (Boolean.TRUE.equals(exists)) {
                              return Mono.error(new IllegalArgumentException("Ya existe un usuario con este email"));
                         }
                         return Mono.empty();
                    });
     }

     private void updateUserFields(User user, UpdateUserRequest request) {
          if (request.getFirstName() != null) {
               user.getPersonalInfo().setFirstName(request.getFirstName());
          }
          if (request.getLastName() != null) {
               user.getPersonalInfo().setLastName(request.getLastName());
          }
          if (request.getEmail() != null) {
               user.getContact().setEmail(request.getEmail());
          }
          if (request.getPhone() != null) {
               user.getContact().setPhone(request.getPhone());
          }
          if (request.getAddress() != null) {
               if (user.getContact().getAddress() == null) {
                    user.getContact().setAddress(AddressUsers.builder().build());
               }
               user.getContact().getAddress().setFullAddress(request.getAddress());
          }
          if (request.getRoles() != null && !request.getRoles().isEmpty()) {
               user.setRoles(request.getRoles());
          }
          user.setUpdatedAt(LocalDateTime.now());
     }

     private void updateUserPatchFields(User user, UpdateUserPatchRequest request) {
          if (request.getEmail() != null) {
               user.getContact().setEmail(request.getEmail());
          }
          if (request.getPhone() != null) {
               user.getContact().setPhone(request.getPhone());
          }

          String addressToUpdate = null;
          if (request.getAddress() != null) {
               addressToUpdate = request.getAddress();
          } else if (request.getStreetAddress() != null) {
               addressToUpdate = request.getStreetAddress();
          }

          if (addressToUpdate != null) {
               if (user.getContact().getAddress() == null) {
                    user.getContact().setAddress(AddressUsers.builder().build());
               }
               user.getContact().getAddress().setFullAddress(addressToUpdate);
          }

          if (request.getStreetId() != null) {
               if (user.getContact().getAddress() == null) {
                    user.getContact().setAddress(AddressUsers.builder().build());
               }
               user.getContact().getAddress().setStreetId(request.getStreetId());
          }
          if (request.getZoneId() != null) {
               if (user.getContact().getAddress() == null) {
                    user.getContact().setAddress(AddressUsers.builder().build());
               }
               user.getContact().getAddress().setZoneId(request.getZoneId());
          }
          user.setUpdatedAt(LocalDateTime.now());
     }

     private UserResponse mapToUserResponse(User user) {
          return UserResponse.builder()
                    .id(user.getId())
                    .userCode(user.getUserCode())
                    .firstName(user.getPersonalInfo() != null ? user.getPersonalInfo().getFirstName() : null)
                    .lastName(user.getPersonalInfo() != null ? user.getPersonalInfo().getLastName() : null)
                    .documentType(user.getPersonalInfo() != null ? user.getPersonalInfo().getDocumentType() : null)
                    .documentNumber(user.getPersonalInfo() != null ? user.getPersonalInfo().getDocumentNumber() : null)
                    .email(user.getContact() != null ? user.getContact().getEmail() : null)
                    .phone(user.getContact() != null ? user.getContact().getPhone() : null)
                    .address(user.getContact() != null && user.getContact().getAddress() != null
                              ? user.getContact().getAddress().getFullAddress()
                              : null)
                    .organizationId(user.getOrganizationId())
                    .streetId(user.getContact() != null && user.getContact().getAddress() != null
                              ? user.getContact().getAddress().getStreetId()
                              : null)
                    .zoneId(user.getContact() != null && user.getContact().getAddress() != null
                              ? user.getContact().getAddress().getZoneId()
                              : null)
                    .roles(user.getRoles() != null ? user.getRoles() : Set.of())
                    .status(user.getStatus())
                    .createdAt(user.getCreatedAt())
                    .updatedAt(user.getUpdatedAt())
                    .build();
     }

     @Override
     public Mono<ApiResponse<Boolean>> getEmailAvailability(String email) {
          return userRepository.existsByContactEmailAndDeletedAtIsNull(email)
                    .map(exists -> ApiResponse.<Boolean>builder()
                              .data(!exists)
                              .message("Verificación de disponibilidad de email completada")
                              .success(true)
                              .build())
                    .onErrorReturn(ApiResponse.<Boolean>builder()
                              .data(false)
                              .message("Error al verificar disponibilidad del email")
                              .success(false)
                              .build());
     }

     @Override
     public Mono<Long> countSuperAdmins() {
          return userRepository.countByRolesContainingAndDeletedAtIsNull(RolesUsers.SUPER_ADMIN);
     }

     @Override
     public Mono<ApiResponse<List<CompleteUserResponse>>> getCompleteUsersByOrganization(String organizationId) {
          return userRepository.findByOrganizationIdAndDeletedAtIsNull(organizationId)
                    .map(userMapper::toDomain)
                    .collectList()
                    .flatMap(users -> {
                         if (users.isEmpty()) {
                              return Mono.just(ApiResponse
                                        .<List<CompleteUserResponse>>success("No se encontraron usuarios", List.of()));
                         }

                         return Mono.fromCallable(() -> users.stream()
                                   .map(this::mapToCompleteUserResponse)
                                   .toList())
                                   .map(completeUsers -> {
                                        return ApiResponse.<List<CompleteUserResponse>>success(
                                                  "Usuarios completos obtenidos exitosamente",
                                                  completeUsers);
                                   });
                    })
                    .onErrorReturn(
                              ApiResponse.<List<CompleteUserResponse>>error("Error obteniendo usuarios completos"));
     }

     @Override
     public Mono<ApiResponse<List<CompleteUserResponse>>> getCompleteUsersByRole(String organizationId,
               RolesUsers role) {
          return userRepository.findByOrganizationIdAndRoleAndDeletedAtIsNull(organizationId, role)
                    .map(userMapper::toDomain)
                    .collectList()
                    .flatMap(users -> {
                         if (users.isEmpty()) {
                              return Mono.just(ApiResponse.<List<CompleteUserResponse>>success(
                                        "No se encontraron usuarios con el rol especificado",
                                        List.of()));
                         }

                         return Mono.fromCallable(() -> users.stream()
                                   .map(this::mapToCompleteUserResponse)
                                   .toList())
                                   .map(completeUsers -> {
                                        return ApiResponse.<List<CompleteUserResponse>>success(
                                                  "Usuarios completos obtenidos exitosamente",
                                                  completeUsers);
                                   });
                    })
                    .onErrorReturn(ApiResponse
                              .<List<CompleteUserResponse>>error("Error obteniendo usuarios completos por rol"));
     }

     @Override
     public Mono<ApiResponse<CompleteUserResponse>> getCompleteUserById(String userId) {
          return userRepository.findByIdAndDeletedAtIsNull(userId)
                    .map(userMapper::toDomain)
                    .map(this::mapToCompleteUserResponse)
                    .map(completeUser -> {
                         return ApiResponse.success("Usuario completo obtenido exitosamente", completeUser);
                    })
                    .switchIfEmpty(Mono.just(ApiResponse.error("Usuario no encontrado", null)))
                    .onErrorReturn(ApiResponse.error("Error obteniendo usuario completo", null));
     }

     @Override
     public Mono<ApiResponse<CompleteUserResponse>> getCompleteUserByIdIncludingDeleted(String userId) {
          return userRepository.findById(userId)
                    .map(userMapper::toDomain)
                    .map(this::mapToCompleteUserResponse)
                    .map(completeUser -> {
                         return ApiResponse.success("Usuario completo obtenido exitosamente", completeUser);
                    })
                    .switchIfEmpty(Mono.just(ApiResponse.error("Usuario no encontrado", null)))
                    .onErrorReturn(ApiResponse.error("Error obteniendo usuario completo", null));
     }

     private CompleteUserResponse mapToCompleteUserResponse(User user) {
          return CompleteUserResponse.builder()
                    .id(user.getId())
                    .userCode(user.getUserCode())
                    .username(user.getUsername())
                    .firstName(user.getPersonalInfo() != null ? user.getPersonalInfo().getFirstName() : null)
                    .lastName(user.getPersonalInfo() != null ? user.getPersonalInfo().getLastName() : null)
                    .documentType(user.getPersonalInfo() != null && user.getPersonalInfo().getDocumentType() != null
                              ? user.getPersonalInfo().getDocumentType().name()
                              : null)
                    .documentNumber(user.getPersonalInfo() != null ? user.getPersonalInfo().getDocumentNumber() : null)
                    .email(user.getContact() != null ? user.getContact().getEmail() : null)
                    .phone(user.getContact() != null ? user.getContact().getPhone() : null)
                    .address(user.getContact() != null && user.getContact().getAddress() != null
                              ? user.getContact().getAddress().getFullAddress()
                              : null)
                    .organizationId(user.getOrganizationId())
                    .streetId(user.getContact() != null && user.getContact().getAddress() != null
                              ? user.getContact().getAddress().getStreetId()
                              : null)
                    .zoneId(user.getContact() != null && user.getContact().getAddress() != null
                              ? user.getContact().getAddress().getZoneId()
                              : null)
                    .roles(user.getRoles() != null ? user.getRoles() : Set.of())
                    .status(user.getStatus())
                    .createdAt(user.getCreatedAt())
                    .updatedAt(user.getUpdatedAt())
                    .deletedAt(user.getDeletedAt())
                    .build();
     }
}
