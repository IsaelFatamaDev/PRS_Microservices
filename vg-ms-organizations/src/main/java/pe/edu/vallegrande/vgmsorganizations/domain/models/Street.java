package pe.edu.vallegrande.vgmsorganizations.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.vallegrande.vgmsorganizations.domain.events.*;
import pe.edu.vallegrande.vgmsorganizations.domain.models.valueobjects.RecordStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Aggregate Root - Street (Calle dentro de una zona)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Street {

     private String id;
     private String zoneId;
     private String streetName;
     private String streetCode;
     private String description;
     private RecordStatus status;
     private LocalDateTime createdAt;
     private String createdBy;

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
     public static Street createNew(Street street) {
          street.registerEvent(StreetCreatedEvent.from(street));
          return street;
     }
}
