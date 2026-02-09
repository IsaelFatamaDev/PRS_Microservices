# 🧩 DOMAIN LAYER - Capa de Dominio

> **El corazón del microservicio. Sin dependencias externas.**

## 📋 Principios

1. **Cero dependencias de frameworks**: No importa Spring, MongoDB, RabbitMQ
2. **Modelos ricos**: Lógica de negocio en los modelos, no en servicios anémicos
3. **Puertos como contratos**: Interfaces que definen QUÉ se necesita, no CÓMO
4. **Excepciones tipadas**: Cada error tiene su excepción específica

---

## 📂 Estructura

```text
domain/
├── models/                                         # 📦 Modelos de negocio
│   ├── Complaint.java                              #    └─ Model (Class)
│   ├── ComplaintCategory.java                      #    └─ Model (Class)
│   ├── ComplaintResponse.java                      #    └─ Model (Class)
│   ├── Incident.java                               #    └─ Model (Class)
│   ├── IncidentType.java                           #    └─ Model (Class)
│   ├── IncidentResolution.java                     #    └─ Model (Class)
│   └── valueobjects/                               # 🏷️ Value Objects
│       ├── RecordStatus.java                       #    └─ Enum (ACTIVE/INACTIVE)
│       ├── ComplaintPriority.java                  #    └─ Enum
│       ├── ComplaintStatus.java                    #    └─ Enum
│       ├── ResponseType.java                       #    └─ Enum
│       ├── IncidentSeverity.java                   #    └─ Enum
│       ├── IncidentStatus.java                     #    └─ Enum
│       ├── ResolutionType.java                     #    └─ Enum
│       └── MaterialUsed.java                       #    └─ Value Object (Embedded)
│
├── ports/                                          # 🔌 Puertos (Contratos)
│   ├── in/                                         # ⬅️ Puertos de ENTRADA (Use Cases)
│   │   ├── complaint/
│   │   │   ├── ICreateComplaintUseCase.java
│   │   │   ├── IGetComplaintUseCase.java
│   │   │   ├── IUpdateComplaintUseCase.java
│   │   │   ├── IDeleteComplaintUseCase.java
│   │   │   ├── IRestoreComplaintUseCase.java
│   │   │   ├── IAddResponseUseCase.java
│   │   │   └── ICloseComplaintUseCase.java
│   │   ├── complaint-category/
│   │   │   ├── ICreateComplaintCategoryUseCase.java
│   │   │   ├── IGetComplaintCategoryUseCase.java
│   │   │   ├── IUpdateComplaintCategoryUseCase.java
│   │   │   ├── IDeleteComplaintCategoryUseCase.java
│   │   │   └── IRestoreComplaintCategoryUseCase.java
│   │   ├── incident/
│   │   │   ├── ICreateIncidentUseCase.java
│   │   │   ├── IGetIncidentUseCase.java
│   │   │   ├── IUpdateIncidentUseCase.java
│   │   │   ├── IDeleteIncidentUseCase.java
│   │   │   ├── IRestoreIncidentUseCase.java
│   │   │   ├── IAssignIncidentUseCase.java
│   │   │   ├── IResolveIncidentUseCase.java
│   │   │   └── ICloseIncidentUseCase.java
│   │   └── incident-type/
│   │       ├── ICreateIncidentTypeUseCase.java
│   │       ├── IGetIncidentTypeUseCase.java
│   │       ├── IUpdateIncidentTypeUseCase.java
│   │       ├── IDeleteIncidentTypeUseCase.java
│   │       └── IRestoreIncidentTypeUseCase.java
│   └── out/                                        # ➡️ Puertos de SALIDA
│       ├── IComplaintRepository.java
│       ├── IComplaintCategoryRepository.java
│       ├── IComplaintResponseRepository.java
│       ├── IIncidentRepository.java
│       ├── IIncidentTypeRepository.java
│       ├── IIncidentResolutionRepository.java
│       ├── IClaimsEventPublisher.java
│       ├── IUserServiceClient.java
│       ├── IInfrastructureClient.java
│       └── ISecurityContext.java
│
├── services/                                       # 🔧 Domain Services
│   └── ClaimsAuthorizationService.java
│
└── exceptions/                                     # ❌ Excepciones de dominio
    ├── base/
    │   ├── DomainException.java
    │   ├── NotFoundException.java
    │   ├── BusinessRuleException.java
    │   ├── ValidationException.java
    │   └── ConflictException.java
    └── specific/
        ├── ComplaintNotFoundException.java
        ├── IncidentNotFoundException.java
        ├── ComplaintAlreadyClosedException.java
        ├── IncidentAlreadyResolvedException.java
        ├── InvalidTransitionException.java
        └── UnauthorizedAssignmentException.java
```

---

## 1️⃣ MODELOS DE DOMINIO

### 📄 Complaint.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.models;

import lombok.Builder;
import lombok.Getter;
import pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects.ComplaintPriority;
import pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects.ComplaintStatus;
import pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects.RecordStatus;

import java.time.LocalDateTime;

@Getter
@Builder(toBuilder = true)
public class Complaint {

