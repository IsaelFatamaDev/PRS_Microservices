# ⚙️ APPLICATION LAYER - Capa de Aplicación

> **Orquestación de casos de uso. Conecta el dominio con la infraestructura.**

## 📋 Principios

1. **Un caso de uso = Una clase**: Cada operación tiene su propia implementación
2. **DTOs para entrada/salida**: Nunca exponer modelos de dominio directamente
3. **Mappers centralizados**: Conversión entre DTOs, modelos y documentos
4. **Eventos asíncronos**: Publicación de eventos tras cada operación

---

## 📂 Estructura

```text
application/
├── usecases/                                       # 🎯 Casos de uso (Servicios)
│   ├── complaint/
│   │   ├── CreateComplaintUseCaseImpl.java         #    └─ @Service (implements ICreate...)
│   │   ├── GetComplaintUseCaseImpl.java            #    └─ @Service (implements IGet...)
│   │   ├── UpdateComplaintUseCaseImpl.java         #    └─ @Service (implements IUpdate...)
│   │   ├── DeleteComplaintUseCaseImpl.java         #    └─ @Service (implements IDelete...)
│   │   ├── RestoreComplaintUseCaseImpl.java        #    └─ @Service (implements IRestore...)
│   │   ├── AddResponseUseCaseImpl.java             #    └─ @Service (implements IAddResponse...)
│   │   └── CloseComplaintUseCaseImpl.java          #    └─ @Service (implements IClose...)
│   ├── complaintcategory/
│   │   ├── CreateComplaintCategoryUseCaseImpl.java #    └─ @Service
│   │   ├── GetComplaintCategoryUseCaseImpl.java    #    └─ @Service
│   │   ├── UpdateComplaintCategoryUseCaseImpl.java #    └─ @Service
│   │   ├── DeleteComplaintCategoryUseCaseImpl.java #    └─ @Service
│   │   └── RestoreComplaintCategoryUseCaseImpl.java#    └─ @Service
│   ├── incident/
│   │   ├── CreateIncidentUseCaseImpl.java          #    └─ @Service
│   │   ├── GetIncidentUseCaseImpl.java             #    └─ @Service
│   │   ├── UpdateIncidentUseCaseImpl.java          #    └─ @Service
│   │   ├── DeleteIncidentUseCaseImpl.java          #    └─ @Service
│   │   ├── RestoreIncidentUseCaseImpl.java         #    └─ @Service
│   │   ├── AssignIncidentUseCaseImpl.java          #    └─ @Service
│   │   ├── ResolveIncidentUseCaseImpl.java         #    └─ @Service
│   │   └── CloseIncidentUseCaseImpl.java           #    └─ @Service
│   └── incidenttype/
│       ├── CreateIncidentTypeUseCaseImpl.java      #    └─ @Service
│       ├── GetIncidentTypeUseCaseImpl.java         #    └─ @Service
│       ├── UpdateIncidentTypeUseCaseImpl.java      #    └─ @Service
│       ├── DeleteIncidentTypeUseCaseImpl.java      #    └─ @Service
│       └── RestoreIncidentTypeUseCaseImpl.java     #    └─ @Service
│
├── dto/                                            # 📝 DTOs (Data Transfer Objects)
│   ├── common/                                     #    └─ DTOs comunes
│   │   ├── ApiResponse.java                        #       └─ Clase genérica (respuesta estándar)
│   │   ├── PageResponse.java                       #       └─ Clase genérica (paginación)
│   │   └── ErrorMessage.java                       #       └─ Clase (errores)
│   ├── complaint/
│   │   ├── CreateComplaintRequest.java             #    └─ DTO Request
│   │   ├── UpdateComplaintRequest.java             #    └─ DTO Request
│   │   ├── AddComplaintResponseRequest.java        #    └─ DTO Request
│   │   ├── ComplaintResponse.java                  #    └─ DTO Response (renamed ComplaintResponseDTO)
│   │   └── ComplaintResponseDTO.java               #    └─ DTO Response (para ComplaintResponse entity)
│   ├── complaintcategory/
│   │   ├── CreateComplaintCategoryRequest.java     #    └─ DTO Request
│   │   ├── UpdateComplaintCategoryRequest.java     #    └─ DTO Request
│   │   └── ComplaintCategoryResponse.java          #    └─ DTO Response
│   ├── incident/
│   │   ├── CreateIncidentRequest.java              #    └─ DTO Request
│   │   ├── UpdateIncidentRequest.java              #    └─ DTO Request
│   │   ├── AssignIncidentRequest.java              #    └─ DTO Request
│   │   ├── ResolveIncidentRequest.java             #    └─ DTO Request
│   │   ├── MaterialUsedRequest.java                #    └─ DTO Request (embedded)
│   │   ├── IncidentResponseDTO.java                #    └─ DTO Response
│   │   └── IncidentResolutionResponseDTO.java      #    └─ DTO Response
│   └── incidenttype/
│       ├── CreateIncidentTypeRequest.java          #    └─ DTO Request
│       ├── UpdateIncidentTypeRequest.java          #    └─ DTO Request
│       └── IncidentTypeResponse.java               #    └─ DTO Response
│
├── mappers/                                        # 🗺️ Mappers (Conversión)
│   ├── ComplaintMapper.java                        #    └─ @Component (DTO<->Model<->Doc)
│   ├── ComplaintCategoryMapper.java                #    └─ @Component
│   ├── IncidentMapper.java                         #    └─ @Component
│   └── IncidentTypeMapper.java                     #    └─ @Component
│
└── events/                                         # 📨 Eventos de dominio (RabbitMQ)
    ├── complaint/
    │   ├── ComplaintCreatedEvent.java              #    └─ Event Payload
    │   ├── ComplaintUpdatedEvent.java              #    └─ Event Payload
    │   ├── ComplaintClosedEvent.java               #    └─ Event Payload
    │   └── ComplaintResponseAddedEvent.java        #    └─ Event Payload
    └── incident/
        ├── IncidentCreatedEvent.java               #    └─ Event Payload
        ├── IncidentAssignedEvent.java              #    └─ Event Payload
        ├── IncidentUpdatedEvent.java               #    └─ Event Payload
        ├── IncidentResolvedEvent.java              #    └─ Event Payload
        ├── IncidentClosedEvent.java                #    └─ Event Payload
        └── UrgentIncidentAlertEvent.java           #    └─ Event Payload
```

---

## 1️⃣ USE CASES - Implementaciones

### 📁 complaint/

#### 📄 CreateComplaintUseCaseImpl.java

```java
package pe.edu.vallegrande.vgmsclaims.application.usecases.complaint;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsclaims.domain.models.Complaint;
import pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects.ComplaintStatus;
import pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects.RecordStatus;
import pe.edu.vallegrande.vgmsclaims.domain.ports.in.complaint.ICreateComplaintUseCase;
import pe.edu.vallegrande.vgmsclaims.domain.ports.out.IClaimsEventPublisher;
import pe.edu.vallegrande.vgmsclaims.domain.ports.out.IComplaintCategoryRepository;
import pe.edu.vallegrande.vgmsclaims.domain.ports.out.IComplaintRepository;
import pe.edu.vallegrande.vgmsclaims.domain.exceptions.base.NotFoundException;
import pe.edu.vallegrande.vgmsclaims.domain.exceptions.base.BusinessRuleException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateComplaintUseCaseImpl implements ICreateComplaintUseCase {

    private final IComplaintRepository complaintRepository;
    private final IComplaintCategoryRepository categoryRepository;
    private final IClaimsEventPublisher eventPublisher;

    @Override
    public Mono<Complaint> execute(Complaint complaint, String createdBy) {
        log.info("Creating complaint for user: {} in organization: {}", complaint.getUserId(), complaint.getOrganizationId());

        return categoryRepository.findById(complaint.getCategoryId())
            .switchIfEmpty(Mono.error(new NotFoundException("ComplaintCategory", complaint.getCategoryId())))
            .flatMap(category -> {
                if (!category.isActive()) {
                    return Mono.error(new BusinessRuleException("Cannot create complaint with an inactive category"));
                }

                String code = "RCL-" + System.currentTimeMillis();

                Complaint newComplaint = complaint.toBuilder()
                    .id(UUID.randomUUID().toString())
                    .complaintCode(code)
                    .complaintDate(LocalDateTime.now())
                    .complaintStatus(ComplaintStatus.RECEIVED)
                    .recordStatus(RecordStatus.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .createdBy(createdBy)
                    .updatedAt(LocalDateTime.now())
                    .updatedBy(createdBy)
                    .build();

                return complaintRepository.save(newComplaint);
            })
            .flatMap(saved -> publishCreateEvent(saved, createdBy))
            .doOnSuccess(c -> log.info("Complaint created: {} - Code: {}", c.getId(), c.getComplaintCode()))
            .doOnError(e -> log.error("Error creating complaint: {}", e.getMessage()));
    }

    private Mono<Complaint> publishCreateEvent(Complaint complaint, String createdBy) {
        return eventPublisher.publishComplaintCreated(complaint, createdBy)
            .thenReturn(complaint)
            .onErrorResume(e -> {
                log.warn("Failed to publish complaint create event: {}", e.getMessage());
                return Mono.just(complaint);
            });
    }
}
```

---

#### 📄 GetComplaintUseCaseImpl.java

```java
package pe.edu.vallegrande.vgmsclaims.application.usecases.complaint;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsclaims.domain.exceptions.specific.ComplaintNotFoundException;
import pe.edu.vallegrande.vgmsclaims.domain.models.Complaint;
import pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects.ComplaintPriority;
import pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects.ComplaintStatus;
import pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects.RecordStatus;
import pe.edu.vallegrande.vgmsclaims.domain.ports.in.complaint.IGetComplaintUseCase;
import pe.edu.vallegrande.vgmsclaims.domain.ports.out.IComplaintRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetComplaintUseCaseImpl implements IGetComplaintUseCase {

    private final IComplaintRepository complaintRepository;

    @Override
    public Mono<Complaint> findById(String id) {
        log.info("Finding complaint by ID: {}", id);
        return complaintRepository.findById(id)
            .switchIfEmpty(Mono.error(new ComplaintNotFoundException(id)));
    }

    @Override
    public Flux<Complaint> findAllActive() {
        log.info("Finding all active complaints");
        return complaintRepository.findByRecordStatus(RecordStatus.ACTIVE);
    }

    @Override
    public Flux<Complaint> findAll() {
        log.info("Finding all complaints");
        return complaintRepository.findAll();
    }

    @Override
    public Flux<Complaint> findByStatus(ComplaintStatus status) {
        log.info("Finding complaints by status: {}", status);
        return complaintRepository.findByComplaintStatus(status);
    }

    @Override
    public Flux<Complaint> findByUserId(String userId) {
        log.info("Finding complaints by user: {}", userId);
        return complaintRepository.findByUserId(userId);
    }

    @Override
    public Flux<Complaint> findByCategoryId(String categoryId) {
        log.info("Finding complaints by category: {}", categoryId);
        return complaintRepository.findByCategoryId(categoryId);
    }

    @Override
    public Flux<Complaint> findByPriority(ComplaintPriority priority) {
        log.info("Finding complaints by priority: {}", priority);
        return complaintRepository.findByComplaintPriority(priority);
    }

    @Override
    public Flux<Complaint> findByOrganizationId(String organizationId) {
        log.info("Finding complaints by organization: {}", organizationId);
        return complaintRepository.findByOrganizationId(organizationId);
    }
}
```

---

#### 📄 UpdateComplaintUseCaseImpl.java

