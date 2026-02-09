# ⚙️ APPLICATION LAYER - Capa de Aplicación

> **Orquestación de casos de uso. Conecta el dominio con la infraestructura.**

## 📋 Principios

1. **Un caso de uso = Una clase**: Cada operación tiene su propia implementación
2. **DTOs para entrada/salida**: Nunca exponer modelos de dominio directamente
3. **Mappers centralizados**: Conversión entre DTOs, modelos y entidades
4. **Eventos asíncronos**: Publicación de eventos tras cada operación

---

## 📂 Estructura

```text
application/
├── usecases/                                       # 🎯 Casos de uso (Servicios)
│   ├── organization/
│   │   ├── CreateOrganizationUseCaseImpl.java      #    └─ @Service (implements ICreate...)
│   │   ├── GetOrganizationUseCaseImpl.java         #    └─ @Service (implements IGet...)
│   │   ├── UpdateOrganizationUseCaseImpl.java      #    └─ @Service (implements IUpdate...)
│   │   ├── DeleteOrganizationUseCaseImpl.java      #    └─ @Service (implements IDelete...)
│   │   └── RestoreOrganizationUseCaseImpl.java     #    └─ @Service (implements IRestore...)
│   ├── zone/
│   │   ├── CreateZoneUseCaseImpl.java              #    └─ @Service
│   │   ├── GetZoneUseCaseImpl.java                 #    └─ @Service
│   │   ├── UpdateZoneUseCaseImpl.java              #    └─ @Service
│   │   ├── DeleteZoneUseCaseImpl.java              #    └─ @Service
│   │   └── RestoreZoneUseCaseImpl.java             #    └─ @Service
│   ├── street/
│   │   ├── CreateStreetUseCaseImpl.java            #    └─ @Service
│   │   ├── GetStreetUseCaseImpl.java               #    └─ @Service
│   │   ├── UpdateStreetUseCaseImpl.java            #    └─ @Service
│   │   ├── DeleteStreetUseCaseImpl.java            #    └─ @Service
│   │   └── RestoreStreetUseCaseImpl.java           #    └─ @Service
│   ├── fare/
│   │   ├── CreateFareUseCaseImpl.java              #    └─ @Service
│   │   ├── GetFareUseCaseImpl.java                 #    └─ @Service
│   │   ├── UpdateFareUseCaseImpl.java              #    └─ @Service
│   │   ├── DeleteFareUseCaseImpl.java              #    └─ @Service
│   │   └── RestoreFareUseCaseImpl.java             #    └─ @Service
│   └── parameter/
│       ├── CreateParameterUseCaseImpl.java         #    └─ @Service
│       ├── GetParameterUseCaseImpl.java            #    └─ @Service
│       ├── UpdateParameterUseCaseImpl.java         #    └─ @Service
│       ├── DeleteParameterUseCaseImpl.java         #    └─ @Service
│       └── RestoreParameterUseCaseImpl.java        #    └─ @Service
│
├── dto/                                            # 📝 DTOs (Data Transfer Objects)
│   ├── common/                                     #    └─ DTOs comunes
│   │   ├── ApiResponse.java                        #       └─ Record (respuesta genérica)
│   │   ├── PageResponse.java                       #       └─ Record (paginación)
│   │   └── ErrorMessage.java                       #       └─ Record (errores)
│   ├── organization/
│   │   ├── CreateOrganizationRequest.java          #    └─ Record (DTO Request)
│   │   ├── UpdateOrganizationRequest.java          #    └─ Record (DTO Request)
│   │   └── OrganizationResponse.java               #    └─ Record (DTO Response)
│   ├── zone/
│   │   ├── CreateZoneRequest.java                  #    └─ Record (DTO Request)
│   │   ├── UpdateZoneRequest.java                  #    └─ Record (DTO Request)
│   │   └── ZoneResponse.java                       #    └─ Record (DTO Response)
│   ├── street/
│   │   ├── CreateStreetRequest.java                #    └─ Record (DTO Request)
│   │   ├── UpdateStreetRequest.java                #    └─ Record (DTO Request)
│   │   └── StreetResponse.java                     #    └─ Record (DTO Response)
│   ├── fare/
│   │   ├── CreateFareRequest.java                  #    └─ Record (DTO Request)
│   │   ├── UpdateFareRequest.java                  #    └─ Record (DTO Request)
│   │   └── FareResponse.java                       #    └─ Record (DTO Response)
│   └── parameter/
│       ├── CreateParameterRequest.java             #    └─ Record (DTO Request)
│       ├── UpdateParameterRequest.java             #    └─ Record (DTO Request)
│       └── ParameterResponse.java                  #    └─ Record (DTO Response)
│
├── mappers/                                        # 🗺️ Mappers (Conversión)
│   ├── OrganizationMapper.java                     #    └─ @Component (DTO<->Model<->Doc)
│   ├── ZoneMapper.java                             #    └─ @Component
│   ├── StreetMapper.java                           #    └─ @Component
│   ├── FareMapper.java                             #    └─ @Component
│   └── ParameterMapper.java                        #    └─ @Component
│
└── events/                                         # 📨 Eventos de dominio (RabbitMQ)
    ├── organization/
    │   ├── OrganizationCreatedEvent.java           #    └─ Record (Event Payload)
    │   ├── OrganizationUpdatedEvent.java           #    └─ Record
    │   ├── OrganizationDeletedEvent.java           #    └─ Record
    │   └── OrganizationRestoredEvent.java          #    └─ Record
    ├── zone/
    │   ├── ZoneCreatedEvent.java                   #    └─ Record
    │   ├── ZoneUpdatedEvent.java                   #    └─ Record
    │   ├── ZoneDeletedEvent.java                   #    └─ Record
    │   └── ZoneRestoredEvent.java                  #    └─ Record
    ├── street/
    │   ├── StreetCreatedEvent.java                 #    └─ Record
    │   ├── StreetUpdatedEvent.java                 #    └─ Record
    │   ├── StreetDeletedEvent.java                 #    └─ Record
    │   └── StreetRestoredEvent.java                #    └─ Record
    ├── fare/
    │   ├── FareCreatedEvent.java                   #    └─ Record
    │   ├── FareUpdatedEvent.java                   #    └─ Record
    │   ├── FareDeletedEvent.java                   #    └─ Record
    │   └── FareRestoredEvent.java                  #    └─ Record
    └── parameter/
        ├── ParameterCreatedEvent.java              #    └─ Record
        ├── ParameterUpdatedEvent.java              #    └─ Record
        ├── ParameterDeletedEvent.java              #    └─ Record
        └── ParameterRestoredEvent.java             #    └─ Record
```

---

## 1️⃣ USE CASES - Implementaciones

### 📁 organization/