    private String id;
    private String organizationId;
    private String complaintCode;
    private String userId;
    private String categoryId;
    private LocalDateTime complaintDate;
    private ComplaintPriority complaintPriority;
    private ComplaintStatus complaintStatus;
    private String description;
    private String assignedToUserId;

    private RecordStatus recordStatus;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;

    public boolean isActive() {
        return recordStatus == RecordStatus.ACTIVE;
    }

    public boolean isInactive() {
        return recordStatus == RecordStatus.INACTIVE;
    }

    public boolean isClosed() {
        return complaintStatus == ComplaintStatus.CLOSED;
    }

    public boolean isResolved() {
        return complaintStatus == ComplaintStatus.RESOLVED;
    }

    public boolean canBeUpdated() {
        return !isClosed() && isActive();
    }

    public boolean canBeClosed() {
        return complaintStatus == ComplaintStatus.RESOLVED && isActive();
    }

    public Complaint advanceStatus() {
        ComplaintStatus next = switch (complaintStatus) {
            case RECEIVED -> ComplaintStatus.IN_PROGRESS;
            case IN_PROGRESS -> ComplaintStatus.RESOLVED;
            case RESOLVED -> ComplaintStatus.CLOSED;
            case CLOSED -> throw new IllegalStateException("Complaint is already closed");
        };
        return this.toBuilder().complaintStatus(next).build();
    }

    public Complaint close(String closedBy) {
        return this.toBuilder()
            .complaintStatus(ComplaintStatus.CLOSED)
            .updatedAt(LocalDateTime.now())
            .updatedBy(closedBy)
            .build();
    }

    public Complaint markAsDeleted(String deletedBy) {
        return this.toBuilder()
            .recordStatus(RecordStatus.INACTIVE)
            .updatedAt(LocalDateTime.now())
            .updatedBy(deletedBy)
            .build();
    }

    public Complaint restore(String restoredBy) {
        return this.toBuilder()
            .recordStatus(RecordStatus.ACTIVE)
            .updatedAt(LocalDateTime.now())
            .updatedBy(restoredBy)
            .build();
    }

    public Complaint updateWith(Complaint changes, String updatedBy) {
        var builder = this.toBuilder()
            .updatedAt(LocalDateTime.now())
            .updatedBy(updatedBy);

        if (changes.getCategoryId() != null) builder.categoryId(changes.getCategoryId());
        if (changes.getComplaintPriority() != null) builder.complaintPriority(changes.getComplaintPriority());
        if (changes.getDescription() != null) builder.description(changes.getDescription());
        if (changes.getAssignedToUserId() != null) builder.assignedToUserId(changes.getAssignedToUserId());

        return builder.build();
    }
}
```

---

### 📄 ComplaintCategory.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.models;

import lombok.Builder;
import lombok.Getter;
import pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects.RecordStatus;

import java.time.LocalDateTime;

@Getter
@Builder(toBuilder = true)
public class ComplaintCategory {

    private String id;
    private String organizationId;
    private String categoryCode;
    private String categoryName;
    private String description;

    private RecordStatus recordStatus;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;

    public boolean isActive() {
        return recordStatus == RecordStatus.ACTIVE;
    }

    public boolean isInactive() {
        return recordStatus == RecordStatus.INACTIVE;
    }

    public ComplaintCategory markAsDeleted(String deletedBy) {
        return this.toBuilder()
            .recordStatus(RecordStatus.INACTIVE)
            .updatedAt(LocalDateTime.now())
            .updatedBy(deletedBy)
            .build();
    }

    public ComplaintCategory restore(String restoredBy) {
        return this.toBuilder()
            .recordStatus(RecordStatus.ACTIVE)
            .updatedAt(LocalDateTime.now())
            .updatedBy(restoredBy)
            .build();
    }

    public ComplaintCategory updateWith(ComplaintCategory changes, String updatedBy) {
        var builder = this.toBuilder()
            .updatedAt(LocalDateTime.now())
            .updatedBy(updatedBy);

        if (changes.getCategoryName() != null) builder.categoryName(changes.getCategoryName());
        if (changes.getDescription() != null) builder.description(changes.getDescription());

        return builder.build();
    }
}
```

---

### 📄 ComplaintResponse.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.models;

import lombok.Builder;
import lombok.Getter;
import pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects.ResponseType;

import java.time.LocalDateTime;

@Getter
@Builder(toBuilder = true)
public class ComplaintResponse {

