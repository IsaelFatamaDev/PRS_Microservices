package pe.edu.vallegrande.vgmsusers.application.mappers;

import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vgmsusers.application.dto.request.CreateUserRequest;
import pe.edu.vallegrande.vgmsusers.application.dto.request.UpdateUserRequest;
import pe.edu.vallegrande.vgmsusers.application.dto.response.UserResponse;
import pe.edu.vallegrande.vgmsusers.domain.models.User;
import pe.edu.vallegrande.vgmsusers.domain.models.valueobjects.RecordStatus;

import java.time.LocalDateTime;

// SRP: Responsabilidad única - Conversión DTO ↔ Domain (Capa Aplicación)
// NO maneja conversiones Domain ↔ Entity (eso es responsabilidad de Infrastructure)
@Component
public class UserMapper {

     // DTO → Domain (Capa Aplicación → Dominio)
     public User toDomain(CreateUserRequest request) {
          return User.builder()
                    .username(request.getUsername())
                    .firstName(request.getFirstName())
                    .lastName(request.getLastName())
                    .documentType(request.getDocumentType())
                    .documentNumber(request.getDocumentNumber())
                    .email(request.getEmail())
                    .phone(request.getPhone())
                    .address(request.getAddress())
                    .role(request.getRole())
                    .status(RecordStatus.ACTIVE)
                    .organizationId(request.getOrganizationId())
                    .zoneId(request.getZoneId())
                    .streetId(request.getStreetId())
                    .createdAt(LocalDateTime.now())
                    .build();
     }

     // UpdateRequest → Domain (Actualización parcial)
     public User updateDomain(User existing, UpdateUserRequest request) {
          if (request.getFirstName() != null)
               existing.setFirstName(request.getFirstName());
          if (request.getLastName() != null)
               existing.setLastName(request.getLastName());
          if (request.getDocumentType() != null)
               existing.setDocumentType(request.getDocumentType());
          if (request.getDocumentNumber() != null)
               existing.setDocumentNumber(request.getDocumentNumber());
          if (request.getEmail() != null)
               existing.setEmail(request.getEmail());
          if (request.getPhone() != null)
               existing.setPhone(request.getPhone());
          if (request.getAddress() != null)
               existing.setAddress(request.getAddress());
          if (request.getRole() != null)
               existing.setRole(request.getRole());
          if (request.getOrganizationId() != null)
               existing.setOrganizationId(request.getOrganizationId());
          if (request.getZoneId() != null)
               existing.setZoneId(request.getZoneId());
          if (request.getStreetId() != null)
               existing.setStreetId(request.getStreetId());
          existing.setUpdatedAt(LocalDateTime.now());
          return existing;
     }

     // Domain → DTO (Dominio → Capa Aplicación)
     public UserResponse toResponse(User user) {
          return UserResponse.builder()
                    .userId(user.getUserId())
                    .username(user.getUsername())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .fullName(user.getFullName())
                    .documentType(user.getDocumentType())
                    .documentNumber(user.getDocumentNumber())
                    .email(user.getEmail())
                    .phone(user.getPhone())
                    .address(user.getAddress())
                    .role(user.getRole())
                    .status(user.getStatus())
                    .organizationId(user.getOrganizationId())
                    .zoneId(user.getZoneId())
                    .streetId(user.getStreetId())
                    .createdAt(user.getCreatedAt())
                    .updatedAt(user.getUpdatedAt())
                    .build();
     }
}