#### 📄 CreateOrganizationUseCaseImpl.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.usecases.organization;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsorganizations.domain.exceptions.DuplicateOrganizationException;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Organization;
import pe.edu.vallegrande.vgmsorganizations.domain.models.valueobjects.RecordStatus;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.in.ICreateOrganizationUseCase;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IOrganizationEventPublisher;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IOrganizationRepository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateOrganizationUseCaseImpl implements ICreateOrganizationUseCase {

    private final IOrganizationRepository organizationRepository;
    private final IOrganizationEventPublisher eventPublisher;

    @Override
    public Mono<Organization> execute(Organization organization, String createdBy) {
        log.info("Creating organization: {}", organization.getOrganizationName());

        return organizationRepository.existsByOrganizationName(organization.getOrganizationName())
            .flatMap(exists -> {
                if (exists) {
                    return Mono.error(new DuplicateOrganizationException(organization.getOrganizationName()));
                }

                organization.validateContact();

                Organization newOrg = organization.toBuilder()
                    .id(UUID.randomUUID().toString())
                    .recordStatus(RecordStatus.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .createdBy(createdBy)
                    .updatedAt(LocalDateTime.now())
                    .updatedBy(createdBy)
                    .build();

                return organizationRepository.save(newOrg);
            })
            .flatMap(saved -> publishCreateEvent(saved, createdBy))
            .doOnSuccess(o -> log.info("Organization created: {}", o.getId()))
            .doOnError(e -> log.error("Error creating organization: {}", e.getMessage()));
    }

    private Mono<Organization> publishCreateEvent(Organization org, String createdBy) {
        return eventPublisher.publishOrganizationCreated(org, createdBy)
            .thenReturn(org)
            .onErrorResume(e -> {
                log.warn("Failed to publish create event: {}", e.getMessage());
                return Mono.just(org);
            });
    }
}
```

---

#### 📄 GetOrganizationUseCaseImpl.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.usecases.organization;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsorganizations.domain.exceptions.OrganizationNotFoundException;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Organization;
import pe.edu.vallegrande.vgmsorganizations.domain.models.valueobjects.RecordStatus;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.in.IGetOrganizationUseCase;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IOrganizationRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetOrganizationUseCaseImpl implements IGetOrganizationUseCase {

    private final IOrganizationRepository organizationRepository;

    @Override
    public Mono<Organization> findById(String id) {
        log.info("Finding organization by ID: {}", id);
        return organizationRepository.findById(id)
            .switchIfEmpty(Mono.error(new OrganizationNotFoundException(id)));
    }

    @Override
    public Flux<Organization> findAllActive() {
        log.info("Finding all active organizations");
        return organizationRepository.findByRecordStatus(RecordStatus.ACTIVE);
    }

    @Override
    public Flux<Organization> findAll() {
        log.info("Finding all organizations");
        return organizationRepository.findAll();
    }
}
```

---

#### 📄 UpdateOrganizationUseCaseImpl.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.usecases.organization;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsorganizations.domain.exceptions.OrganizationNotFoundException;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Organization;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.in.IUpdateOrganizationUseCase;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IOrganizationEventPublisher;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IOrganizationRepository;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateOrganizationUseCaseImpl implements IUpdateOrganizationUseCase {

    private final IOrganizationRepository organizationRepository;
    private final IOrganizationEventPublisher eventPublisher;

    @Override
    public Mono<Organization> execute(String id, Organization changes, String updatedBy) {
        log.info("Updating organization: {}", id);

        return organizationRepository.findById(id)
            .switchIfEmpty(Mono.error(new OrganizationNotFoundException(id)))
            .flatMap(existing -> {
                Map<String, Object> changedFields = detectChanges(existing, changes);

                if (changedFields.isEmpty()) {
                    log.info("No changes detected for organization: {}", id);
                    return Mono.just(existing);
                }

                Organization updated = existing.updateWith(changes, updatedBy);
                return organizationRepository.update(updated)
                    .flatMap(saved -> publishUpdateEvent(saved, changedFields, updatedBy));
            })
            .doOnSuccess(o -> log.info("Organization updated: {}", o.getId()))
            .doOnError(e -> log.error("Error updating organization: {}", e.getMessage()));
    }

    private Map<String, Object> detectChanges(Organization existing, Organization changes) {
        Map<String, Object> changedFields = new HashMap<>();

        if (changes.getOrganizationName() != null && !changes.getOrganizationName().equals(existing.getOrganizationName())) {
            changedFields.put("organizationName", changes.getOrganizationName());
        }
        if (changes.getDistrict() != null && !changes.getDistrict().equals(existing.getDistrict())) {
            changedFields.put("district", changes.getDistrict());
        }
        if (changes.getProvince() != null && !changes.getProvince().equals(existing.getProvince())) {
            changedFields.put("province", changes.getProvince());
        }
        if (changes.getDepartment() != null && !changes.getDepartment().equals(existing.getDepartment())) {
            changedFields.put("department", changes.getDepartment());
        }
        if (changes.getAddress() != null && !changes.getAddress().equals(existing.getAddress())) {
            changedFields.put("address", changes.getAddress());
        }
        if (changes.getPhone() != null && !changes.getPhone().equals(existing.getPhone())) {
            changedFields.put("phone", changes.getPhone());
        }
        if (changes.getEmail() != null && !changes.getEmail().equals(existing.getEmail())) {
            changedFields.put("email", changes.getEmail());
        }

        return changedFields;
    }

    private Mono<Organization> publishUpdateEvent(Organization org, Map<String, Object> changedFields, String updatedBy) {
        return eventPublisher.publishOrganizationUpdated(org, changedFields, updatedBy)
            .thenReturn(org)
            .onErrorResume(e -> {
                log.warn("Failed to publish update event: {}", e.getMessage());
                return Mono.just(org);
            });
    }
}
```

---

#### 📄 DeleteOrganizationUseCaseImpl.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.usecases.organization;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsorganizations.domain.exceptions.BusinessRuleException;
import pe.edu.vallegrande.vgmsorganizations.domain.exceptions.OrganizationNotFoundException;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Organization;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.in.IDeleteOrganizationUseCase;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IOrganizationEventPublisher;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IOrganizationRepository;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteOrganizationUseCaseImpl implements IDeleteOrganizationUseCase {

    private final IOrganizationRepository organizationRepository;
    private final IOrganizationEventPublisher eventPublisher;

    @Override
    public Mono<Organization> execute(String id, String deletedBy, String reason) {
        log.info("Deleting organization: {} - Reason: {}", id, reason);

        return organizationRepository.findById(id)
            .switchIfEmpty(Mono.error(new OrganizationNotFoundException(id)))
            .flatMap(org -> {
                if (org.isInactive()) {
                    return Mono.error(new BusinessRuleException("Organization is already inactive"));
                }

                Organization deleted = org.markAsDeleted(deletedBy);
                return organizationRepository.update(deleted)
                    .flatMap(saved -> publishDeleteEvent(saved.getId(), reason, deletedBy));
            })
            .doOnSuccess(o -> log.info("Organization deleted: {}", o.getId()))
            .doOnError(e -> log.error("Error deleting organization: {}", e.getMessage()));
    }

    private Mono<Organization> publishDeleteEvent(String orgId, String reason, String deletedBy) {
        return eventPublisher.publishOrganizationDeleted(orgId, reason, deletedBy)
            .then(organizationRepository.findById(orgId))
            .onErrorResume(e -> {
                log.warn("Failed to publish delete event: {}", e.getMessage());
                return organizationRepository.findById(orgId);
            });
    }
}
```

---

#### 📄 RestoreOrganizationUseCaseImpl.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.usecases.organization;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsorganizations.domain.exceptions.BusinessRuleException;
import pe.edu.vallegrande.vgmsorganizations.domain.exceptions.OrganizationNotFoundException;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Organization;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.in.IRestoreOrganizationUseCase;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IOrganizationEventPublisher;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IOrganizationRepository;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestoreOrganizationUseCaseImpl implements IRestoreOrganizationUseCase {

    private final IOrganizationRepository organizationRepository;
    private final IOrganizationEventPublisher eventPublisher;

    @Override
    public Mono<Organization> execute(String id, String restoredBy) {
        log.info("Restoring organization: {}", id);

        return organizationRepository.findById(id)
            .switchIfEmpty(Mono.error(new OrganizationNotFoundException(id)))
            .flatMap(org -> {
                if (org.isActive()) {
                    return Mono.error(new BusinessRuleException("Organization is already active"));
                }

                Organization restored = org.restore(restoredBy);
                return organizationRepository.update(restored)
                    .flatMap(saved -> publishRestoreEvent(saved, restoredBy));
            })
            .doOnSuccess(o -> log.info("Organization restored: {}", o.getId()))
            .doOnError(e -> log.error("Error restoring organization: {}", e.getMessage()));
    }

    private Mono<Organization> publishRestoreEvent(Organization org, String restoredBy) {
        return eventPublisher.publishOrganizationRestored(org.getId(), restoredBy)
            .thenReturn(org)
            .onErrorResume(e -> {
                log.warn("Failed to publish restore event: {}", e.getMessage());
                return Mono.just(org);
            });
    }
}
```

---

### 📁 zone/

#### 📄 CreateZoneUseCaseImpl.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.usecases.zone;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsorganizations.domain.exceptions.BusinessRuleException;
import pe.edu.vallegrande.vgmsorganizations.domain.exceptions.OrganizationNotFoundException;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Zone;
import pe.edu.vallegrande.vgmsorganizations.domain.models.valueobjects.RecordStatus;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.in.ICreateZoneUseCase;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IOrganizationRepository;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IZoneEventPublisher;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IZoneRepository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateZoneUseCaseImpl implements ICreateZoneUseCase {

    private final IZoneRepository zoneRepository;
    private final IOrganizationRepository organizationRepository;
    private final IZoneEventPublisher eventPublisher;

    @Override
    public Mono<Zone> execute(Zone zone, String createdBy) {
        log.info("Creating zone: {} for organization: {}", zone.getZoneName(), zone.getOrganizationId());

        return organizationRepository.findById(zone.getOrganizationId())
            .switchIfEmpty(Mono.error(new OrganizationNotFoundException(zone.getOrganizationId())))
            .flatMap(org -> {
                if (org.isInactive()) {
                    return Mono.error(new BusinessRuleException("Cannot create a zone in an inactive organization"));
                }
                return zoneRepository.existsByZoneNameAndOrganizationId(zone.getZoneName(), zone.getOrganizationId());
            })
            .flatMap(exists -> {
                if (exists) {
                    return Mono.error(new BusinessRuleException(
                        String.format("Zone '%s' already exists in this organization", zone.getZoneName())
                    ));
                }

                Zone newZone = zone.toBuilder()
                    .id(UUID.randomUUID().toString())
                    .recordStatus(RecordStatus.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .createdBy(createdBy)
                    .updatedAt(LocalDateTime.now())
                    .updatedBy(createdBy)
                    .build();

                return zoneRepository.save(newZone);
            })
            .flatMap(saved -> eventPublisher.publishZoneCreated(saved, createdBy)
                .thenReturn(saved)
                .onErrorResume(e -> {
                    log.warn("Failed to publish zone create event: {}", e.getMessage());
                    return Mono.just(saved);
                })
            )
            .doOnSuccess(z -> log.info("Zone created: {}", z.getId()))
            .doOnError(e -> log.error("Error creating zone: {}", e.getMessage()));
    }
}
```

---