    private String id;
    private String complaintId;
    private LocalDateTime responseDate;
    private ResponseType responseType;
    private String responseDescription;
    private String respondedByUserId;
    private LocalDateTime createdAt;
}
```

---

### 📄 Incident.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.models;

import lombok.Builder;
import lombok.Getter;
import pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects.IncidentSeverity;
import pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects.IncidentStatus;
import pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects.RecordStatus;

import java.time.LocalDateTime;

@Getter
@Builder(toBuilder = true)
public class Incident {

    private String id;
    private String organizationId;
    private String incidentCode;
    private String incidentTypeId;
    private LocalDateTime incidentDate;
    private String location;
    private String zoneId;
    private String streetId;
    private IncidentSeverity incidentSeverity;
    private IncidentStatus incidentStatus;
    private String description;
    private String reportedByUserId;
    private String assignedToUserId;

    private RecordStatus recordStatus;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;

    public boolean isActive() {
        return recordStatus == RecordStatus.ACTIVE;
    }

    public boolean isInactive() {
        return recordStatus == RecordStatus.INACTIVE;
    }

    public boolean isClosed() {
        return incidentStatus == IncidentStatus.CLOSED;
    }

    public boolean isResolved() {
        return incidentStatus == IncidentStatus.RESOLVED;
    }

    public boolean isUrgent() {
        return incidentSeverity == IncidentSeverity.CRITICAL;
    }

    public boolean canBeAssigned() {
        return (incidentStatus == IncidentStatus.REPORTED || incidentStatus == IncidentStatus.ASSIGNED) && isActive();
    }

    public boolean canBeResolved() {
        return (incidentStatus == IncidentStatus.ASSIGNED || incidentStatus == IncidentStatus.IN_PROGRESS) && isActive();
    }

    public boolean canBeClosed() {
        return incidentStatus == IncidentStatus.RESOLVED && isActive();
    }

    public boolean canBeUpdated() {
        return !isClosed() && isActive();
    }

    public Incident assign(String technicianUserId, String assignedBy) {
        return this.toBuilder()
            .assignedToUserId(technicianUserId)
            .incidentStatus(IncidentStatus.ASSIGNED)
            .updatedAt(LocalDateTime.now())
            .updatedBy(assignedBy)
            .build();
    }

    public Incident startProgress(String updatedBy) {
        return this.toBuilder()
            .incidentStatus(IncidentStatus.IN_PROGRESS)
            .updatedAt(LocalDateTime.now())
            .updatedBy(updatedBy)
            .build();
    }

    public Incident resolve(String resolvedBy) {
        return this.toBuilder()
            .incidentStatus(IncidentStatus.RESOLVED)
            .updatedAt(LocalDateTime.now())
            .updatedBy(resolvedBy)
            .build();
    }

    public Incident close(String closedBy) {
        return this.toBuilder()
            .incidentStatus(IncidentStatus.CLOSED)
            .updatedAt(LocalDateTime.now())
            .updatedBy(closedBy)
            .build();
    }

    public Incident markAsDeleted(String deletedBy) {
        return this.toBuilder()
            .recordStatus(RecordStatus.INACTIVE)
            .updatedAt(LocalDateTime.now())
            .updatedBy(deletedBy)
            .build();
    }

    public Incident restore(String restoredBy) {
        return this.toBuilder()
            .recordStatus(RecordStatus.ACTIVE)
            .updatedAt(LocalDateTime.now())
            .updatedBy(restoredBy)
            .build();
    }

    public Incident updateWith(Incident changes, String updatedBy) {
        var builder = this.toBuilder()
            .updatedAt(LocalDateTime.now())
            .updatedBy(updatedBy);

        if (changes.getIncidentTypeId() != null) builder.incidentTypeId(changes.getIncidentTypeId());
        if (changes.getLocation() != null) builder.location(changes.getLocation());
        if (changes.getZoneId() != null) builder.zoneId(changes.getZoneId());
        if (changes.getStreetId() != null) builder.streetId(changes.getStreetId());
        if (changes.getIncidentSeverity() != null) builder.incidentSeverity(changes.getIncidentSeverity());
        if (changes.getDescription() != null) builder.description(changes.getDescription());

        return builder.build();
    }
}
```

---

### 📄 IncidentType.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.models;

import lombok.Builder;
import lombok.Getter;
import pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects.RecordStatus;

import java.time.LocalDateTime;

@Getter
@Builder(toBuilder = true)
public class IncidentType {

    private String id;
    private String organizationId;
    private String typeCode;
    private String typeName;
    private String description;

    private RecordStatus recordStatus;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;

    public boolean isActive() {
        return recordStatus == RecordStatus.ACTIVE;
    }

    public boolean isInactive() {
        return recordStatus == RecordStatus.INACTIVE;
    }

    public IncidentType markAsDeleted(String deletedBy) {
        return this.toBuilder()
            .recordStatus(RecordStatus.INACTIVE)
            .updatedAt(LocalDateTime.now())
            .updatedBy(deletedBy)
            .build();
    }

    public IncidentType restore(String restoredBy) {
        return this.toBuilder()
            .recordStatus(RecordStatus.ACTIVE)
            .updatedAt(LocalDateTime.now())
            .updatedBy(restoredBy)
            .build();
    }

    public IncidentType updateWith(IncidentType changes, String updatedBy) {
        var builder = this.toBuilder()
            .updatedAt(LocalDateTime.now())
            .updatedBy(updatedBy);

        if (changes.getTypeName() != null) builder.typeName(changes.getTypeName());
        if (changes.getDescription() != null) builder.description(changes.getDescription());

        return builder.build();
    }
}
```

---

### 📄 IncidentResolution.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.models;

import lombok.Builder;
import lombok.Getter;
import pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects.MaterialUsed;
import pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects.ResolutionType;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder(toBuilder = true)
public class IncidentResolution {

    private String id;
    private String incidentId;
    private LocalDateTime resolutionDate;
    private ResolutionType resolutionType;
    private String resolutionDescription;
    private List<MaterialUsed> materialsUsed;
    private Double totalCost;
    private String resolvedByUserId;
    private LocalDateTime createdAt;

    public Double calculateTotalCost() {
        if (materialsUsed == null || materialsUsed.isEmpty()) {
            return 0.0;
        }
        return materialsUsed.stream()
            .mapToDouble(m -> m.getQuantity() * m.getUnitCost())
            .sum();
    }
}
```

