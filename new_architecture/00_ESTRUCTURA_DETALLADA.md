# 📂 ESTRUCTURA DETALLADA MAESTRA - 11 MICROSERVICIOS

Este documento contiene la **ESTRUCTURA COMPLETA Y DEFINITIVA** de TODOS los microservicios del proyecto JASS Digital, con cada archivo, cada clase, cada configuración definida.

> **📌 NOTA IMPORTANTE**: Cada microservicio es INDEPENDIENTE y tiene sus propias clases base (no hay paquete compartido entre servicios).

---

## 1. 📦 vg-ms-users {#estructura-users}

```text
vg-ms-users/
├── src/main/
│   ├── java/pe/edu/vallegrande/vgmsusers/
│   │   ├── domain/
│   │   │   ├── models/
│   │   │   │   ├── User.java                           → [CLASS] extends BaseEntity
│   │   │   │   │                                         Campos: firstName, lastName,
│   │   │   │   │                                         documentType, documentNumber,
│   │   │   │   │                                         email (OPCIONAL), phone (OPCIONAL),
│   │   │   │   │                                         address, zoneId, streetId, role
│   │   │   │   └── valueobjects/
│   │   │   │       ├── Role.java                       → [ENUM] SUPER_ADMIN, ADMIN, CLIENT
│   │   │   │       └── DocumentType.java               → [ENUM] DNI, RUC, CE
│   │   │   ├── ports/
│   │   │   │   ├── in/
│   │   │   │   │   ├── ICreateUserUseCase.java         → [INTERFACE]
│   │   │   │   │   ├── IGetUserUseCase.java            → [INTERFACE]
│   │   │   │   │   ├── IUpdateUserUseCase.java         → [INTERFACE]
│   │   │   │   │   └── IDeleteUserUseCase.java         → [INTERFACE]
│   │   │   │   └── out/
│   │   │   │       ├── IUserRepository.java            → [INTERFACE] Reactivo (Mono/Flux)
│   │   │   │       ├── IAuthenticationClient.java      → [INTERFACE] Crear usuario en Keycloak
│   │   │   │       ├── IOrganizationClient.java        → [INTERFACE] Validar org/zona/calle
│   │   │   │       ├── INotificationClient.java        → [INTERFACE] Enviar WhatsApp
│   │   │   │       └── IUserEventPublisher.java        → [INTERFACE] RabbitMQ
│   │   │   └── exceptions/
│   │   │       ├── UserNotFoundException.java          → [CLASS] extends RuntimeException
│   │   │       ├── OrganizationNotFoundException.java  → [CLASS] extends RuntimeException
│   │   │       └── InvalidContactException.java        → [CLASS] Al menos email O phone requerido
│   │   │
│   │   ├── application/
│   │   │   ├── usecases/
│   │   │   │   ├── CreateUserUseCaseImpl.java          → [CLASS] @Service
│   │   │   │   ├── GetUserUseCaseImpl.java             → [CLASS] @Service
│   │   │   │   └── UpdateUserUseCaseImpl.java          → [CLASS] @Service
│   │   │   ├── dto/
│   │   │   │   ├── common/
│   │   │   │   │   ├── ApiResponse.java                → [CLASS] ✅ ESTÁNDAR (Wrapper)
│   │   │   │   │   ├── PageResponse.java               → [CLASS] ✅ ESTÁNDAR (Paginación)
│   │   │   │   │   └── ErrorMessage.java               → [CLASS] ✅ ESTÁNDAR (Errores)
│   │   │   │   ├── request/
│   │   │   │   │   ├── CreateUserRequest.java          → [CLASS] @Valid
│   │   │   │   │   └── UpdateUserRequest.java          → [CLASS] @Valid
│   │   │   │   └── response/
│   │   │   │       └── UserResponse.java               → [CLASS] DTO
│   │   │   ├── mappers/
│   │   │   │   └── UserMapper.java                     → [CLASS] @Component
│   │   │   └── events/
│   │   │       ├── UserCreatedEvent.java               → [CLASS] Evento
│   │   │       └── publishers/
│   │   │           └── UserEventPublisherImpl.java     → [CLASS] @Component
│   │   │
│   │   └── infrastructure/
│   │       ├── adapters/
│   │       │   ├── in/
│   │       │   │   └── rest/
│   │       │   │       ├── UserRest.java         → [CLASS] @RestController
│   │       │   │       └── GlobalExceptionHandler.java     → [CLASS] @RestControllerAdvice
│   │       │   └── out/
│   │       │       ├── persistence/
│   │       │       │   └── UserRepositoryImpl.java     → [CLASS] @Repository
│   │       │       └── external/
│   │       │           ├── AuthenticationClientImpl.java → [CLASS] @Component
│   │       │           ├── OrganizationClientImpl.java → [CLASS] @Component (validar org/zona/calle)
│   │       │           └── NotificationClientImpl.java → [CLASS] @Component
│   │       ├── persistence/
│   │       │   ├── entities/
│   │       │   │   └── UserEntity.java                 → [CLASS] @Table("users")
│   │       │   └── repositories/
│   │       │       └── UserR2dbcRepository.java        → [INTERFACE] R2dbcRepository
│   │       └── config/
│   │           ├── R2dbcConfig.java                    → [CLASS] @Configuration
│   │           ├── WebClientConfig.java                → [CLASS] @Configuration
│   │           ├── RabbitMQConfig.java                 → [CLASS] @Configuration
│   │           ├── Resilience4jConfig.java             → [CLASS] @Configuration
│   │           ├── SecurityConfig.java                 → [CLASS] @Configuration
│   │           └── RequestContextFilter.java           → [CLASS] @Component WebFilter
│   │
│   └── resources/
│       ├── application.yml                             → Base común
│       ├── application-dev.yml                         → Docker local
│       ├── application-prod.yml                        → Docker Compose VPC
│       └── db/migration/
│           └── V1__create_users_table.sql              → SQL Script
│
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

---

## 2. 📦 vg-ms-authentication {#estructura-authentication}

> **⚠️ IMPORTANTE**: Este servicio es un **PROXY a Keycloak**. NO guarda passwords en base de datos.

```text
vg-ms-authentication/
├── src/main/
│   ├── java/pe/edu/vallegrande/vgmsauthentication/
│   │   ├── domain/
│   │   │   ├── models/
│   │   │   │   └── UserCredentials.java                → [CLASS] DTO temporal (NO persiste)
│   │   │   │                                             username, password (solo para request)
│   │   │   ├── ports/
│   │   │   │   ├── in/
│   │   │   │   │   ├── ILoginUseCase.java              → [INTERFACE]
│   │   │   │   │   ├── IRegisterUserUseCase.java       → [INTERFACE] Crea usuario en Keycloak
│   │   │   │   │   └── IRefreshTokenUseCase.java       → [INTERFACE]
│   │   │   │   └── out/
│   │   │   │       ├── IKeycloakClient.java            → [INTERFACE] Admin API Keycloak
│   │   │   │       └── IUserServiceClient.java         → [INTERFACE] WebClient a vg-ms-users
│   │   │   └── exceptions/
│   │   │       ├── InvalidCredentialsException.java    → [CLASS]
│   │   │       └── KeycloakException.java              → [CLASS]
│   │   │
│   │   ├── application/
│   │   │   ├── usecases/
│   │   │   │   ├── LoginUseCaseImpl.java               → [CLASS] @Service (delega a Keycloak)
│   │   │   │   ├── RegisterUserUseCaseImpl.java        → [CLASS] @Service
│   │   │   │   └── RefreshTokenUseCaseImpl.java        → [CLASS] @Service
│   │   │   ├── dto/
│   │   │   │   ├── common/
│   │   │   │   │   ├── ApiResponse.java                → [CLASS] ✅ ESTÁNDAR (Wrapper)
│   │   │   │   │   ├── PageResponse.java               → [CLASS] ✅ ESTÁNDAR (Paginación)
│   │   │   │   │   └── ErrorMessage.java               → [CLASS] ✅ ESTÁNDAR (Errores)
│   │   │   │   ├── request/
│   │   │   │   │   ├── LoginRequest.java               → [CLASS] { username, password }
│   │   │   │   │   ├── RegisterUserRequest.java        → [CLASS]
│   │   │   │   │   └── RefreshTokenRequest.java        → [CLASS]
│   │   │   │   └── response/
│   │   │   │       └── LoginResponse.java              → [CLASS] { accessToken, refreshToken, expiresIn }
│   │   │   └── security/
│   │   │       └── JwtValidator.java                   → [CLASS] @Component
│   │   │
│   │   └── infrastructure/
│   │       ├── adapters/
│   │       │   ├── in/
│   │       │   │   └── rest/
│   │       │   │       ├── AuthRest.java         → [CLASS] @RestController
│   │       │   │       └── GlobalExceptionHandler.java     → [CLASS] @RestControllerAdvice
│   │       │   └── out/
│   │       │       └── external/
│   │       │           ├── KeycloakClientImpl.java     → [CLASS] @Component (Admin API)
│   │       │           └── UserServiceClientImpl.java  → [CLASS] @Component
│   │       ├── messaging/
│   │       │   └── listeners/
│   │       │       └── UserEventListener.java          → [CLASS] @Component @RabbitListener (user.created)
│   │       └── config/
│   │           ├── KeycloakConfig.java                 → [CLASS] Keycloak Admin Client
│   │           ├── WebClientConfig.java                → [CLASS]
│   │           ├── Resilience4jConfig.java             → [CLASS] Circuit Breaker
│   │           └── SecurityConfig.java                 → [CLASS]
│   │
│   └── resources/
│       ├── application.yml
│       ├── application-dev.yml
│       └── application-prod.yml
│
├── Dockerfile
├── pom.xml
└── README.md
```

**NOTAS**:
- ❌ NO hay tabla `credentials` ni PostgreSQL
- ✅ TODA la autenticación se maneja en Keycloak
- ✅ Este servicio solo CONSULTA y CREA usuarios en Keycloak via Admin API

---

## 3. 📦 vg-ms-organizations {#estructura-organizations}

```text
vg-ms-organizations/
├── src/main/
│   ├── java/pe/edu/vallegrande/vgmsorganizations/
│   │   ├── domain/
│   │   │   ├── models/
│   │   │   │   ├── Organization.java                   → [CLASS] Organización/JASS
│   │   │   │   ├── Zone.java                           → [CLASS] Zonas geográficas
│   │   │   │   ├── Street.java                         → [CLASS] Calles por zona
│   │   │   │   ├── Fare.java                           → [CLASS] Tarifas (MONTHLY_FEE, INSTALLATION_FEE, etc.)
│   │   │   │   ├── Parameter.java                      → [CLASS] Parámetros de configuración
│   │   │   │   └── valueobjects/
│   │   │   │       ├── OrganizationType.java           → [ENUM] JASS, JAAS, OMSABAR
│   │   │   │       ├── FareType.java                   → [ENUM] MONTHLY_FEE, INSTALLATION_FEE, RECONNECTION_FEE, LATE_FEE, TRANSFER_FEE
│   │   │   │       ├── StreetType.java                 → [ENUM] JR, AV, CALLE, PASAJE
│   │   │   │       ├── ParameterType.java              → [ENUM] BILLING_DAY, GRACE_PERIOD, etc.
│   │   │   │       └── RecordStatus.java               → [ENUM] ACTIVE, INACTIVE
│   │   │   ├── ports/
│   │   │   │   ├── in/
│   │   │   │   │   ├── ICreateOrganizationUseCase.java → [INTERFACE]
│   │   │   │   │   ├── ICreateZoneUseCase.java         → [INTERFACE]
│   │   │   │   │   └── ICreateStreetUseCase.java       → [INTERFACE]
│   │   │   │   └── out/
│   │   │   │       ├── IOrganizationRepository.java    → [INTERFACE] Reactive
│   │   │   │       ├── IZoneRepository.java            → [INTERFACE]
│   │   │   │       └── IStreetRepository.java          → [INTERFACE]
│   │   │   └── exceptions/
│   │   │       └── OrganizationNotFoundException.java  → [CLASS]
│   │   │
│   │   ├── application/
│   │   │   ├── usecases/
│   │   │   │   ├── CreateOrganizationUseCaseImpl.java  → [CLASS] @Service
│   │   │   │   ├── CreateZoneUseCaseImpl.java          → [CLASS] @Service
│   │   │   │   └── CreateStreetUseCaseImpl.java        → [CLASS] @Service
│   │   │   ├── dto/
│   │   │   │   ├── common/
│   │   │   │   │   ├── ApiResponse.java                → [CLASS]
│   │   │   │   │   ├── PageResponse.java               → [CLASS] ✅ ESTÁNDAR (Paginación)
│   │   │   │   │   └── ErrorMessage.java               → [CLASS]
│   │   │   │   ├── request/
│   │   │   │   │   ├── CreateOrganizationRequest.java  → [CLASS]
│   │   │   │   │   ├── CreateZoneRequest.java          → [CLASS]
│   │   │   │   │   └── CreateStreetRequest.java        → [CLASS]
│   │   │   │   └── response/
│   │   │   │       ├── OrganizationResponse.java       → [CLASS]
│   │   │   │       ├── ZoneResponse.java               → [CLASS]
│   │   │   │       └── StreetResponse.java             → [CLASS]
│   │   │   ├── mappers/
│   │   │   │   ├── OrganizationMapper.java             → [CLASS] @Component
│   │   │   │   ├── ZoneMapper.java                     → [CLASS] @Component
│   │   │   │   └── StreetMapper.java                   → [CLASS] @Component
│   │   │   └── events/
│   │   │       ├── OrganizationCreatedEvent.java       → [CLASS]
│   │   │       └── publishers/
│   │   │           └── OrganizationEventPublisherImpl.java → [CLASS]
│   │   │
│   │   └── infrastructure/
│   │       ├── adapters/
│   │       │   ├── in/
│   │       │   │   └── rest/
│   │       │   │       ├── OrganizationRest.java → [CLASS] @RestController
│   │       │   │       ├── ZoneRest.java         → [CLASS] @RestController
│   │       │   │       ├── StreetRest.java       → [CLASS] @RestController
│   │       │   │       └── GlobalExceptionHandler.java     → [CLASS] @RestControllerAdvice
│   │       │   └── out/
│   │       │       └── persistence/
│   │       │           └── OrganizationRepositoryImpl.java → [CLASS] @Repository
│   │       ├── persistence/
│   │       │   ├── documents/
│   │       │   │   ├── OrganizationDocument.java       → [CLASS] @Document("organizations")
│   │       │   │   ├── ZoneDocument.java               → [CLASS] @Document("zones")
│   │       │   │   ├── StreetDocument.java             → [CLASS] @Document("streets")
│   │       │   │   ├── FareDocument.java               → [CLASS] @Document("fares")
│   │       │   │   └── ParameterDocument.java          → [CLASS] @Document("parameters")
│   │       │   └── repositories/
│   │       │       ├── OrganizationMongoRepository.java → [INTERFACE] ReactiveMongoRepository
│   │       │       ├── ZoneMongoRepository.java        → [INTERFACE]
│   │       │       ├── StreetMongoRepository.java      → [INTERFACE]
│   │       │       ├── FareMongoRepository.java        → [INTERFACE]
│   │       │       └── ParameterMongoRepository.java   → [INTERFACE]
│   │       └── config/
│   │           ├── MongoConfig.java                    → [CLASS] @Configuration
│   │           ├── RabbitMQConfig.java                 → [CLASS] @Configuration
│   │           ├── SecurityConfig.java                 → [CLASS] @Configuration
│   │           └── RequestContextFilter.java           → [CLASS] @Component
│   │
│   └── resources/
│       ├── application.yml
│       ├── application-dev.yml
│       └── application-prod.yml
│
├── Dockerfile
├── pom.xml
└── README.md
```

---

## 4. 📦 vg-ms-commercial-operations {#estructura-commercial}

> **Responsabilidad**: Facturación (Recibos), Pagos, Deudas, Cortes de Servicio, Caja Chica.

```text
vg-ms-commercial-operations/
├── src/main/
│   ├── java/pe/edu/vallegrande/vgmscommercial/
│   │   ├── domain/
│   │   │   ├── models/
│   │   │   │   ├── Receipt.java                        → [CLASS] Recibo mensual (PRINCIPAL)
│   │   │   │   ├── ReceiptDetail.java                  → [CLASS] Detalles del recibo
│   │   │   │   ├── Payment.java                        → [CLASS] Pago principal
│   │   │   │   ├── PaymentDetail.java                  → [CLASS] Detalles/desglose del pago
│   │   │   │   ├── Debt.java                           → [CLASS] Deuda pendiente
│   │   │   │   ├── ServiceCut.java                     → [CLASS] Cortes de servicio
│   │   │   │   ├── PettyCash.java                      → [CLASS] Caja chica
│   │   │   │   ├── PettyCashMovement.java              → [CLASS] Movimientos de caja
│   │   │   │   └── valueobjects/
│   │   │   │       ├── ConceptType.java                → [ENUM] MONTHLY_FEE, INSTALLATION_FEE, RECONNECTION_FEE, LATE_FEE, TRANSFER_FEE, OTHER
│   │   │   │       ├── ReceiptStatus.java              → [ENUM] PENDING, PAID, OVERDUE, CANCELLED
│   │   │   │       ├── PaymentType.java                → [ENUM] MONTHLY_FEE, INSTALLATION_FEE, RECONNECTION_FEE, etc.
│   │   │   │       ├── PaymentMethod.java              → [ENUM] CASH, BANK_TRANSFER, CARD, YAPE, PLIN
│   │   │   │       ├── PaymentStatus.java              → [ENUM] PENDING, COMPLETED, CANCELLED, FAILED
│   │   │   │       ├── DebtStatus.java                 → [ENUM] PENDING, PARTIAL, PAID, CANCELLED
│   │   │   │       ├── CutReason.java                  → [ENUM] NON_PAYMENT, MAINTENANCE, USER_REQUEST, REGULATORY
│   │   │   │       ├── CutStatus.java                  → [ENUM] PENDING, EXECUTED, RECONNECTED, CANCELLED
│   │   │   │       ├── MovementType.java               → [ENUM] IN, OUT, ADJUSTMENT
│   │   │   │       └── RecordStatus.java               → [ENUM] ACTIVE, INACTIVE
│   │   │   ├── ports/
│   │   │   │   ├── in/
│   │   │   │   │   ├── ICreateReceiptUseCase.java      → [INTERFACE]
│   │   │   │   │   ├── IGetReceiptUseCase.java         → [INTERFACE]
│   │   │   │   │   ├── ICreatePaymentUseCase.java      → [INTERFACE]
│   │   │   │   │   ├── IGetPaymentUseCase.java         → [INTERFACE]
│   │   │   │   │   ├── ICreateDebtUseCase.java         → [INTERFACE]
│   │   │   │   │   ├── IGetDebtUseCase.java            → [INTERFACE]
│   │   │   │   │   ├── ICreateServiceCutUseCase.java   → [INTERFACE]
│   │   │   │   │   ├── IGetServiceCutUseCase.java      → [INTERFACE]
│   │   │   │   │   ├── ICreatePettyCashUseCase.java    → [INTERFACE]
│   │   │   │   │   ├── IGetPettyCashUseCase.java       → [INTERFACE]
│   │   │   │   │   ├── IRegisterMovementUseCase.java   → [INTERFACE]
│   │   │   │   │   └── IGetPettyCashBalanceUseCase.java → [INTERFACE]
│   │   │   │   └── out/
│   │   │   │       ├── IReceiptRepository.java         → [INTERFACE]
│   │   │   │       ├── IReceiptDetailRepository.java   → [INTERFACE]
│   │   │   │       ├── IPaymentRepository.java         → [INTERFACE]
│   │   │   │       ├── IPaymentDetailRepository.java   → [INTERFACE]
│   │   │   │       ├── IDebtRepository.java            → [INTERFACE]
│   │   │   │       ├── IServiceCutRepository.java      → [INTERFACE]
│   │   │   │       ├── IPettyCashRepository.java       → [INTERFACE]
│   │   │   │       ├── IPettyCashMovementRepository.java → [INTERFACE]
│   │   │   │       ├── IUserServiceClient.java         → [INTERFACE] WebClient a vg-ms-users
│   │   │   │       ├── IInfrastructureClient.java      → [INTERFACE] WebClient a vg-ms-infrastructure
│   │   │   │       ├── INotificationClient.java        → [INTERFACE] WhatsApp/Email
│   │   │   │       └── ICommercialEventPublisher.java  → [INTERFACE] RabbitMQ
│   │   │   └── exceptions/
│   │   │       ├── ReceiptNotFoundException.java       → [CLASS]
│   │   │       ├── PaymentNotFoundException.java       → [CLASS]
│   │   │       ├── DebtNotFoundException.java          → [CLASS]
│   │   │       ├── ServiceCutNotFoundException.java    → [CLASS]
│   │   │       ├── PettyCashNotFoundException.java     → [CLASS]
│   │   │       └── InsufficientBalanceException.java   → [CLASS]
│   │   │
│   │   ├── application/
│   │   │   ├── usecases/
│   │   │   │   ├── receipt/
│   │   │   │   │   ├── CreateReceiptUseCaseImpl.java   → [CLASS] @Service
│   │   │   │   │   ├── GetReceiptUseCaseImpl.java      → [CLASS] @Service
│   │   │   │   │   └── GenerateMonthlyReceiptsUseCaseImpl.java → [CLASS] @Service (Job mensual)
│   │   │   │   ├── payment/
│   │   │   │   │   ├── CreatePaymentUseCaseImpl.java   → [CLASS] @Service
│   │   │   │   │   ├── GetPaymentUseCaseImpl.java      → [CLASS] @Service
│   │   │   │   │   └── ProcessPaymentUseCaseImpl.java  → [CLASS] @Service (Actualiza deudas)
│   │   │   │   ├── debt/
│   │   │   │   │   ├── CreateDebtUseCaseImpl.java      → [CLASS] @Service
│   │   │   │   │   ├── GetDebtUseCaseImpl.java         → [CLASS] @Service
│   │   │   │   │   └── UpdateDebtStatusUseCaseImpl.java → [CLASS] @Service
│   │   │   │   ├── servicecut/
│   │   │   │   │   ├── CreateServiceCutUseCaseImpl.java → [CLASS] @Service
│   │   │   │   │   ├── GetServiceCutUseCaseImpl.java   → [CLASS] @Service
│   │   │   │   │   └── ExecuteServiceCutUseCaseImpl.java → [CLASS] @Service
│   │   │   │   └── pettycash/
│   │   │   │       ├── CreatePettyCashUseCaseImpl.java → [CLASS] @Service
│   │   │   │       ├── GetPettyCashUseCaseImpl.java    → [CLASS] @Service
│   │   │   │       ├── RegisterMovementUseCaseImpl.java → [CLASS] @Service
│   │   │   │       └── GetPettyCashBalanceUseCaseImpl.java → [CLASS] @Service
│   │   │   ├── dto/
│   │   │   │   ├── common/
│   │   │   │   │   ├── ApiResponse.java                → [CLASS] ✅ ESTÁNDAR (Wrapper)
│   │   │   │   │   ├── PageResponse.java               → [CLASS] ✅ ESTÁNDAR (Paginación)
│   │   │   │   │   └── ErrorMessage.java               → [CLASS] ✅ ESTÁNDAR (Errores)
│   │   │   │   ├── request/
│   │   │   │   │   ├── CreateReceiptRequest.java       → [CLASS] @Valid
│   │   │   │   │   ├── CreatePaymentRequest.java       → [CLASS] @Valid
│   │   │   │   │   ├── CreateDebtRequest.java          → [CLASS] @Valid
│   │   │   │   │   ├── CreateServiceCutRequest.java    → [CLASS] @Valid
│   │   │   │   │   ├── CreatePettyCashRequest.java     → [CLASS] @Valid
│   │   │   │   │   └── RegisterMovementRequest.java    → [CLASS] @Valid
│   │   │   │   └── response/
│   │   │   │       ├── ReceiptResponse.java            → [CLASS] DTO (incluye detalles)
│   │   │   │       ├── PaymentResponse.java            → [CLASS] DTO (incluye detalles)
│   │   │   │       ├── DebtResponse.java               → [CLASS] DTO
│   │   │   │       ├── ServiceCutResponse.java         → [CLASS] DTO
│   │   │   │       ├── PettyCashResponse.java          → [CLASS] DTO
│   │   │   │       └── PettyCashMovementResponse.java  → [CLASS] DTO
│   │   │   ├── mappers/
│   │   │   │   ├── ReceiptMapper.java                  → [CLASS] @Component
│   │   │   │   ├── PaymentMapper.java                  → [CLASS] @Component
│   │   │   │   ├── DebtMapper.java                     → [CLASS] @Component
│   │   │   │   ├── ServiceCutMapper.java               → [CLASS] @Component
│   │   │   │   ├── PettyCashMapper.java                → [CLASS] @Component
│   │   │   │   └── PettyCashMovementMapper.java        → [CLASS] @Component
│   │   │   └── events/
│   │   │       ├── ReceiptGeneratedEvent.java          → [CLASS]
│   │   │       ├── PaymentCompletedEvent.java          → [CLASS]
│   │   │       ├── DebtCreatedEvent.java               → [CLASS]
│   │   │       ├── ServiceCutExecutedEvent.java        → [CLASS]
│   │   │       ├── PettyCashMovementEvent.java         → [CLASS]
│   │   │       └── publishers/
│   │   │           └── CommercialEventPublisherImpl.java → [CLASS] @Component
│   │   │
│   │   └── infrastructure/
│   │       ├── adapters/
│   │       │   ├── in/
│   │       │   │   └── rest/
│   │       │   │       ├── ReceiptRest.java      → [CLASS] @RestController
│   │       │   │       ├── PaymentRest.java      → [CLASS] @RestController
│   │       │   │       ├── DebtRest.java         → [CLASS] @RestController
│   │       │   │       ├── ServiceCutRest.java   → [CLASS] @RestController
│   │       │   │       └── PettyCashRest.java    → [CLASS] @RestController
│   │       │   └── out/
│   │       │       ├── persistence/
│   │       │       │   ├── ReceiptRepositoryImpl.java  → [CLASS] @Repository
│   │       │       │   ├── PaymentRepositoryImpl.java  → [CLASS] @Repository
│   │       │       │   ├── DebtRepositoryImpl.java     → [CLASS] @Repository
│   │       │       │   ├── ServiceCutRepositoryImpl.java → [CLASS] @Repository
│   │       │       │   └── PettyCashRepositoryImpl.java → [CLASS] @Repository
│   │       │       └── external/
│   │       │           ├── UserServiceClientImpl.java  → [CLASS] @Component
│   │       │           ├── InfrastructureClientImpl.java → [CLASS] @Component
│   │       │           └── NotificationClientImpl.java → [CLASS] @Component
│   │       ├── persistence/
│   │       │   ├── entities/
│   │       │   │   ├── ReceiptEntity.java              → [CLASS] @Table("receipts")
│   │       │   │   ├── ReceiptDetailEntity.java        → [CLASS] @Table("receipt_details")
│   │       │   │   ├── PaymentEntity.java              → [CLASS] @Table("payments")
│   │       │   │   ├── PaymentDetailEntity.java        → [CLASS] @Table("payment_details")
│   │       │   │   ├── DebtEntity.java                 → [CLASS] @Table("debts")
│   │       │   │   ├── ServiceCutEntity.java           → [CLASS] @Table("service_cuts")
│   │       │   │   ├── PettyCashEntity.java            → [CLASS] @Table("petty_cash")
│   │       │   │   └── PettyCashMovementEntity.java    → [CLASS] @Table("petty_cash_movements")
│   │       │   └── repositories/
│   │       │       ├── ReceiptR2dbcRepository.java     → [INTERFACE] R2dbcRepository
│   │       │       ├── ReceiptDetailR2dbcRepository.java → [INTERFACE] R2dbcRepository
│   │       │       ├── PaymentR2dbcRepository.java     → [INTERFACE] R2dbcRepository
│   │       │       ├── PaymentDetailR2dbcRepository.java → [INTERFACE] R2dbcRepository
│   │       │       ├── DebtR2dbcRepository.java        → [INTERFACE] R2dbcRepository
│   │       │       ├── ServiceCutR2dbcRepository.java  → [INTERFACE] R2dbcRepository
│   │       │       ├── PettyCashR2dbcRepository.java   → [INTERFACE] R2dbcRepository
│   │       │       └── PettyCashMovementR2dbcRepository.java → [INTERFACE] R2dbcRepository
│   │       └── config/
│   │           ├── R2dbcConfig.java                    → [CLASS] @Configuration
│   │           ├── SecurityConfig.java                 → [CLASS] @Configuration
│   │           ├── RabbitMQConfig.java                 → [CLASS] @Configuration
│   │           ├── Resilience4jConfig.java             → [CLASS] @Configuration
│   │           ├── WebClientConfig.java                → [CLASS] @Configuration
│   │           └── RequestContextFilter.java           → [CLASS] @Component (Lee headers)
│   │
│   └── resources/
│       ├── application.yml
│       ├── application-dev.yml
│       ├── application-prod.yml
│       └── db/migration/
│           ├── V1__create_payments_table.sql
│           ├── V2__create_payment_details_table.sql
│           ├── V3__create_debts_table.sql
│           ├── V4__create_receipts_table.sql
│           ├── V5__create_receipt_details_table.sql
│           ├── V6__create_service_cuts_table.sql
│           ├── V7__create_petty_cash_table.sql
│           └── V8__create_petty_cash_movements_table.sql
│
├── Dockerfile
├── pom.xml
└── README.md
```

---

## 5. 📦 vg-ms-water-quality {#estructura-water-quality}

```text
vg-ms-water-quality/
├── src/main/
│   ├── java/pe/edu/vallegrande/vgmswaterquality/
│   │   ├── domain/
│   │   │   ├── models/
│   │   │   │   ├── TestingPoint.java                   → [CLASS] Puntos de muestreo
│   │   │   │   ├── QualityTest.java                    → [CLASS] Pruebas de calidad
│   │   │   │   └── valueobjects/
│   │   │   │       ├── PointType.java                  → [ENUM] RESERVOIR, TAP, WELL, SOURCE
│   │   │   │       ├── TestType.java                   → [ENUM] CHLORINE, PH, TURBIDITY, BACTERIOLOGICAL, CHEMICAL
│   │   │   │       ├── TestResult.java                 → [ENUM] APPROVED, REJECTED, REQUIRES_TREATMENT
│   │   │   │       └── RecordStatus.java               → [ENUM] ACTIVE, INACTIVE
│   │   │   ├── ports/
│   │   │   │   ├── in/
│   │   │   │   │   ├── ICreateMeasurementUseCase.java  → [INTERFACE]
│   │   │   │   │   └── IGetMeasurementUseCase.java     → [INTERFACE]
│   │   │   │   └── out/
│   │   │   │       └── IWaterQualityRepository.java    → [INTERFACE]
│   │   │   └── exceptions/
│   │   │       └── MeasurementNotFoundException.java   → [CLASS]
│   │   │
│   │   ├── application/
│   │   │   ├── usecases/
│   │   │   │   ├── CreateMeasurementUseCaseImpl.java   → [CLASS] @Service
│   │   │   │   └── GetMeasurementUseCaseImpl.java      → [CLASS] @Service
│   │   │   ├── dto/
│   │   │   │   ├── common/
│   │   │   │   │   ├── ApiResponse.java                → [CLASS] ✅ ESTÁNDAR (Wrapper)
│   │   │   │   │   ├── PageResponse.java               → [CLASS] ✅ ESTÁNDAR (Paginación)
│   │   │   │   │   └── ErrorMessage.java               → [CLASS] ✅ ESTÁNDAR (Errores)
│   │   │   │   ├── request/
│   │   │   │   │   └── CreateMeasurementRequest.java   → [CLASS]
│   │   │   │   └── response/
│   │   │   │       └── WaterQualityResponse.java       → [CLASS]
│   │   │   ├── mappers/
│   │   │   │   └── WaterQualityMapper.java             → [CLASS] @Component
│   │   │   └── events/
│   │   │       ├── QualityTestCompletedEvent.java      → [CLASS]
│   │   │       └── publishers/
│   │   │           └── QualityEventPublisherImpl.java  → [CLASS]
│   │   │
│   │   └── infrastructure/
│   │       ├── adapters/
│   │       │   ├── in/
│   │       │   │   └── rest/
│   │       │   │       └── WaterQualityRest.java → [CLASS] @RestController
│   │       │   └── out/
│   │       │       └── persistence/
│   │       │           └── WaterQualityRepositoryImpl.java → [CLASS] @Repository
│   │       ├── persistence/
│   │       │   ├── documents/
│   │       │   │   ├── TestingPointDocument.java       → [CLASS] @Document("testing_points")
│   │       │   │   └── QualityTestDocument.java        → [CLASS] @Document("quality_tests")
│   │       │   └── repositories/
│   │       │       ├── TestingPointMongoRepository.java → [INTERFACE]
│   │       │       └── QualityTestMongoRepository.java → [INTERFACE]
│   │       └── config/
│   │           ├── MongoConfig.java                    → [CLASS] @Configuration
│   │           ├── SecurityConfig.java                 → [CLASS] @Configuration
│   │           └── RequestContextFilter.java           → [CLASS] @Component (Lee headers)
│   │
│   └── resources/
│       ├── application.yml
│       ├── application-dev.yml
│       └── application-prod.yml
│
├── Dockerfile
├── pom.xml
└── README.md
```

---

## 6. 📦 vg-ms-distribution {#estructura-distribution}

```text
vg-ms-distribution/
├── src/main/
│   ├── java/pe/edu/vallegrande/vgmsdistribution/
│   │   ├── domain/
│   │   │   ├── models/
│   │   │   │   ├── DistributionProgram.java            → [CLASS] Programa de distribución
│   │   │   │   ├── DistributionRoute.java              → [CLASS] Rutas de distribución
│   │   │   │   ├── DistributionSchedule.java           → [CLASS] Horarios de distribución
│   │   │   │   └── valueobjects/
│   │   │   │       ├── DayOfWeek.java                  → [ENUM] MONDAY, TUESDAY, WEDNESDAY, etc.
│   │   │   │       ├── DistributionStatus.java         → [ENUM] ACTIVE, INACTIVE, SUSPENDED
│   │   │   │       └── RecordStatus.java               → [ENUM] ACTIVE, INACTIVE
│   │   │   ├── ports/
│   │   │   │   ├── in/
│   │   │   │   │   ├── ICreateDistributionUseCase.java → [INTERFACE]
│   │   │   │   │   └── IGetDistributionUseCase.java    → [INTERFACE]
│   │   │   │   └── out/
│   │   │   │       └── IDistributionRepository.java    → [INTERFACE]
│   │   │   └── exceptions/
│   │   │       └── DistributionNotFoundException.java  → [CLASS]
│   │   │
│   │   ├── application/
│   │   │   ├── usecases/
│   │   │   │   ├── CreateDistributionUseCaseImpl.java  → [CLASS] @Service
│   │   │   │   └── GetDistributionUseCaseImpl.java     → [CLASS] @Service
│   │   │   ├── dto/
│   │   │   │   ├── common/
│   │   │   │   │   ├── ApiResponse.java                → [CLASS] ✅ ESTÁNDAR (Wrapper)
│   │   │   │   │   ├── PageResponse.java               → [CLASS] ✅ ESTÁNDAR (Paginación)
│   │   │   │   │   └── ErrorMessage.java               → [CLASS] ✅ ESTÁNDAR (Errores)
│   │   │   │   ├── request/
│   │   │   │   │   └── CreateDistributionRequest.java  → [CLASS]
│   │   │   │   └── response/
│   │   │   │       └── DistributionResponse.java       → [CLASS]
│   │   │   ├── mappers/
│   │   │   │   └── DistributionMapper.java             → [CLASS] @Component
│   │   │   └── events/
│   │   │       ├── DistributionScheduledEvent.java     → [CLASS]
│   │   │       └── publishers/
│   │   │           └── DistributionEventPublisherImpl.java → [CLASS]
│   │   │
│   │   └── infrastructure/
│   │       ├── adapters/
│   │       │   ├── in/
│   │       │   │   └── rest/
│   │       │   │       └── DistributionRest.java → [CLASS] @RestController
│   │       │   └── out/
│   │       │       └── persistence/
│   │       │           └── DistributionRepositoryImpl.java → [CLASS] @Repository
│   │       ├── persistence/
│   │       │   ├── documents/
│   │       │   │   ├── DistributionProgramDocument.java → [CLASS] @Document("distribution_programs")
│   │       │   │   ├── DistributionRouteDocument.java  → [CLASS] @Document("distribution_routes")
│   │       │   │   └── DistributionScheduleDocument.java → [CLASS] @Document("distribution_schedules")
│   │       │   └── repositories/
│   │       │       ├── DistributionProgramMongoRepository.java → [INTERFACE]
│   │       │       ├── DistributionRouteMongoRepository.java → [INTERFACE]
│   │       │       └── DistributionScheduleMongoRepository.java → [INTERFACE]
│   │       └── config/
│   │           ├── MongoConfig.java                    → [CLASS] @Configuration
│   │           ├── SecurityConfig.java                 → [CLASS] @Configuration
│   │           └── RequestContextFilter.java           → [CLASS] @Component (Lee headers)
│   │
│   └── resources/
│       ├── application.yml
│       ├── application-dev.yml
│       └── application-prod.yml
│
├── Dockerfile
├── pom.xml
└── README.md
```

---

## 7. 📦 vg-ms-inventory-purchases {#estructura-inventory}

```text
vg-ms-inventory-purchases/
├── src/main/
│   ├── java/pe/edu/vallegrande/vgmsinventory/
│   │   ├── domain/
│   │   │   ├── models/
│   │   │   │   ├── Supplier.java                       → [CLASS] Proveedores
│   │   │   │   ├── Material.java                       → [CLASS] Materiales/Productos
│   │   │   │   ├── ProductCategory.java                → [CLASS] Categorías de productos
│   │   │   │   ├── Purchase.java                       → [CLASS] Orden de compra
│   │   │   │   ├── PurchaseDetail.java                 → [CLASS] Detalle de compra (líneas)
│   │   │   │   ├── InventoryMovement.java              → [CLASS] Kardex/movimientos
│   │   │   │   └── valueobjects/
│   │   │   │       ├── MovementType.java               → [ENUM] IN, OUT, ADJUSTMENT
│   │   │   │       ├── PurchaseStatus.java             → [ENUM] PENDING, RECEIVED, CANCELLED
│   │   │   │       ├── Unit.java                       → [ENUM] UNIT, METERS, KG, LITERS
│   │   │   │       └── RecordStatus.java               → [ENUM] ACTIVE, INACTIVE
│   │   │   ├── ports/
│   │   │   │   ├── in/
│   │   │   │   │   ├── ICreateProductUseCase.java      → [INTERFACE]
│   │   │   │   │   └── IRegisterKardexUseCase.java     → [INTERFACE]
│   │   │   │   └── out/
│   │   │   │       ├── IProductRepository.java         → [INTERFACE]
│   │   │   │       └── IKardexRepository.java          → [INTERFACE]
│   │   │   └── exceptions/
│   │   │       └── ProductNotFoundException.java       → [CLASS]
│   │   │
│   │   ├── application/
│   │   │   ├── usecases/
│   │   │   │   ├── CreateProductUseCaseImpl.java       → [CLASS] @Service
│   │   │   │   └── RegisterKardexUseCaseImpl.java      → [CLASS] @Service
│   │   │   ├── dto/
│   │   │   │   ├── common/
│   │   │   │   │   ├── ApiResponse.java                → [CLASS] ✅ ESTÁNDAR (Wrapper)
│   │   │   │   │   ├── PageResponse.java               → [CLASS] ✅ ESTÁNDAR (Paginación)
│   │   │   │   │   └── ErrorMessage.java               → [CLASS] ✅ ESTÁNDAR (Errores)
│   │   │   │   ├── request/
│   │   │   │   │   ├── CreateProductRequest.java       → [CLASS]
│   │   │   │   │   └── KardexConsumptionRequest.java   → [CLASS]
│   │   │   │   └── response/
│   │   │   │       ├── ProductResponse.java            → [CLASS]
│   │   │   │       └── KardexResponse.java             → [CLASS]
│   │   │   ├── mappers/
│   │   │   │   ├── ProductMapper.java                  → [CLASS] @Component
│   │   │   │   └── KardexMapper.java                   → [CLASS] @Component
│   │   │   └── events/
│   │   │       ├── MaterialConsumedEvent.java          → [CLASS]
│   │   │       └── publishers/
│   │   │           └── InventoryEventPublisherImpl.java → [CLASS]
│   │   │
│   │   └── infrastructure/
│   │       ├── adapters/
│   │       │   ├── in/
│   │       │   │   └── rest/
│   │       │   │       ├── ProductRest.java      → [CLASS] @RestController
│   │       │   │       └── KardexRest.java       → [CLASS] @RestController
│   │       │   └── out/
│   │       │       └── persistence/
│   │       │           └── InventoryRepositoryImpl.java → [CLASS] @Repository
│   │       ├── persistence/
│   │       │   ├── entities/
│   │       │   │   ├── SupplierEntity.java             → [CLASS] @Table("suppliers")
│   │       │   │   ├── MaterialEntity.java             → [CLASS] @Table("materials")
│   │       │   │   ├── ProductCategoryEntity.java      → [CLASS] @Table("product_categories")
│   │       │   │   ├── PurchaseEntity.java             → [CLASS] @Table("purchases")
│   │       │   │   ├── PurchaseDetailEntity.java       → [CLASS] @Table("purchase_details")
│   │       │   │   └── InventoryMovementEntity.java    → [CLASS] @Table("inventory_movements")
│   │       │   └── repositories/
│   │       │       ├── SupplierR2dbcRepository.java    → [INTERFACE]
│   │       │       ├── MaterialR2dbcRepository.java    → [INTERFACE]
│   │       │       ├── ProductCategoryR2dbcRepository.java → [INTERFACE]
│   │       │       ├── PurchaseR2dbcRepository.java    → [INTERFACE]
│   │       │       ├── PurchaseDetailR2dbcRepository.java → [INTERFACE]
│   │       │       └── InventoryMovementR2dbcRepository.java → [INTERFACE]
│   │       └── config/
│   │           ├── R2dbcConfig.java                    → [CLASS] @Configuration
│   │           ├── SecurityConfig.java                 → [CLASS] @Configuration
│   │           └── RequestContextFilter.java           → [CLASS] @Component (Lee headers)
│   │
│   └── resources/
│       ├── application.yml
│       ├── application-dev.yml
│       ├── application-prod.yml
│       └── db/migration/
│           ├── V1__create_suppliers_table.sql
│           ├── V2__create_materials_table.sql
│           ├── V3__create_product_categories_table.sql
│           ├── V4__create_purchases_table.sql
│           ├── V5__create_purchase_details_table.sql
│           └── V6__create_inventory_movements_table.sql
│
├── Dockerfile
├── pom.xml
└── README.md
```

---

## 8. 📦 vg-ms-claims-incidents {#estructura-claims}

```text
vg-ms-claims-incidents/
├── src/main/
│   ├── java/pe/edu/vallegrande/vgmsclaims/
│   │   ├── domain/
│   │   │   ├── models/
│   │   │   │   ├── Complaint.java                      → [CLASS] Quejas de clientes
│   │   │   │   ├── ComplaintCategory.java              → [CLASS] Categorías de quejas
│   │   │   │   ├── ComplaintResponse.java              → [CLASS] Respuestas a quejas
│   │   │   │   ├── Incident.java                       → [CLASS] Incidentes de infraestructura
│   │   │   │   ├── IncidentType.java                   → [CLASS] Tipos de incidentes
│   │   │   │   ├── IncidentResolution.java             → [CLASS] Resoluciones de incidentes
│   │   │   │   └── valueobjects/
│   │   │   │       ├── ComplaintPriority.java          → [ENUM] LOW, MEDIUM, HIGH, URGENT
│   │   │   │       ├── ComplaintStatus.java            → [ENUM] RECEIVED, IN_PROGRESS, RESOLVED, CLOSED
│   │   │   │       ├── ResponseType.java               → [ENUM] INVESTIGACION, SOLUCION, SEGUIMIENTO
│   │   │   │       ├── IncidentSeverity.java           → [ENUM] LOW, MEDIUM, HIGH, CRITICAL
│   │   │   │       ├── IncidentStatus.java             → [ENUM] REPORTED, ASSIGNED, IN_PROGRESS, RESOLVED, CLOSED
│   │   │   │       ├── ResolutionType.java             → [ENUM] REPARACION_TEMPORAL, REPARACION_COMPLETA, REEMPLAZO
│   │   │   │       ├── MaterialUsed.java               → [VALUE OBJECT] Embedded: productId, quantity, unit, unitCost
│   │   │   │       └── RecordStatus.java               → [ENUM] ACTIVE, INACTIVE
│   │   │   ├── ports/
│   │   │   │   ├── in/
│   │   │   │   │   ├── ICreateClaimUseCase.java        → [INTERFACE]
│   │   │   │   │   └── IGetClaimUseCase.java           → [INTERFACE]
│   │   │   │   └── out/
│   │   │   │       └── IClaimRepository.java           → [INTERFACE]
│   │   │   └── exceptions/
│   │   │       └── ClaimNotFoundException.java         → [CLASS]
│   │   │
│   │   ├── application/
│   │   │   ├── usecases/
│   │   │   │   ├── CreateClaimUseCaseImpl.java         → [CLASS] @Service
│   │   │   │   └── GetClaimUseCaseImpl.java            → [CLASS] @Service
│   │   │   ├── dto/
│   │   │   │   ├── common/
│   │   │   │   │   ├── ApiResponse.java                → [CLASS] ✅ ESTÁNDAR (Wrapper)
│   │   │   │   │   ├── PageResponse.java               → [CLASS] ✅ ESTÁNDAR (Paginación)
│   │   │   │   │   └── ErrorMessage.java               → [CLASS] ✅ ESTÁNDAR (Errores)
│   │   │   │   ├── request/
│   │   │   │   │   └── CreateClaimRequest.java         → [CLASS]
│   │   │   │   └── response/
│   │   │   │       └── ClaimResponse.java              → [CLASS]
│   │   │   ├── mappers/
│   │   │   │   └── ClaimMapper.java                    → [CLASS] @Component
│   │   │   └── events/
│   │   │       ├── ClaimCreatedEvent.java              → [CLASS]
│   │   │       └── publishers/
│   │   │           └── ClaimEventPublisherImpl.java    → [CLASS]
│   │   │
│   │   └── infrastructure/
│   │       ├── adapters/
│   │       │   ├── in/
│   │       │   │   └── rest/
│   │       │   │       └── ClaimRest.java        → [CLASS] @RestController
│   │       │   └── out/
│   │       │       └── persistence/
│   │       │           └── ClaimRepositoryImpl.java    → [CLASS] @Repository
│   │       ├── persistence/
│   │       │   ├── documents/
│   │       │   │   ├── ComplaintDocument.java          → [CLASS] @Document("complaints")
│   │       │   │   ├── ComplaintCategoryDocument.java  → [CLASS] @Document("complaint_categories")
│   │       │   │   ├── ComplaintResponseDocument.java  → [CLASS] @Document("complaint_responses")
│   │       │   │   ├── IncidentDocument.java           → [CLASS] @Document("incidents")
│   │       │   │   ├── IncidentTypeDocument.java       → [CLASS] @Document("incident_types")
│   │       │   │   └── IncidentResolutionDocument.java → [CLASS] @Document("incident_resolutions")
│   │       │   └── repositories/
│   │       │       ├── ComplaintMongoRepository.java   → [INTERFACE]
│   │       │       ├── ComplaintCategoryMongoRepository.java → [INTERFACE]
│   │       │       ├── ComplaintResponseMongoRepository.java → [INTERFACE]
│   │       │       ├── IncidentMongoRepository.java    → [INTERFACE]
│   │       │       ├── IncidentTypeMongoRepository.java → [INTERFACE]
│   │       │       └── IncidentResolutionMongoRepository.java → [INTERFACE]
│   │       └── config/
│   │           ├── MongoConfig.java                    → [CLASS] @Configuration
│   │           ├── SecurityConfig.java                 → [CLASS] @Configuration
│   │           └── RequestContextFilter.java           → [CLASS] @Component (Lee headers)
│   │
│   └── resources/
│       ├── application.yml
│       ├── application-dev.yml
│       └── application-prod.yml
│
├── Dockerfile
├── pom.xml
└── README.md
```

---

## 9. 📦 vg-ms-infrastructure {#estructura-infrastructure}

```text
vg-ms-infrastructure/
├── src/main/
│   ├── java/pe/edu/vallegrande/vgmsinfrastructure/
│   │   ├── domain/
│   │   │   ├── models/
│   │   │   │   ├── WaterBox.java                       → [CLASS] Caja de agua principal
│   │   │   │   ├── WaterBoxAssignment.java             → [CLASS] Asignación de caja a usuario
│   │   │   │   ├── WaterBoxTransfer.java               → [CLASS] Transferencia entre usuarios
│   │   │   │   └── valueobjects/
│   │   │   │       ├── BoxType.java                    → [ENUM] RESIDENTIAL, COMMERCIAL, COMMUNAL, INSTITUTIONAL
│   │   │   │       ├── AssignmentStatus.java           → [ENUM] ACTIVE, INACTIVE, TRANSFERRED
│   │   │   │       └── RecordStatus.java               → [ENUM] ACTIVE, INACTIVE, SUSPENDED
│   │   │   ├── ports/
│   │   │   │   ├── in/
│   │   │   │   │   ├── ICreateWaterBoxUseCase.java     → [INTERFACE]
│   │   │   │   │   ├── IAssignWaterBoxUseCase.java     → [INTERFACE]
│   │   │   │   │   └── ITransferWaterBoxUseCase.java   → [INTERFACE]
│   │   │   │   └── out/
│   │   │   │       ├── IWaterBoxRepository.java        → [INTERFACE]
│   │   │   │       ├── IWaterBoxAssignmentRepository.java → [INTERFACE]
│   │   │   │       └── IWaterBoxTransferRepository.java → [INTERFACE]
│   │   │   └── exceptions/
│   │   │       ├── WaterBoxNotFoundException.java      → [CLASS]
│   │   │       └── WaterBoxAlreadyAssignedException.java → [CLASS]
│   │   │
│   │   ├── application/
│   │   │   ├── usecases/
│   │   │   │   ├── CreateWaterBoxUseCaseImpl.java      → [CLASS] @Service
│   │   │   │   ├── AssignWaterBoxUseCaseImpl.java      → [CLASS] @Service
│   │   │   │   └── TransferWaterBoxUseCaseImpl.java    → [CLASS] @Service
│   │   │   ├── dto/
│   │   │   │   ├── common/
│   │   │   │   │   ├── ApiResponse.java                → [CLASS] ✅ ESTÁNDAR (Wrapper)
│   │   │   │   │   ├── PageResponse.java               → [CLASS] ✅ ESTÁNDAR (Paginación)
│   │   │   │   │   └── ErrorMessage.java               → [CLASS] ✅ ESTÁNDAR (Errores)
│   │   │   │   ├── request/
│   │   │   │   │   ├── CreateWaterBoxRequest.java      → [CLASS]
│   │   │   │   │   ├── AssignWaterBoxRequest.java      → [CLASS]
│   │   │   │   │   └── TransferWaterBoxRequest.java    → [CLASS]
│   │   │   │   └── response/
│   │   │   │       ├── WaterBoxResponse.java           → [CLASS]
│   │   │   │       ├── WaterBoxAssignmentResponse.java → [CLASS]
│   │   │   │       └── WaterBoxTransferResponse.java   → [CLASS]
│   │   │   ├── mappers/
│   │   │   │   ├── WaterBoxMapper.java                 → [CLASS] @Component
│   │   │   │   ├── WaterBoxAssignmentMapper.java       → [CLASS] @Component
│   │   │   │   └── WaterBoxTransferMapper.java         → [CLASS] @Component
│   │   │   └── events/
│   │   │       ├── WaterBoxAssignedEvent.java          → [CLASS]
│   │   │       └── publishers/
│   │   │           └── InfrastructureEventPublisherImpl.java → [CLASS]
│   │   │
│   │   └── infrastructure/
│   │       ├── adapters/
│   │       │   ├── in/
│   │       │   │   └── rest/
│   │       │   │       ├── WaterBoxRest.java     → [CLASS] @RestController
│   │       │   │       ├── WaterBoxAssignmentRest.java → [CLASS] @RestController
│   │       │   │       └── WaterBoxTransferRest.java → [CLASS] @RestController
│   │       │   └── out/
│   │       │       └── persistence/
│   │       │           └── WaterBoxRepositoryImpl.java → [CLASS] @Repository
│   │       ├── persistence/
│   │       │   ├── entities/
│   │       │   │   ├── WaterBoxEntity.java             → [CLASS] @Table("water_boxes")
│   │       │   │   ├── WaterBoxAssignmentEntity.java   → [CLASS] @Table("water_box_assignments")
│   │       │   │   └── WaterBoxTransferEntity.java     → [CLASS] @Table("water_box_transfers")
│   │       │   └── repositories/
│   │       │       ├── WaterBoxR2dbcRepository.java    → [INTERFACE]
│   │       │       ├── WaterBoxAssignmentR2dbcRepository.java → [INTERFACE]
│   │       │       └── WaterBoxTransferR2dbcRepository.java → [INTERFACE]
│   │       └── config/
│   │           ├── R2dbcConfig.java                    → [CLASS] @Configuration
│   │           ├── SecurityConfig.java                 → [CLASS] @Configuration
│   │           └── RequestContextFilter.java           → [CLASS] @Component
│   │
│   └── resources/
│       ├── application.yml
│       ├── application-dev.yml
│       ├── application-prod.yml
│       └── db/migration/
│           ├── V1__create_water_boxes_table.sql
│           ├── V2__create_water_box_assignments_table.sql
│           └── V3__create_water_box_transfers_table.sql
│
├── Dockerfile
├── pom.xml
└── README.md
```

---

## 10. 📦 vg-ms-notification {#estructura-notification}

```text
vg-ms-notification/
├── src/
│   ├── index.ts                                        → [FILE] Express server
│   ├── routes/
│   │   └── whatsapp.routes.ts                          → [FILE] Rutas WhatsApp
│   ├── controllers/
│   │   └── whatsapp.controller.ts                      → [FILE] Lógica de envío
│   ├── services/
│   │   └── whatsapp.service.ts                         → [FILE] Twilio/WhatsApp API
│   ├── middlewares/
│   │   └── auth.middleware.ts                          → [FILE] Validación headers
│   ├── config/
│   │   └── twilio.config.ts                            → [FILE] Configuración Twilio
│   └── types/
│       └── notification.types.ts                       → [FILE] TypeScript interfaces
│
├── package.json
├── tsconfig.json
├── Dockerfile
└── README.md
```

---

## 11. 📦 vg-ms-gateway {#estructura-gateway}

```text
vg-ms-gateway/
├── src/main/
│   ├── java/pe/edu/vallegrande/vgmsgateway/
│   │   ├── config/
│   │   │   ├── GatewayConfig.java                      → [CLASS] @Configuration (Routes)
│   │   │   ├── SecurityConfig.java                     → [CLASS] ResourceServer JWT
│   │   │   └── CorsConfig.java                         → [CLASS] Global CORS
│   │   ├── filters/
│   │   │   ├── AuthenticationFilter.java               → [CLASS] Pre-filter JWT validation
│   │   │   ├── TenantFilter.java                       → [CLASS] Extract organization_id
│   │   │   └── RateLimitFilter.java                    → [CLASS] Redis Rate Limiter
│   │   └── GatewayApplication.java                     → [CLASS] @SpringBootApplication
│   │
│   └── resources/
│       ├── application.yml                             → [CONFIG] Routes Definition
│       ├── application-dev.yml
│       └── application-prod.yml
│
├── Dockerfile
├── pom.xml
└── README.md
```

---

## 📝 NOTAS FINALES

### Tecnologías por Microservicio

| Microservicio              | Base de Datos | Stack Principal         |
|----------------------------|---------------|-------------------------|
| vg-ms-users                | PostgreSQL    | Spring WebFlux + R2DBC  |
| vg-ms-authentication       | PostgreSQL    | Spring WebFlux + R2DBC  |
| vg-ms-organizations        | MongoDB       | Spring WebFlux + Reactive Mongo |
| vg-ms-payments-billing     | PostgreSQL    | Spring WebFlux + R2DBC  |
| vg-ms-water-quality        | MongoDB       | Spring WebFlux + Reactive Mongo |
| vg-ms-distribution         | MongoDB       | Spring WebFlux + Reactive Mongo |
| vg-ms-inventory-purchases  | PostgreSQL    | Spring WebFlux + R2DBC  |
| vg-ms-claims-incidents     | MongoDB       | Spring WebFlux + Reactive Mongo |
| vg-ms-infrastructure       | PostgreSQL    | Spring WebFlux + R2DBC  |
| vg-ms-notification         | N/A           | Node.js + Express + Twilio |
| vg-ms-gateway              | N/A           | Spring Cloud Gateway    |

### Principios de Arquitectura

1. **Hexagonal Architecture** (Ports & Adapters) en TODOS los servicios
2. **Clean Architecture** con separación domain/application/infrastructure
3. **Reactive Programming** con Reactor (Mono/Flux)
4. **Event-Driven** con RabbitMQ para comunicación asíncrona
5. **Multi-Tenancy** con organization_id en headers
6. **Security** con JWT validation en Gateway