#### 📄 GetZoneUseCaseImpl.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.usecases.zone;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsorganizations.domain.exceptions.ZoneNotFoundException;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Zone;
import pe.edu.vallegrande.vgmsorganizations.domain.models.valueobjects.RecordStatus;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.in.IGetZoneUseCase;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IZoneRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetZoneUseCaseImpl implements IGetZoneUseCase {

    private final IZoneRepository zoneRepository;

    @Override
    public Mono<Zone> findById(String id) {
        log.info("Finding zone by ID: {}", id);
        return zoneRepository.findById(id)
            .switchIfEmpty(Mono.error(new ZoneNotFoundException(id)));
    }

    @Override
    public Flux<Zone> findAllActive() {
        log.info("Finding all active zones");
        return zoneRepository.findByRecordStatus(RecordStatus.ACTIVE);
    }

    @Override
    public Flux<Zone> findAll() {
        log.info("Finding all zones");
        return zoneRepository.findAll();
    }

    @Override
    public Flux<Zone> findByOrganizationId(String organizationId) {
        log.info("Finding zones by organization: {}", organizationId);
        return zoneRepository.findByOrganizationId(organizationId);
    }

    @Override
    public Flux<Zone> findActiveByOrganizationId(String organizationId) {
        log.info("Finding active zones by organization: {}", organizationId);
        return zoneRepository.findByOrganizationIdAndRecordStatus(organizationId, RecordStatus.ACTIVE);
    }
}
```

---

#### 📄 UpdateZoneUseCaseImpl.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.usecases.zone;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsorganizations.domain.exceptions.ZoneNotFoundException;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Zone;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.in.IUpdateZoneUseCase;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IZoneEventPublisher;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IZoneRepository;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateZoneUseCaseImpl implements IUpdateZoneUseCase {

    private final IZoneRepository zoneRepository;
    private final IZoneEventPublisher eventPublisher;

    @Override
    public Mono<Zone> execute(String id, Zone changes, String updatedBy) {
        log.info("Updating zone: {}", id);

        return zoneRepository.findById(id)
            .switchIfEmpty(Mono.error(new ZoneNotFoundException(id)))
            .flatMap(existing -> {
                Map<String, Object> changedFields = detectChanges(existing, changes);
                if (changedFields.isEmpty()) {
                    return Mono.just(existing);
                }

                Zone updated = existing.updateWith(changes, updatedBy);
                return zoneRepository.update(updated)
                    .flatMap(saved -> eventPublisher.publishZoneUpdated(saved, changedFields, updatedBy)
                        .thenReturn(saved)
                        .onErrorResume(e -> {
                            log.warn("Failed to publish zone update event: {}", e.getMessage());
                            return Mono.just(saved);
                        })
                    );
            });
    }

    private Map<String, Object> detectChanges(Zone existing, Zone changes) {
        Map<String, Object> changedFields = new HashMap<>();
        if (changes.getZoneName() != null && !changes.getZoneName().equals(existing.getZoneName())) {
            changedFields.put("zoneName", changes.getZoneName());
        }
        if (changes.getDescription() != null && !changes.getDescription().equals(existing.getDescription())) {
            changedFields.put("description", changes.getDescription());
        }
        return changedFields;
    }
}
```

---

#### 📄 DeleteZoneUseCaseImpl.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.usecases.zone;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsorganizations.domain.exceptions.BusinessRuleException;
import pe.edu.vallegrande.vgmsorganizations.domain.exceptions.ZoneNotFoundException;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Zone;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.in.IDeleteZoneUseCase;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IZoneEventPublisher;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IZoneRepository;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteZoneUseCaseImpl implements IDeleteZoneUseCase {

    private final IZoneRepository zoneRepository;
    private final IZoneEventPublisher eventPublisher;

    @Override
    public Mono<Zone> execute(String id, String deletedBy, String reason) {
        log.info("Deleting zone: {} - Reason: {}", id, reason);

        return zoneRepository.findById(id)
            .switchIfEmpty(Mono.error(new ZoneNotFoundException(id)))
            .flatMap(zone -> {
                if (zone.isInactive()) {
                    return Mono.error(new BusinessRuleException("Zone is already inactive"));
                }

                Zone deleted = zone.markAsDeleted(deletedBy);
                return zoneRepository.update(deleted)
                    .flatMap(saved -> eventPublisher.publishZoneDeleted(saved.getId(), saved.getOrganizationId(), reason, deletedBy)
                        .thenReturn(saved)
                        .onErrorResume(e -> {
                            log.warn("Failed to publish zone delete event: {}", e.getMessage());
                            return Mono.just(saved);
                        })
                    );
            });
    }
}
```

---

#### 📄 RestoreZoneUseCaseImpl.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.usecases.zone;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsorganizations.domain.exceptions.BusinessRuleException;
import pe.edu.vallegrande.vgmsorganizations.domain.exceptions.ZoneNotFoundException;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Zone;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.in.IRestoreZoneUseCase;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IZoneEventPublisher;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IZoneRepository;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestoreZoneUseCaseImpl implements IRestoreZoneUseCase {

    private final IZoneRepository zoneRepository;
    private final IZoneEventPublisher eventPublisher;

    @Override
    public Mono<Zone> execute(String id, String restoredBy) {
        log.info("Restoring zone: {}", id);

        return zoneRepository.findById(id)
            .switchIfEmpty(Mono.error(new ZoneNotFoundException(id)))
            .flatMap(zone -> {
                if (zone.isActive()) {
                    return Mono.error(new BusinessRuleException("Zone is already active"));
                }

                Zone restored = zone.restore(restoredBy);
                return zoneRepository.update(restored)
                    .flatMap(saved -> eventPublisher.publishZoneRestored(saved.getId(), saved.getOrganizationId(), restoredBy)
                        .thenReturn(saved)
                        .onErrorResume(e -> {
                            log.warn("Failed to publish zone restore event: {}", e.getMessage());
                            return Mono.just(saved);
                        })
                    );
            });
    }
}
```

---

### 📁 street/

#### 📄 CreateStreetUseCaseImpl.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.usecases.street;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsorganizations.domain.exceptions.BusinessRuleException;
import pe.edu.vallegrande.vgmsorganizations.domain.exceptions.ZoneNotFoundException;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Street;
import pe.edu.vallegrande.vgmsorganizations.domain.models.valueobjects.RecordStatus;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.in.ICreateStreetUseCase;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IStreetEventPublisher;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IStreetRepository;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IZoneRepository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateStreetUseCaseImpl implements ICreateStreetUseCase {

    private final IStreetRepository streetRepository;
    private final IZoneRepository zoneRepository;
    private final IStreetEventPublisher eventPublisher;

    @Override
    public Mono<Street> execute(Street street, String createdBy) {
        log.info("Creating street: {} in zone: {}", street.getStreetName(), street.getZoneId());

        return zoneRepository.findById(street.getZoneId())
            .switchIfEmpty(Mono.error(new ZoneNotFoundException(street.getZoneId())))
            .flatMap(zone -> {
                if (zone.isInactive()) {
                    return Mono.error(new BusinessRuleException("Cannot create a street in an inactive zone"));
                }
                return streetRepository.existsByStreetNameAndZoneId(street.getStreetName(), street.getZoneId());
            })
            .flatMap(exists -> {
                if (exists) {
                    return Mono.error(new BusinessRuleException(
                        String.format("Street '%s' already exists in this zone", street.getStreetName())
                    ));
                }

                Street newStreet = street.toBuilder()
                    .id(UUID.randomUUID().toString())
                    .recordStatus(RecordStatus.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .createdBy(createdBy)
                    .updatedAt(LocalDateTime.now())
                    .updatedBy(createdBy)
                    .build();

                return streetRepository.save(newStreet);
            })
            .flatMap(saved -> eventPublisher.publishStreetCreated(saved, createdBy)
                .thenReturn(saved)
                .onErrorResume(e -> {
                    log.warn("Failed to publish street create event: {}", e.getMessage());
                    return Mono.just(saved);
                })
            )
            .doOnSuccess(s -> log.info("Street created: {}", s.getId()))
            .doOnError(e -> log.error("Error creating street: {}", e.getMessage()));
    }
}
```

---

#### 📄 GetStreetUseCaseImpl.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.usecases.street;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsorganizations.domain.exceptions.StreetNotFoundException;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Street;
import pe.edu.vallegrande.vgmsorganizations.domain.models.valueobjects.RecordStatus;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.in.IGetStreetUseCase;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IStreetRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetStreetUseCaseImpl implements IGetStreetUseCase {

    private final IStreetRepository streetRepository;

    @Override
    public Mono<Street> findById(String id) {
        return streetRepository.findById(id)
            .switchIfEmpty(Mono.error(new StreetNotFoundException(id)));
    }

    @Override
    public Flux<Street> findAllActive() {
        return streetRepository.findByRecordStatus(RecordStatus.ACTIVE);
    }

    @Override
    public Flux<Street> findAll() {
        return streetRepository.findAll();
    }

    @Override
    public Flux<Street> findByZoneId(String zoneId) {
        return streetRepository.findByZoneId(zoneId);
    }

    @Override
    public Flux<Street> findActiveByZoneId(String zoneId) {
        return streetRepository.findByZoneIdAndRecordStatus(zoneId, RecordStatus.ACTIVE);
    }
}
```

---

#### 📄 UpdateStreetUseCaseImpl.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.usecases.street;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsorganizations.domain.exceptions.StreetNotFoundException;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Street;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.in.IUpdateStreetUseCase;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IStreetEventPublisher;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IStreetRepository;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateStreetUseCaseImpl implements IUpdateStreetUseCase {

    private final IStreetRepository streetRepository;
    private final IStreetEventPublisher eventPublisher;

    @Override
    public Mono<Street> execute(String id, Street changes, String updatedBy) {
        return streetRepository.findById(id)
            .switchIfEmpty(Mono.error(new StreetNotFoundException(id)))
            .flatMap(existing -> {
                Map<String, Object> changedFields = detectChanges(existing, changes);
                if (changedFields.isEmpty()) return Mono.just(existing);

                Street updated = existing.updateWith(changes, updatedBy);
                return streetRepository.update(updated)
                    .flatMap(saved -> eventPublisher.publishStreetUpdated(saved, changedFields, updatedBy)
                        .thenReturn(saved)
                        .onErrorResume(e -> Mono.just(saved))
                    );
            });
    }

    private Map<String, Object> detectChanges(Street existing, Street changes) {
        Map<String, Object> changedFields = new HashMap<>();
        if (changes.getStreetType() != null && !changes.getStreetType().equals(existing.getStreetType())) {
            changedFields.put("streetType", changes.getStreetType().name());
        }
        if (changes.getStreetName() != null && !changes.getStreetName().equals(existing.getStreetName())) {
            changedFields.put("streetName", changes.getStreetName());
        }
        return changedFields;
    }
}
```

---

#### 📄 DeleteStreetUseCaseImpl.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.usecases.street;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsorganizations.domain.exceptions.BusinessRuleException;
import pe.edu.vallegrande.vgmsorganizations.domain.exceptions.StreetNotFoundException;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Street;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.in.IDeleteStreetUseCase;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IStreetEventPublisher;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IStreetRepository;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteStreetUseCaseImpl implements IDeleteStreetUseCase {

    private final IStreetRepository streetRepository;
    private final IStreetEventPublisher eventPublisher;

    @Override
    public Mono<Street> execute(String id, String deletedBy, String reason) {
        return streetRepository.findById(id)
            .switchIfEmpty(Mono.error(new StreetNotFoundException(id)))
            .flatMap(street -> {
                if (street.isInactive()) {
                    return Mono.error(new BusinessRuleException("La calle ya se encuentra inactiva"));
                }
                Street deleted = street.markAsDeleted(deletedBy);
                return streetRepository.update(deleted)
                    .flatMap(saved -> eventPublisher.publishStreetDeleted(saved.getId(), saved.getZoneId(), reason, deletedBy)
                        .thenReturn(saved)
                        .onErrorResume(e -> Mono.just(saved))
                    );
            });
    }
}
```

