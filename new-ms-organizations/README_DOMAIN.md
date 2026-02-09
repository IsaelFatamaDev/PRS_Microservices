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
│   ├── Organization.java                           #    └─ Model (Class)
│   ├── Zone.java                                   #    └─ Model (Class)
│   ├── Street.java                                 #    └─ Model (Class)
│   ├── Fare.java                                   #    └─ Model (Class)
│   ├── Parameter.java                              #    └─ Model (Class)
│   └── valueobjects/                               # 🏷️ Value Objects
│       ├── RecordStatus.java                       #    └─ Enum (ACTIVE/INACTIVE)
│       ├── StreetType.java                         #    └─ Enum
│       ├── FareType.java                           #    └─ Enum
│       └── ParameterType.java                      #    └─ Enum
│
├── ports/                                          # 🔌 Puertos (Contratos)
│   ├── in/                                         # ⬅️ Puertos de ENTRADA (Use Cases)
│   │   ├── organization/
│   │   │   ├── ICreateOrganizationUseCase.java     #    └─ Interface
│   │   │   ├── IGetOrganizationUseCase.java        #    └─ Interface
│   │   │   ├── IUpdateOrganizationUseCase.java     #    └─ Interface
│   │   │   ├── IDeleteOrganizationUseCase.java     #    └─ Interface
│   │   │   └── IRestoreOrganizationUseCase.java    #    └─ Interface
│   │   ├── zone/
│   │   │   ├── ICreateZoneUseCase.java             #    └─ Interface
│   │   │   ├── IGetZoneUseCase.java                #    └─ Interface
│   │   │   ├── IUpdateZoneUseCase.java             #    └─ Interface
│   │   │   ├── IDeleteZoneUseCase.java             #    └─ Interface
│   │   │   └── IRestoreZoneUseCase.java            #    └─ Interface
│   │   ├── street/
│   │   │   ├── ICreateStreetUseCase.java           #    └─ Interface
│   │   │   ├── IGetStreetUseCase.java              #    └─ Interface
│   │   │   ├── IUpdateStreetUseCase.java           #    └─ Interface
│   │   │   ├── IDeleteStreetUseCase.java           #    └─ Interface
│   │   │   └── IRestoreStreetUseCase.java          #    └─ Interface
│   │   ├── fare/
│   │   │   ├── ICreateFareUseCase.java             #    └─ Interface
│   │   │   ├── IGetFareUseCase.java                #    └─ Interface
│   │   │   ├── IUpdateFareUseCase.java             #    └─ Interface
│   │   │   ├── IDeleteFareUseCase.java             #    └─ Interface
│   │   │   └── IRestoreFareUseCase.java            #    └─ Interface
│   │   └── parameter/
│   │       ├── ICreateParameterUseCase.java        #    └─ Interface
│   │       ├── IGetParameterUseCase.java           #    └─ Interface
│   │       ├── IUpdateParameterUseCase.java        #    └─ Interface
│   │       ├── IDeleteParameterUseCase.java        #    └─ Interface
│   │       └── IRestoreParameterUseCase.java       #    └─ Interface
│   └── out/                                        # ➡️ Puertos de SALIDA (Repos/Events)
│       ├── organization/
│       │   ├── IOrganizationRepository.java        #    └─ Interface (Repository)
│       │   └── IOrganizationEventPublisher.java    #    └─ Interface (EventPublisher)
│       ├── zone/
│       │   ├── IZoneRepository.java                #    └─ Interface (Repository)
│       │   └── IZoneEventPublisher.java            #    └─ Interface (EventPublisher)
│       ├── street/
│       │   ├── IStreetRepository.java              #    └─ Interface (Repository)
│       │   └── IStreetEventPublisher.java          #    └─ Interface (EventPublisher)
│       ├── fare/
│       │   ├── IFareRepository.java                #    └─ Interface (Repository)
│       │   └── IFareEventPublisher.java            #    └─ Interface (EventPublisher)
│       └── parameter/
│           ├── IParameterRepository.java           #    └─ Interface (Repository)
│           └── IParameterEventPublisher.java       #    └─ Interface (EventPublisher)
│
└── exceptions/                                     # ❌ Excepciones de dominio
    ├── base/                                       #    └─ Excepciones genéricas
    │   ├── DomainException.java                    #       └─ Exception (Base)
    │   ├── NotFoundException.java                  #       └─ Exception (extends Domain)
    │   ├── BusinessRuleException.java              #       └─ Exception (extends Domain)
    │   ├── ValidationException.java                #       └─ Exception (extends Domain)
    │   └── ConflictException.java                  #       └─ Exception (extends Domain)
    └── specific/                                   #    └─ Excepciones específicas
        ├── OrganizationNotFoundException.java      #       └─ Exception (extends NotFound)
        ├── ZoneNotFoundException.java              #       └─ Exception (extends NotFound)
        ├── StreetNotFoundException.java            #       └─ Exception (extends NotFound)
        ├── FareNotFoundException.java              #       └─ Exception (extends NotFound)
        ├── ParameterNotFoundException.java         #       └─ Exception (extends NotFound)
        └── DuplicateOrganizationException.java     #       └─ Exception (extends Conflict)