```java
package pe.edu.vallegrande.vgmsclaims.application.usecases.complaint;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsclaims.domain.exceptions.specific.ComplaintAlreadyClosedException;
import pe.edu.vallegrande.vgmsclaims.domain.exceptions.specific.ComplaintNotFoundException;
import pe.edu.vallegrande.vgmsclaims.domain.models.Complaint;
import pe.edu.vallegrande.vgmsclaims.domain.ports.in.complaint.IUpdateComplaintUseCase;
import pe.edu.vallegrande.vgmsclaims.domain.ports.out.IClaimsEventPublisher;
import pe.edu.vallegrande.vgmsclaims.domain.ports.out.IComplaintRepository;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateComplaintUseCaseImpl implements IUpdateComplaintUseCase {

    private final IComplaintRepository complaintRepository;
    private final IClaimsEventPublisher eventPublisher;

    @Override
    public Mono<Complaint> execute(String id, Complaint changes, String updatedBy) {
        log.info("Updating complaint: {}", id);

        return complaintRepository.findById(id)
            .switchIfEmpty(Mono.error(new ComplaintNotFoundException(id)))
            .flatMap(existing -> {
                if (!existing.canBeUpdated()) {
                    return Mono.error(new ComplaintAlreadyClosedException(id));
                }

                Map<String, Object> changedFields = detectChanges(existing, changes);

                if (changedFields.isEmpty()) {
                    log.info("No changes detected for complaint: {}", id);
                    return Mono.just(existing);
                }

                Complaint updated = existing.updateWith(changes, updatedBy);
                return complaintRepository.update(updated)
                    .flatMap(saved -> publishUpdateEvent(saved, changedFields, updatedBy));
            })
            .doOnSuccess(c -> log.info("Complaint updated: {}", c.getId()))
            .doOnError(e -> log.error("Error updating complaint: {}", e.getMessage()));
    }

    private Map<String, Object> detectChanges(Complaint existing, Complaint changes) {
        Map<String, Object> changedFields = new HashMap<>();

        if (changes.getCategoryId() != null && !changes.getCategoryId().equals(existing.getCategoryId())) {
            changedFields.put("categoryId", changes.getCategoryId());
        }
        if (changes.getComplaintPriority() != null && !changes.getComplaintPriority().equals(existing.getComplaintPriority())) {
            changedFields.put("complaintPriority", changes.getComplaintPriority().name());
        }
        if (changes.getDescription() != null && !changes.getDescription().equals(existing.getDescription())) {
            changedFields.put("description", changes.getDescription());
        }
        if (changes.getAssignedToUserId() != null && !changes.getAssignedToUserId().equals(existing.getAssignedToUserId())) {
            changedFields.put("assignedToUserId", changes.getAssignedToUserId());
        }

        return changedFields;
    }

    private Mono<Complaint> publishUpdateEvent(Complaint complaint, Map<String, Object> changedFields, String updatedBy) {
        return eventPublisher.publishComplaintUpdated(complaint, changedFields, updatedBy)
            .thenReturn(complaint)
            .onErrorResume(e -> {
                log.warn("Failed to publish complaint update event: {}", e.getMessage());
                return Mono.just(complaint);
            });
    }
}
```

---

#### 📄 DeleteComplaintUseCaseImpl.java

```java
package pe.edu.vallegrande.vgmsclaims.application.usecases.complaint;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsclaims.domain.exceptions.base.BusinessRuleException;
import pe.edu.vallegrande.vgmsclaims.domain.exceptions.specific.ComplaintNotFoundException;
import pe.edu.vallegrande.vgmsclaims.domain.models.Complaint;
import pe.edu.vallegrande.vgmsclaims.domain.ports.in.complaint.IDeleteComplaintUseCase;
import pe.edu.vallegrande.vgmsclaims.domain.ports.out.IClaimsEventPublisher;
import pe.edu.vallegrande.vgmsclaims.domain.ports.out.IComplaintRepository;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteComplaintUseCaseImpl implements IDeleteComplaintUseCase {

    private final IComplaintRepository complaintRepository;
    private final IClaimsEventPublisher eventPublisher;

    @Override
    public Mono<Complaint> execute(String id, String deletedBy, String reason) {
        log.info("Deleting complaint: {} - Reason: {}", id, reason);

        return complaintRepository.findById(id)
            .switchIfEmpty(Mono.error(new ComplaintNotFoundException(id)))
            .flatMap(complaint -> {
                if (complaint.isInactive()) {
                    return Mono.error(new BusinessRuleException("Complaint is already inactive"));
                }

                Complaint deleted = complaint.markAsDeleted(deletedBy);
                return complaintRepository.update(deleted)
                    .flatMap(saved -> eventPublisher.publishComplaintUpdated(saved, Map.of("recordStatus", "INACTIVE", "reason", reason), deletedBy)
                        .thenReturn(saved)
                        .onErrorResume(e -> {
                            log.warn("Failed to publish complaint delete event: {}", e.getMessage());
                            return Mono.just(saved);
                        })
                    );
            })
            .doOnSuccess(c -> log.info("Complaint deleted: {}", c.getId()))
            .doOnError(e -> log.error("Error deleting complaint: {}", e.getMessage()));
    }
}
```

---

#### 📄 RestoreComplaintUseCaseImpl.java

```java
package pe.edu.vallegrande.vgmsclaims.application.usecases.complaint;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsclaims.domain.exceptions.base.BusinessRuleException;
import pe.edu.vallegrande.vgmsclaims.domain.exceptions.specific.ComplaintNotFoundException;
import pe.edu.vallegrande.vgmsclaims.domain.models.Complaint;
import pe.edu.vallegrande.vgmsclaims.domain.ports.in.complaint.IRestoreComplaintUseCase;
import pe.edu.vallegrande.vgmsclaims.domain.ports.out.IClaimsEventPublisher;
import pe.edu.vallegrande.vgmsclaims.domain.ports.out.IComplaintRepository;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestoreComplaintUseCaseImpl implements IRestoreComplaintUseCase {

    private final IComplaintRepository complaintRepository;
    private final IClaimsEventPublisher eventPublisher;

    @Override
    public Mono<Complaint> execute(String id, String restoredBy) {
        log.info("Restoring complaint: {}", id);

        return complaintRepository.findById(id)
            .switchIfEmpty(Mono.error(new ComplaintNotFoundException(id)))
            .flatMap(complaint -> {
                if (complaint.isActive()) {
                    return Mono.error(new BusinessRuleException("Complaint is already active"));
                }

                Complaint restored = complaint.restore(restoredBy);
                return complaintRepository.update(restored)
                    .flatMap(saved -> eventPublisher.publishComplaintUpdated(saved, Map.of("recordStatus", "ACTIVE"), restoredBy)
                        .thenReturn(saved)
                        .onErrorResume(e -> {
                            log.warn("Failed to publish complaint restore event: {}", e.getMessage());
                            return Mono.just(saved);
                        })
                    );
            })
            .doOnSuccess(c -> log.info("Complaint restored: {}", c.getId()))
            .doOnError(e -> log.error("Error restoring complaint: {}", e.getMessage()));
    }
}
```

---

#### 📄 AddResponseUseCaseImpl.java

```java
package pe.edu.vallegrande.vgmsclaims.application.usecases.complaint;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsclaims.domain.exceptions.specific.ComplaintAlreadyClosedException;
import pe.edu.vallegrande.vgmsclaims.domain.exceptions.specific.ComplaintNotFoundException;
import pe.edu.vallegrande.vgmsclaims.domain.models.ComplaintResponse;
import pe.edu.vallegrande.vgmsclaims.domain.ports.in.complaint.IAddResponseUseCase;
import pe.edu.vallegrande.vgmsclaims.domain.ports.out.IClaimsEventPublisher;
import pe.edu.vallegrande.vgmsclaims.domain.ports.out.IComplaintRepository;
import pe.edu.vallegrande.vgmsclaims.domain.ports.out.IComplaintResponseRepository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddResponseUseCaseImpl implements IAddResponseUseCase {

    private final IComplaintRepository complaintRepository;
    private final IComplaintResponseRepository responseRepository;
    private final IClaimsEventPublisher eventPublisher;

    @Override
    public Mono<ComplaintResponse> execute(String complaintId, ComplaintResponse response, String respondedBy) {
        log.info("Adding response to complaint: {} by user: {}", complaintId, respondedBy);

        return complaintRepository.findById(complaintId)
            .switchIfEmpty(Mono.error(new ComplaintNotFoundException(complaintId)))
            .flatMap(complaint -> {
                if (complaint.isClosed()) {
                    return Mono.error(new ComplaintAlreadyClosedException(complaintId));
                }

                // Advance complaint status if it's still RECEIVED
                Mono<Void> advanceMono = Mono.empty();
                if (complaint.getComplaintStatus() == pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects.ComplaintStatus.RECEIVED) {
                    var advanced = complaint.advanceStatus();
                    advanceMono = complaintRepository.update(advanced).then();
                }

                ComplaintResponse newResponse = response.toBuilder()
                    .id(UUID.randomUUID().toString())
                    .complaintId(complaintId)
                    .responseDate(LocalDateTime.now())
                    .respondedByUserId(respondedBy)
                    .createdAt(LocalDateTime.now())
                    .build();

                return advanceMono
                    .then(responseRepository.save(newResponse));
            })
            .flatMap(saved -> eventPublisher.publishComplaintResponseAdded(saved, respondedBy)
                .thenReturn(saved)
                .onErrorResume(e -> {
                    log.warn("Failed to publish response added event: {}", e.getMessage());
                    return Mono.just(saved);
                })
            )
            .doOnSuccess(r -> log.info("Response added: {} to complaint: {}", r.getId(), r.getComplaintId()))
            .doOnError(e -> log.error("Error adding response: {}", e.getMessage()));
    }
}
```

---

#### 📄 CloseComplaintUseCaseImpl.java

```java
package pe.edu.vallegrande.vgmsclaims.application.usecases.complaint;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsclaims.domain.exceptions.base.BusinessRuleException;
import pe.edu.vallegrande.vgmsclaims.domain.exceptions.specific.ComplaintAlreadyClosedException;
import pe.edu.vallegrande.vgmsclaims.domain.exceptions.specific.ComplaintNotFoundException;
import pe.edu.vallegrande.vgmsclaims.domain.models.Complaint;
import pe.edu.vallegrande.vgmsclaims.domain.ports.in.complaint.ICloseComplaintUseCase;
import pe.edu.vallegrande.vgmsclaims.domain.ports.out.IClaimsEventPublisher;
import pe.edu.vallegrande.vgmsclaims.domain.ports.out.IComplaintRepository;
import pe.edu.vallegrande.vgmsclaims.domain.services.ClaimsAuthorizationService;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloseComplaintUseCaseImpl implements ICloseComplaintUseCase {

    private final IComplaintRepository complaintRepository;
    private final IClaimsEventPublisher eventPublisher;
    private final ClaimsAuthorizationService authorizationService;

    @Override
    public Mono<Complaint> execute(String id, String closedBy) {
        log.info("Closing complaint: {} by user: {}", id, closedBy);

        return authorizationService.validateCanClose()
            .then(complaintRepository.findById(id))
            .switchIfEmpty(Mono.error(new ComplaintNotFoundException(id)))
            .flatMap(complaint -> {
                if (complaint.isClosed()) {
                    return Mono.error(new ComplaintAlreadyClosedException(id));
                }
                if (!complaint.canBeClosed()) {
                    return Mono.error(new BusinessRuleException(
                        "Complaint must be in RESOLVED status to be closed"
                    ));
                }

                Complaint closed = complaint.close(closedBy);
                return complaintRepository.update(closed)
                    .flatMap(saved -> eventPublisher.publishComplaintClosed(saved.getId(), closedBy)
                        .thenReturn(saved)
                        .onErrorResume(e -> {
                            log.warn("Failed to publish complaint close event: {}", e.getMessage());
                            return Mono.just(saved);
                        })
                    );
            })
            .doOnSuccess(c -> log.info("Complaint closed: {}", c.getId()))
            .doOnError(e -> log.error("Error closing complaint: {}", e.getMessage()));
    }
}
```