---

## 2️⃣ VALUE OBJECTS

### 📄 RecordStatus.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects;

public enum RecordStatus {
    ACTIVE,
    INACTIVE
}
```

---

### 📄 ComplaintPriority.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ComplaintPriority {

    LOW("Baja", 1),
    MEDIUM("Media", 2),
    HIGH("Alta", 3),
    URGENT("Urgente", 4);

    private final String displayName;
    private final int level;
}
```

---

### 📄 ComplaintStatus.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ComplaintStatus {

    RECEIVED("Recibido"),
    IN_PROGRESS("En Proceso"),
    RESOLVED("Resuelto"),
    CLOSED("Cerrado");

    private final String displayName;
}
```

---

### 📄 ResponseType.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResponseType {

    INVESTIGACION("Investigación"),
    SOLUCION("Solución"),
    SEGUIMIENTO("Seguimiento");

    private final String displayName;
}
```

---

### 📄 IncidentSeverity.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum IncidentSeverity {

    LOW("Baja", 1),
    MEDIUM("Media", 2),
    HIGH("Alta", 3),
    CRITICAL("Crítica", 4);

    private final String displayName;
    private final int level;
}
```

---

### 📄 IncidentStatus.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum IncidentStatus {

    REPORTED("Reportado"),
    ASSIGNED("Asignado"),
    IN_PROGRESS("En Progreso"),
    RESOLVED("Resuelto"),
    CLOSED("Cerrado");

    private final String displayName;
}
```

---

### 📄 ResolutionType.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ResolutionType {

    REPARACION_TEMPORAL("Reparación Temporal"),
    REPARACION_COMPLETA("Reparación Completa"),
    REEMPLAZO("Reemplazo");

    private final String displayName;
}
```

---

### 📄 MaterialUsed.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MaterialUsed {

    private String materialId;
    private Double quantity;
    private String unit;
    private Double unitCost;

    public Double getSubtotal() {
        return quantity * unitCost;
    }
}
```

---

## 3️⃣ PORTS IN - Casos de Uso (Interfaces)

### Complaint Ports

#### 📄 ICreateComplaintUseCase.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.ports.in.complaint;

import pe.edu.vallegrande.vgmsclaims.domain.models.Complaint;
import reactor.core.publisher.Mono;

public interface ICreateComplaintUseCase {
    Mono<Complaint> execute(Complaint complaint, String createdBy);
}
```

---

#### 📄 IGetComplaintUseCase.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.ports.in.complaint;

import pe.edu.vallegrande.vgmsclaims.domain.models.Complaint;
import pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects.ComplaintPriority;
import pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects.ComplaintStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IGetComplaintUseCase {
    Mono<Complaint> findById(String id);
    Flux<Complaint> findAllActive();
    Flux<Complaint> findAll();
    Flux<Complaint> findByStatus(ComplaintStatus status);
    Flux<Complaint> findByUserId(String userId);
    Flux<Complaint> findByCategoryId(String categoryId);
    Flux<Complaint> findByPriority(ComplaintPriority priority);
    Flux<Complaint> findByOrganizationId(String organizationId);
}
```

---

#### 📄 IUpdateComplaintUseCase.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.ports.in.complaint;

import pe.edu.vallegrande.vgmsclaims.domain.models.Complaint;
import reactor.core.publisher.Mono;

public interface IUpdateComplaintUseCase {
    Mono<Complaint> execute(String id, Complaint changes, String updatedBy);
}
```

---

#### 📄 IDeleteComplaintUseCase.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.ports.in.complaint;

import pe.edu.vallegrande.vgmsclaims.domain.models.Complaint;
import reactor.core.publisher.Mono;

public interface IDeleteComplaintUseCase {
    Mono<Complaint> execute(String id, String deletedBy, String reason);
}
```

---

#### 📄 IRestoreComplaintUseCase.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.ports.in.complaint;

import pe.edu.vallegrande.vgmsclaims.domain.models.Complaint;
import reactor.core.publisher.Mono;

public interface IRestoreComplaintUseCase {
    Mono<Complaint> execute(String id, String restoredBy);
}
```

---

#### 📄 IAddResponseUseCase.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.ports.in.complaint;

import pe.edu.vallegrande.vgmsclaims.domain.models.ComplaintResponse;
import reactor.core.publisher.Mono;

public interface IAddResponseUseCase {
    Mono<ComplaintResponse> execute(String complaintId, ComplaintResponse response, String respondedBy);
}
```

---

#### 📄 ICloseComplaintUseCase.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.ports.in.complaint;