```

---

## 1️⃣ MODELOS DE DOMINIO

### 📄 Organization.java

```java
package pe.edu.vallegrande.vgmsorganizations.domain.models;

import lombok.Builder;
import lombok.Getter;
import pe.edu.vallegrande.vgmsorganizations.domain.models.valueobjects.RecordStatus;

import java.time.LocalDateTime;

@Getter
@Builder(toBuilder = true)
public class Organization {

    private String id;
    private String organizationName;
    private String district;
    private String province;
    private String department;
    private String address;
    private String phone;
    private String email;

    private RecordStatus recordStatus;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;

    public String getFullLocation() {
        return String.format("%s, %s - %s", district, province, department);
    }

    public boolean isActive() {
        return recordStatus == RecordStatus.ACTIVE;
    }

    public boolean isInactive() {
        return recordStatus == RecordStatus.INACTIVE;
    }

    public Organization markAsDeleted(String deletedBy) {
        return this.toBuilder()
            .recordStatus(RecordStatus.INACTIVE)
            .updatedAt(LocalDateTime.now())
            .updatedBy(deletedBy)
            .build();
    }

    public Organization restore(String restoredBy) {
        return this.toBuilder()
            .recordStatus(RecordStatus.ACTIVE)
            .updatedAt(LocalDateTime.now())
            .updatedBy(restoredBy)
            .build();
    }

    public Organization updateWith(Organization changes, String updatedBy) {
        var builder = this.toBuilder()
            .updatedAt(LocalDateTime.now())
            .updatedBy(updatedBy);

        if (changes.getOrganizationName() != null) builder.organizationName(changes.getOrganizationName());
        if (changes.getDistrict() != null) builder.district(changes.getDistrict());
        if (changes.getProvince() != null) builder.province(changes.getProvince());
        if (changes.getDepartment() != null) builder.department(changes.getDepartment());
        if (changes.getAddress() != null) builder.address(changes.getAddress());
        if (changes.getPhone() != null) builder.phone(changes.getPhone());
        if (changes.getEmail() != null) builder.email(changes.getEmail());

        return builder.build();
    }

    public void validateContact() {
        if ((email == null || email.isBlank()) && (phone == null || phone.isBlank())) {
            throw new IllegalArgumentException("Organization must have at least email or phone");
        }
    }
}
```

---

### 📄 Zone.java

```java
package pe.edu.vallegrande.vgmsorganizations.domain.models;

import lombok.Builder;
import lombok.Getter;
import pe.edu.vallegrande.vgmsorganizations.domain.models.valueobjects.RecordStatus;

import java.time.LocalDateTime;

@Getter
@Builder(toBuilder = true)
public class Zone {

    private String id;
    private String organizationId;
    private String zoneName;
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

    public Zone markAsDeleted(String deletedBy) {
        return this.toBuilder()
            .recordStatus(RecordStatus.INACTIVE)
            .updatedAt(LocalDateTime.now())
            .updatedBy(deletedBy)
            .build();
    }

    public Zone restore(String restoredBy) {
        return this.toBuilder()
            .recordStatus(RecordStatus.ACTIVE)
            .updatedAt(LocalDateTime.now())
            .updatedBy(restoredBy)
            .build();
    }

    public Zone updateWith(Zone changes, String updatedBy) {
        var builder = this.toBuilder()
            .updatedAt(LocalDateTime.now())
            .updatedBy(updatedBy);

        if (changes.getZoneName() != null) builder.zoneName(changes.getZoneName());
        if (changes.getDescription() != null) builder.description(changes.getDescription());

        return builder.build();
    }
}
```

---

### 📄 Street.java

```java
package pe.edu.vallegrande.vgmsorganizations.domain.models;