---

#### 📄 RestoreStreetUseCaseImpl.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.usecases.street;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsorganizations.domain.exceptions.BusinessRuleException;
import pe.edu.vallegrande.vgmsorganizations.domain.exceptions.StreetNotFoundException;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Street;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.in.IRestoreStreetUseCase;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IStreetEventPublisher;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IStreetRepository;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestoreStreetUseCaseImpl implements IRestoreStreetUseCase {

    private final IStreetRepository streetRepository;
    private final IStreetEventPublisher eventPublisher;

    @Override
    public Mono<Street> execute(String id, String restoredBy) {
        return streetRepository.findById(id)
            .switchIfEmpty(Mono.error(new StreetNotFoundException(id)))
            .flatMap(street -> {
                if (street.isActive()) {
                    return Mono.error(new BusinessRuleException("La calle ya se encuentra activa"));
                }
                Street restored = street.restore(restoredBy);
                return streetRepository.update(restored)
                    .flatMap(saved -> eventPublisher.publishStreetRestored(saved.getId(), saved.getZoneId(), restoredBy)
                        .thenReturn(saved)
                        .onErrorResume(e -> Mono.just(saved))
                    );
            });
    }
}
```

---

### 📁 fare/

#### 📄 CreateFareUseCaseImpl.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.usecases.fare;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsorganizations.domain.exceptions.OrganizationNotFoundException;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Fare;
import pe.edu.vallegrande.vgmsorganizations.domain.models.valueobjects.RecordStatus;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.in.ICreateFareUseCase;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IFareEventPublisher;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IFareRepository;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IOrganizationRepository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateFareUseCaseImpl implements ICreateFareUseCase {

    private final IFareRepository fareRepository;
    private final IOrganizationRepository organizationRepository;
    private final IFareEventPublisher eventPublisher;

    @Override
    public Mono<Fare> execute(Fare fare, String createdBy) {
        log.info("Creating fare: {} for organization: {}", fare.getFareType(), fare.getOrganizationId());

        return organizationRepository.findById(fare.getOrganizationId())
            .switchIfEmpty(Mono.error(new OrganizationNotFoundException(fare.getOrganizationId())))
            .flatMap(org -> {
                fare.validateAmount();

                // Deactivate old fares of the same type
                return fareRepository.findActiveByOrganizationAndType(fare.getOrganizationId(), fare.getFareType())
                    .flatMap(oldFare -> fareRepository.update(oldFare.markAsDeleted(createdBy)))
                    .then(Mono.defer(() -> {
                        Fare newFare = fare.toBuilder()
                            .id(UUID.randomUUID().toString())
                            .recordStatus(RecordStatus.ACTIVE)
                            .createdAt(LocalDateTime.now())
                            .createdBy(createdBy)
                            .updatedAt(LocalDateTime.now())
                            .updatedBy(createdBy)
                            .build();

                        return fareRepository.save(newFare);
                    }));
            })
            .flatMap(saved -> eventPublisher.publishFareCreated(saved, createdBy)
                .thenReturn(saved)
                .onErrorResume(e -> Mono.just(saved))
            );
    }
}
```

---

#### 📄 GetFareUseCaseImpl.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.usecases.fare;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsorganizations.domain.exceptions.FareNotFoundException;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Fare;
import pe.edu.vallegrande.vgmsorganizations.domain.models.valueobjects.RecordStatus;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.in.IGetFareUseCase;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IFareRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetFareUseCaseImpl implements IGetFareUseCase {

    private final IFareRepository fareRepository;

    @Override
    public Mono<Fare> findById(String id) {
        return fareRepository.findById(id)
            .switchIfEmpty(Mono.error(new FareNotFoundException(id)));
    }

    @Override
    public Flux<Fare> findAllActive() {
        return fareRepository.findByRecordStatus(RecordStatus.ACTIVE);
    }

    @Override
    public Flux<Fare> findAll() {
        return fareRepository.findAll();
    }

    @Override
    public Flux<Fare> findByOrganizationId(String organizationId) {
        return fareRepository.findByOrganizationId(organizationId);
    }

    @Override
    public Flux<Fare> findActiveByOrganizationId(String organizationId) {
        return fareRepository.findByOrganizationIdAndRecordStatus(organizationId, RecordStatus.ACTIVE);
    }
}
```

---

#### 📄 UpdateFareUseCaseImpl.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.usecases.fare;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsorganizations.domain.exceptions.FareNotFoundException;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Fare;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.in.IUpdateFareUseCase;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IFareEventPublisher;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IFareRepository;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateFareUseCaseImpl implements IUpdateFareUseCase {

    private final IFareRepository fareRepository;
    private final IFareEventPublisher eventPublisher;

    @Override
    public Mono<Fare> execute(String id, Fare changes, String updatedBy) {
        return fareRepository.findById(id)
            .switchIfEmpty(Mono.error(new FareNotFoundException(id)))
            .flatMap(existing -> {
                Map<String, Object> changedFields = detectChanges(existing, changes);
                if (changedFields.isEmpty()) return Mono.just(existing);

                Fare updated = existing.updateWith(changes, updatedBy);
                return fareRepository.update(updated)
                    .flatMap(saved -> eventPublisher.publishFareUpdated(saved, changedFields, updatedBy)
                        .thenReturn(saved)
                        .onErrorResume(e -> Mono.just(saved))
                    );
            });
    }

    private Map<String, Object> detectChanges(Fare existing, Fare changes) {
        Map<String, Object> changedFields = new HashMap<>();
        if (changes.getFareType() != null && !changes.getFareType().equals(existing.getFareType())) {
            changedFields.put("fareType", changes.getFareType().name());
        }
        if (changes.getAmount() != null && !changes.getAmount().equals(existing.getAmount())) {
            changedFields.put("amount", changes.getAmount());
        }
        if (changes.getDescription() != null && !changes.getDescription().equals(existing.getDescription())) {
            changedFields.put("description", changes.getDescription());
        }
        return changedFields;
    }
}
```

---