import pe.edu.vallegrande.vgmsclaims.domain.models.Complaint;
import reactor.core.publisher.Mono;

public interface ICloseComplaintUseCase {
    Mono<Complaint> execute(String id, String closedBy);
}
```

---

### Complaint Category Ports

#### 📄 ICreateComplaintCategoryUseCase.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.ports.in.complaintcategory;

import pe.edu.vallegrande.vgmsclaims.domain.models.ComplaintCategory;
import reactor.core.publisher.Mono;

public interface ICreateComplaintCategoryUseCase {
    Mono<ComplaintCategory> execute(ComplaintCategory category, String createdBy);
}
```

---

#### 📄 IGetComplaintCategoryUseCase.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.ports.in.complaintcategory;

import pe.edu.vallegrande.vgmsclaims.domain.models.ComplaintCategory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IGetComplaintCategoryUseCase {
    Mono<ComplaintCategory> findById(String id);
    Flux<ComplaintCategory> findAllActive();
    Flux<ComplaintCategory> findAll();
    Flux<ComplaintCategory> findByOrganizationId(String organizationId);
}
```

---

#### 📄 IUpdateComplaintCategoryUseCase.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.ports.in.complaintcategory;

import pe.edu.vallegrande.vgmsclaims.domain.models.ComplaintCategory;
import reactor.core.publisher.Mono;

public interface IUpdateComplaintCategoryUseCase {
    Mono<ComplaintCategory> execute(String id, ComplaintCategory changes, String updatedBy);
}
```

---

#### 📄 IDeleteComplaintCategoryUseCase.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.ports.in.complaintcategory;

import pe.edu.vallegrande.vgmsclaims.domain.models.ComplaintCategory;
import reactor.core.publisher.Mono;

public interface IDeleteComplaintCategoryUseCase {
    Mono<ComplaintCategory> execute(String id, String deletedBy, String reason);
}
```

---

#### 📄 IRestoreComplaintCategoryUseCase.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.ports.in.complaintcategory;

import pe.edu.vallegrande.vgmsclaims.domain.models.ComplaintCategory;
import reactor.core.publisher.Mono;

public interface IRestoreComplaintCategoryUseCase {
    Mono<ComplaintCategory> execute(String id, String restoredBy);
}
```

---

### Incident Ports

#### 📄 ICreateIncidentUseCase.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.ports.in.incident;

import pe.edu.vallegrande.vgmsclaims.domain.models.Incident;
import reactor.core.publisher.Mono;

public interface ICreateIncidentUseCase {
    Mono<Incident> execute(Incident incident, String createdBy);
}
```

---

#### 📄 IGetIncidentUseCase.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.ports.in.incident;

import pe.edu.vallegrande.vgmsclaims.domain.models.Incident;
import pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects.IncidentSeverity;
import pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects.IncidentStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IGetIncidentUseCase {
    Mono<Incident> findById(String id);
    Flux<Incident> findAllActive();
    Flux<Incident> findAll();
    Flux<Incident> findByStatus(IncidentStatus status);
    Flux<Incident> findByTypeId(String typeId);
    Flux<Incident> findBySeverity(IncidentSeverity severity);
    Flux<Incident> findByAssignedTo(String userId);
    Flux<Incident> findByOrganizationId(String organizationId);
}
```

---

#### 📄 IUpdateIncidentUseCase.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.ports.in.incident;

import pe.edu.vallegrande.vgmsclaims.domain.models.Incident;
import reactor.core.publisher.Mono;

public interface IUpdateIncidentUseCase {
    Mono<Incident> execute(String id, Incident changes, String updatedBy);
}
```

---

#### 📄 IDeleteIncidentUseCase.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.ports.in.incident;

import pe.edu.vallegrande.vgmsclaims.domain.models.Incident;
import reactor.core.publisher.Mono;

public interface IDeleteIncidentUseCase {
    Mono<Incident> execute(String id, String deletedBy, String reason);
}
```

---

#### 📄 IRestoreIncidentUseCase.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.ports.in.incident;

import pe.edu.vallegrande.vgmsclaims.domain.models.Incident;
import reactor.core.publisher.Mono;

public interface IRestoreIncidentUseCase {
    Mono<Incident> execute(String id, String restoredBy);
}
```

---

#### 📄 IAssignIncidentUseCase.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.ports.in.incident;

import pe.edu.vallegrande.vgmsclaims.domain.models.Incident;
import reactor.core.publisher.Mono;

public interface IAssignIncidentUseCase {
    Mono<Incident> execute(String incidentId, String technicianUserId, String assignedBy);
}
```

---

#### 📄 IResolveIncidentUseCase.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.ports.in.incident;

import pe.edu.vallegrande.vgmsclaims.domain.models.Incident;
import pe.edu.vallegrande.vgmsclaims.domain.models.IncidentResolution;
import reactor.core.publisher.Mono;

public interface IResolveIncidentUseCase {
    Mono<Incident> execute(String incidentId, IncidentResolution resolution, String resolvedBy);
}
```

---

#### 📄 ICloseIncidentUseCase.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.ports.in.incident;

import pe.edu.vallegrande.vgmsclaims.domain.models.Incident;
import reactor.core.publisher.Mono;

public interface ICloseIncidentUseCase {
    Mono<Incident> execute(String id, String closedBy);
}
```

