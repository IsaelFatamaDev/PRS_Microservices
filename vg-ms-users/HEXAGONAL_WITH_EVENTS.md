# 🎯 ARQUITECTURA HEXAGONAL CON EVENTOS DE DOMINIO

## 📐 Estructura de Eventos

### ✅ **Domain Layer (Núcleo)**

```
domain/
├── events/
│   ├── DomainEvent.java ← Interface base
│   ├── UserCreatedEvent.java
│   ├── UserUpdatedEvent.java
│   ├── UserDeletedEvent.java
│   └── UserRestoredEvent.java
├── models/
│   └── User.java ← Aggregate Root (registra eventos)
└── ports/
    └── out/
        └── IDomainEventPublisher.java ← Puerto para eventos
```

### ✅ **Application Layer**

```
application/
└── usecases/
    ├── CreateUserUseCaseImpl.java ← Publica eventos del agregado
    ├── UpdateUserUseCaseImpl.java
    └── DeleteUserUseCaseImpl.java
```

### ✅ **Infrastructure Layer**

```
infrastructure/
└── adapters/
    └── out/
        └── messaging/
            └── DomainEventPublisherImpl.java ← Implementa puerto (RabbitMQ)
```

---

## 🔄 Flujo de Eventos

### 1️⃣ **Aggregate Root registra evento**

```java
// User.java (Domain)
public static User createNew(User user) {
    user.registerEvent(UserCreatedEvent.from(user));
    return user;
}
```

### 2️⃣ **Use Case publica eventos**

```java
// CreateUserUseCaseImpl.java (Application)
public Mono<User> execute(User user, String password) {
    User newUser = User.createNew(user); // Registra evento

    return userRepository.save(newUser)
        .flatMap(this::publishDomainEvents); // Publica eventos
}
```

### 3️⃣ **Infrastructure envía a RabbitMQ**

```java
// DomainEventPublisherImpl.java (Infrastructure)
public Mono<Void> publish(DomainEvent event) {
    rabbitTemplate.convertAndSend("users.events.exchange", routingKey, message);
}
```

---

## 🎯 Beneficios

✅ **Separación de responsabilidades**: Dominio no conoce RabbitMQ
✅ **Testeable**: Fácil mockear IDomainEventPublisher
✅ **Extensible**: Agregar nuevos eventos sin modificar código
✅ **Consistencia**: Eventos se publican DESPUÉS de guardar en BD
✅ **Hexagonal puro**: Dominio 100% independiente de infraestructura