import lombok.Builder;
import lombok.Getter;
import pe.edu.vallegrande.vgmsorganizations.domain.models.valueobjects.RecordStatus;
import pe.edu.vallegrande.vgmsorganizations.domain.models.valueobjects.StreetType;

import java.time.LocalDateTime;

@Getter
@Builder(toBuilder = true)
public class Street {

    private String id;
    private String organizationId;
    private String zoneId;
    private StreetType streetType;
    private String streetName;

    private RecordStatus recordStatus;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;

    public String getFullStreetName() {
        return streetType.getPrefix() + " " + streetName;
    }

    public boolean isActive() {
        return recordStatus == RecordStatus.ACTIVE;
    }

    public boolean isInactive() {
        return recordStatus == RecordStatus.INACTIVE;
    }

    public Street markAsDeleted(String deletedBy) {
        return this.toBuilder()
            .recordStatus(RecordStatus.INACTIVE)
            .updatedAt(LocalDateTime.now())
            .updatedBy(deletedBy)
            .build();
    }

    public Street restore(String restoredBy) {
        return this.toBuilder()
            .recordStatus(RecordStatus.ACTIVE)
            .updatedAt(LocalDateTime.now())
            .updatedBy(restoredBy)
            .build();
    }

    public Street updateWith(Street changes, String updatedBy) {
        var builder = this.toBuilder()
            .updatedAt(LocalDateTime.now())
            .updatedBy(updatedBy);

        if (changes.getStreetType() != null) builder.streetType(changes.getStreetType());
        if (changes.getStreetName() != null) builder.streetName(changes.getStreetName());

        return builder.build();
    }
}
```

---

### 📄 Fare.java

```java
package pe.edu.vallegrande.vgmsorganizations.domain.models;

import lombok.Builder;
import lombok.Getter;
import pe.edu.vallegrande.vgmsorganizations.domain.models.valueobjects.RecordStatus;

import java.time.LocalDateTime;

@Getter
@Builder(toBuilder = true)
public class Fare {

    private String id;
    private String organizationId;
    private String fareType;
    private Double amount;
    private String description;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;

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

    public boolean isCurrentlyValid() {
        LocalDateTime now = LocalDateTime.now();
        boolean afterStart = validFrom == null || !now.isBefore(validFrom);
        boolean beforeEnd = validTo == null || !now.isAfter(validTo);
        return afterStart && beforeEnd && isActive();
    }

    public void validateAmount() {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Fare amount must be greater than 0");
        }
    }

    public Fare markAsDeleted(String deletedBy) {
        return this.toBuilder()
            .recordStatus(RecordStatus.INACTIVE)
            .updatedAt(LocalDateTime.now())
            .updatedBy(deletedBy)
            .build();
    }

    public Fare restore(String restoredBy) {
        return this.toBuilder()
            .recordStatus(RecordStatus.ACTIVE)
            .updatedAt(LocalDateTime.now())
            .updatedBy(restoredBy)
            .build();
    }

    public Fare updateWith(Fare changes, String updatedBy) {
        var builder = this.toBuilder()
            .updatedAt(LocalDateTime.now())
            .updatedBy(updatedBy);

        if (changes.getFareType() != null) builder.fareType(changes.getFareType());
        if (changes.getAmount() != null) builder.amount(changes.getAmount());
        if (changes.getDescription() != null) builder.description(changes.getDescription());
        if (changes.getValidFrom() != null) builder.validFrom(changes.getValidFrom());
        if (changes.getValidTo() != null) builder.validTo(changes.getValidTo());

        return builder.build();
    }
}
```

---

### 📄 Parameter.java

```java
package pe.edu.vallegrande.vgmsorganizations.domain.models;

import lombok.Builder;
import lombok.Getter;
import pe.edu.vallegrande.vgmsorganizations.domain.models.valueobjects.RecordStatus;

import java.time.LocalDateTime;

@Getter
@Builder(toBuilder = true)
public class Parameter {

    private String id;
    private String organizationId;
    private String parameterType;
    private String parameterValue;
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

    public Parameter markAsDeleted(String deletedBy) {
        return this.toBuilder()
            .recordStatus(RecordStatus.INACTIVE)
            .updatedAt(LocalDateTime.now())
            .updatedBy(deletedBy)
            .build();
    }

    public Parameter restore(String restoredBy) {
        return this.toBuilder()
            .recordStatus(RecordStatus.ACTIVE)
            .updatedAt(LocalDateTime.now())
            .updatedBy(restoredBy)
            .build();
    }