---

### 📁 complaintcategory/

#### 📄 CreateComplaintCategoryUseCaseImpl.java

```java
package pe.edu.vallegrande.vgmsclaims.application.usecases.complaintcategory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsclaims.domain.exceptions.base.ConflictException;
import pe.edu.vallegrande.vgmsclaims.domain.models.ComplaintCategory;
import pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects.RecordStatus;
import pe.edu.vallegrande.vgmsclaims.domain.ports.in.complaintcategory.ICreateComplaintCategoryUseCase;
import pe.edu.vallegrande.vgmsclaims.domain.ports.out.IClaimsEventPublisher;
import pe.edu.vallegrande.vgmsclaims.domain.ports.out.IComplaintCategoryRepository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateComplaintCategoryUseCaseImpl implements ICreateComplaintCategoryUseCase {

    private final IComplaintCategoryRepository categoryRepository;
    private final IClaimsEventPublisher eventPublisher;

    @Override
    public Mono<ComplaintCategory> execute(ComplaintCategory category, String createdBy) {
        log.info("Creating complaint category: {} for organization: {}", category.getCategoryName(), category.getOrganizationId());

        return categoryRepository.existsByCategoryCodeAndOrganizationId(category.getCategoryCode(), category.getOrganizationId())
            .flatMap(exists -> {
                if (exists) {
                    return Mono.error(new ConflictException(
                        String.format("Category code '%s' already exists in this organization", category.getCategoryCode())
                    ));
                }

                ComplaintCategory newCategory = category.toBuilder()
                    .id(UUID.randomUUID().toString())
                    .recordStatus(RecordStatus.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .createdBy(createdBy)
                    .updatedAt(LocalDateTime.now())
                    .updatedBy(createdBy)
                    .build();

                return categoryRepository.save(newCategory);
            })
            .flatMap(saved -> eventPublisher.publishComplaintCreated(null, createdBy)
                .thenReturn(saved)
                .onErrorResume(e -> {
                    log.warn("Failed to publish category create event: {}", e.getMessage());
                    return Mono.just(saved);
                })
            )
            .doOnSuccess(c -> log.info("Complaint category created: {}", c.getId()))
            .doOnError(e -> log.error("Error creating complaint category: {}", e.getMessage()));
    }
}
```

---

#### 📄 GetComplaintCategoryUseCaseImpl.java

```java
package pe.edu.vallegrande.vgmsclaims.application.usecases.complaintcategory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsclaims.domain.exceptions.base.NotFoundException;
import pe.edu.vallegrande.vgmsclaims.domain.models.ComplaintCategory;
import pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects.RecordStatus;
import pe.edu.vallegrande.vgmsclaims.domain.ports.in.complaintcategory.IGetComplaintCategoryUseCase;
import pe.edu.vallegrande.vgmsclaims.domain.ports.out.IComplaintCategoryRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetComplaintCategoryUseCaseImpl implements IGetComplaintCategoryUseCase {

    private final IComplaintCategoryRepository categoryRepository;

    @Override
    public Mono<ComplaintCategory> findById(String id) {
        log.info("Finding complaint category by ID: {}", id);
        return categoryRepository.findById(id)
            .switchIfEmpty(Mono.error(new NotFoundException("ComplaintCategory", id)));
    }

    @Override
    public Flux<ComplaintCategory> findAllActive() {
        log.info("Finding all active complaint categories");
        return categoryRepository.findByRecordStatus(RecordStatus.ACTIVE);
    }

    @Override
    public Flux<ComplaintCategory> findAll() {
        log.info("Finding all complaint categories");
        return categoryRepository.findAll();
    }

    @Override
    public Flux<ComplaintCategory> findByOrganizationId(String organizationId) {
        log.info("Finding complaint categories by organization: {}", organizationId);
        return categoryRepository.findByOrganizationId(organizationId);
    }
}
```

---

#### 📄 UpdateComplaintCategoryUseCaseImpl.java

```java
package pe.edu.vallegrande.vgmsclaims.application.usecases.complaintcategory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsclaims.domain.exceptions.base.NotFoundException;
import pe.edu.vallegrande.vgmsclaims.domain.models.ComplaintCategory;
import pe.edu.vallegrande.vgmsclaims.domain.ports.in.complaintcategory.IUpdateComplaintCategoryUseCase;
import pe.edu.vallegrande.vgmsclaims.domain.ports.out.IComplaintCategoryRepository;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateComplaintCategoryUseCaseImpl implements IUpdateComplaintCategoryUseCase {

    private final IComplaintCategoryRepository categoryRepository;

    @Override
    public Mono<ComplaintCategory> execute(String id, ComplaintCategory changes, String updatedBy) {
        log.info("Updating complaint category: {}", id);

        return categoryRepository.findById(id)
            .switchIfEmpty(Mono.error(new NotFoundException("ComplaintCategory", id)))
            .flatMap(existing -> {
                Map<String, Object> changedFields = detectChanges(existing, changes);
                if (changedFields.isEmpty()) {
                    log.info("No changes detected for complaint category: {}", id);
                    return Mono.just(existing);
                }

                ComplaintCategory updated = existing.updateWith(changes, updatedBy);
                return categoryRepository.update(updated);
            })
            .doOnSuccess(c -> log.info("Complaint category updated: {}", c.getId()))
            .doOnError(e -> log.error("Error updating complaint category: {}", e.getMessage()));
    }

    private Map<String, Object> detectChanges(ComplaintCategory existing, ComplaintCategory changes) {
        Map<String, Object> changedFields = new HashMap<>();
        if (changes.getCategoryName() != null && !changes.getCategoryName().equals(existing.getCategoryName())) {
            changedFields.put("categoryName", changes.getCategoryName());
        }
        if (changes.getDescription() != null && !changes.getDescription().equals(existing.getDescription())) {
            changedFields.put("description", changes.getDescription());
        }
        return changedFields;
    }
}
```

---

#### 📄 DeleteComplaintCategoryUseCaseImpl.java

```java
package pe.edu.vallegrande.vgmsclaims.application.usecases.complaintcategory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsclaims.domain.exceptions.base.BusinessRuleException;
import pe.edu.vallegrande.vgmsclaims.domain.exceptions.base.NotFoundException;
import pe.edu.vallegrande.vgmsclaims.domain.models.ComplaintCategory;
import pe.edu.vallegrande.vgmsclaims.domain.ports.in.complaintcategory.IDeleteComplaintCategoryUseCase;
import pe.edu.vallegrande.vgmsclaims.domain.ports.out.IComplaintCategoryRepository;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteComplaintCategoryUseCaseImpl implements IDeleteComplaintCategoryUseCase {

    private final IComplaintCategoryRepository categoryRepository;

    @Override
    public Mono<ComplaintCategory> execute(String id, String deletedBy, String reason) {
        log.info("Deleting complaint category: {} - Reason: {}", id, reason);

        return categoryRepository.findById(id)
            .switchIfEmpty(Mono.error(new NotFoundException("ComplaintCategory", id)))
            .flatMap(category -> {
                if (category.isInactive()) {
                    return Mono.error(new BusinessRuleException("Complaint category is already inactive"));
                }

                ComplaintCategory deleted = category.markAsDeleted(deletedBy);
                return categoryRepository.update(deleted);
            })
            .doOnSuccess(c -> log.info("Complaint category deleted: {}", c.getId()))
            .doOnError(e -> log.error("Error deleting complaint category: {}", e.getMessage()));
    }
}
```

---

#### 📄 RestoreComplaintCategoryUseCaseImpl.java

```java
package pe.edu.vallegrande.vgmsclaims.application.usecases.complaintcategory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsclaims.domain.exceptions.base.BusinessRuleException;
import pe.edu.vallegrande.vgmsclaims.domain.exceptions.base.NotFoundException;
import pe.edu.vallegrande.vgmsclaims.domain.models.ComplaintCategory;
import pe.edu.vallegrande.vgmsclaims.domain.ports.in.complaintcategory.IRestoreComplaintCategoryUseCase;
import pe.edu.vallegrande.vgmsclaims.domain.ports.out.IComplaintCategoryRepository;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestoreComplaintCategoryUseCaseImpl implements IRestoreComplaintCategoryUseCase {

    private final IComplaintCategoryRepository categoryRepository;

    @Override
    public Mono<ComplaintCategory> execute(String id, String restoredBy) {
        log.info("Restoring complaint category: {}", id);

        return categoryRepository.findById(id)
            .switchIfEmpty(Mono.error(new NotFoundException("ComplaintCategory", id)))
            .flatMap(category -> {
                if (category.isActive()) {
                    return Mono.error(new BusinessRuleException("Complaint category is already active"));
                }

                ComplaintCategory restored = category.restore(restoredBy);
                return categoryRepository.update(restored);
            })
            .doOnSuccess(c -> log.info("Complaint category restored: {}", c.getId()))
            .doOnError(e -> log.error("Error restoring complaint category: {}", e.getMessage()));
    }
}
```

---

### 📁 incident/

#### 📄 CreateIncidentUseCaseImpl.java

```java
package pe.edu.vallegrande.vgmsclaims.application.usecases.incident;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsclaims.domain.exceptions.base.NotFoundException;
import pe.edu.vallegrande.vgmsclaims.domain.exceptions.base.BusinessRuleException;
import pe.edu.vallegrande.vgmsclaims.domain.models.Incident;
import pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects.IncidentStatus;
import pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects.RecordStatus;
import pe.edu.vallegrande.vgmsclaims.domain.ports.in.incident.ICreateIncidentUseCase;
import pe.edu.vallegrande.vgmsclaims.domain.ports.out.IClaimsEventPublisher;
import pe.edu.vallegrande.vgmsclaims.domain.ports.out.IIncidentRepository;
import pe.edu.vallegrande.vgmsclaims.domain.ports.out.IIncidentTypeRepository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateIncidentUseCaseImpl implements ICreateIncidentUseCase {

    private final IIncidentRepository incidentRepository;
    private final IIncidentTypeRepository incidentTypeRepository;
    private final IClaimsEventPublisher eventPublisher;

    @Override
    public Mono<Incident> execute(Incident incident, String createdBy) {
        log.info("Creating incident of type: {} in organization: {}", incident.getIncidentTypeId(), incident.getOrganizationId());

        return incidentTypeRepository.findById(incident.getIncidentTypeId())
            .switchIfEmpty(Mono.error(new NotFoundException("IncidentType", incident.getIncidentTypeId())))
            .flatMap(type -> {
                if (!type.isActive()) {
                    return Mono.error(new BusinessRuleException("Cannot create incident with an inactive type"));
                }

                String code = "INC-" + System.currentTimeMillis();

                Incident newIncident = incident.toBuilder()
                    .id(UUID.randomUUID().toString())
                    .incidentCode(code)
                    .incidentDate(LocalDateTime.now())
                    .incidentStatus(IncidentStatus.REPORTED)
                    .recordStatus(RecordStatus.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .createdBy(createdBy)
                    .updatedAt(LocalDateTime.now())
                    .updatedBy(createdBy)
                    .build();

                return incidentRepository.save(newIncident);
            })
            .flatMap(saved -> {
                // Publish creation event
                Mono<Incident> publishCreate = eventPublisher.publishIncidentCreated(saved, createdBy)
                    .thenReturn(saved)
                    .onErrorResume(e -> {
                        log.warn("Failed to publish incident create event: {}", e.getMessage());
                        return Mono.just(saved);
                    });

                // If urgent, also publish alert
                if (saved.isUrgent()) {
                    return publishCreate.flatMap(s ->
                        eventPublisher.publishUrgentIncidentAlert(s, createdBy)
                            .thenReturn(s)
                            .onErrorResume(e -> {
                                log.warn("Failed to publish urgent alert: {}", e.getMessage());
                                return Mono.just(s);
                            })
                    );
                }

                return publishCreate;
            })
            .doOnSuccess(i -> log.info("Incident created: {} - Code: {}", i.getId(), i.getIncidentCode()))
            .doOnError(e -> log.error("Error creating incident: {}", e.getMessage()));
    }
}
```