#### 📄 DeleteFareUseCaseImpl.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.usecases.fare;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsorganizations.domain.exceptions.BusinessRuleException;
import pe.edu.vallegrande.vgmsorganizations.domain.exceptions.FareNotFoundException;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Fare;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.in.IDeleteFareUseCase;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IFareEventPublisher;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IFareRepository;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteFareUseCaseImpl implements IDeleteFareUseCase {

    private final IFareRepository fareRepository;
    private final IFareEventPublisher eventPublisher;

    @Override
    public Mono<Fare> execute(String id, String deletedBy, String reason) {
        return fareRepository.findById(id)
            .switchIfEmpty(Mono.error(new FareNotFoundException(id)))
            .flatMap(fare -> {
                if (fare.isInactive()) {
                    return Mono.error(new BusinessRuleException("La tarifa ya se encuentra inactiva"));
                }
                Fare deleted = fare.markAsDeleted(deletedBy);
                return fareRepository.update(deleted)
                    .flatMap(saved -> eventPublisher.publishFareDeleted(saved.getId(), saved.getOrganizationId(), reason, deletedBy)
                        .thenReturn(saved)
                        .onErrorResume(e -> Mono.just(saved))
                    );
            });
    }
}
```

---

#### 📄 RestoreFareUseCaseImpl.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.usecases.fare;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsorganizations.domain.exceptions.BusinessRuleException;
import pe.edu.vallegrande.vgmsorganizations.domain.exceptions.FareNotFoundException;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Fare;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.in.IRestoreFareUseCase;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IFareEventPublisher;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IFareRepository;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestoreFareUseCaseImpl implements IRestoreFareUseCase {

    private final IFareRepository fareRepository;
    private final IFareEventPublisher eventPublisher;

    @Override
    public Mono<Fare> execute(String id, String restoredBy) {
        return fareRepository.findById(id)
            .switchIfEmpty(Mono.error(new FareNotFoundException(id)))
            .flatMap(fare -> {
                if (fare.isActive()) {
                    return Mono.error(new BusinessRuleException("La tarifa ya se encuentra activa"));
                }
                Fare restored = fare.restore(restoredBy);
                return fareRepository.update(restored)
                    .flatMap(saved -> eventPublisher.publishFareRestored(saved.getId(), saved.getOrganizationId(), restoredBy)
                        .thenReturn(saved)
                        .onErrorResume(e -> Mono.just(saved))
                    );
            });
    }
}
```

---

### 📁 parameter/

#### 📄 CreateParameterUseCaseImpl.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.usecases.parameter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsorganizations.domain.exceptions.BusinessRuleException;
import pe.edu.vallegrande.vgmsorganizations.domain.exceptions.OrganizationNotFoundException;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Parameter;
import pe.edu.vallegrande.vgmsorganizations.domain.models.valueobjects.RecordStatus;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.in.ICreateParameterUseCase;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IOrganizationRepository;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IParameterEventPublisher;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IParameterRepository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateParameterUseCaseImpl implements ICreateParameterUseCase {

    private final IParameterRepository parameterRepository;
    private final IOrganizationRepository organizationRepository;
    private final IParameterEventPublisher eventPublisher;

    @Override
    public Mono<Parameter> execute(Parameter parameter, String createdBy) {
        log.info("Creating parameter: {} for organization: {}", parameter.getParameterType(), parameter.getOrganizationId());

        return organizationRepository.findById(parameter.getOrganizationId())
            .switchIfEmpty(Mono.error(new OrganizationNotFoundException(parameter.getOrganizationId())))
            .flatMap(org -> parameterRepository.existsByParameterTypeAndOrganizationId(
                parameter.getParameterType().name(), parameter.getOrganizationId()
            ))
            .flatMap(exists -> {
                if (exists) {
                    return Mono.error(new BusinessRuleException(
                        String.format("Parameter '%s' already exists for this organization", parameter.getParameterType().getDisplayName())
                    ));
                }

                Parameter newParam = parameter.toBuilder()
                    .id(UUID.randomUUID().toString())
                    .recordStatus(RecordStatus.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .createdBy(createdBy)
                    .updatedAt(LocalDateTime.now())
                    .updatedBy(createdBy)
                    .build();

                return parameterRepository.save(newParam);
            })
            .flatMap(saved -> eventPublisher.publishParameterCreated(saved, createdBy)
                .thenReturn(saved)
                .onErrorResume(e -> Mono.just(saved))
            );
    }
}
```

---

#### 📄 GetParameterUseCaseImpl.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.usecases.parameter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsorganizations.domain.exceptions.ParameterNotFoundException;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Parameter;
import pe.edu.vallegrande.vgmsorganizations.domain.models.valueobjects.RecordStatus;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.in.IGetParameterUseCase;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IParameterRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetParameterUseCaseImpl implements IGetParameterUseCase {

    private final IParameterRepository parameterRepository;

    @Override
    public Mono<Parameter> findById(String id) {
        return parameterRepository.findById(id)
            .switchIfEmpty(Mono.error(new ParameterNotFoundException(id)));
    }

    @Override
    public Flux<Parameter> findAllActive() {
        return parameterRepository.findByRecordStatus(RecordStatus.ACTIVE);
    }

    @Override
    public Flux<Parameter> findAll() {
        return parameterRepository.findAll();
    }

    @Override
    public Flux<Parameter> findByOrganizationId(String organizationId) {
        return parameterRepository.findByOrganizationId(organizationId);
    }

    @Override
    public Flux<Parameter> findActiveByOrganizationId(String organizationId) {
        return parameterRepository.findByOrganizationIdAndRecordStatus(organizationId, RecordStatus.ACTIVE);
    }
}
```

---

#### 📄 UpdateParameterUseCaseImpl.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.usecases.parameter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsorganizations.domain.exceptions.ParameterNotFoundException;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Parameter;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.in.IUpdateParameterUseCase;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IParameterEventPublisher;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IParameterRepository;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateParameterUseCaseImpl implements IUpdateParameterUseCase {

    private final IParameterRepository parameterRepository;
    private final IParameterEventPublisher eventPublisher;

    @Override
    public Mono<Parameter> execute(String id, Parameter changes, String updatedBy) {
        return parameterRepository.findById(id)
            .switchIfEmpty(Mono.error(new ParameterNotFoundException(id)))
            .flatMap(existing -> {
                Map<String, Object> changedFields = detectChanges(existing, changes);
                if (changedFields.isEmpty()) return Mono.just(existing);

                Parameter updated = existing.updateWith(changes, updatedBy);
                return parameterRepository.update(updated)
                    .flatMap(saved -> eventPublisher.publishParameterUpdated(saved, changedFields, updatedBy)
                        .thenReturn(saved)
                        .onErrorResume(e -> Mono.just(saved))
                    );
            });
    }

    private Map<String, Object> detectChanges(Parameter existing, Parameter changes) {
        Map<String, Object> changedFields = new HashMap<>();
        if (changes.getParameterValue() != null && !changes.getParameterValue().equals(existing.getParameterValue())) {
            changedFields.put("parameterValue", changes.getParameterValue());
        }
        if (changes.getDescription() != null && !changes.getDescription().equals(existing.getDescription())) {
            changedFields.put("description", changes.getDescription());
        }
        return changedFields;
    }
}
```

---

#### 📄 DeleteParameterUseCaseImpl.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.usecases.parameter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsorganizations.domain.exceptions.BusinessRuleException;
import pe.edu.vallegrande.vgmsorganizations.domain.exceptions.ParameterNotFoundException;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Parameter;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.in.IDeleteParameterUseCase;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IParameterEventPublisher;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IParameterRepository;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteParameterUseCaseImpl implements IDeleteParameterUseCase {

    private final IParameterRepository parameterRepository;
    private final IParameterEventPublisher eventPublisher;

    @Override
    public Mono<Parameter> execute(String id, String deletedBy, String reason) {
        return parameterRepository.findById(id)
            .switchIfEmpty(Mono.error(new ParameterNotFoundException(id)))
            .flatMap(param -> {
                if (param.isInactive()) {
                    return Mono.error(new BusinessRuleException("Parameter is already inactive"));
                }
                Parameter deleted = param.markAsDeleted(deletedBy);
                return parameterRepository.update(deleted)
                    .flatMap(saved -> eventPublisher.publishParameterDeleted(saved.getId(), saved.getOrganizationId(), reason, deletedBy)
                        .thenReturn(saved)
                        .onErrorResume(e -> Mono.just(saved))
                    );
            });
    }
}
```

---

#### 📄 RestoreParameterUseCaseImpl.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.usecases.parameter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsorganizations.domain.exceptions.BusinessRuleException;
import pe.edu.vallegrande.vgmsorganizations.domain.exceptions.ParameterNotFoundException;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Parameter;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.in.IRestoreParameterUseCase;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IParameterEventPublisher;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IParameterRepository;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestoreParameterUseCaseImpl implements IRestoreParameterUseCase {

    private final IParameterRepository parameterRepository;
    private final IParameterEventPublisher eventPublisher;

    @Override
    public Mono<Parameter> execute(String id, String restoredBy) {
        return parameterRepository.findById(id)
            .switchIfEmpty(Mono.error(new ParameterNotFoundException(id)))
            .flatMap(param -> {
                if (param.isActive()) {
                    return Mono.error(new BusinessRuleException("Parameter is already active"));
                }
                Parameter restored = param.restore(restoredBy);
                return parameterRepository.update(restored)
                    .flatMap(saved -> eventPublisher.publishParameterRestored(saved.getId(), saved.getOrganizationId(), restoredBy)
                        .thenReturn(saved)
                        .onErrorResume(e -> Mono.just(saved))
                    );
            });
    }
}
```

---

## 2️⃣ DTOs - Objetos de Transferencia

### 📁 dto/common/

#### 📄 ApiResponse.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private List<ErrorMessage> errors;
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
            .success(true)
            .message(message)
            .data(data)
            .build();
    }

    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
            .success(true)
            .message(message)
            .build();
    }

    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
            .success(false)
            .message(message)
            .build();
    }

    public static <T> ApiResponse<T> error(String message, ErrorMessage error) {
        return ApiResponse.<T>builder()
            .success(false)
            .message(message)
            .errors(List.of(error))
            .build();
    }

    public static <T> ApiResponse<T> error(String message, List<ErrorMessage> errors) {
        return ApiResponse.<T>builder()
            .success(false)
            .message(message)
            .errors(errors)
            .build();
    }
}
```

---