    public Parameter updateWith(Parameter changes, String updatedBy) {
        var builder = this.toBuilder()
            .updatedAt(LocalDateTime.now())
            .updatedBy(updatedBy);

        if (changes.getParameterType() != null) builder.parameterType(changes.getParameterType());
        if (changes.getParameterValue() != null) builder.parameterValue(changes.getParameterValue());
        if (changes.getDescription() != null) builder.description(changes.getDescription());

        return builder.build();
    }
}
```

---

## 2️⃣ VALUE OBJECTS

### 📄 RecordStatus.java

```java
package pe.edu.vallegrande.vgmsorganizations.domain.models.valueobjects;

public enum RecordStatus {
    ACTIVE,
    INACTIVE
}
```

---

### 📄 StreetType.java

```java
package pe.edu.vallegrande.vgmsorganizations.domain.models.valueobjects;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StreetType {

    JR("Jr.", "Jirón"),
    AV("Av.", "Avenida"),
    CALLE("Calle", "Calle"),
    PASAJE("Psje.", "Pasaje");

    private final String prefix;
    private final String displayName;
}
```

---

## 3️⃣ PORTS IN - Casos de Uso (Interfaces)

### Organization Ports

#### 📄 ICreateOrganizationUseCase.java

```java
package pe.edu.vallegrande.vgmsorganizations.domain.ports.in;

import pe.edu.vallegrande.vgmsorganizations.domain.models.Organization;
import reactor.core.publisher.Mono;

public interface ICreateOrganizationUseCase {
    Mono<Organization> execute(Organization organization, String createdBy);
}
```

---

#### 📄 IGetOrganizationUseCase.java

```java
package pe.edu.vallegrande.vgmsorganizations.domain.ports.in;

import pe.edu.vallegrande.vgmsorganizations.domain.models.Organization;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IGetOrganizationUseCase {
    Mono<Organization> findById(String id);
    Flux<Organization> findAllActive();
    Flux<Organization> findAll();
}
```

---

#### 📄 IUpdateOrganizationUseCase.java

```java
package pe.edu.vallegrande.vgmsorganizations.domain.ports.in;

import pe.edu.vallegrande.vgmsorganizations.domain.models.Organization;
import reactor.core.publisher.Mono;

public interface IUpdateOrganizationUseCase {
    Mono<Organization> execute(String id, Organization changes, String updatedBy);
}
```

---

#### 📄 IDeleteOrganizationUseCase.java

```java
package pe.edu.vallegrande.vgmsorganizations.domain.ports.in;

import pe.edu.vallegrande.vgmsorganizations.domain.models.Organization;
import reactor.core.publisher.Mono;

public interface IDeleteOrganizationUseCase {
    Mono<Organization> execute(String id, String deletedBy, String reason);
}
```

---

#### 📄 IRestoreOrganizationUseCase.java

```java
package pe.edu.vallegrande.vgmsorganizations.domain.ports.in;

import pe.edu.vallegrande.vgmsorganizations.domain.models.Organization;
import reactor.core.publisher.Mono;

public interface IRestoreOrganizationUseCase {
    Mono<Organization> execute(String id, String restoredBy);
}
```

---

### Zone Ports

#### 📄 ICreateZoneUseCase.java

```java
package pe.edu.vallegrande.vgmsorganizations.domain.ports.in;

import pe.edu.vallegrande.vgmsorganizations.domain.models.Zone;
import reactor.core.publisher.Mono;

public interface ICreateZoneUseCase {
    Mono<Zone> execute(Zone zone, String createdBy);
}
```

---

#### 📄 IGetZoneUseCase.java

```java
package pe.edu.vallegrande.vgmsorganizations.domain.ports.in;

import pe.edu.vallegrande.vgmsorganizations.domain.models.Zone;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IGetZoneUseCase {
    Mono<Zone> findById(String id);
    Flux<Zone> findAllActive();
    Flux<Zone> findAll();
    Flux<Zone> findByOrganizationId(String organizationId);
    Flux<Zone> findActiveByOrganizationId(String organizationId);
}
```

---

#### 📄 IUpdateZoneUseCase.java

```java
package pe.edu.vallegrande.vgmsorganizations.domain.ports.in;

import pe.edu.vallegrande.vgmsorganizations.domain.models.Zone;
import reactor.core.publisher.Mono;