---

#### 📄 GetIncidentUseCaseImpl.java

```java
package pe.edu.vallegrande.vgmsclaims.application.usecases.incident;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsclaims.domain.exceptions.specific.IncidentNotFoundException;
import pe.edu.vallegrande.vgmsclaims.domain.models.Incident;
import pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects.IncidentSeverity;
import pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects.IncidentStatus;
import pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects.RecordStatus;
import pe.edu.vallegrande.vgmsclaims.domain.ports.in.incident.IGetIncidentUseCase;
import pe.edu.vallegrande.vgmsclaims.domain.ports.out.IIncidentRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetIncidentUseCaseImpl implements IGetIncidentUseCase {

    private final IIncidentRepository incidentRepository;

    @Override
    public Mono<Incident> findById(String id) {
        log.info("Finding incident by ID: {}", id);
        return incidentRepository.findById(id)
            .switchIfEmpty(Mono.error(new IncidentNotFoundException(id)));
    }

    @Override
    public Flux<Incident> findAllActive() {
        log.info("Finding all active incidents");
        return incidentRepository.findByRecordStatus(RecordStatus.ACTIVE);
    }

    @Override
    public Flux<Incident> findAll() {
        log.info("Finding all incidents");
        return incidentRepository.findAll();
    }

    @Override
    public Flux<Incident> findByStatus(IncidentStatus status) {
        log.info("Finding incidents by status: {}", status);
        return incidentRepository.findByIncidentStatus(status);
    }

    @Override
    public Flux<Incident> findByTypeId(String typeId) {
        log.info("Finding incidents by type: {}", typeId);
        return incidentRepository.findByIncidentTypeId(typeId);
    }

    @Override
    public Flux<Incident> findBySeverity(IncidentSeverity severity) {
        log.info("Finding incidents by severity: {}", severity);
        return incidentRepository.findByIncidentSeverity(severity);
    }

    @Override
    public Flux<Incident> findByAssignedTo(String userId) {
        log.info("Finding incidents assigned to user: {}", userId);
        return incidentRepository.findByAssignedToUserId(userId);
    }

    @Override
    public Flux<Incident> findByOrganizationId(String organizationId) {
        log.info("Finding incidents by organization: {}", organizationId);
        return incidentRepository.findByOrganizationId(organizationId);
    }
}
```

---

#### 📄 UpdateIncidentUseCaseImpl.java

```java
package pe.edu.vallegrande.vgmsclaims.application.usecases.incident;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsclaims.domain.exceptions.specific.IncidentAlreadyResolvedException;
import pe.edu.vallegrande.vgmsclaims.domain.exceptions.specific.IncidentNotFoundException;
import pe.edu.vallegrande.vgmsclaims.domain.models.Incident;
import pe.edu.vallegrande.vgmsclaims.domain.ports.in.incident.IUpdateIncidentUseCase;
import pe.edu.vallegrande.vgmsclaims.domain.ports.out.IClaimsEventPublisher;
import pe.edu.vallegrande.vgmsclaims.domain.ports.out.IIncidentRepository;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateIncidentUseCaseImpl implements IUpdateIncidentUseCase {

    private final IIncidentRepository incidentRepository;
    private final IClaimsEventPublisher eventPublisher;

    @Override
    public Mono<Incident> execute(String id, Incident changes, String updatedBy) {
        log.info("Updating incident: {}", id);

        return incidentRepository.findById(id)
            .switchIfEmpty(Mono.error(new IncidentNotFoundException(id)))
            .flatMap(existing -> {
                if (!existing.canBeUpdated()) {
                    return Mono.error(new IncidentAlreadyResolvedException(id));
                }

                Map<String, Object> changedFields = detectChanges(existing, changes);

                if (changedFields.isEmpty()) {
                    log.info("No changes detected for incident: {}", id);
                    return Mono.just(existing);
                }

                Incident updated = existing.updateWith(changes, updatedBy);
                return incidentRepository.update(updated)
                    .flatMap(saved -> publishUpdateEvent(saved, changedFields, updatedBy));
            })
            .doOnSuccess(i -> log.info("Incident updated: {}", i.getId()))
            .doOnError(e -> log.error("Error updating incident: {}", e.getMessage()));
    }

    private Map<String, Object> detectChanges(Incident existing, Incident changes) {
        Map<String, Object> changedFields = new HashMap<>();

        if (changes.getIncidentTypeId() != null && !changes.getIncidentTypeId().equals(existing.getIncidentTypeId())) {
            changedFields.put("incidentTypeId", changes.getIncidentTypeId());
        }
        if (changes.getLocation() != null && !changes.getLocation().equals(existing.getLocation())) {
            changedFields.put("location", changes.getLocation());
        }
        if (changes.getZoneId() != null && !changes.getZoneId().equals(existing.getZoneId())) {
            changedFields.put("zoneId", changes.getZoneId());
        }
        if (changes.getStreetId() != null && !changes.getStreetId().equals(existing.getStreetId())) {
            changedFields.put("streetId", changes.getStreetId());
        }
        if (changes.getIncidentSeverity() != null && !changes.getIncidentSeverity().equals(existing.getIncidentSeverity())) {
            changedFields.put("incidentSeverity", changes.getIncidentSeverity().name());
        }
        if (changes.getDescription() != null && !changes.getDescription().equals(existing.getDescription())) {
            changedFields.put("description", changes.getDescription());
        }

        return changedFields;
    }

    private Mono<Incident> publishUpdateEvent(Incident incident, Map<String, Object> changedFields, String updatedBy) {
        return eventPublisher.publishIncidentUpdated(incident, changedFields, updatedBy)
            .thenReturn(incident)
            .onErrorResume(e -> {
                log.warn("Failed to publish incident update event: {}", e.getMessage());
                return Mono.just(incident);
            });
    }
}
```

---

#### 📄 DeleteIncidentUseCaseImpl.java

```java
package pe.edu.vallegrande.vgmsclaims.application.usecases.incident;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsclaims.domain.exceptions.base.BusinessRuleException;
import pe.edu.vallegrande.vgmsclaims.domain.exceptions.specific.IncidentNotFoundException;
import pe.edu.vallegrande.vgmsclaims.domain.models.Incident;
import pe.edu.vallegrande.vgmsclaims.domain.ports.in.incident.IDeleteIncidentUseCase;
import pe.edu.vallegrande.vgmsclaims.domain.ports.out.IIncidentRepository;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteIncidentUseCaseImpl implements IDeleteIncidentUseCase {

    private final IIncidentRepository incidentRepository;

    @Override
    public Mono<Incident> execute(String id, String deletedBy, String reason) {
        log.info("Deleting incident: {} - Reason: {}", id, reason);

        return incidentRepository.findById(id)
            .switchIfEmpty(Mono.error(new IncidentNotFoundException(id)))
            .flatMap(incident -> {
                if (incident.isInactive()) {
                    return Mono.error(new BusinessRuleException("Incident is already inactive"));
                }

                Incident deleted = incident.markAsDeleted(deletedBy);
                return incidentRepository.update(deleted);
            })
            .doOnSuccess(i -> log.info("Incident deleted: {}", i.getId()))
            .doOnError(e -> log.error("Error deleting incident: {}", e.getMessage()));
    }
}
```

---

#### 📄 RestoreIncidentUseCaseImpl.java

```java
package pe.edu.vallegrande.vgmsclaims.application.usecases.incident;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsclaims.domain.exceptions.base.BusinessRuleException;
import pe.edu.vallegrande.vgmsclaims.domain.exceptions.specific.IncidentNotFoundException;
import pe.edu.vallegrande.vgmsclaims.domain.models.Incident;
import pe.edu.vallegrande.vgmsclaims.domain.ports.in.incident.IRestoreIncidentUseCase;
import pe.edu.vallegrande.vgmsclaims.domain.ports.out.IIncidentRepository;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestoreIncidentUseCaseImpl implements IRestoreIncidentUseCase {

    private final IIncidentRepository incidentRepository;

    @Override
    public Mono<Incident> execute(String id, String restoredBy) {
        log.info("Restoring incident: {}", id);

        return incidentRepository.findById(id)
            .switchIfEmpty(Mono.error(new IncidentNotFoundException(id)))
            .flatMap(incident -> {
                if (incident.isActive()) {
                    return Mono.error(new BusinessRuleException("Incident is already active"));
                }

                Incident restored = incident.restore(restoredBy);
                return incidentRepository.update(restored);
            })
            .doOnSuccess(i -> log.info("Incident restored: {}", i.getId()))
            .doOnError(e -> log.error("Error restoring incident: {}", e.getMessage()));
    }
}
```

---

#### 📄 AssignIncidentUseCaseImpl.java

```java
package pe.edu.vallegrande.vgmsclaims.application.usecases.incident;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsclaims.domain.exceptions.base.BusinessRuleException;
import pe.edu.vallegrande.vgmsclaims.domain.exceptions.specific.IncidentNotFoundException;
import pe.edu.vallegrande.vgmsclaims.domain.models.Incident;
import pe.edu.vallegrande.vgmsclaims.domain.ports.in.incident.IAssignIncidentUseCase;
import pe.edu.vallegrande.vgmsclaims.domain.ports.out.IClaimsEventPublisher;
import pe.edu.vallegrande.vgmsclaims.domain.ports.out.IIncidentRepository;
import pe.edu.vallegrande.vgmsclaims.domain.ports.out.IUserServiceClient;
import pe.edu.vallegrande.vgmsclaims.domain.services.ClaimsAuthorizationService;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class AssignIncidentUseCaseImpl implements IAssignIncidentUseCase {

    private final IIncidentRepository incidentRepository;
    private final IUserServiceClient userServiceClient;
    private final IClaimsEventPublisher eventPublisher;
    private final ClaimsAuthorizationService authorizationService;

    @Override
    public Mono<Incident> execute(String incidentId, String technicianUserId, String assignedBy) {
        log.info("Assigning incident: {} to technician: {} by: {}", incidentId, technicianUserId, assignedBy);

        return authorizationService.validateCanAssign()
            .then(incidentRepository.findById(incidentId))
            .switchIfEmpty(Mono.error(new IncidentNotFoundException(incidentId)))
            .flatMap(incident -> {
                if (!incident.canBeAssigned()) {
                    return Mono.error(new BusinessRuleException(
                        "Incident cannot be assigned in current status: " + incident.getIncidentStatus()
                    ));
                }

                return userServiceClient.existsUser(technicianUserId)
                    .flatMap(exists -> {
                        if (!exists) {
                            return Mono.error(new BusinessRuleException(
                                "Technician user not found: " + technicianUserId
                            ));
                        }

                        Incident assigned = incident.assign(technicianUserId, assignedBy);
                        return incidentRepository.update(assigned);
                    });
            })
            .flatMap(saved -> eventPublisher.publishIncidentAssigned(saved, assignedBy)
                .thenReturn(saved)
                .onErrorResume(e -> {
                    log.warn("Failed to publish incident assigned event: {}", e.getMessage());
                    return Mono.just(saved);
                })
            )
            .doOnSuccess(i -> log.info("Incident assigned: {} to: {}", i.getId(), i.getAssignedToUserId()))
            .doOnError(e -> log.error("Error assigning incident: {}", e.getMessage()));
    }
}
```

---

#### 📄 ResolveIncidentUseCaseImpl.java