---

### Incident Type Ports

#### 📄 ICreateIncidentTypeUseCase.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.ports.in.incidenttype;

import pe.edu.vallegrande.vgmsclaims.domain.models.IncidentType;
import reactor.core.publisher.Mono;

public interface ICreateIncidentTypeUseCase {
    Mono<IncidentType> execute(IncidentType type, String createdBy);
}
```

---

#### 📄 IGetIncidentTypeUseCase.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.ports.in.incidenttype;

import pe.edu.vallegrande.vgmsclaims.domain.models.IncidentType;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IGetIncidentTypeUseCase {
    Mono<IncidentType> findById(String id);
    Flux<IncidentType> findAllActive();
    Flux<IncidentType> findAll();
    Flux<IncidentType> findByOrganizationId(String organizationId);
}
```

---

#### 📄 IUpdateIncidentTypeUseCase.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.ports.in.incidenttype;

import pe.edu.vallegrande.vgmsclaims.domain.models.IncidentType;
import reactor.core.publisher.Mono;

public interface IUpdateIncidentTypeUseCase {
    Mono<IncidentType> execute(String id, IncidentType changes, String updatedBy);
}
```

---

#### 📄 IDeleteIncidentTypeUseCase.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.ports.in.incidenttype;

import pe.edu.vallegrande.vgmsclaims.domain.models.IncidentType;
import reactor.core.publisher.Mono;

public interface IDeleteIncidentTypeUseCase {
    Mono<IncidentType> execute(String id, String deletedBy, String reason);
}
```

---

#### 📄 IRestoreIncidentTypeUseCase.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.ports.in.incidenttype;

import pe.edu.vallegrande.vgmsclaims.domain.models.IncidentType;
import reactor.core.publisher.Mono;

public interface IRestoreIncidentTypeUseCase {
    Mono<IncidentType> execute(String id, String restoredBy);
}
```

---

## 4️⃣ PORTS OUT - Contratos de Infraestructura

### 📄 IComplaintRepository.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.ports.out;

import pe.edu.vallegrande.vgmsclaims.domain.models.Complaint;
import pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects.ComplaintPriority;
import pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects.ComplaintStatus;
import pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects.RecordStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IComplaintRepository {
    Mono<Complaint> save(Complaint complaint);
    Mono<Complaint> update(Complaint complaint);
    Mono<Complaint> findById(String id);
    Flux<Complaint> findAll();
    Flux<Complaint> findByRecordStatus(RecordStatus status);
    Flux<Complaint> findByComplaintStatus(ComplaintStatus status);
    Flux<Complaint> findByUserId(String userId);
    Flux<Complaint> findByCategoryId(String categoryId);
    Flux<Complaint> findByComplaintPriority(ComplaintPriority priority);
    Flux<Complaint> findByOrganizationId(String organizationId);
    Mono<Long> countByOrganizationId(String organizationId);
    Mono<Void> deleteById(String id);
}
```

---

### 📄 IComplaintCategoryRepository.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.ports.out;

import pe.edu.vallegrande.vgmsclaims.domain.models.ComplaintCategory;
import pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects.RecordStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IComplaintCategoryRepository {
    Mono<ComplaintCategory> save(ComplaintCategory category);
    Mono<ComplaintCategory> update(ComplaintCategory category);
    Mono<ComplaintCategory> findById(String id);
    Flux<ComplaintCategory> findAll();
    Flux<ComplaintCategory> findByRecordStatus(RecordStatus status);
    Flux<ComplaintCategory> findByOrganizationId(String organizationId);
    Mono<Boolean> existsByCategoryCodeAndOrganizationId(String categoryCode, String organizationId);
    Mono<Void> deleteById(String id);
}
```

---

### 📄 IComplaintResponseRepository.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.ports.out;

import pe.edu.vallegrande.vgmsclaims.domain.models.ComplaintResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IComplaintResponseRepository {
    Mono<ComplaintResponse> save(ComplaintResponse response);
    Flux<ComplaintResponse> findByComplaintId(String complaintId);
    Mono<ComplaintResponse> findById(String id);
}
```

---

### 📄 IIncidentRepository.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.ports.out;

import pe.edu.vallegrande.vgmsclaims.domain.models.Incident;
import pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects.IncidentSeverity;
import pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects.IncidentStatus;
import pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects.RecordStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IIncidentRepository {
    Mono<Incident> save(Incident incident);
    Mono<Incident> update(Incident incident);
    Mono<Incident> findById(String id);
    Flux<Incident> findAll();
    Flux<Incident> findByRecordStatus(RecordStatus status);
    Flux<Incident> findByIncidentStatus(IncidentStatus status);
    Flux<Incident> findByIncidentTypeId(String typeId);
    Flux<Incident> findByIncidentSeverity(IncidentSeverity severity);
    Flux<Incident> findByAssignedToUserId(String userId);
    Flux<Incident> findByOrganizationId(String organizationId);
    Mono<Long> countByOrganizationId(String organizationId);
    Mono<Void> deleteById(String id);
}
```

---

### 📄 IIncidentTypeRepository.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.ports.out;

import pe.edu.vallegrande.vgmsclaims.domain.models.IncidentType;
import pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects.RecordStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IIncidentTypeRepository {
    Mono<IncidentType> save(IncidentType type);
    Mono<IncidentType> update(IncidentType type);
    Mono<IncidentType> findById(String id);
    Flux<IncidentType> findAll();
    Flux<IncidentType> findByRecordStatus(RecordStatus status);
    Flux<IncidentType> findByOrganizationId(String organizationId);
    Mono<Boolean> existsByTypeCodeAndOrganizationId(String typeCode, String organizationId);
    Mono<Void> deleteById(String id);
}
```