public interface IUpdateZoneUseCase {
    Mono<Zone> execute(String id, Zone changes, String updatedBy);
}
```

---

#### 📄 IDeleteZoneUseCase.java

```java
package pe.edu.vallegrande.vgmsorganizations.domain.ports.in;

import pe.edu.vallegrande.vgmsorganizations.domain.models.Zone;
import reactor.core.publisher.Mono;

public interface IDeleteZoneUseCase {
    Mono<Zone> execute(String id, String deletedBy, String reason);
}
```

---

#### 📄 IRestoreZoneUseCase.java

```java
package pe.edu.vallegrande.vgmsorganizations.domain.ports.in;

import pe.edu.vallegrande.vgmsorganizations.domain.models.Zone;
import reactor.core.publisher.Mono;

public interface IRestoreZoneUseCase {
    Mono<Zone> execute(String id, String restoredBy);
}
```

---

### Street Ports

#### 📄 ICreateStreetUseCase.java

```java
package pe.edu.vallegrande.vgmsorganizations.domain.ports.in;

import pe.edu.vallegrande.vgmsorganizations.domain.models.Street;
import reactor.core.publisher.Mono;

public interface ICreateStreetUseCase {
    Mono<Street> execute(Street street, String createdBy);
}
```

---

#### 📄 IGetStreetUseCase.java

```java
package pe.edu.vallegrande.vgmsorganizations.domain.ports.in;

import pe.edu.vallegrande.vgmsorganizations.domain.models.Street;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IGetStreetUseCase {
    Mono<Street> findById(String id);
    Flux<Street> findAllActive();
    Flux<Street> findAll();
    Flux<Street> findByZoneId(String zoneId);
    Flux<Street> findActiveByZoneId(String zoneId);
}
```

---

#### 📄 IUpdateStreetUseCase.java

```java
package pe.edu.vallegrande.vgmsorganizations.domain.ports.in;

import pe.edu.vallegrande.vgmsorganizations.domain.models.Street;
import reactor.core.publisher.Mono;

public interface IUpdateStreetUseCase {
    Mono<Street> execute(String id, Street changes, String updatedBy);
}
```

---

#### 📄 IDeleteStreetUseCase.java

```java
package pe.edu.vallegrande.vgmsorganizations.domain.ports.in;

import pe.edu.vallegrande.vgmsorganizations.domain.models.Street;
import reactor.core.publisher.Mono;

public interface IDeleteStreetUseCase {
    Mono<Street> execute(String id, String deletedBy, String reason);
}
```

---

#### 📄 IRestoreStreetUseCase.java

```java
package pe.edu.vallegrande.vgmsorganizations.domain.ports.in;

import pe.edu.vallegrande.vgmsorganizations.domain.models.Street;
import reactor.core.publisher.Mono;

public interface IRestoreStreetUseCase {
    Mono<Street> execute(String id, String restoredBy);
}
```

---

### Fare Ports

#### 📄 ICreateFareUseCase.java

```java
package pe.edu.vallegrande.vgmsorganizations.domain.ports.in;

import pe.edu.vallegrande.vgmsorganizations.domain.models.Fare;
import reactor.core.publisher.Mono;

public interface ICreateFareUseCase {
    Mono<Fare> execute(Fare fare, String createdBy);
}
```

---

#### 📄 IGetFareUseCase.java

```java
package pe.edu.vallegrande.vgmsorganizations.domain.ports.in;

import pe.edu.vallegrande.vgmsorganizations.domain.models.Fare;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IGetFareUseCase {
    Mono<Fare> findById(String id);
    Flux<Fare> findAllActive();
    Flux<Fare> findAll();
    Flux<Fare> findByOrganizationId(String organizationId);
    Flux<Fare> findActiveByOrganizationId(String organizationId);
}
```

---

#### 📄 IUpdateFareUseCase.java

```java
package pe.edu.vallegrande.vgmsorganizations.domain.ports.in;

import pe.edu.vallegrande.vgmsorganizations.domain.models.Fare;
import reactor.core.publisher.Mono;

public interface IUpdateFareUseCase {
    Mono<Fare> execute(String id, Fare changes, String updatedBy);
}
```

---

#### 📄 IDeleteFareUseCase.java

```java
package pe.edu.vallegrande.vgmsorganizations.domain.ports.in;

import pe.edu.vallegrande.vgmsorganizations.domain.models.Fare;
import reactor.core.publisher.Mono;

