package pe.edu.vallegrande.vgmsorganizations.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.vallegrande.vgmsorganizations.domain.events.*;
import pe.edu.vallegrande.vgmsorganizations.domain.models.valueobjects.Coordinates;
import pe.edu.vallegrande.vgmsorganizations.domain.models.valueobjects.RecordStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Aggregate Root - Zone (Zona/Sector dentro de una organización)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Zone {
    
    private String id;
    private String organizationId;
    private String zoneName;
    private String zoneCode;
    private String description;
    private BigDecimal currentMonthlyFee;
    private Coordinates coordinates;
    private Integer totalWaterBoxes;
    private Integer activeWaterBoxes;
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

    public boolean isActive() {
        return this.status == RecordStatus.ACTIVE;
    }

    public void updateFee(BigDecimal newFee, String changedBy, String reason) {
        BigDecimal oldFee = this.currentMonthlyFee;
        this.currentMonthlyFee = newFee;
        this.registerEvent(ZoneFeeChangedEvent.from(this.id, oldFee, newFee, changedBy, reason));
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
    public static Zone createNew(Zone zone) {
        zone.registerEvent(ZoneCreatedEvent.from(zone));
        return zone;
    }

    public void updateWith(Zone updates) {
        Zone previousState = this.copy();
        this.setZoneName(updates.getZoneName());
        this.setDescription(updates.getDescription());
        this.setCoordinates(updates.getCoordinates());
        this.registerEvent(ZoneUpdatedEvent.from(previousState, this));
    }

    private Zone copy() {
        return Zone.builder()
                .id(this.id)
                .organizationId(this.organizationId)
                .zoneName(this.zoneName)
                .zoneCode(this.zoneCode)
                .description(this.description)
                .currentMonthlyFee(this.currentMonthlyFee)
                .coordinates(this.coordinates)
                .totalWaterBoxes(this.totalWaterBoxes)
                .activeWaterBoxes(this.activeWaterBoxes)
                .status(this.status)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .createdBy(this.createdBy)
                .updatedBy(this.updatedBy)
                .build();
    }
}