---

### 📄 IIncidentResolutionRepository.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.ports.out;

import pe.edu.vallegrande.vgmsclaims.domain.models.IncidentResolution;
import reactor.core.publisher.Mono;

public interface IIncidentResolutionRepository {
    Mono<IncidentResolution> save(IncidentResolution resolution);
    Mono<IncidentResolution> findByIncidentId(String incidentId);
    Mono<IncidentResolution> findById(String id);
}
```

---

### 📄 IClaimsEventPublisher.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.ports.out;

import pe.edu.vallegrande.vgmsclaims.domain.models.Complaint;
import pe.edu.vallegrande.vgmsclaims.domain.models.ComplaintResponse;
import pe.edu.vallegrande.vgmsclaims.domain.models.Incident;
import pe.edu.vallegrande.vgmsclaims.domain.models.IncidentResolution;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface IClaimsEventPublisher {

    // Complaint events
    Mono<Void> publishComplaintCreated(Complaint complaint, String createdBy);
    Mono<Void> publishComplaintUpdated(Complaint complaint, Map<String, Object> changedFields, String updatedBy);
    Mono<Void> publishComplaintClosed(String complaintId, String closedBy);
    Mono<Void> publishComplaintResponseAdded(ComplaintResponse response, String respondedBy);

    // Incident events
    Mono<Void> publishIncidentCreated(Incident incident, String createdBy);
    Mono<Void> publishIncidentAssigned(Incident incident, String assignedBy);
    Mono<Void> publishIncidentUpdated(Incident incident, Map<String, Object> changedFields, String updatedBy);
    Mono<Void> publishIncidentResolved(Incident incident, IncidentResolution resolution, String resolvedBy);
    Mono<Void> publishIncidentClosed(String incidentId, String closedBy);
    Mono<Void> publishUrgentIncidentAlert(Incident incident, String createdBy);
}
```

---

### 📄 IUserServiceClient.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.ports.out;

import reactor.core.publisher.Mono;

public interface IUserServiceClient {

    Mono<Boolean> existsUser(String userId);

    Mono<UserInfo> getUserById(String userId);

    record UserInfo(
        String id,
        String organizationId,
        String email,
        String firstName,
        String lastName,
        String role
    ) {}
}
```

---

### 📄 IInfrastructureClient.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.ports.out;

import reactor.core.publisher.Mono;

public interface IInfrastructureClient {

    Mono<Boolean> existsZone(String zoneId);

    Mono<Boolean> existsStreet(String streetId);

    Mono<ZoneInfo> getZoneById(String zoneId);

    record ZoneInfo(
        String id,
        String organizationId,
        String zoneName,
        String description
    ) {}
}
```

---

### 📄 ISecurityContext.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.ports.out;

import reactor.core.publisher.Mono;

public interface ISecurityContext {
    Mono<String> getCurrentUserId();
    Mono<String> getCurrentOrganizationId();
    Mono<Boolean> isSuperAdmin();
    Mono<Boolean> isAdmin();
    Mono<Boolean> belongsToOrganization(String organizationId);
}
```

---

## 5️⃣ DOMAIN SERVICES

### 📄 ClaimsAuthorizationService.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.services;

import lombok.RequiredArgsConstructor;
import pe.edu.vallegrande.vgmsclaims.domain.exceptions.specific.UnauthorizedAssignmentException;
import pe.edu.vallegrande.vgmsclaims.domain.ports.out.ISecurityContext;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ClaimsAuthorizationService {

    private final ISecurityContext securityContext;

    /**
     * Verifica que el usuario actual puede asignar técnicos a incidentes.
     * Solo ADMIN y SUPER_ADMIN pueden asignar.
     */
    public Mono<Void> validateCanAssign() {
        return securityContext.isAdmin()
            .flatMap(isAdmin -> {
                if (!isAdmin) {
                    return Mono.error(new UnauthorizedAssignmentException(
                        "Only ADMIN or SUPER_ADMIN can assign incidents"
                    ));
                }
                return Mono.empty();
            });
    }

    /**
     * Verifica que el usuario actual puede cerrar incidentes/reclamos.
     * Solo ADMIN y SUPER_ADMIN pueden cerrar.
     */
    public Mono<Void> validateCanClose() {
        return securityContext.isAdmin()
            .flatMap(isAdmin -> {
                if (!isAdmin) {
                    return Mono.error(new UnauthorizedAssignmentException(
                        "Only ADMIN or SUPER_ADMIN can close items"
                    ));
                }
                return Mono.empty();
            });
    }

    /**
     * Verifica que el usuario pertenece a la organización.
     */
    public Mono<Void> validateBelongsToOrganization(String organizationId) {
        return securityContext.belongsToOrganization(organizationId)
            .flatMap(belongs -> {
                if (!belongs) {
                    return Mono.error(new UnauthorizedAssignmentException(
                        "User does not belong to the specified organization"
                    ));
                }
                return Mono.empty();
            });
    }
}
```