public interface IDeleteFareUseCase {
    Mono<Fare> execute(String id, String deletedBy, String reason);
}
```

---

#### 📄 IRestoreFareUseCase.java

```java
package pe.edu.vallegrande.vgmsorganizations.domain.ports.in;

import pe.edu.vallegrande.vgmsorganizations.domain.models.Fare;
import reactor.core.publisher.Mono;

public interface IRestoreFareUseCase {
    Mono<Fare> execute(String id, String restoredBy);
}
```

---

### Parameter Ports

#### 📄 ICreateParameterUseCase.java

```java
package pe.edu.vallegrande.vgmsorganizations.domain.ports.in;

import pe.edu.vallegrande.vgmsorganizations.domain.models.Parameter;
import reactor.core.publisher.Mono;

public interface ICreateParameterUseCase {
    Mono<Parameter> execute(Parameter parameter, String createdBy);
}
```

---

#### 📄 IGetParameterUseCase.java

```java
package pe.edu.vallegrande.vgmsorganizations.domain.ports.in;

import pe.edu.vallegrande.vgmsorganizations.domain.models.Parameter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IGetParameterUseCase {
    Mono<Parameter> findById(String id);
    Flux<Parameter> findAllActive();
    Flux<Parameter> findAll();
    Flux<Parameter> findByOrganizationId(String organizationId);
    Flux<Parameter> findActiveByOrganizationId(String organizationId);
}
```

---

#### 📄 IUpdateParameterUseCase.java

```java
package pe.edu.vallegrande.vgmsorganizations.domain.ports.in;

import pe.edu.vallegrande.vgmsorganizations.domain.models.Parameter;
import reactor.core.publisher.Mono;

public interface IUpdateParameterUseCase {
    Mono<Parameter> execute(String id, Parameter changes, String updatedBy);
}
```

---

#### 📄 IDeleteParameterUseCase.java

```java
package pe.edu.vallegrande.vgmsorganizations.domain.ports.in;

import pe.edu.vallegrande.vgmsorganizations.domain.models.Parameter;
import reactor.core.publisher.Mono;

public interface IDeleteParameterUseCase {
    Mono<Parameter> execute(String id, String deletedBy, String reason);
}
```

---

#### 📄 IRestoreParameterUseCase.java

```java
package pe.edu.vallegrande.vgmsorganizations.domain.ports.in;

import pe.edu.vallegrande.vgmsorganizations.domain.models.Parameter;
import reactor.core.publisher.Mono;

public interface IRestoreParameterUseCase {
    Mono<Parameter> execute(String id, String restoredBy);
}
```

---

## 4️⃣ PORTS OUT - Contratos de Infraestructura

### 📄 IOrganizationRepository.java

```java
package pe.edu.vallegrande.vgmsorganizations.domain.ports.out;

import pe.edu.vallegrande.vgmsorganizations.domain.models.Organization;
import pe.edu.vallegrande.vgmsorganizations.domain.models.valueobjects.RecordStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IOrganizationRepository {
    Mono<Organization> save(Organization organization);
    Mono<Organization> update(Organization organization);
    Mono<Organization> findById(String id);
    Mono<Organization> findByOrganizationName(String name);
    Flux<Organization> findAll();
    Flux<Organization> findByRecordStatus(RecordStatus status);
    Mono<Boolean> existsByOrganizationName(String name);
    Mono<Void> deleteById(String id);
}
```

---

### 📄 IZoneRepository.java

```java
package pe.edu.vallegrande.vgmsorganizations.domain.ports.out;

import pe.edu.vallegrande.vgmsorganizations.domain.models.Zone;
import pe.edu.vallegrande.vgmsorganizations.domain.models.valueobjects.RecordStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IZoneRepository {
    Mono<Zone> save(Zone zone);
    Mono<Zone> update(Zone zone);
    Mono<Zone> findById(String id);
    Flux<Zone> findAll();
    Flux<Zone> findByRecordStatus(RecordStatus status);
    Flux<Zone> findByOrganizationId(String organizationId);
    Flux<Zone> findByOrganizationIdAndRecordStatus(String organizationId, RecordStatus status);
    Mono<Boolean> existsByZoneNameAndOrganizationId(String zoneName, String organizationId);
    Mono<Void> deleteById(String id);
}
```

---

### 📄 IStreetRepository.java

```java
package pe.edu.vallegrande.vgmsorganizations.domain.ports.out;