#### 📄 ErrorMessage.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorMessage {
    private String field;
    private String message;
    private String errorCode;
    private Integer status;

    public static ErrorMessage validation(String field, String message, String errorCode) {
        return ErrorMessage.builder()
            .field(field)
            .message(message)
            .errorCode(errorCode)
            .status(400)
            .build();
    }

    public static ErrorMessage of(String message, String errorCode, int status) {
        return ErrorMessage.builder()
            .message(message)
            .errorCode(errorCode)
            .status(status)
            .build();
    }
}
```

---

#### 📄 PageResponse.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;

    public static <T> PageResponse<T> of(List<T> content, int page, int size, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / size);
        return PageResponse.<T>builder()
            .content(content)
            .page(page)
            .size(size)
            .totalElements(totalElements)
            .totalPages(totalPages)
            .first(page == 0)
            .last(page >= totalPages - 1)
            .build();
    }
}
```

---

### 📁 dto/request/

#### 📄 CreateOrganizationRequest.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrganizationRequest {

    @NotBlank(message = "Organization name is required")
    @Size(min = 3, max = 200, message = "Name must be between 3 and 200 characters")
    private String organizationName;

    @NotBlank(message = "District is required")
    @Size(max = 100)
    private String district;

    @NotBlank(message = "Province is required")
    @Size(max = 100)
    private String province;

    @NotBlank(message = "Department is required")
    @Size(max = 100)
    private String department;

    @Size(max = 250, message = "Address cannot exceed 250 characters")
    private String address;

    @Pattern(regexp = "^[+]?[0-9]{9,15}$", message = "Phone must be valid")
    private String phone;

    @Email(message = "Email must be valid")
    private String email;
}
```

---

#### 📄 UpdateOrganizationRequest.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrganizationRequest {

    @Size(min = 3, max = 200, message = "Name must be between 3 and 200 characters")
    private String organizationName;

    @Size(max = 100)
    private String district;

    @Size(max = 100)
    private String province;

    @Size(max = 100)
    private String department;

    @Size(max = 250)
    private String address;

    @Pattern(regexp = "^[+]?[0-9]{9,15}$", message = "Phone must be valid")
    private String phone;

    @Email(message = "Email must be valid")
    private String email;
}
```

---

#### 📄 CreateZoneRequest.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateZoneRequest {

    @NotBlank(message = "Organization ID is required")
    private String organizationId;

    @NotBlank(message = "Zone name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String zoneName;

    @Size(max = 250)
    private String description;
}
```

---

#### 📄 UpdateZoneRequest.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateZoneRequest {

    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String zoneName;

    @Size(max = 250)
    private String description;
}
```

---

#### 📄 CreateStreetRequest.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateStreetRequest {

    @NotBlank(message = "Organization ID is required")
    private String organizationId;

    @NotBlank(message = "Zone ID is required")
    private String zoneId;

    @NotBlank(message = "Street type is required")
    @Pattern(regexp = "^(JR|AV|CALLE|PASAJE)$", message = "Street type must be JR, AV, CALLE, or PASAJE")
    private String streetType;

    @NotBlank(message = "Street name is required")
    @Size(min = 2, max = 150, message = "Name must be between 2 and 150 characters")
    private String streetName;
}
```

---

#### 📄 UpdateStreetRequest.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStreetRequest {

    @Pattern(regexp = "^(JR|AV|CALLE|PASAJE)$", message = "Street type must be JR, AV, CALLE, or PASAJE")
    private String streetType;

    @Size(min = 2, max = 150, message = "Name must be between 2 and 150 characters")
    private String streetName;
}
```

---

#### 📄 CreateFareRequest.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateFareRequest {

    @NotBlank(message = "Organization ID is required")
    private String organizationId;

    @NotBlank(message = "Fare type is required")
    @Pattern(regexp = "^[A-Z0-9_]{2,50}$", message = "Invalid fare type format")
    private String fareType;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than 0")
    private Double amount;

    @Size(max = 250)
    private String description;

    private LocalDateTime validFrom;
    private LocalDateTime validTo;
}
```

---

#### 📄 UpdateFareRequest.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFareRequest {

    @Pattern(regexp = "^[A-Z0-9_]{2,50}$", message = "Invalid fare type format")
    private String fareType;

    @Positive(message = "Amount must be greater than 0")
    private Double amount;

    @Size(max = 250)
    private String description;

    private LocalDateTime validFrom;
    private LocalDateTime validTo;
}
```

---

#### 📄 CreateParameterRequest.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateParameterRequest {

    @NotBlank(message = "Organization ID is required")
    private String organizationId;

    @NotBlank(message = "Parameter type is required")
    @Pattern(regexp = "^[A-Z0-9_]{2,50}$", message = "Invalid parameter type format")
    private String parameterType;

    @NotBlank(message = "Value is required")
    private String parameterValue;

    @Size(max = 250)
    private String description;
}
```

---

#### 📄 UpdateParameterRequest.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateParameterRequest {

    private String parameterValue;

    @Size(max = 250)
    private String description;
}
```

---

### 📁 dto/response/

#### 📄 OrganizationResponse.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrganizationResponse {
    private String id;
    private String organizationName;
    private String district;
    private String province;
    private String department;
    private String fullLocation;
    private String address;
    private String phone;
    private String email;
    private String recordStatus;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
```

---

#### 📄 ZoneResponse.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ZoneResponse {
    private String id;
    private String organizationId;
    private String zoneName;
    private String description;
    private String recordStatus;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
```

---

#### 📄 StreetResponse.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StreetResponse {
    private String id;
    private String organizationId;
    private String zoneId;
    private String streetType;
    private String streetTypeDisplayName;
    private String streetName;
    private String fullStreetName;
    private String recordStatus;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
```

---

#### 📄 FareResponse.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FareResponse {
    private String id;
    private String organizationId;
    private String fareType;
    private String fareTypeDisplayName;
    private Double amount;
    private String description;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private boolean currentlyValid;
    private String recordStatus;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
```

---

#### 📄 ParameterResponse.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ParameterResponse {
    private String id;
    private String organizationId;
    private String parameterType;
    private String parameterTypeDisplayName;
    private String parameterValue;
    private String description;
    private String recordStatus;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
```

---

## 3️⃣ MAPPERS

#### 📄 OrganizationMapper.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.mappers;

import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vgmsorganizations.application.dto.request.CreateOrganizationRequest;
import pe.edu.vallegrande.vgmsorganizations.application.dto.request.UpdateOrganizationRequest;
import pe.edu.vallegrande.vgmsorganizations.application.dto.response.OrganizationResponse;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Organization;
import pe.edu.vallegrande.vgmsorganizations.domain.models.valueobjects.RecordStatus;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.persistence.documents.OrganizationDocument;

@Component
public class OrganizationMapper {

    public Organization toModel(CreateOrganizationRequest request) {
        return Organization.builder()
            .organizationName(request.getOrganizationName())
            .district(request.getDistrict())
            .province(request.getProvince())
            .department(request.getDepartment())
            .address(request.getAddress())
            .phone(request.getPhone())
            .email(request.getEmail())
            .build();
    }

    public Organization toModel(UpdateOrganizationRequest request) {
        return Organization.builder()
            .organizationName(request.getOrganizationName())
            .district(request.getDistrict())
            .province(request.getProvince())
            .department(request.getDepartment())
            .address(request.getAddress())
            .phone(request.getPhone())
            .email(request.getEmail())
            .build();
    }

    public OrganizationResponse toResponse(Organization org) {
        return OrganizationResponse.builder()
            .id(org.getId())
            .organizationName(org.getOrganizationName())
            .district(org.getDistrict())
            .province(org.getProvince())
            .department(org.getDepartment())
            .fullLocation(org.getFullLocation())
            .address(org.getAddress())
            .phone(org.getPhone())
            .email(org.getEmail())
            .recordStatus(org.getRecordStatus().name())
            .createdAt(org.getCreatedAt())
            .createdBy(org.getCreatedBy())
            .updatedAt(org.getUpdatedAt())
            .updatedBy(org.getUpdatedBy())
            .build();
    }

    public OrganizationDocument toDocument(Organization org) {
        return OrganizationDocument.builder()
            .id(org.getId())
            .organizationName(org.getOrganizationName())
            .district(org.getDistrict())
            .province(org.getProvince())
            .department(org.getDepartment())
            .address(org.getAddress())
            .phone(org.getPhone())
            .email(org.getEmail())
            .recordStatus(org.getRecordStatus().name())
            .createdAt(org.getCreatedAt())
            .createdBy(org.getCreatedBy())
            .updatedAt(org.getUpdatedAt())
            .updatedBy(org.getUpdatedBy())
            .build();
    }

    public Organization toModel(OrganizationDocument doc) {
        return Organization.builder()
            .id(doc.getId())
            .organizationName(doc.getOrganizationName())
            .district(doc.getDistrict())
            .province(doc.getProvince())
            .department(doc.getDepartment())
            .address(doc.getAddress())
            .phone(doc.getPhone())
            .email(doc.getEmail())
            .recordStatus(RecordStatus.valueOf(doc.getRecordStatus()))
            .createdAt(doc.getCreatedAt())
            .createdBy(doc.getCreatedBy())
            .updatedAt(doc.getUpdatedAt())
            .updatedBy(doc.getUpdatedBy())
            .build();
    }
}
```

---

#### 📄 ZoneMapper.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.mappers;

