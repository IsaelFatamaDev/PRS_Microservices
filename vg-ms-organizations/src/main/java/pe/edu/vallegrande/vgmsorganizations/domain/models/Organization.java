package pe.edu.vallegrande.vgmsorganizations.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.vallegrande.vgmsorganizations.domain.events.*;
import pe.edu.vallegrande.vgmsorganizations.domain.models.valueobjects.OrganizationType;
import pe.edu.vallegrande.vgmsorganizations.domain.models.valueobjects.RecordStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Aggregate Root - Organization (JASS)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Organization {
    
    private String id;
    private String name;
    private String ruc;
    private String address;
    private String phone;
    private String email;
    private String region;
    private String province;
    private String district;
    private String legalRepresentative;
    private OrganizationType type;
    private RecordStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;

    // Eventos de dominio
    @Builder.Default
    private List<DomainEvent> domainEvents = new ArrayList<>();

    // Métodos de negocio
    public void activate() {
        this.status = RecordStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = RecordStatus.INACTIVE;
    }

    public void suspend() {
        this.status = RecordStatus.SUSPENDED;
    }

    public boolean isActive() {
        return this.status == RecordStatus.ACTIVE;
    }

    // Gestión de eventos
    public void registerEvent(DomainEvent event) {
        this.domainEvents.add(event);
    }

    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    public void clearDomainEvents() {
        this.domainEvents.clear();
    }

    // Factory methods
    public static Organization createNew(Organization org) {
        org.registerEvent(OrganizationCreatedEvent.from(org));
        return org;
    }

    public void updateWith(Organization updates) {
        Organization previousState = this.copy();
        this.setName(updates.getName());
        this.setAddress(updates.getAddress());
        this.setPhone(updates.getPhone());
        this.setEmail(updates.getEmail());
        this.setLegalRepresentative(updates.getLegalRepresentative());
        this.registerEvent(OrganizationUpdatedEvent.from(previousState, this));
    }

    public void markAsDeleted() {
        this.setStatus(RecordStatus.INACTIVE);
        this.registerEvent(OrganizationDeletedEvent.from(this.id, this.name));
    }

    private Organization copy() {
        return Organization.builder()
                .id(this.id)
                .name(this.name)
                .ruc(this.ruc)
                .address(this.address)
                .phone(this.phone)
                .email(this.email)
                .region(this.region)
                .province(this.province)
                .district(this.district)
                .legalRepresentative(this.legalRepresentative)
                .type(this.type)
                .status(this.status)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .createdBy(this.createdBy)
                .updatedBy(this.updatedBy)
                .build();
    }
}