import pe.edu.vallegrande.vgmsorganizations.domain.models.Street;
import pe.edu.vallegrande.vgmsorganizations.domain.models.valueobjects.RecordStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IStreetRepository {
    Mono<Street> save(Street street);
    Mono<Street> update(Street street);
    Mono<Street> findById(String id);
    Flux<Street> findAll();
    Flux<Street> findByRecordStatus(RecordStatus status);
    Flux<Street> findByZoneId(String zoneId);
    Flux<Street> findByZoneIdAndRecordStatus(String zoneId, RecordStatus status);
    Mono<Boolean> existsByStreetNameAndZoneId(String streetName, String zoneId);
    Mono<Void> deleteById(String id);
}
```

---

### 📄 IFareRepository.java

```java
package pe.edu.vallegrande.vgmsorganizations.domain.ports.out;

import pe.edu.vallegrande.vgmsorganizations.domain.models.Fare;
import pe.edu.vallegrande.vgmsorganizations.domain.models.valueobjects.RecordStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IFareRepository {
    Mono<Fare> save(Fare fare);
    Mono<Fare> update(Fare fare);
    Mono<Fare> findById(String id);
    Flux<Fare> findAll();
    Flux<Fare> findByRecordStatus(RecordStatus status);
    Flux<Fare> findByOrganizationId(String organizationId);
    Flux<Fare> findByOrganizationIdAndRecordStatus(String organizationId, RecordStatus status);
    Flux<Fare> findActiveByOrganizationAndType(String organizationId, String fareType);
    Mono<Void> deleteById(String id);
}
```

---

### 📄 IParameterRepository.java

```java
package pe.edu.vallegrande.vgmsorganizations.domain.ports.out;

import pe.edu.vallegrande.vgmsorganizations.domain.models.Parameter;
import pe.edu.vallegrande.vgmsorganizations.domain.models.valueobjects.RecordStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IParameterRepository {
    Mono<Parameter> save(Parameter parameter);
    Mono<Parameter> update(Parameter parameter);
    Mono<Parameter> findById(String id);
    Flux<Parameter> findAll();
    Flux<Parameter> findByRecordStatus(RecordStatus status);
    Flux<Parameter> findByOrganizationId(String organizationId);
    Flux<Parameter> findByOrganizationIdAndRecordStatus(String organizationId, RecordStatus status);
    Mono<Boolean> existsByParameterTypeAndOrganizationId(String parameterType, String organizationId);
    Mono<Void> deleteById(String id);
}
```

---

### 📄 IOrganizationEventPublisher.java

```java
package pe.edu.vallegrande.vgmsorganizations.domain.ports.out;

import pe.edu.vallegrande.vgmsorganizations.domain.models.Organization;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface IOrganizationEventPublisher {
    Mono<Void> publishOrganizationCreated(Organization organization, String createdBy);
    Mono<Void> publishOrganizationUpdated(Organization organization, Map<String, Object> changedFields, String updatedBy);
    Mono<Void> publishOrganizationDeleted(String organizationId, String reason, String deletedBy);
    Mono<Void> publishOrganizationRestored(String organizationId, String restoredBy);
}
```

---

### 📄 IZoneEventPublisher.java

```java
package pe.edu.vallegrande.vgmsorganizations.domain.ports.out;

import pe.edu.vallegrande.vgmsorganizations.domain.models.Zone;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface IZoneEventPublisher {
    Mono<Void> publishZoneCreated(Zone zone, String createdBy);
    Mono<Void> publishZoneUpdated(Zone zone, Map<String, Object> changedFields, String updatedBy);
    Mono<Void> publishZoneDeleted(String zoneId, String organizationId, String reason, String deletedBy);
    Mono<Void> publishZoneRestored(String zoneId, String organizationId, String restoredBy);
}
```

---

### 📄 IStreetEventPublisher.java

```java
package pe.edu.vallegrande.vgmsorganizations.domain.ports.out;

import pe.edu.vallegrande.vgmsorganizations.domain.models.Street;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface IStreetEventPublisher {
    Mono<Void> publishStreetCreated(Street street, String createdBy);
    Mono<Void> publishStreetUpdated(Street street, Map<String, Object> changedFields, String updatedBy);
    Mono<Void> publishStreetDeleted(String streetId, String zoneId, String reason, String deletedBy);
    Mono<Void> publishStreetRestored(String streetId, String zoneId, String restoredBy);
}
```

---

### 📄 IFareEventPublisher.java

```java
package pe.edu.vallegrande.vgmsorganizations.domain.ports.out;