```java
package pe.edu.vallegrande.vgmsclaims.application.usecases.incident;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsclaims.domain.exceptions.base.BusinessRuleException;
import pe.edu.vallegrande.vgmsclaims.domain.exceptions.specific.IncidentNotFoundException;
import pe.edu.vallegrande.vgmsclaims.domain.models.Incident;
import pe.edu.vallegrande.vgmsclaims.domain.models.IncidentResolution;
import pe.edu.vallegrande.vgmsclaims.domain.ports.in.incident.IResolveIncidentUseCase;
import pe.edu.vallegrande.vgmsclaims.domain.ports.out.IClaimsEventPublisher;
import pe.edu.vallegrande.vgmsclaims.domain.ports.out.IIncidentRepository;
import pe.edu.vallegrande.vgmsclaims.domain.ports.out.IIncidentResolutionRepository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ResolveIncidentUseCaseImpl implements IResolveIncidentUseCase {

    private final IIncidentRepository incidentRepository;
    private final IIncidentResolutionRepository resolutionRepository;
    private final IClaimsEventPublisher eventPublisher;

    @Override
    public Mono<Incident> execute(String incidentId, IncidentResolution resolution, String resolvedBy) {
        log.info("Resolving incident: {} by user: {}", incidentId, resolvedBy);

        return incidentRepository.findById(incidentId)
            .switchIfEmpty(Mono.error(new IncidentNotFoundException(incidentId)))
            .flatMap(incident -> {
                if (!incident.canBeResolved()) {
                    return Mono.error(new BusinessRuleException(
                        "Incident cannot be resolved in current status: " + incident.getIncidentStatus()
                    ));
                }

                // Calculate total cost from materials
                Double totalCost = resolution.calculateTotalCost();

                IncidentResolution newResolution = resolution.toBuilder()
                    .id(UUID.randomUUID().toString())
                    .incidentId(incidentId)
                    .resolutionDate(LocalDateTime.now())
                    .totalCost(totalCost)
                    .resolvedByUserId(resolvedBy)
                    .createdAt(LocalDateTime.now())
                    .build();

                Incident resolved = incident.resolve(resolvedBy);

                return resolutionRepository.save(newResolution)
                    .then(incidentRepository.update(resolved))
                    .flatMap(saved -> eventPublisher.publishIncidentResolved(saved, newResolution, resolvedBy)
                        .thenReturn(saved)
                        .onErrorResume(e -> {
                            log.warn("Failed to publish incident resolved event: {}", e.getMessage());
                            return Mono.just(saved);
                        })
                    );
            })
            .doOnSuccess(i -> log.info("Incident resolved: {}", i.getId()))
            .doOnError(e -> log.error("Error resolving incident: {}", e.getMessage()));
    }
}
```

---

#### 📄 CloseIncidentUseCaseImpl.java

```java
package pe.edu.vallegrande.vgmsclaims.application.usecases.incident;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsclaims.domain.exceptions.base.BusinessRuleException;
import pe.edu.vallegrande.vgmsclaims.domain.exceptions.specific.IncidentNotFoundException;
import pe.edu.vallegrande.vgmsclaims.domain.models.Incident;
import pe.edu.vallegrande.vgmsclaims.domain.ports.in.incident.ICloseIncidentUseCase;
import pe.edu.vallegrande.vgmsclaims.domain.ports.out.IClaimsEventPublisher;
import pe.edu.vallegrande.vgmsclaims.domain.ports.out.IIncidentRepository;
import pe.edu.vallegrande.vgmsclaims.domain.services.ClaimsAuthorizationService;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloseIncidentUseCaseImpl implements ICloseIncidentUseCase {

    private final IIncidentRepository incidentRepository;
    private final IClaimsEventPublisher eventPublisher;
    private final ClaimsAuthorizationService authorizationService;

    @Override
    public Mono<Incident> execute(String id, String closedBy) {
        log.info("Closing incident: {} by user: {}", id, closedBy);

        return authorizationService.validateCanClose()
            .then(incidentRepository.findById(id))
            .switchIfEmpty(Mono.error(new IncidentNotFoundException(id)))
            .flatMap(incident -> {
                if (incident.isClosed()) {
                    return Mono.error(new BusinessRuleException("Incident is already closed"));
                }
                if (!incident.canBeClosed()) {
                    return Mono.error(new BusinessRuleException(
                        "Incident must be in RESOLVED status to be closed"
                    ));
                }

                Incident closed = incident.close(closedBy);
                return incidentRepository.update(closed)
                    .flatMap(saved -> eventPublisher.publishIncidentClosed(saved.getId(), closedBy)
                        .thenReturn(saved)
                        .onErrorResume(e -> {
                            log.warn("Failed to publish incident close event: {}", e.getMessage());
                            return Mono.just(saved);
                        })
                    );
            })
            .doOnSuccess(i -> log.info("Incident closed: {}", i.getId()))
            .doOnError(e -> log.error("Error closing incident: {}", e.getMessage()));
    }
}
```

---

### 📁 incidenttype/

#### 📄 CreateIncidentTypeUseCaseImpl.java

```java
package pe.edu.vallegrande.vgmsclaims.application.usecases.incidenttype;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsclaims.domain.exceptions.base.ConflictException;
import pe.edu.vallegrande.vgmsclaims.domain.models.IncidentType;
import pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects.RecordStatus;
import pe.edu.vallegrande.vgmsclaims.domain.ports.in.incidenttype.ICreateIncidentTypeUseCase;
import pe.edu.vallegrande.vgmsclaims.domain.ports.out.IIncidentTypeRepository;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateIncidentTypeUseCaseImpl implements ICreateIncidentTypeUseCase {

    private final IIncidentTypeRepository typeRepository;

    @Override
    public Mono<IncidentType> execute(IncidentType type, String createdBy) {
        log.info("Creating incident type: {} for organization: {}", type.getTypeName(), type.getOrganizationId());

        return typeRepository.existsByTypeCodeAndOrganizationId(type.getTypeCode(), type.getOrganizationId())
            .flatMap(exists -> {
                if (exists) {
                    return Mono.error(new ConflictException(
                        String.format("Incident type code '%s' already exists in this organization", type.getTypeCode())
                    ));
                }

                IncidentType newType = type.toBuilder()
                    .id(UUID.randomUUID().toString())
                    .recordStatus(RecordStatus.ACTIVE)
                    .createdAt(LocalDateTime.now())
                    .createdBy(createdBy)
                    .updatedAt(LocalDateTime.now())
                    .updatedBy(createdBy)
                    .build();

                return typeRepository.save(newType);
            })
            .doOnSuccess(t -> log.info("Incident type created: {}", t.getId()))
            .doOnError(e -> log.error("Error creating incident type: {}", e.getMessage()));
    }
}
```

---

#### 📄 GetIncidentTypeUseCaseImpl.java

```java
package pe.edu.vallegrande.vgmsclaims.application.usecases.incidenttype;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsclaims.domain.exceptions.base.NotFoundException;
import pe.edu.vallegrande.vgmsclaims.domain.models.IncidentType;
import pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects.RecordStatus;
import pe.edu.vallegrande.vgmsclaims.domain.ports.in.incidenttype.IGetIncidentTypeUseCase;
import pe.edu.vallegrande.vgmsclaims.domain.ports.out.IIncidentTypeRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetIncidentTypeUseCaseImpl implements IGetIncidentTypeUseCase {

    private final IIncidentTypeRepository typeRepository;

    @Override
    public Mono<IncidentType> findById(String id) {
        log.info("Finding incident type by ID: {}", id);
        return typeRepository.findById(id)
            .switchIfEmpty(Mono.error(new NotFoundException("IncidentType", id)));
    }

    @Override
    public Flux<IncidentType> findAllActive() {
        log.info("Finding all active incident types");
        return typeRepository.findByRecordStatus(RecordStatus.ACTIVE);
    }

    @Override
    public Flux<IncidentType> findAll() {
        log.info("Finding all incident types");
        return typeRepository.findAll();
    }

    @Override
    public Flux<IncidentType> findByOrganizationId(String organizationId) {
        log.info("Finding incident types by organization: {}", organizationId);
        return typeRepository.findByOrganizationId(organizationId);
    }
}
```

---

#### 📄 UpdateIncidentTypeUseCaseImpl.java

```java
package pe.edu.vallegrande.vgmsclaims.application.usecases.incidenttype;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsclaims.domain.exceptions.base.NotFoundException;
import pe.edu.vallegrande.vgmsclaims.domain.models.IncidentType;
import pe.edu.vallegrande.vgmsclaims.domain.ports.in.incidenttype.IUpdateIncidentTypeUseCase;
import pe.edu.vallegrande.vgmsclaims.domain.ports.out.IIncidentTypeRepository;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateIncidentTypeUseCaseImpl implements IUpdateIncidentTypeUseCase {

    private final IIncidentTypeRepository typeRepository;

    @Override
    public Mono<IncidentType> execute(String id, IncidentType changes, String updatedBy) {
        log.info("Updating incident type: {}", id);

        return typeRepository.findById(id)
            .switchIfEmpty(Mono.error(new NotFoundException("IncidentType", id)))
            .flatMap(existing -> {
                Map<String, Object> changedFields = detectChanges(existing, changes);
                if (changedFields.isEmpty()) {
                    log.info("No changes detected for incident type: {}", id);
                    return Mono.just(existing);
                }

                IncidentType updated = existing.updateWith(changes, updatedBy);
                return typeRepository.update(updated);
            })
            .doOnSuccess(t -> log.info("Incident type updated: {}", t.getId()))
            .doOnError(e -> log.error("Error updating incident type: {}", e.getMessage()));
    }

    private Map<String, Object> detectChanges(IncidentType existing, IncidentType changes) {
        Map<String, Object> changedFields = new HashMap<>();
        if (changes.getTypeName() != null && !changes.getTypeName().equals(existing.getTypeName())) {
            changedFields.put("typeName", changes.getTypeName());
        }
        if (changes.getDescription() != null && !changes.getDescription().equals(existing.getDescription())) {
            changedFields.put("description", changes.getDescription());
        }
        return changedFields;
    }
}
```

---

#### 📄 DeleteIncidentTypeUseCaseImpl.java

```java
package pe.edu.vallegrande.vgmsclaims.application.usecases.incidenttype;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsclaims.domain.exceptions.base.BusinessRuleException;
import pe.edu.vallegrande.vgmsclaims.domain.exceptions.base.NotFoundException;
import pe.edu.vallegrande.vgmsclaims.domain.models.IncidentType;
import pe.edu.vallegrande.vgmsclaims.domain.ports.in.incidenttype.IDeleteIncidentTypeUseCase;
import pe.edu.vallegrande.vgmsclaims.domain.ports.out.IIncidentTypeRepository;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteIncidentTypeUseCaseImpl implements IDeleteIncidentTypeUseCase {

    private final IIncidentTypeRepository typeRepository;

    @Override
    public Mono<IncidentType> execute(String id, String deletedBy, String reason) {
        log.info("Deleting incident type: {} - Reason: {}", id, reason);

        return typeRepository.findById(id)
            .switchIfEmpty(Mono.error(new NotFoundException("IncidentType", id)))
            .flatMap(type -> {
                if (type.isInactive()) {
                    return Mono.error(new BusinessRuleException("Incident type is already inactive"));
                }

                IncidentType deleted = type.markAsDeleted(deletedBy);
                return typeRepository.update(deleted);
            })
            .doOnSuccess(t -> log.info("Incident type deleted: {}", t.getId()))
            .doOnError(e -> log.error("Error deleting incident type: {}", e.getMessage()));
    }
}
```

---

#### 📄 RestoreIncidentTypeUseCaseImpl.java