import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vgmsorganizations.application.dto.request.CreateZoneRequest;
import pe.edu.vallegrande.vgmsorganizations.application.dto.request.UpdateZoneRequest;
import pe.edu.vallegrande.vgmsorganizations.application.dto.response.ZoneResponse;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Zone;
import pe.edu.vallegrande.vgmsorganizations.domain.models.valueobjects.RecordStatus;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.persistence.documents.ZoneDocument;

@Component
public class ZoneMapper {

    public Zone toModel(CreateZoneRequest request) {
        return Zone.builder()
            .organizationId(request.getOrganizationId())
            .zoneName(request.getZoneName())
            .description(request.getDescription())
            .build();
    }

    public Zone toModel(UpdateZoneRequest request) {
        return Zone.builder()
            .zoneName(request.getZoneName())
            .description(request.getDescription())
            .build();
    }

    public ZoneResponse toResponse(Zone zone) {
        return ZoneResponse.builder()
            .id(zone.getId())
            .organizationId(zone.getOrganizationId())
            .zoneName(zone.getZoneName())
            .description(zone.getDescription())
            .recordStatus(zone.getRecordStatus().name())
            .createdAt(zone.getCreatedAt())
            .createdBy(zone.getCreatedBy())
            .updatedAt(zone.getUpdatedAt())
            .updatedBy(zone.getUpdatedBy())
            .build();
    }

    public ZoneDocument toDocument(Zone zone) {
        return ZoneDocument.builder()
            .id(zone.getId())
            .organizationId(zone.getOrganizationId())
            .zoneName(zone.getZoneName())
            .description(zone.getDescription())
            .recordStatus(zone.getRecordStatus().name())
            .createdAt(zone.getCreatedAt())
            .createdBy(zone.getCreatedBy())
            .updatedAt(zone.getUpdatedAt())
            .updatedBy(zone.getUpdatedBy())
            .build();
    }

    public Zone toModel(ZoneDocument doc) {
        return Zone.builder()
            .id(doc.getId())
            .organizationId(doc.getOrganizationId())
            .zoneName(doc.getZoneName())
            .description(doc.getDescription())
            .recordStatus(RecordStatus.valueOf(doc.getRecordStatus()))
            .createdAt(doc.getCreatedAt())
            .createdBy(doc.getCreatedBy())
            .updatedAt(doc.getUpdatedAt())
            .updatedBy(doc.getUpdatedBy())
            .build();
    }
}
```

---

#### 📄 StreetMapper.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.mappers;

import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vgmsorganizations.application.dto.request.CreateStreetRequest;
import pe.edu.vallegrande.vgmsorganizations.application.dto.request.UpdateStreetRequest;
import pe.edu.vallegrande.vgmsorganizations.application.dto.response.StreetResponse;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Street;
import pe.edu.vallegrande.vgmsorganizations.domain.models.valueobjects.RecordStatus;
import pe.edu.vallegrande.vgmsorganizations.domain.models.valueobjects.StreetType;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.persistence.documents.StreetDocument;

@Component
public class StreetMapper {

    public Street toModel(CreateStreetRequest request) {
        return Street.builder()
            .organizationId(request.getOrganizationId())
            .zoneId(request.getZoneId())
            .streetType(StreetType.valueOf(request.getStreetType()))
            .streetName(request.getStreetName())
            .build();
    }

    public Street toModel(UpdateStreetRequest request) {
        return Street.builder()
            .streetType(request.getStreetType() != null ? StreetType.valueOf(request.getStreetType()) : null)
            .streetName(request.getStreetName())
            .build();
    }

    public StreetResponse toResponse(Street street) {
        return StreetResponse.builder()
            .id(street.getId())
            .organizationId(street.getOrganizationId())
            .zoneId(street.getZoneId())
            .streetType(street.getStreetType().name())
            .streetTypeDisplayName(street.getStreetType().getDisplayName())
            .streetName(street.getStreetName())
            .fullStreetName(street.getFullStreetName())
            .recordStatus(street.getRecordStatus().name())
            .createdAt(street.getCreatedAt())
            .createdBy(street.getCreatedBy())
            .updatedAt(street.getUpdatedAt())
            .updatedBy(street.getUpdatedBy())
            .build();
    }

    public StreetDocument toDocument(Street street) {
        return StreetDocument.builder()
            .id(street.getId())
            .organizationId(street.getOrganizationId())
            .zoneId(street.getZoneId())
            .streetType(street.getStreetType().name())
            .streetName(street.getStreetName())
            .recordStatus(street.getRecordStatus().name())
            .createdAt(street.getCreatedAt())
            .createdBy(street.getCreatedBy())
            .updatedAt(street.getUpdatedAt())
            .updatedBy(street.getUpdatedBy())
            .build();
    }

    public Street toModel(StreetDocument doc) {
        return Street.builder()
            .id(doc.getId())
            .organizationId(doc.getOrganizationId())
            .zoneId(doc.getZoneId())
            .streetType(StreetType.valueOf(doc.getStreetType()))
            .streetName(doc.getStreetName())
            .recordStatus(RecordStatus.valueOf(doc.getRecordStatus()))
            .createdAt(doc.getCreatedAt())
            .createdBy(doc.getCreatedBy())
            .updatedAt(doc.getUpdatedAt())
            .updatedBy(doc.getUpdatedBy())
            .build();
    }
}
```

---

#### 📄 FareMapper.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.mappers;

import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vgmsorganizations.application.dto.request.CreateFareRequest;
import pe.edu.vallegrande.vgmsorganizations.application.dto.request.UpdateFareRequest;
import pe.edu.vallegrande.vgmsorganizations.application.dto.response.FareResponse;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Fare;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Fare;
import pe.edu.vallegrande.vgmsorganizations.domain.models.valueobjects.RecordStatus;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.persistence.documents.FareDocument;

@Component
public class FareMapper {

    public Fare toModel(CreateFareRequest request) {
        return Fare.builder()
            .organizationId(request.getOrganizationId())
            .fareType(request.getFareType())
            .amount(request.getAmount())
            .description(request.getDescription())
            .validFrom(request.getValidFrom())
            .validTo(request.getValidTo())
            .build();
    }

    public Fare toModel(UpdateFareRequest request) {
        return Fare.builder()
            .fareType(request.getFareType())
            .amount(request.getAmount())
            .description(request.getDescription())
            .validFrom(request.getValidFrom())
            .validTo(request.getValidTo())
            .build();
    }

    public FareResponse toResponse(Fare fare) {
        return FareResponse.builder()
            .id(fare.getId())
            .organizationId(fare.getOrganizationId())
            .fareType(fare.getFareType())
            .fareTypeDisplayName(fare.getFareType())
            .amount(fare.getAmount())
            .description(fare.getDescription())
            .validFrom(fare.getValidFrom())
            .validTo(fare.getValidTo())
            .currentlyValid(fare.isCurrentlyValid())
            .recordStatus(fare.getRecordStatus().name())
            .createdAt(fare.getCreatedAt())
            .createdBy(fare.getCreatedBy())
            .updatedAt(fare.getUpdatedAt())
            .updatedBy(fare.getUpdatedBy())
            .build();
    }

    public FareDocument toDocument(Fare fare) {
        return FareDocument.builder()
            .id(fare.getId())
            .organizationId(fare.getOrganizationId())
            .fareType(fare.getFareType())
            .amount(fare.getAmount())
            .description(fare.getDescription())
            .validFrom(fare.getValidFrom())
            .validTo(fare.getValidTo())
            .recordStatus(fare.getRecordStatus().name())
            .createdAt(fare.getCreatedAt())
            .createdBy(fare.getCreatedBy())
            .updatedAt(fare.getUpdatedAt())
            .updatedBy(fare.getUpdatedBy())
            .build();
    }