import pe.edu.vallegrande.vgmsorganizations.domain.models.Fare;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface IFareEventPublisher {
    Mono<Void> publishFareCreated(Fare fare, String createdBy);
    Mono<Void> publishFareUpdated(Fare fare, Map<String, Object> changedFields, String updatedBy);
    Mono<Void> publishFareDeleted(String fareId, String organizationId, String reason, String deletedBy);
    Mono<Void> publishFareRestored(String fareId, String organizationId, String restoredBy);
}
```

---

### 📄 IParameterEventPublisher.java

```java
package pe.edu.vallegrande.vgmsorganizations.domain.ports.out;

import pe.edu.vallegrande.vgmsorganizations.domain.models.Parameter;
import reactor.core.publisher.Mono;

import java.util.Map;

public interface IParameterEventPublisher {
    Mono<Void> publishParameterCreated(Parameter parameter, String createdBy);
    Mono<Void> publishParameterUpdated(Parameter parameter, Map<String, Object> changedFields, String updatedBy);
    Mono<Void> publishParameterDeleted(String parameterId, String organizationId, String reason, String deletedBy);
    Mono<Void> publishParameterRestored(String parameterId, String organizationId, String restoredBy);
}
```

---

## 5️⃣ EXCEPTIONS - Excepciones de Dominio

### 📄 DomainException.java

```java
package pe.edu.vallegrande.vgmsorganizations.domain.exceptions;

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
package pe.edu.vallegrande.vgmsorganizations.domain.exceptions;

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
package pe.edu.vallegrande.vgmsorganizations.domain.exceptions;

public class BusinessRuleException extends DomainException {

    public BusinessRuleException(String message) {
        super(message, "BUSINESS_RULE_VIOLATION", 422);
    }
}
```

---

### 📄 ValidationException.java

```java
package pe.edu.vallegrande.vgmsorganizations.domain.exceptions;

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
package pe.edu.vallegrande.vgmsorganizations.domain.exceptions;

public class ConflictException extends DomainException {

    public ConflictException(String message) {
        super(message, "CONFLICT", 409);
    }
}
```

---

### 📄 OrganizationNotFoundException.java

```java
package pe.edu.vallegrande.vgmsorganizations.domain.exceptions;

public class OrganizationNotFoundException extends NotFoundException {

    public OrganizationNotFoundException(String id) {
        super("Organization", id);
    }
}
```

---

### 📄 ZoneNotFoundException.java

```java
package pe.edu.vallegrande.vgmsorganizations.domain.exceptions;

public class ZoneNotFoundException extends NotFoundException {

    public ZoneNotFoundException(String id) {
        super("Zona", id);
    }
}
```

---

### 📄 StreetNotFoundException.java

```java
package pe.edu.vallegrande.vgmsorganizations.domain.exceptions;

public class StreetNotFoundException extends NotFoundException {

    public StreetNotFoundException(String id) {
        super("Calle", id);
    }
}
```

---

### 📄 FareNotFoundException.java

```java
package pe.edu.vallegrande.vgmsorganizations.domain.exceptions;

public class FareNotFoundException extends NotFoundException {

    public FareNotFoundException(String id) {
        super("Tarifa", id);
    }
}
```

---

### 📄 ParameterNotFoundException.java

```java
package pe.edu.vallegrande.vgmsorganizations.domain.exceptions;

public class ParameterNotFoundException extends NotFoundException {

    public ParameterNotFoundException(String id) {
        super("Parameter", id);
    }
}
```

---

### 📄 DuplicateOrganizationException.java

```java
package pe.edu.vallegrande.vgmsorganizations.domain.exceptions;

public class DuplicateOrganizationException extends ConflictException {

    public DuplicateOrganizationException(String organizationName) {
        super(String.format("Organization with name '%s' already exists", organizationName));
    }
}
```

---

## ✅ Resumen de la Capa de Dominio

| Componente | Cantidad | Descripción |
|------------|----------|-------------|
| Modelos | 5 clases | Organization, Zone, Street, Fare, Parameter |
| Value Objects | 4 enums | RecordStatus, StreetType, FareType, ParameterType |
| Ports In | 25 interfaces | 5 por entidad (Create, Get, Update, Delete, Restore) |
| Ports Out | 10 interfaces | 5 repositorios + 5 event publishers |
| Exceptions | 11 clases | Base + específicas por entidad |

---

> **Siguiente paso**: Lee [README_APPLICATION.md](README_APPLICATION.md) para ver la implementación de los casos de uso.