```java
package pe.edu.vallegrande.vgmsclaims.application.usecases.incidenttype;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsclaims.domain.exceptions.base.BusinessRuleException;
import pe.edu.vallegrande.vgmsclaims.domain.exceptions.base.NotFoundException;
import pe.edu.vallegrande.vgmsclaims.domain.models.IncidentType;
import pe.edu.vallegrande.vgmsclaims.domain.ports.in.incidenttype.IRestoreIncidentTypeUseCase;
import pe.edu.vallegrande.vgmsclaims.domain.ports.out.IIncidentTypeRepository;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class RestoreIncidentTypeUseCaseImpl implements IRestoreIncidentTypeUseCase {

    private final IIncidentTypeRepository typeRepository;

    @Override
    public Mono<IncidentType> execute(String id, String restoredBy) {
        log.info("Restoring incident type: {}", id);

        return typeRepository.findById(id)
            .switchIfEmpty(Mono.error(new NotFoundException("IncidentType", id)))
            .flatMap(type -> {
                if (type.isActive()) {
                    return Mono.error(new BusinessRuleException("Incident type is already active"));
                }

                IncidentType restored = type.restore(restoredBy);
                return typeRepository.update(restored);
            })
            .doOnSuccess(t -> log.info("Incident type restored: {}", t.getId()))
            .doOnError(e -> log.error("Error restoring incident type: {}", e.getMessage()));
    }
}
```

---

## 2️⃣ DTOs - Data Transfer Objects

### 📁 dto/common/

#### 📄 ApiResponse.java

```java
package pe.edu.vallegrande.vgmsclaims.application.dto.common;

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
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private ErrorMessage error;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
            .success(true)
            .message("Operation successful")
            .data(data)
            .build();
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
            .success(true)
            .message(message)
            .data(data)
            .build();
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder()
            .success(false)
            .error(new ErrorMessage(code, message))
            .build();
    }

    public static <T> ApiResponse<T> error(String code, String message, String detail) {
        return ApiResponse.<T>builder()
            .success(false)
            .error(new ErrorMessage(code, message, detail))
            .build();
    }
}
```

---

#### 📄 ErrorMessage.java

```java
package pe.edu.vallegrande.vgmsclaims.application.dto.common;

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

    private String code;
    private String message;
    private String detail;

    public ErrorMessage(String code, String message) {
        this.code = code;
        this.message = message;
    }
}
```

---

#### 📄 PageResponse.java

```java
package pe.edu.vallegrande.vgmsclaims.application.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
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

### 📁 dto/complaint/

#### 📄 CreateComplaintRequest.java

```java
package pe.edu.vallegrande.vgmsclaims.application.dto.complaint;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateComplaintRequest {

    @NotBlank(message = "Organization ID is required")
    private String organizationId;

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Category ID is required")
    private String categoryId;

    @NotBlank(message = "Priority is required")
    @Pattern(regexp = "^(LOW|MEDIUM|HIGH|URGENT)$", message = "Priority must be LOW, MEDIUM, HIGH or URGENT")
    private String complaintPriority;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 1000, message = "Description must be between 10 and 1000 characters")
    private String description;
}
```

---

#### 📄 UpdateComplaintRequest.java

```java
package pe.edu.vallegrande.vgmsclaims.application.dto.complaint;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateComplaintRequest {

    private String categoryId;

    @Pattern(regexp = "^(LOW|MEDIUM|HIGH|URGENT)$", message = "Priority must be LOW, MEDIUM, HIGH or URGENT")
    private String complaintPriority;

    @Size(min = 10, max = 1000, message = "Description must be between 10 and 1000 characters")
    private String description;

    private String assignedToUserId;
}
```

---

#### 📄 AddComplaintResponseRequest.java

```java
package pe.edu.vallegrande.vgmsclaims.application.dto.complaint;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddComplaintResponseRequest {

    @NotBlank(message = "Response type is required")
    @Pattern(regexp = "^(INVESTIGACION|SOLUCION|SEGUIMIENTO)$", message = "Response type must be INVESTIGACION, SOLUCION or SEGUIMIENTO")
    private String responseType;

    @NotBlank(message = "Response description is required")
    @Size(min = 10, max = 1000, message = "Description must be between 10 and 1000 characters")
    private String responseDescription;
}
```

---

#### 📄 ComplaintResponseDTO.java

> **Nota**: Se usa `ComplaintResponseDTO` para el DTO de respuesta de la entidad `Complaint`, para evitar conflicto de nombre con la entidad `ComplaintResponse`.

```java
package pe.edu.vallegrande.vgmsclaims.application.dto.complaint;

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
public class ComplaintResponseDTO {
    private String id;
    private String organizationId;
    private String complaintCode;
    private String userId;
    private String categoryId;
    private LocalDateTime complaintDate;
    private String complaintPriority;
    private String complaintPriorityDisplayName;
    private String complaintStatus;
    private String complaintStatusDisplayName;
    private String description;
    private String assignedToUserId;
    private String recordStatus;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
```

---

#### 📄 ComplaintResponseItemDTO.java

> DTO para la sub-entidad `ComplaintResponse` (respuestas a un reclamo).

```java
package pe.edu.vallegrande.vgmsclaims.application.dto.complaint;

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
public class ComplaintResponseItemDTO {
    private String id;
    private String complaintId;
    private LocalDateTime responseDate;
    private String responseType;
    private String responseTypeDisplayName;
    private String responseDescription;
    private String respondedByUserId;
    private LocalDateTime createdAt;
}
```

---

### 📁 dto/complaintcategory/

#### 📄 CreateComplaintCategoryRequest.java

```java
package pe.edu.vallegrande.vgmsclaims.application.dto.complaintcategory;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateComplaintCategoryRequest {

    @NotBlank(message = "Organization ID is required")
    private String organizationId;

    @NotBlank(message = "Category code is required")
    @Pattern(regexp = "^[A-Z0-9_]{2,30}$", message = "Category code must be uppercase alphanumeric with underscores")
    private String categoryCode;

    @NotBlank(message = "Category name is required")
    @Size(min = 3, max = 100, message = "Category name must be between 3 and 100 characters")
    private String categoryName;

    @Size(max = 250, message = "Description cannot exceed 250 characters")
    private String description;
}
```

---

#### 📄 UpdateComplaintCategoryRequest.java

```java
package pe.edu.vallegrande.vgmsclaims.application.dto.complaintcategory;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateComplaintCategoryRequest {

    @Size(min = 3, max = 100, message = "Category name must be between 3 and 100 characters")
    private String categoryName;

    @Size(max = 250, message = "Description cannot exceed 250 characters")
    private String description;
}
```

---

#### 📄 ComplaintCategoryResponse.java

```java
package pe.edu.vallegrande.vgmsclaims.application.dto.complaintcategory;

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
public class ComplaintCategoryResponse {
    private String id;
    private String organizationId;
    private String categoryCode;
    private String categoryName;
    private String description;
    private String recordStatus;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
```

---

### 📁 dto/incident/

#### 📄 CreateIncidentRequest.java

```java
package pe.edu.vallegrande.vgmsclaims.application.dto.incident;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateIncidentRequest {

    @NotBlank(message = "Organization ID is required")
    private String organizationId;

    @NotBlank(message = "Incident type ID is required")
    private String incidentTypeId;

    @NotBlank(message = "Location is required")
    @Size(max = 250, message = "Location cannot exceed 250 characters")
    private String location;

    private String zoneId;

    private String streetId;

    @NotBlank(message = "Severity is required")
    @Pattern(regexp = "^(LOW|MEDIUM|HIGH|CRITICAL)$", message = "Severity must be LOW, MEDIUM, HIGH or CRITICAL")
    private String incidentSeverity;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 1000, message = "Description must be between 10 and 1000 characters")
    private String description;

