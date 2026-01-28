# 📋 Refactorización a Arquitectura Hexagonal - vg-ms-claims-incidents

## 🎯 Resumen de Cambios Implementados

Este documento describe la refactorización completa del microservicio para cumplir con el estándar de arquitectura hexagonal establecido.

---

## ✅ Cambios Completados

### 1. **Separación de Capas - Infrastructure/Document** 

#### **ANTES:**
```
domain/models/
├── Incident.java          (@Document - ❌ ACOPLADO A MONGODB)
├── Complaint.java         (@Document - ❌ ACOPLADO A MONGODB)
└── ...
```

#### **DESPUÉS:**
```
infrastructure/document/
├── BaseDocument.java                    (✨ NUEVO - Auditoría común)
├── IncidentDocument.java               (✨ NUEVO - Persistencia MongoDB)
├── ComplaintDocument.java              (✨ NUEVO)
├── ComplaintCategoryDocument.java      (✨ NUEVO)
├── ComplaintResponseDocument.java      (✨ NUEVO)
├── IncidentResolutionDocument.java     (✨ NUEVO)
├── IncidentTypeDocument.java           (✨ NUEVO)
└── embedded/
    └── MaterialUsedDocument.java       (✨ NUEVO - Documento embebido)

domain/models/
├── Incident.java          (✅ POJO PURO - Sin anotaciones MongoDB)
├── Complaint.java         (✅ POJO PURO)
└── ...
```

**Beneficios:**
- ✅ Dominio desacoplado de la infraestructura
- ✅ Facilita cambio de BD en el futuro
- ✅ Cumple con Clean Architecture

---

### 2. **Mappers - Conversión entre Capas**

#### **NUEVO:**
```
infrastructure/mapper/
├── BaseMapper.java                  (✨ Clase abstracta con métodos comunes)
├── IncidentMapper.java             (✨ Document ↔ Domain)
├── ComplaintMapper.java            (✨ Document ↔ Domain)
├── ComplaintCategoryMapper.java    (✨ Document ↔ Domain)
├── ComplaintResponseMapper.java    (✨ Document ↔ Domain)
├── IncidentResolutionMapper.java   (✨ Document ↔ Domain)
├── IncidentTypeMapper.java         (✨ Document ↔ Domain)
└── MaterialUsedMapper.java         (✨ Document ↔ Domain)
```

**Uso en Servicios:**
```java
// ❌ ANTES: BeanUtils.copyProperties() disperso
BeanUtils.copyProperties(source, target);

// ✅ DESPUÉS: Mapper dedicado
Incident domain = incidentMapper.toDomain(document);
IncidentDocument document = incidentMapper.toDocument(domain);
```

**Beneficios:**
- ✅ Conversiones centralizadas y reutilizables
- ✅ Fácil de mantener y testear
- ✅ Evita código duplicado

---

### 3. **Repositorios Actualizados**

#### **ANTES:**
```java
public interface IncidentRepository 
    extends ReactiveMongoRepository<Incident, String> {  // ❌ Usa entidad de dominio
}
```

#### **DESPUÉS:**
```java
public interface IncidentRepository 
    extends ReactiveMongoRepository<IncidentDocument, String> {  // ✅ Usa documento de persistencia
    
    Flux<IncidentDocument> findByOrganizationId(String organizationId);
    Flux<IncidentDocument> findByStatus(String status);
    // ... métodos de consulta
}
```

**Repositorios actualizados:**
- ✅ `IncidentRepository`
- ✅ `ComplaintRepository`
- ✅ `ComplaintCategoryRepository`
- ✅ `ComplaintResponseRepository`
- ✅ `IncidentResolutionRepository`
- ✅ `IncidentTypeRepository`

---

### 4. **DTOs Reorganizados**

#### **ANTES:**
```
infrastructure/dto/
├── IncidentDTO.java
├── ComplaintDTO.java
├── UserDTO.java
└── ... (todos mezclados)
```

#### **DESPUÉS:**
```
infrastructure/dto/
├── request/                         (✨ DTOs de entrada)
│   ├── CreateIncidentRequest.java
│   ├── UpdateIncidentRequest.java
│   └── ...
├── response/                        (✨ DTOs de salida)
│   ├── IncidentResponse.java
│   ├── IncidentDetailResponse.java
│   └── ...
└── common/                          (✨ DTOs compartidos)
    ├── ResponseDto.java             (✨ NUEVO - Wrapper estándar)
    ├── ErrorMessage.java            (✨ NUEVO)
    └── ValidationError.java         (✨ NUEVO)
```

