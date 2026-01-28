package pe.edu.vallegrande.vgmsusers.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.vallegrande.vgmsusers.domain.events.*;
import pe.edu.vallegrande.vgmsusers.domain.models.valueobjects.DocumentType;
import pe.edu.vallegrande.vgmsusers.domain.models.valueobjects.RecordStatus;
import pe.edu.vallegrande.vgmsusers.domain.models.valueobjects.Role;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

// Aggregate Root - Maneja eventos de dominio
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private UUID userId;
    private String username;
    private String firstName;
    private String lastName;
    private DocumentType documentType;
    private String documentNumber;
    private String email;
    private String phone;
    private String address;
    private Role role;
    private RecordStatus status;
    private String organizationId;
    private String zoneId;
    private String streetId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UUID createdBy;
    private UUID updatedBy;

    public void activate() {
        this.status = RecordStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.status = RecordStatus.INACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return this.status == RecordStatus.ACTIVE;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    // Gestión de eventos de dominio
    public void registerEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }

    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        this.domainEvents.clear();
    }

    // Factory methods que registran eventos
    public static User createNew(User user) {
        user.registerEvent(UserCreatedEvent.from(user));
        return user;
    }

    public void updateWith(User updates) {
        User previousState = this.copy();
        // Actualizar campos
        this.setFirstName(updates.getFirstName());
        this.setLastName(updates.getLastName());
        this.setEmail(updates.getEmail());
        this.setPhone(updates.getPhone());
        this.setAddress(updates.getAddress());
        // Registrar evento
        this.registerEvent(UserUpdatedEvent.from(previousState, this));
    }

    public void markAsDeleted() {
        this.setStatus(RecordStatus.INACTIVE);
        this.registerEvent(UserDeletedEvent.from(this.userId, this.username));
    }

    public void markAsRestored() {
        this.setStatus(RecordStatus.ACTIVE);
        this.registerEvent(UserRestoredEvent.from(this.userId, this.username));
    }

    private User copy() {
          return User.builder()
                    .userId(this.userId)
                    .username(this.username)
                    .firstName(this.firstName)
                    .lastName(this.lastName)
                    .documentType(this.documentType)
                    .documentNumber(this.documentNumber)
                    .email(this.email)
                    .phone(this.phone)
                    .address(this.address)
                    .role(this.role)
                    .status(this.status)
                    .organizationId(this.organizationId)
                    .zoneId(this.zoneId)
                    .streetId(this.streetId)
                    .createdAt(this.createdAt)
                    .updatedAt(this.updatedAt)
                    .createdBy(this.createdBy)
                    .updatedBy(this.updatedBy)
                    .build();
     }