    @NotBlank(message = "Reported by user ID is required")
    private String reportedByUserId;
}
```

---

#### 📄 UpdateIncidentRequest.java

```java
package pe.edu.vallegrande.vgmsclaims.application.dto.incident;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateIncidentRequest {

    private String incidentTypeId;

    @Size(max = 250, message = "Location cannot exceed 250 characters")
    private String location;

    private String zoneId;

    private String streetId;

    @Pattern(regexp = "^(LOW|MEDIUM|HIGH|CRITICAL)$", message = "Severity must be LOW, MEDIUM, HIGH or CRITICAL")
    private String incidentSeverity;

    @Size(min = 10, max = 1000, message = "Description must be between 10 and 1000 characters")
    private String description;
}
```

---

#### 📄 AssignIncidentRequest.java

```java
package pe.edu.vallegrande.vgmsclaims.application.dto.incident;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignIncidentRequest {

    @NotBlank(message = "Technician user ID is required")
    private String technicianUserId;
}
```

---

#### 📄 ResolveIncidentRequest.java

```java
package pe.edu.vallegrande.vgmsclaims.application.dto.incident;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResolveIncidentRequest {

    @NotBlank(message = "Resolution type is required")
    @Pattern(regexp = "^(REPARACION_TEMPORAL|REPARACION_COMPLETA|REEMPLAZO)$",
        message = "Resolution type must be REPARACION_TEMPORAL, REPARACION_COMPLETA or REEMPLAZO")
    private String resolutionType;

    @NotBlank(message = "Resolution description is required")
    @Size(min = 10, max = 1000, message = "Description must be between 10 and 1000 characters")
    private String resolutionDescription;

    private List<MaterialUsedRequest> materialsUsed;
}
```

---

#### 📄 MaterialUsedRequest.java

```java
package pe.edu.vallegrande.vgmsclaims.application.dto.incident;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialUsedRequest {

    @NotBlank(message = "Material ID is required")
    private String materialId;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than 0")
    private Double quantity;

    @NotBlank(message = "Unit is required")
    private String unit;

    @NotNull(message = "Unit cost is required")
    @Positive(message = "Unit cost must be greater than 0")
    private Double unitCost;
}
```

---

#### 📄 IncidentResponseDTO.java

```java
package pe.edu.vallegrande.vgmsclaims.application.dto.incident;

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
public class IncidentResponseDTO {
    private String id;
    private String organizationId;
    private String incidentCode;
    private String incidentTypeId;
    private LocalDateTime incidentDate;
    private String location;
    private String zoneId;
    private String streetId;
    private String incidentSeverity;
    private String incidentSeverityDisplayName;
    private String incidentStatus;
    private String incidentStatusDisplayName;
    private String description;
    private String reportedByUserId;
    private String assignedToUserId;
    private String recordStatus;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
```

---

#### 📄 IncidentResolutionResponseDTO.java

```java
package pe.edu.vallegrande.vgmsclaims.application.dto.incident;

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
public class IncidentResolutionResponseDTO {
    private String id;
    private String incidentId;
    private LocalDateTime resolutionDate;
    private String resolutionType;
    private String resolutionTypeDisplayName;
    private String resolutionDescription;
    private List<MaterialUsedResponseDTO> materialsUsed;
    private Double totalCost;
    private String resolvedByUserId;
    private LocalDateTime createdAt;
}
```

---

#### 📄 MaterialUsedResponseDTO.java

```java
package pe.edu.vallegrande.vgmsclaims.application.dto.incident;

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
public class MaterialUsedResponseDTO {
    private String materialId;
    private Double quantity;
    private String unit;
    private Double unitCost;
    private Double subtotal;
}
```

---

### 📁 dto/incidenttype/

#### 📄 CreateIncidentTypeRequest.java

```java
package pe.edu.vallegrande.vgmsclaims.application.dto.incidenttype;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateIncidentTypeRequest {

    @NotBlank(message = "Organization ID is required")
    private String organizationId;

    @NotBlank(message = "Type code is required")
    @Pattern(regexp = "^[A-Z0-9_]{2,30}$", message = "Type code must be uppercase alphanumeric with underscores")
    private String typeCode;

    @NotBlank(message = "Type name is required")
    @Size(min = 3, max = 100, message = "Type name must be between 3 and 100 characters")
    private String typeName;

    @Size(max = 250, message = "Description cannot exceed 250 characters")
    private String description;
}
```

---

#### 📄 UpdateIncidentTypeRequest.java

```java
package pe.edu.vallegrande.vgmsclaims.application.dto.incidenttype;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateIncidentTypeRequest {

    @Size(min = 3, max = 100, message = "Type name must be between 3 and 100 characters")
    private String typeName;

    @Size(max = 250, message = "Description cannot exceed 250 characters")
    private String description;
}
```

---

#### 📄 IncidentTypeResponse.java

```java
package pe.edu.vallegrande.vgmsclaims.application.dto.incidenttype;

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
public class IncidentTypeResponse {
    private String id;
    private String organizationId;
    private String typeCode;
    private String typeName;
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

#### 📄 ComplaintMapper.java

```java
package pe.edu.vallegrande.vgmsclaims.application.mappers;

import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vgmsclaims.application.dto.complaint.*;
import pe.edu.vallegrande.vgmsclaims.domain.models.Complaint;
import pe.edu.vallegrande.vgmsclaims.domain.models.ComplaintResponse;
import pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects.ComplaintPriority;
import pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects.RecordStatus;
import pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects.ResponseType;
import pe.edu.vallegrande.vgmsclaims.infrastructure.persistence.documents.ComplaintDocument;
import pe.edu.vallegrande.vgmsclaims.infrastructure.persistence.documents.ComplaintResponseDocument;

@Component
public class ComplaintMapper {

    // ─── Request → Domain Model ───

    public Complaint toModel(CreateComplaintRequest request) {
        return Complaint.builder()
            .organizationId(request.getOrganizationId())
            .userId(request.getUserId())
            .categoryId(request.getCategoryId())
            .complaintPriority(ComplaintPriority.valueOf(request.getComplaintPriority()))
            .description(request.getDescription())
            .build();
    }

    public Complaint toModel(UpdateComplaintRequest request) {
        return Complaint.builder()
            .categoryId(request.getCategoryId())
            .complaintPriority(request.getComplaintPriority() != null
                ? ComplaintPriority.valueOf(request.getComplaintPriority()) : null)
            .description(request.getDescription())
            .assignedToUserId(request.getAssignedToUserId())
            .build();
    }

    public ComplaintResponse toResponseModel(AddComplaintResponseRequest request) {
        return ComplaintResponse.builder()
            .responseType(ResponseType.valueOf(request.getResponseType()))
            .responseDescription(request.getResponseDescription())
            .build();
    }

    // ─── Domain Model → Response DTO ───

    public ComplaintResponseDTO toResponseDTO(Complaint complaint) {
        return ComplaintResponseDTO.builder()
            .id(complaint.getId())
            .organizationId(complaint.getOrganizationId())
            .complaintCode(complaint.getComplaintCode())
            .userId(complaint.getUserId())
            .categoryId(complaint.getCategoryId())
            .complaintDate(complaint.getComplaintDate())
            .complaintPriority(complaint.getComplaintPriority().name())
            .complaintPriorityDisplayName(complaint.getComplaintPriority().getDisplayName())
            .complaintStatus(complaint.getComplaintStatus().name())
            .complaintStatusDisplayName(complaint.getComplaintStatus().getDisplayName())
            .description(complaint.getDescription())
            .assignedToUserId(complaint.getAssignedToUserId())
            .recordStatus(complaint.getRecordStatus().name())
            .createdAt(complaint.getCreatedAt())
            .createdBy(complaint.getCreatedBy())
            .updatedAt(complaint.getUpdatedAt())
            .updatedBy(complaint.getUpdatedBy())
            .build();
    }

    public ComplaintResponseItemDTO toResponseItemDTO(ComplaintResponse response) {
        return ComplaintResponseItemDTO.builder()
            .id(response.getId())
            .complaintId(response.getComplaintId())
            .responseDate(response.getResponseDate())
            .responseType(response.getResponseType().name())
            .responseTypeDisplayName(response.getResponseType().getDisplayName())
            .responseDescription(response.getResponseDescription())
            .respondedByUserId(response.getRespondedByUserId())
            .createdAt(response.getCreatedAt())
            .build();
    }

    // ─── Domain Model → Document ───

    public ComplaintDocument toDocument(Complaint complaint) {
        return ComplaintDocument.builder()
            .id(complaint.getId())
            .organizationId(complaint.getOrganizationId())
            .complaintCode(complaint.getComplaintCode())
            .userId(complaint.getUserId())
            .categoryId(complaint.getCategoryId())
            .complaintDate(complaint.getComplaintDate())
            .complaintPriority(complaint.getComplaintPriority().name())
            .complaintStatus(complaint.getComplaintStatus().name())
            .description(complaint.getDescription())
            .assignedToUserId(complaint.getAssignedToUserId())
            .recordStatus(complaint.getRecordStatus().name())
            .createdAt(complaint.getCreatedAt())
            .createdBy(complaint.getCreatedBy())
            .updatedAt(complaint.getUpdatedAt())
            .updatedBy(complaint.getUpdatedBy())
            .build();
    }

    public ComplaintResponseDocument toDocument(ComplaintResponse response) {
        return ComplaintResponseDocument.builder()
            .id(response.getId())
            .complaintId(response.getComplaintId())
            .responseDate(response.getResponseDate())
            .responseType(response.getResponseType().name())
            .responseDescription(response.getResponseDescription())
            .respondedByUserId(response.getRespondedByUserId())
            .createdAt(response.getCreatedAt())
            .build();
    }

    // ─── Document → Domain Model ───

    public Complaint toModel(ComplaintDocument doc) {
        return Complaint.builder()
            .id(doc.getId())
            .organizationId(doc.getOrganizationId())
            .complaintCode(doc.getComplaintCode())
            .userId(doc.getUserId())
            .categoryId(doc.getCategoryId())
            .complaintDate(doc.getComplaintDate())
            .complaintPriority(ComplaintPriority.valueOf(doc.getComplaintPriority()))
            .complaintStatus(pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects.ComplaintStatus.valueOf(doc.getComplaintStatus()))
            .description(doc.getDescription())
            .assignedToUserId(doc.getAssignedToUserId())
            .recordStatus(RecordStatus.valueOf(doc.getRecordStatus()))
            .createdAt(doc.getCreatedAt())
            .createdBy(doc.getCreatedBy())
            .updatedAt(doc.getUpdatedAt())
            .updatedBy(doc.getUpdatedBy())
            .build();
    }

    public ComplaintResponse toModel(ComplaintResponseDocument doc) {
        return ComplaintResponse.builder()
            .id(doc.getId())
            .complaintId(doc.getComplaintId())
            .responseDate(doc.getResponseDate())
            .responseType(ResponseType.valueOf(doc.getResponseType()))
            .responseDescription(doc.getResponseDescription())
            .respondedByUserId(doc.getRespondedByUserId())
            .createdAt(doc.getCreatedAt())
            .build();
    }
}
```

---

#### 📄 ComplaintCategoryMapper.java

```java
package pe.edu.vallegrande.vgmsclaims.application.mappers;

import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vgmsclaims.application.dto.complaintcategory.*;
import pe.edu.vallegrande.vgmsclaims.domain.models.ComplaintCategory;
import pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects.RecordStatus;
import pe.edu.vallegrande.vgmsclaims.infrastructure.persistence.documents.ComplaintCategoryDocument;

@Component
public class ComplaintCategoryMapper {

    public ComplaintCategory toModel(CreateComplaintCategoryRequest request) {
        return ComplaintCategory.builder()
            .organizationId(request.getOrganizationId())
            .categoryCode(request.getCategoryCode())
            .categoryName(request.getCategoryName())
            .description(request.getDescription())
            .build();
    }

    public ComplaintCategory toModel(UpdateComplaintCategoryRequest request) {
        return ComplaintCategory.builder()
            .categoryName(request.getCategoryName())
            .description(request.getDescription())
            .build();
    }

    public ComplaintCategoryResponse toResponse(ComplaintCategory category) {
        return ComplaintCategoryResponse.builder()
            .id(category.getId())
            .organizationId(category.getOrganizationId())
            .categoryCode(category.getCategoryCode())
            .categoryName(category.getCategoryName())
            .description(category.getDescription())
            .recordStatus(category.getRecordStatus().name())
            .createdAt(category.getCreatedAt())
            .createdBy(category.getCreatedBy())
            .updatedAt(category.getUpdatedAt())
            .updatedBy(category.getUpdatedBy())
            .build();
    }

    public ComplaintCategoryDocument toDocument(ComplaintCategory category) {
        return ComplaintCategoryDocument.builder()
            .id(category.getId())
            .organizationId(category.getOrganizationId())
            .categoryCode(category.getCategoryCode())
            .categoryName(category.getCategoryName())
            .description(category.getDescription())
            .recordStatus(category.getRecordStatus().name())
            .createdAt(category.getCreatedAt())
            .createdBy(category.getCreatedBy())
            .updatedAt(category.getUpdatedAt())
            .updatedBy(category.getUpdatedBy())
            .build();
    }

    public ComplaintCategory toModel(ComplaintCategoryDocument doc) {
        return ComplaintCategory.builder()
            .id(doc.getId())
            .organizationId(doc.getOrganizationId())
            .categoryCode(doc.getCategoryCode())
            .categoryName(doc.getCategoryName())
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

#### 📄 IncidentMapper.java

```java
package pe.edu.vallegrande.vgmsclaims.application.mappers;

import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vgmsclaims.application.dto.incident.*;
import pe.edu.vallegrande.vgmsclaims.domain.models.Incident;
import pe.edu.vallegrande.vgmsclaims.domain.models.IncidentResolution;
import pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects.*;
import pe.edu.vallegrande.vgmsclaims.infrastructure.persistence.documents.IncidentDocument;
import pe.edu.vallegrande.vgmsclaims.infrastructure.persistence.documents.IncidentResolutionDocument;

import java.util.Collections;
import java.util.stream.Collectors;

@Component
public class IncidentMapper {

    // ─── Request → Domain Model ───

    public Incident toModel(CreateIncidentRequest request) {
        return Incident.builder()
            .organizationId(request.getOrganizationId())
            .incidentTypeId(request.getIncidentTypeId())
            .location(request.getLocation())
            .zoneId(request.getZoneId())
            .streetId(request.getStreetId())
            .incidentSeverity(IncidentSeverity.valueOf(request.getIncidentSeverity()))
            .description(request.getDescription())
            .reportedByUserId(request.getReportedByUserId())
            .build();
    }

    public Incident toModel(UpdateIncidentRequest request) {
        return Incident.builder()
            .incidentTypeId(request.getIncidentTypeId())
            .location(request.getLocation())
            .zoneId(request.getZoneId())
            .streetId(request.getStreetId())
            .incidentSeverity(request.getIncidentSeverity() != null
                ? IncidentSeverity.valueOf(request.getIncidentSeverity()) : null)
            .description(request.getDescription())
            .build();
    }

    public IncidentResolution toResolutionModel(ResolveIncidentRequest request) {
        var materials = request.getMaterialsUsed() != null
            ? request.getMaterialsUsed().stream()
                .map(m -> MaterialUsed.builder()
                    .materialId(m.getMaterialId())
                    .quantity(m.getQuantity())
                    .unit(m.getUnit())
                    .unitCost(m.getUnitCost())
                    .build())
                .collect(Collectors.toList())
            : Collections.<MaterialUsed>emptyList();

        return IncidentResolution.builder()
            .resolutionType(ResolutionType.valueOf(request.getResolutionType()))
            .resolutionDescription(request.getResolutionDescription())
            .materialsUsed(materials)
            .build();
    }

    // ─── Domain Model → Response DTO ───