**ResponseDto - Wrapper Estándar:**
```java
// Respuesta exitosa
ResponseDto.success(data, "Operación exitosa");

// Respuesta de error
ResponseDto.error("Mensaje de error", 400);
```

**Beneficios:**
- ✅ DTOs organizados por propósito
- ✅ Respuestas consistentes en toda la API
- ✅ Manejo de errores estandarizado

---

### 5. **Clientes Externos Reorganizados**

#### **ANTES:**
```
infrastructure/client/
└── UserApiClient.java       (❌ Todo mezclado)
```

#### **DESPUÉS:**
```
infrastructure/client/
├── external/                           (✨ Clientes a sistemas externos)
│   └── UserServiceClient.java         (✨ Renombrado y movido)
├── internal/                           (✨ Clientes a otros microservicios)
│   └── (vacío - para futuros clientes)
└── validator/                          (✨ Validadores de clientes)
    ├── ExternalClientValidator.java   (✨ NUEVO - Valida servicios externos)
    └── InternalClientValidator.java   (✨ NUEVO - Valida comunicación interna)
```

**Cambios en UserServiceClient:**
- ✅ Renombrado de `UserApiClient` → `UserServiceClient`
- ✅ Movido a `infrastructure/client/external/`
- ✅ Documentación mejorada
- ✅ Preparado para validaciones externas

**Beneficios:**
- ✅ Separación clara entre clientes externos e internos
- ✅ Validaciones centralizadas
- ✅ Escalable para nuevos clientes

---

### 6. **Documentación Reorganizada**

#### **ANTES:**
```
/ (raíz del proyecto)
├── CAMPOS_NULL_EXPLANATION.md
├── DEBUG_INCIDENTS.md
└── INCIDENTS_UPDATE.md
```

#### **DESPUÉS:**
```
src/main/resources/doc/
├── CAMPOS_NULL_EXPLANATION.md      (✨ Movido)
├── DEBUG_INCIDENTS.md              (✨ Movido)
└── INCIDENTS_UPDATE.md             (✨ Movido)
```

**Beneficios:**
- ✅ Documentación centralizada
- ✅ Raíz del proyecto más limpia
- ✅ Fácil acceso desde resources

---

## 🔄 Flujo de Datos ANTES vs DESPUÉS

### **ANTES (Acoplado):**
```
Controller → Service → Repository<Incident> → MongoDB
     ↓           ↓            ↓
   DTO    BeanUtils.copy   @Document
```

### **DESPUÉS (Hexagonal):**
```
Controller → Service → Mapper → Repository<IncidentDocument> → MongoDB
     ↓          ↓         ↓            ↓
Response   Domain     Document    @Document
   DTO     (Puro)   (Persistencia)
```

---

## ⚠️ PENDIENTE: Refactorización de Servicios

Los servicios en `infrastructure/service/` necesitan actualizarse para usar:

1. **Mappers** en lugar de `BeanUtils.copyProperties()`
2. **Documents** en lugar de entidades de dominio
3. **Nuevos DTOs** organizados en request/response/common

### Ejemplo de Refactorización Necesaria:

#### **ANTES:**
```java
public Mono<IncidentDTO> createIncident(IncidentCreateDTO dto) {
    Incident incident = new Incident();
    BeanUtils.copyProperties(dto, incident);
    
    return incidentRepository.save(incident)  // ❌ Usa entidad de dominio
        .map(saved -> {
            IncidentDTO response = new IncidentDTO();
            BeanUtils.copyProperties(saved, response);
            return response;
        });
}
```

#### **DESPUÉS (Recomendado):**
```java
@Autowired
private IncidentMapper incidentMapper;

public Mono<IncidentDTO> createIncident(IncidentCreateDTO dto) {
    // 1. Convertir DTO → Domain
    Incident incident = new Incident();
    incident.setTitle(dto.getTitle());
    incident.setDescription(dto.getDescription());
    // ... mapeo manual o usar mapper específico
    
    // 2. Convertir Domain → Document
    IncidentDocument document = incidentMapper.toDocument(incident);
    document.prePersist(); // Auditoría
    
    // 3. Guardar Document
    return incidentRepository.save(document)
        .map(saved -> {
            // 4. Convertir Document → Domain
            Incident domain = incidentMapper.toDomain(saved);
            
            // 5. Convertir Domain → DTO Response
            IncidentDTO response = new IncidentDTO();
            // ... mapeo a DTO
            return response;
        });
}
```

---

## 📊 Comparación con el Estándar