---

## 6️⃣ EXCEPTIONS - Excepciones de Dominio

### 📄 DomainException.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.exceptions.base;

import lombok.Getter;

@Getter
public abstract class DomainException extends RuntimeException {

    private final String errorCode;
    private final int httpStatus;

    protected DomainException(String message, String errorCode, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    protected DomainException(String message, String errorCode, int httpStatus, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
}
```

---

### 📄 NotFoundException.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.exceptions.base;

public class NotFoundException extends DomainException {

    public NotFoundException(String resource, String id) {
        super(
            String.format("%s with ID '%s' not found", resource, id),
            "NOT_FOUND",
            404
        );
    }
}
```

---

### 📄 BusinessRuleException.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.exceptions.base;

public class BusinessRuleException extends DomainException {

    public BusinessRuleException(String message) {
        super(message, "BUSINESS_RULE_VIOLATION", 422);
    }
}
```

---

### 📄 ValidationException.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.exceptions.base;

import lombok.Getter;

@Getter
public class ValidationException extends DomainException {

    private final String field;

    public ValidationException(String field, String message) {
        super(message, "VALIDATION_ERROR", 400);
        this.field = field;
    }
}
```

---

### 📄 ConflictException.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.exceptions.base;

public class ConflictException extends DomainException {

    public ConflictException(String message) {
        super(message, "CONFLICT", 409);
    }
}
```

---

### 📄 ComplaintNotFoundException.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.exceptions.specific;

import pe.edu.vallegrande.vgmsclaims.domain.exceptions.base.NotFoundException;

public class ComplaintNotFoundException extends NotFoundException {

    public ComplaintNotFoundException(String id) {
        super("Complaint", id);
    }
}
```

---

### 📄 IncidentNotFoundException.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.exceptions.specific;

import pe.edu.vallegrande.vgmsclaims.domain.exceptions.base.NotFoundException;

public class IncidentNotFoundException extends NotFoundException {

    public IncidentNotFoundException(String id) {
        super("Incident", id);
    }
}
```

---

### 📄 ComplaintAlreadyClosedException.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.exceptions.specific;

import pe.edu.vallegrande.vgmsclaims.domain.exceptions.base.BusinessRuleException;

public class ComplaintAlreadyClosedException extends BusinessRuleException {

    public ComplaintAlreadyClosedException(String complaintId) {
        super(String.format("Complaint '%s' is already closed and cannot be modified", complaintId));
    }
}
```

---

### 📄 IncidentAlreadyResolvedException.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.exceptions.specific;

import pe.edu.vallegrande.vgmsclaims.domain.exceptions.base.BusinessRuleException;

public class IncidentAlreadyResolvedException extends BusinessRuleException {

    public IncidentAlreadyResolvedException(String incidentId) {
        super(String.format("Incident '%s' is already resolved and cannot be modified", incidentId));
    }
}
```

---

### 📄 InvalidTransitionException.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.exceptions.specific;

import pe.edu.vallegrande.vgmsclaims.domain.exceptions.base.BusinessRuleException;

public class InvalidTransitionException extends BusinessRuleException {

    public InvalidTransitionException(String currentStatus, String targetStatus) {
        super(String.format("Cannot transition from '%s' to '%s'", currentStatus, targetStatus));
    }
}
```

---

### 📄 UnauthorizedAssignmentException.java

```java
package pe.edu.vallegrande.vgmsclaims.domain.exceptions.specific;

import pe.edu.vallegrande.vgmsclaims.domain.exceptions.base.BusinessRuleException;

public class UnauthorizedAssignmentException extends BusinessRuleException {

    public UnauthorizedAssignmentException(String message) {
        super(message);
    }
}
```

---

## ✅ Resumen de la Capa de Dominio

| Componente | Cantidad | Descripción |
|------------|----------|-------------|
| Modelos | 6 clases | Complaint, ComplaintCategory, ComplaintResponse, Incident, IncidentType, IncidentResolution |
| Value Objects | 8 | RecordStatus, ComplaintPriority, ComplaintStatus, ResponseType, IncidentSeverity, IncidentStatus, ResolutionType, MaterialUsed |
| Ports In | 25 interfaces | 7 complaint, 5 complaint-category, 8 incident, 5 incident-type |
| Ports Out | 10 interfaces | 6 repositorios + 1 event publisher + 2 clientes + 1 security |
| Domain Services | 1 | ClaimsAuthorizationService |
| Exceptions | 11 clases | 5 base + 6 específicas |

---

> **Siguiente paso**: Lee [README_APPLICATION.md](README_APPLICATION.md) para ver la implementación de los casos de uso.