    public IncidentResponseDTO toResponseDTO(Incident incident) {
        return IncidentResponseDTO.builder()
            .id(incident.getId())
            .organizationId(incident.getOrganizationId())
            .incidentCode(incident.getIncidentCode())
            .incidentTypeId(incident.getIncidentTypeId())
            .incidentDate(incident.getIncidentDate())
            .location(incident.getLocation())
            .zoneId(incident.getZoneId())
            .streetId(incident.getStreetId())
            .incidentSeverity(incident.getIncidentSeverity().name())
            .incidentSeverityDisplayName(incident.getIncidentSeverity().getDisplayName())
            .incidentStatus(incident.getIncidentStatus().name())
            .incidentStatusDisplayName(incident.getIncidentStatus().getDisplayName())
            .description(incident.getDescription())
            .reportedByUserId(incident.getReportedByUserId())
            .assignedToUserId(incident.getAssignedToUserId())
            .recordStatus(incident.getRecordStatus().name())
            .createdAt(incident.getCreatedAt())
            .createdBy(incident.getCreatedBy())
            .updatedAt(incident.getUpdatedAt())
            .updatedBy(incident.getUpdatedBy())
            .build();
    }

    public IncidentResolutionResponseDTO toResolutionResponseDTO(IncidentResolution resolution) {
        var materialsDTO = resolution.getMaterialsUsed() != null
            ? resolution.getMaterialsUsed().stream()
                .map(m -> MaterialUsedResponseDTO.builder()
                    .materialId(m.getMaterialId())
                    .quantity(m.getQuantity())
                    .unit(m.getUnit())
                    .unitCost(m.getUnitCost())
                    .subtotal(m.getSubtotal())
                    .build())
                .collect(Collectors.toList())
            : Collections.<MaterialUsedResponseDTO>emptyList();

        return IncidentResolutionResponseDTO.builder()
            .id(resolution.getId())
            .incidentId(resolution.getIncidentId())
            .resolutionDate(resolution.getResolutionDate())
            .resolutionType(resolution.getResolutionType().name())
            .resolutionTypeDisplayName(resolution.getResolutionType().getDisplayName())
            .resolutionDescription(resolution.getResolutionDescription())
            .materialsUsed(materialsDTO)
            .totalCost(resolution.getTotalCost())
            .resolvedByUserId(resolution.getResolvedByUserId())
            .createdAt(resolution.getCreatedAt())
            .build();
    }

    // ─── Domain Model → Document ───

    public IncidentDocument toDocument(Incident incident) {
        return IncidentDocument.builder()
            .id(incident.getId())
            .organizationId(incident.getOrganizationId())
            .incidentCode(incident.getIncidentCode())
            .incidentTypeId(incident.getIncidentTypeId())
            .incidentDate(incident.getIncidentDate())
            .location(incident.getLocation())
            .zoneId(incident.getZoneId())
            .streetId(incident.getStreetId())
            .incidentSeverity(incident.getIncidentSeverity().name())
            .incidentStatus(incident.getIncidentStatus().name())
            .description(incident.getDescription())
            .reportedByUserId(incident.getReportedByUserId())
            .assignedToUserId(incident.getAssignedToUserId())
            .recordStatus(incident.getRecordStatus().name())
            .createdAt(incident.getCreatedAt())
            .createdBy(incident.getCreatedBy())
            .updatedAt(incident.getUpdatedAt())
            .updatedBy(incident.getUpdatedBy())
            .build();
    }

    // ─── Document → Domain Model ───

    public Incident toModel(IncidentDocument doc) {
        return Incident.builder()
            .id(doc.getId())
            .organizationId(doc.getOrganizationId())
            .incidentCode(doc.getIncidentCode())
            .incidentTypeId(doc.getIncidentTypeId())
            .incidentDate(doc.getIncidentDate())
            .location(doc.getLocation())
            .zoneId(doc.getZoneId())
            .streetId(doc.getStreetId())
            .incidentSeverity(IncidentSeverity.valueOf(doc.getIncidentSeverity()))
            .incidentStatus(IncidentStatus.valueOf(doc.getIncidentStatus()))
            .description(doc.getDescription())
            .reportedByUserId(doc.getReportedByUserId())
            .assignedToUserId(doc.getAssignedToUserId())
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

#### 📄 IncidentTypeMapper.java

```java
package pe.edu.vallegrande.vgmsclaims.application.mappers;

import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vgmsclaims.application.dto.incidenttype.*;
import pe.edu.vallegrande.vgmsclaims.domain.models.IncidentType;
import pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects.RecordStatus;
import pe.edu.vallegrande.vgmsclaims.infrastructure.persistence.documents.IncidentTypeDocument;

@Component
public class IncidentTypeMapper {

    public IncidentType toModel(CreateIncidentTypeRequest request) {
        return IncidentType.builder()
            .organizationId(request.getOrganizationId())
            .typeCode(request.getTypeCode())
            .typeName(request.getTypeName())
            .description(request.getDescription())
            .build();
    }

    public IncidentType toModel(UpdateIncidentTypeRequest request) {
        return IncidentType.builder()
            .typeName(request.getTypeName())
            .description(request.getDescription())
            .build();
    }

    public IncidentTypeResponse toResponse(IncidentType type) {
        return IncidentTypeResponse.builder()
            .id(type.getId())
            .organizationId(type.getOrganizationId())
            .typeCode(type.getTypeCode())
            .typeName(type.getTypeName())
            .description(type.getDescription())
            .recordStatus(type.getRecordStatus().name())
            .createdAt(type.getCreatedAt())
            .createdBy(type.getCreatedBy())
            .updatedAt(type.getUpdatedAt())
            .updatedBy(type.getUpdatedBy())
            .build();
    }

    public IncidentTypeDocument toDocument(IncidentType type) {
        return IncidentTypeDocument.builder()
            .id(type.getId())
            .organizationId(type.getOrganizationId())
            .typeCode(type.getTypeCode())
            .typeName(type.getTypeName())
            .description(type.getDescription())
            .recordStatus(type.getRecordStatus().name())
            .createdAt(type.getCreatedAt())
            .createdBy(type.getCreatedBy())
            .updatedAt(type.getUpdatedAt())
            .updatedBy(type.getUpdatedBy())
            .build();
    }

    public IncidentType toModel(IncidentTypeDocument doc) {
        return IncidentType.builder()
            .id(doc.getId())
            .organizationId(doc.getOrganizationId())
            .typeCode(doc.getTypeCode())
            .typeName(doc.getTypeName())
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

### 📁 events/complaint/

#### 📄 ComplaintCreatedEvent.java

```java
package pe.edu.vallegrande.vgmsclaims.application.events.complaint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintCreatedEvent {
    @Builder.Default
    private String eventType = "COMPLAINT_CREATED";
    private String eventId;
    private LocalDateTime timestamp;
    private String complaintId;
    private String organizationId;
    private String complaintCode;
    private String userId;
    private String categoryId;
    private String complaintPriority;
    private String description;
    private String createdBy;
    private String correlationId;
}
```

---

#### 📄 ComplaintUpdatedEvent.java

```java
package pe.edu.vallegrande.vgmsclaims.application.events.complaint;

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
public class ComplaintUpdatedEvent {
    @Builder.Default
    private String eventType = "COMPLAINT_UPDATED";
    private String eventId;
    private LocalDateTime timestamp;
    private String complaintId;
    private String organizationId;
    private Map<String, Object> changedFields;
    private String updatedBy;
    private String correlationId;
}
```

---

#### 📄 ComplaintClosedEvent.java

```java
package pe.edu.vallegrande.vgmsclaims.application.events.complaint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintClosedEvent {
    @Builder.Default
    private String eventType = "COMPLAINT_CLOSED";
    private String eventId;
    private LocalDateTime timestamp;
    private String complaintId;
    private String closedBy;
    private String correlationId;
}
```

---

#### 📄 ComplaintResponseAddedEvent.java

```java
package pe.edu.vallegrande.vgmsclaims.application.events.complaint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplaintResponseAddedEvent {
    @Builder.Default
    private String eventType = "COMPLAINT_RESPONSE_ADDED";
    private String eventId;
    private LocalDateTime timestamp;
    private String responseId;
    private String complaintId;
    private String responseType;
    private String respondedBy;
    private String correlationId;
}
```

---

### 📁 events/incident/

#### 📄 IncidentCreatedEvent.java

```java
package pe.edu.vallegrande.vgmsclaims.application.events.incident;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentCreatedEvent {
    @Builder.Default
    private String eventType = "INCIDENT_CREATED";
    private String eventId;
    private LocalDateTime timestamp;
    private String incidentId;
    private String organizationId;
    private String incidentCode;
    private String incidentTypeId;
    private String incidentSeverity;
    private String location;
    private String reportedByUserId;
    private String createdBy;
    private String correlationId;
}
```

---

#### 📄 IncidentAssignedEvent.java

```java
package pe.edu.vallegrande.vgmsclaims.application.events.incident;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentAssignedEvent {
    @Builder.Default
    private String eventType = "INCIDENT_ASSIGNED";
    private String eventId;
    private LocalDateTime timestamp;
    private String incidentId;
    private String organizationId;
    private String assignedToUserId;
    private String assignedBy;
    private String correlationId;
}
```

---

#### 📄 IncidentUpdatedEvent.java

```java
package pe.edu.vallegrande.vgmsclaims.application.events.incident;

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
public class IncidentUpdatedEvent {
    @Builder.Default
    private String eventType = "INCIDENT_UPDATED";
    private String eventId;
    private LocalDateTime timestamp;
    private String incidentId;
    private String organizationId;
    private Map<String, Object> changedFields;
    private String updatedBy;
    private String correlationId;
}
```

---

#### 📄 IncidentResolvedEvent.java

```java
package pe.edu.vallegrande.vgmsclaims.application.events.incident;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentResolvedEvent {
    @Builder.Default
    private String eventType = "INCIDENT_RESOLVED";
    private String eventId;
    private LocalDateTime timestamp;
    private String incidentId;
    private String organizationId;
    private String resolutionId;
    private String resolutionType;
    private Double totalCost;
    private String resolvedBy;
    private String correlationId;
}
```

---

#### 📄 IncidentClosedEvent.java

```java
package pe.edu.vallegrande.vgmsclaims.application.events.incident;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentClosedEvent {
    @Builder.Default
    private String eventType = "INCIDENT_CLOSED";
    private String eventId;
    private LocalDateTime timestamp;
    private String incidentId;
    private String closedBy;
    private String correlationId;
}
```

---

#### 📄 UrgentIncidentAlertEvent.java

```java
package pe.edu.vallegrande.vgmsclaims.application.events.incident;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UrgentIncidentAlertEvent {
    @Builder.Default
    private String eventType = "URGENT_INCIDENT_ALERT";
    private String eventId;
    private LocalDateTime timestamp;
    private String incidentId;
    private String organizationId;
    private String incidentCode;
    private String incidentSeverity;
    private String location;
    private String description;
    private String reportedByUserId;
    private String createdBy;
    private String correlationId;
}
```

---

## ✅ Resumen de la Capa de Aplicación

| Componente | Cantidad | Descripción |
|------------|----------|-------------|
| Use Cases | 25 clases | 7 complaint, 5 complaint-category, 8 incident, 5 incident-type |
| DTOs Common | 3 clases | ApiResponse, ErrorMessage, PageResponse |
| DTOs Request | 10 clases | Complaint(3), ComplaintCategory(2), Incident(4), IncidentType(2) |
| DTOs Response | 8 clases | Complaint(2), ComplaintCategory(1), Incident(3), IncidentType(1), MaterialUsed(1) |
| Mappers | 4 clases | ComplaintMapper, ComplaintCategoryMapper, IncidentMapper, IncidentTypeMapper |
| Events | 10 clases | Complaint(4), Incident(6) |

---

> **Siguiente paso**: Lee [README_INFRASTRUCTURE.md](README_INFRASTRUCTURE.md) para ver la implementación de la capa de infraestructura.