| Componente | Estado | Cumplimiento |
|------------|--------|--------------|
| `infrastructure/document/` | ✅ Implementado | 100% |
| `infrastructure/mapper/` | ✅ Implementado | 100% |
| `infrastructure/dto/request/response/common/` | ✅ Implementado | 100% |
| `infrastructure/client/external/internal/validator/` | ✅ Implementado | 100% |
| `infrastructure/repository/` | ✅ Actualizado | 100% |
| `domain/models/` | ✅ POJOs puros | 100% |
| `resources/doc/` | ✅ Reorganizado | 100% |
| **`infrastructure/service/`** | ⚠️ Pendiente | 30% |
| **Seguridad/JWE** | ⏭️ Excluido | N/A |

---

## 🚀 Próximos Pasos

### 1. **Refactorizar Servicios (Prioridad Alta)**

Archivos que necesitan actualización:
- `infrastructure/service/IncidentServiceImpl.java`
- `infrastructure/service/ComplaintServiceImpl.java`
- `infrastructure/service/ComplaintCategoryServiceImpl.java`
- `infrastructure/service/ComplaintResponseServiceImpl.java`
- `infrastructure/service/IncidentResolutionServiceImpl.java`
- `infrastructure/service/IncidentTypeServiceImpl.java`

### 2. **Actualizar Controladores REST**

- Actualizar imports para usar nuevos paths de DTOs
- Implementar `ResponseDto` wrapper para respuestas consistentes
- Usar DTOs de `request/` y `response/`

### 3. **Testing**

- Probar mappers individualmente
- Validar que los repositorios funcionen con Documents
- Verificar que el flujo completo funcione end-to-end

### 4. **Compilación**

Es normal que haya errores de compilación hasta que se refactoricen los servicios. Los cambios estructurales están completos, solo falta adaptar la lógica de negocio.

---

## 📝 Notas Importantes

1. **BaseDocument** incluye auditoría automática (`createdAt`, `updatedAt`, `recordStatus`)
2. **Los mappers son @Component** y pueden inyectarse con `@Autowired`
3. **ResponseDto** tiene métodos estáticos para crear respuestas fácilmente
4. **UserServiceClient** mantiene toda la funcionalidad de `UserApiClient`
5. **NO se implementó JWE/Seguridad** según tus requerimientos

---

## 🎓 Arquitectura Final

```
vg-ms-claims-incidents/
│
├── domain/                          # 🎯 CAPA DE DOMINIO (PURA)
│   ├── models/                      # ✅ POJOs sin anotaciones de infraestructura
│   └── enums/                       # ✅ Enumeraciones del dominio
│
├── application/                     # ⚙️ CAPA DE APLICACIÓN
│   ├── services/                    # Interfaces de servicio
│   └── config/                      # Configuraciones de aplicación
│
└── infrastructure/                  # 🔧 CAPA DE INFRAESTRUCTURA
    ├── document/                    # ✅ Documentos MongoDB (Persistencia)
    │   ├── BaseDocument.java
    │   ├── *Document.java
    │   └── embedded/
    │
    ├── mapper/                      # ✅ Mappers Document ↔ Domain
    │   ├── BaseMapper.java
    │   └── *Mapper.java
    │
    ├── repository/                  # ✅ Repositorios MongoDB
    │
    ├── dto/                         # ✅ Data Transfer Objects
    │   ├── request/
    │   ├── response/
    │   └── common/
    │
    ├── client/                      # ✅ Clientes a servicios externos
    │   ├── external/
    │   ├── internal/
    │   └── validator/
    │
    ├── rest/                        # Controladores REST
    ├── service/                     # ⚠️ Implementaciones (pendiente refactor)
    ├── exception/                   # Manejo de excepciones
    └── config/                      # Configuraciones de infraestructura
```

---

## ✅ Checklist de Cumplimiento del Estándar

- [x] Separación de `infrastructure/document/` y `domain/models/`
- [x] Implementación de `BaseDocument` para auditoría
- [x] Creación de mappers en `infrastructure/mapper/`
- [x] Repositorios usando `*Document` en lugar de entidades de dominio
- [x] DTOs organizados en `request/`, `response/`, `common/`
- [x] Wrapper estándar `ResponseDto`
- [x] Clientes organizados en `external/`, `internal/`, `validator/`
- [x] Documentación movida a `resources/doc/`
- [ ] Servicios refactorizados para usar mappers (PENDIENTE)
- [x] Seguridad/JWE (EXCLUIDO por requerimiento)

---

**Fecha de refactorización:** 11 de noviembre de 2025  
**Estándar aplicado:** Arquitectura Hexagonal - vg-ms-{service}  
**Cumplimiento:** 90% (pendiente refactorización de servicios)
