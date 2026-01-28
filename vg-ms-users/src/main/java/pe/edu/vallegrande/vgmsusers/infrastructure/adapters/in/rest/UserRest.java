package pe.edu.vallegrande.vgmsusers.infrastructure.adapters.in.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.vgmsusers.application.dto.common.ApiResponse;
import pe.edu.vallegrande.vgmsusers.application.dto.request.CreateUserRequest;
import pe.edu.vallegrande.vgmsusers.application.dto.request.UpdateUserRequest;
import pe.edu.vallegrande.vgmsusers.application.dto.response.UserResponse;
import pe.edu.vallegrande.vgmsusers.application.mappers.UserMapper;
import pe.edu.vallegrande.vgmsusers.domain.models.User;
import pe.edu.vallegrande.vgmsusers.domain.models.valueobjects.RecordStatus;
import pe.edu.vallegrande.vgmsusers.domain.models.valueobjects.Role;
import pe.edu.vallegrande.vgmsusers.domain.ports.in.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserRest {

     private final ICreateUserUseCase createUserUseCase;
     private final IGetUserUseCase getUserUseCase;
     private final IUpdateUserUseCase updateUserUseCase;
     private final IDeleteUserUseCase deleteUserUseCase;
     private final UserMapper mapper;

     @PostMapping
     @ResponseStatus(HttpStatus.CREATED)
     public Mono<ApiResponse<UserResponse>> createUser(@Valid @RequestBody CreateUserRequest request) {
          User user = mapper.toDomain(request);
          return createUserUseCase.execute(user, request.getPassword())
                    .map(mapper::toResponse)
                    .map(response -> ApiResponse.success(response, "User created successfully"))
                    .doOnSuccess(r -> log.info("User created: {}", r.getData().getUsername()));
     }

     @GetMapping("/{userId}")
     public Mono<ApiResponse<UserResponse>> getUserById(@PathVariable UUID userId) {
          return getUserUseCase.findById(userId)
                    .map(mapper::toResponse)
                    .map(response -> ApiResponse.success(response, "User found"))
                    .switchIfEmpty(Mono.just(ApiResponse.error(404, "User not found")));
     }

     @GetMapping("/username/{username}")
     public Mono<ApiResponse<UserResponse>> getUserByUsername(@PathVariable String username) {
          return getUserUseCase.findByUsername(username)
                    .map(mapper::toResponse)
                    .map(response -> ApiResponse.success(response, "User found"))
                    .switchIfEmpty(Mono.just(ApiResponse.error(404, "User not found")));
     }

     @GetMapping
     public Flux<UserResponse> getAllUsers() {
          return getUserUseCase.findAll()
                    .map(mapper::toResponse);
     }

     @GetMapping("/role/{role}")
     public Flux<UserResponse> getUsersByRole(@PathVariable Role role) {
          return getUserUseCase.findByRole(role)
                    .map(mapper::toResponse);
     }

     @GetMapping("/status/{status}")
     public Flux<UserResponse> getUsersByStatus(@PathVariable RecordStatus status) {
          return getUserUseCase.findByStatus(status)
                    .map(mapper::toResponse);
     }

     @GetMapping("/organization/{organizationId}")
     public Flux<UserResponse> getUsersByOrganization(@PathVariable String organizationId) {
          return getUserUseCase.findByOrganization(organizationId)
                    .map(mapper::toResponse);
     }

     @PutMapping("/{userId}")
     public Mono<ApiResponse<UserResponse>> updateUser(
               @PathVariable UUID userId,
               @Valid @RequestBody UpdateUserRequest request) {
          return getUserUseCase.findById(userId)
                    .map(existing -> mapper.updateDomain(existing, request))
                    .flatMap(updated -> updateUserUseCase.execute(userId, updated))
                    .map(mapper::toResponse)
                    .map(response -> ApiResponse.success(response, "User updated successfully"))
                    .switchIfEmpty(Mono.just(ApiResponse.error(404, "User not found")));
     }

     @DeleteMapping("/{userId}")
     @ResponseStatus(HttpStatus.NO_CONTENT)
     public Mono<Void> softDeleteUser(@PathVariable UUID userId) {
          return deleteUserUseCase.softDelete(userId);
     }

     @PatchMapping("/{userId}/restore")
     public Mono<ApiResponse<String>> restoreUser(@PathVariable UUID userId) {
          return deleteUserUseCase.restore(userId)
                    .then(Mono.just(ApiResponse.success("User restored", "User restored successfully")));
     }
}