    public Fare toModel(FareDocument doc) {
        return Fare.builder()
            .id(doc.getId())
            .organizationId(doc.getOrganizationId())
            .fareType(doc.getFareType())
            .amount(doc.getAmount())
            .description(doc.getDescription())
            .validFrom(doc.getValidFrom())
            .validTo(doc.getValidTo())
            .recordStatus(RecordStatus.valueOf(doc.getRecordStatus()))
            .createdAt(doc.getCreatedAt())
            .createdBy(doc.getCreatedBy())
            .updatedAt(doc.getUpdatedAt())
            .updatedBy(doc.getUpdatedBy())
            .build();
    }
}
```

---

#### 📄 ParameterMapper.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.mappers;

import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vgmsorganizations.application.dto.request.CreateParameterRequest;
import pe.edu.vallegrande.vgmsorganizations.application.dto.request.UpdateParameterRequest;
import pe.edu.vallegrande.vgmsorganizations.application.dto.response.ParameterResponse;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Parameter;
import pe.edu.vallegrande.vgmsorganizations.domain.models.valueobjects.RecordStatus;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.persistence.documents.ParameterDocument;

@Component
public class ParameterMapper {

    public Parameter toModel(CreateParameterRequest request) {
        return Parameter.builder()
            .organizationId(request.getOrganizationId())
            .parameterType(request.getParameterType())
            .parameterValue(request.getParameterValue())
            .description(request.getDescription())
            .build();
    }

    public Parameter toModel(UpdateParameterRequest request) {
        return Parameter.builder()
            .parameterValue(request.getParameterValue())
            .description(request.getDescription())
            .build();
    }

    public ParameterResponse toResponse(Parameter param) {
        return ParameterResponse.builder()
            .id(param.getId())
            .organizationId(param.getOrganizationId())
            .parameterType(param.getParameterType())
            .parameterTypeDisplayName(param.getParameterType())
            .parameterValue(param.getParameterValue())
            .description(param.getDescription())
            .recordStatus(param.getRecordStatus().name())
            .createdAt(param.getCreatedAt())
            .createdBy(param.getCreatedBy())
            .updatedAt(param.getUpdatedAt())
            .updatedBy(param.getUpdatedBy())
            .build();
    }

    public ParameterDocument toDocument(Parameter param) {
        return ParameterDocument.builder()
            .id(param.getId())
            .organizationId(param.getOrganizationId())
            .parameterType(param.getParameterType())
            .parameterValue(param.getParameterValue())
            .description(param.getDescription())
            .recordStatus(param.getRecordStatus().name())
            .createdAt(param.getCreatedAt())
            .createdBy(param.getCreatedBy())
            .updatedAt(param.getUpdatedAt())
            .updatedBy(param.getUpdatedBy())
            .build();
    }

    public Parameter toModel(ParameterDocument doc) {
        return Parameter.builder()
            .id(doc.getId())
            .organizationId(doc.getOrganizationId())
            .parameterType(ParameterType.valueOf(doc.getParameterType()))
            .parameterValue(doc.getParameterValue())
            .description(doc.getDescription())
            .recordStatus(RecordStatus.valueOf(doc.getRecordStatus()))
            .createdAt(doc.getCreatedAt())
            .createdBy(doc.getCreatedBy())
            .updatedAt(doc.getUpdatedAt())
            .updatedBy(doc.getUpdatedBy())
            .build();
    }
}
```

---

## 4️⃣ EVENTS - DTOs de Eventos

#### 📄 OrganizationCreatedEvent.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationCreatedEvent {
    @Builder.Default
    private String eventType = "ORGANIZATION_CREATED";
    private String eventId;
    private LocalDateTime timestamp;
    private String organizationId;
    private String organizationName;
    private String district;
    private String province;
    private String department;
    private String createdBy;
    private String correlationId;
}
```

---

#### 📄 OrganizationUpdatedEvent.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationUpdatedEvent {
    @Builder.Default
    private String eventType = "ORGANIZATION_UPDATED";
    private String eventId;
    private LocalDateTime timestamp;
    private String organizationId;
    private Map<String, Object> changedFields;
    private String updatedBy;
    private String correlationId;
}
```

---

#### 📄 OrganizationDeletedEvent.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationDeletedEvent {
    @Builder.Default
    private String eventType = "ORGANIZATION_DELETED";
    private String eventId;
    private LocalDateTime timestamp;
    private String organizationId;
    private String previousStatus;
    private String reason;
    private String deletedBy;
    private String correlationId;
}
```

---

#### 📄 OrganizationRestoredEvent.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationRestoredEvent {
    @Builder.Default
    private String eventType = "ORGANIZATION_RESTORED";
    private String eventId;
    private LocalDateTime timestamp;
    private String organizationId;
    private String previousStatus;
    private String restoredBy;
    private String correlationId;
}
```

---

#### 📄 ZoneCreatedEvent.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZoneCreatedEvent {
    @Builder.Default
    private String eventType = "ZONE_CREATED";
    private String eventId;
    private LocalDateTime timestamp;
    private String zoneId;
    private String organizationId;
    private String zoneName;
    private String createdBy;
    private String correlationId;
}
```

---

#### 📄 ZoneUpdatedEvent.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZoneUpdatedEvent {
    @Builder.Default
    private String eventType = "ZONE_UPDATED";
    private String eventId;
    private LocalDateTime timestamp;
    private String zoneId;
    private String organizationId;
    private Map<String, Object> changedFields;
    private String updatedBy;
    private String correlationId;
}
```

---

#### 📄 ZoneDeletedEvent.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZoneDeletedEvent {
    @Builder.Default
    private String eventType = "ZONE_DELETED";
    private String eventId;
    private LocalDateTime timestamp;
    private String zoneId;
    private String organizationId;
    private String reason;
    private String deletedBy;
    private String correlationId;
}
```

---

#### 📄 ZoneRestoredEvent.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZoneRestoredEvent {
    @Builder.Default
    private String eventType = "ZONE_RESTORED";
    private String eventId;
    private LocalDateTime timestamp;
    private String zoneId;
    private String organizationId;
    private String restoredBy;
    private String correlationId;
}
```

---

#### 📄 StreetCreatedEvent.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreetCreatedEvent {
    @Builder.Default
    private String eventType = "STREET_CREATED";
    private String eventId;
    private LocalDateTime timestamp;
    private String streetId;
    private String zoneId;
    private String organizationId;
    private String streetType;
    private String streetName;
    private String createdBy;
    private String correlationId;
}
```

---

#### 📄 StreetUpdatedEvent.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreetUpdatedEvent {
    @Builder.Default
    private String eventType = "STREET_UPDATED";
    private String eventId;
    private LocalDateTime timestamp;
    private String streetId;
    private String zoneId;
    private Map<String, Object> changedFields;
    private String updatedBy;
    private String correlationId;
}
```

---

#### 📄 StreetDeletedEvent.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreetDeletedEvent {
    @Builder.Default
    private String eventType = "STREET_DELETED";
    private String eventId;
    private LocalDateTime timestamp;
    private String streetId;
    private String zoneId;
    private String reason;
    private String deletedBy;
    private String correlationId;
}
```

---

#### 📄 StreetRestoredEvent.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreetRestoredEvent {
    @Builder.Default
    private String eventType = "STREET_RESTORED";
    private String eventId;
    private LocalDateTime timestamp;
    private String streetId;
    private String zoneId;
    private String restoredBy;
    private String correlationId;
}
```

---

#### 📄 FareCreatedEvent.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FareCreatedEvent {
    @Builder.Default
    private String eventType = "FARE_CREATED";
    private String eventId;
    private LocalDateTime timestamp;
    private String fareId;
    private String organizationId;
    private String fareType;
    private Double amount;
    private String createdBy;
    private String correlationId;
}
```

---

#### 📄 FareUpdatedEvent.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FareUpdatedEvent {
    @Builder.Default
    private String eventType = "FARE_UPDATED";
    private String eventId;
    private LocalDateTime timestamp;
    private String fareId;
    private String organizationId;
    private Map<String, Object> changedFields;
    private String updatedBy;
    private String correlationId;
}
```

---

#### 📄 FareDeletedEvent.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FareDeletedEvent {
    @Builder.Default
    private String eventType = "FARE_DELETED";
    private String eventId;
    private LocalDateTime timestamp;
    private String fareId;
    private String organizationId;
    private String reason;
    private String deletedBy;
    private String correlationId;
}
```

---

#### 📄 FareRestoredEvent.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FareRestoredEvent {
    @Builder.Default
    private String eventType = "FARE_RESTORED";
    private String eventId;
    private LocalDateTime timestamp;
    private String fareId;
    private String organizationId;
    private String restoredBy;
    private String correlationId;
}
```

---

#### 📄 ParameterCreatedEvent.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParameterCreatedEvent {
    @Builder.Default
    private String eventType = "PARAMETER_CREATED";
    private String eventId;
    private LocalDateTime timestamp;
    private String parameterId;
    private String organizationId;
    private String parameterType;
    private String parameterValue;
    private String createdBy;
    private String correlationId;
}
```

---

#### 📄 ParameterUpdatedEvent.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParameterUpdatedEvent {
    @Builder.Default
    private String eventType = "PARAMETER_UPDATED";
    private String eventId;
    private LocalDateTime timestamp;
    private String parameterId;
    private String organizationId;
    private Map<String, Object> changedFields;
    private String updatedBy;
    private String correlationId;
}
```

---

#### 📄 ParameterDeletedEvent.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParameterDeletedEvent {
    @Builder.Default
    private String eventType = "PARAMETER_DELETED";
    private String eventId;
    private LocalDateTime timestamp;
    private String parameterId;
    private String organizationId;
    private String reason;
    private String deletedBy;
    private String correlationId;
}
```

---

#### 📄 ParameterRestoredEvent.java

```java
package pe.edu.vallegrande.vgmsorganizations.application.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParameterRestoredEvent {
    @Builder.Default
    private String eventType = "PARAMETER_RESTORED";
    private String eventId;
    private LocalDateTime timestamp;
    private String parameterId;
    private String organizationId;
    private String restoredBy;
    private String correlationId;
}
```

---

## ✅ Resumen de la Capa de Aplicación

| Componente | Cantidad | Descripción |
|------------|----------|-------------|
| Use Cases | 25 clases | 5 por entidad (Create, Get, Update, Delete, Restore) |
| DTOs Common | 3 clases | ApiResponse, PageResponse, ErrorMessage |
| DTOs Request | 10 clases | 2 por entidad (Create, Update) |
| DTOs Response | 5 clases | 1 por entidad |
| Mappers | 5 clases | 1 por entidad |
| Events | 20 clases | 4 por entidad (Created, Updated, Deleted, Restored) |

---

> **Siguiente paso**: Lee [README_INFRASTRUCTURE.md](README_INFRASTRUCTURE.md) para ver la implementación de la capa de infraestructura.
