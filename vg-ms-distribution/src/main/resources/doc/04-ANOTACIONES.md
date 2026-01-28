# Anotaciones Utilizadas en el Proyecto

Este documento explica todas las anotaciones usadas en el microservicio, cómo funcionan y para qué sirven.

---

## Anotaciones de Spring Framework

### @SpringBootApplication

```java
@SpringBootApplication
public class VgMsDistribution {
    public static void main(String[] args) {
        SpringApplication.run(VgMsDistribution.class, args);
    }
}
```

**Ubicación:** Clase principal  
**Propósito:** Combina tres anotaciones:
- `@Configuration` - Marca la clase como fuente de configuración
- `@EnableAutoConfiguration` - Habilita auto-configuración de Spring Boot
- `@ComponentScan` - Escanea componentes en el paquete y subpaquetes

**Funcionamiento:**
1. Escanea todos los paquetes desde `pe.edu.vallegrande.msdistribution`
2. Detecta beans anotados con `@Component`, `@Service`, `@Repository`, `@Controller`
3. Configura automáticamente según dependencias del classpath

---

### @RestController

```java
@RestController
@RequestMapping("/internal")
public class AdminRest {
    // ...
}
```

**Ubicación:** Controllers REST  
**Propósito:** Combina `@Controller` + `@ResponseBody`  
**Funcionamiento:**
- Marca la clase como controller REST
- Todos los métodos retornan datos (JSON) en lugar de vistas
- Spring convierte automáticamente objetos a JSON

---

### @RequestMapping

```java
@RequestMapping("/internal")
public class AdminRest { }

@RequestMapping(value = "/programs", method = RequestMethod.POST)
public Mono<ResponseDto<...>> create(...) { }
```

**Propósito:** Define la ruta base del controller o endpoint  
**Parámetros:**
- `value` - Ruta URL
- `method` - Método HTTP (GET, POST, PUT, DELETE)
- `produces` - Tipo de contenido de respuesta
- `consumes` - Tipo de contenido aceptado

**Variantes:**
- `@GetMapping` = `@RequestMapping(method = GET)`
- `@PostMapping` = `@RequestMapping(method = POST)`
- `@PutMapping` = `@RequestMapping(method = PUT)`
- `@DeleteMapping` = `@RequestMapping(method = DELETE)`

---

### @RequestBody

```java
public Mono<ResponseDto<...>> create(
    @RequestBody DistributionProgramCreateRequest request) {
    // ...
}
```

**Ubicación:** Parámetros de métodos  
**Propósito:** Indica que el parámetro viene del cuerpo del HTTP request  
**Funcionamiento:**
1. Spring lee el body del request (JSON)
2. Deserializa JSON a objeto Java
3. Inyecta el objeto en el parámetro

---

### @PathVariable

```java
@GetMapping("/programs/{id}")
public Mono<ResponseDto<...>> findById(@PathVariable String id) {
    // ...
}
```

**Ubicación:** Parámetros de métodos  
**Propósito:** Extrae valores de la URL  
**Ejemplo:** GET `/programs/123` → `id = "123"`

---

### @RequestParam

```java
@GetMapping("/programs")
public Flux<ResponseDto<...>> findAll(
    @RequestParam(required = false) String organizationId) {
    // ...
}
```

**Ubicación:** Parámetros de métodos  
**Propósito:** Extrae parámetros de query string  
**Ejemplo:** GET `/programs?organizationId=ORG001` → `organizationId = "ORG001"`

**Parámetros:**
- `required` - Si es obligatorio (default: true)
- `defaultValue` - Valor por defecto si no se envía

---

### @RequestHeader

```java
public Mono<ResponseDto<...>> create(
    @RequestHeader("Authorization") String authHeader) {
    // ...
}
```

**Ubicación:** Parámetros de métodos  
**Propósito:** Extrae valores de headers HTTP  
**Ejemplo:** Header `Authorization: Bearer token123` → `authHeader = "Bearer token123"`

---

### @Service

```java
@Service
public class DistributionProgramServiceImpl 
    implements DistributionProgramService {
    // ...
}
```

**Ubicación:** Clases de servicio  
**Propósito:** Marca la clase como componente de servicio  
**Funcionamiento:**
- Spring crea una instancia (bean) automáticamente
- Disponible para inyección de dependencias
- Indica que contiene lógica de negocio

---

### @Repository

```java
@Repository
public interface DistributionProgramRepository 
    extends ReactiveMongoRepository<DistributionProgramDocument, String> {
    // ...
}
```

**Ubicación:** Interfaces de repositorio  
**Propósito:** Marca la interfaz como repositorio de datos  
**Funcionamiento:**
- Spring Data crea implementación automáticamente
- Traduce excepciones de BD a excepciones de Spring
- Habilita características de Spring Data

---

### @Configuration

```java
@Configuration
public class WebClientConfig {
    @Bean
    public WebClient webClient() {
        return WebClient.builder().build();
    }
}
```

**Ubicación:** Clases de configuración  
**Propósito:** Indica que la clase contiene definiciones de beans  
**Funcionamiento:**
- Spring procesa métodos `@Bean`
- Crea y registra beans en el contexto

---

### @Bean

```java
@Bean
public WebClient webClient(WebClient.Builder builder) {
    return builder
        .baseUrl("https://api.example.com")
        .build();
}
```

**Ubicación:** Métodos en clases `@Configuration`  
**Propósito:** Define un bean gestionado por Spring  
**Funcionamiento:**
- Spring llama al método
- Registra el objeto retornado como bean
- Disponible para inyección de dependencias

---

### @Autowired

```java
@Service
public class DistributionProgramServiceImpl {
    @Autowired
    private DistributionProgramRepository repository;
}
```

**Ubicación:** Campos, constructores o setters  
**Propósito:** Inyecta dependencias automáticamente  
**Funcionamiento:**
- Spring busca un bean del tipo requerido
- Lo inyecta en el campo/constructor

**Nota:** En constructores únicos, `@Autowired` es opcional (desde Spring 4.3)

---

### @Value

```java
@Value("${organization-service.base-url}")
private String organizationServiceUrl;
```

**Ubicación:** Campos  
**Propósito:** Inyecta valores de configuración  
**Funcionamiento:**
- Lee del `application.yml` o variables de entorno
- Convierte el valor al tipo del campo

---

## Anotaciones de Validación (Bean Validation)

### @Valid

```java
public Mono<ResponseDto<...>> create(
    @Valid @RequestBody DistributionProgramCreateRequest request) {
    // ...
}
```

**Ubicación:** Parámetros de métodos  
**Propósito:** Activa validación del objeto  
**Funcionamiento:**
1. Spring valida el objeto antes de ejecutar el método
2. Si hay errores, lanza `MethodArgumentNotValidException`
3. `GlobalExceptionHandler` captura y formatea el error

---

### @NotNull

```java
public class DistributionProgramCreateRequest {
    @NotNull(message = "El nombre no puede ser nulo")
    private String name;
}
```

**Ubicación:** Campos de DTOs  
**Propósito:** El campo no puede ser `null`  
**Funcionamiento:** Valida que el valor no sea `null`

---

### @NotBlank

```java
@NotBlank(message = "El nombre es obligatorio")
private String name;
```

**Ubicación:** Campos String  
**Propósito:** El string no puede ser `null`, vacío o solo espacios  
**Funcionamiento:** Valida `!= null && !isEmpty() && !isBlank()`

---

### @NotEmpty

```java
@NotEmpty(message = "Debe tener al menos una ruta")
private List<String> routes;
```

**Ubicación:** Colecciones, arrays, strings  
**Propósito:** No puede ser `null` ni vacío  
**Funcionamiento:** Valida `!= null && size() > 0`

---

### @Size

```java
@Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
private String name;
```

**Ubicación:** Strings, colecciones, arrays  
**Propósito:** Valida el tamaño  
**Parámetros:**
- `min` - Tamaño mínimo
- `max` - Tamaño máximo

---

### @Pattern

```java
@Pattern(regexp = "^[A-Z]{3}\\d{4}$", message = "Formato inválido")
private String code;
```

**Ubicación:** Campos String  
**Propósito:** Valida que coincida con una expresión regular  
**Ejemplo:** `ABC1234` válido, `abc123` inválido

---

## Anotaciones de MongoDB

### @Document

```java
@Document(collection = "distribution_programs")
public class DistributionProgramDocument {
    // ...
}
```

**Ubicación:** Clases de documento  
**Propósito:** Marca la clase como documento MongoDB  
**Parámetros:**
- `collection` - Nombre de la colección en MongoDB

---

### @Id

```java
@Document(collection = "distribution_programs")
public class DistributionProgramDocument {
    @Id
    private String id;
}
```

**Ubicación:** Campo identificador  
**Propósito:** Marca el campo como `_id` en MongoDB  
**Funcionamiento:**
- MongoDB genera automáticamente si es `null`
- Debe ser único en la colección

---

### @Field

```java
@Field("organization_id")
private String organizationId;
```

**Ubicación:** Campos de documento  
**Propósito:** Mapea el campo a un nombre diferente en MongoDB  
**Ejemplo:** Campo Java `organizationId` → MongoDB `organization_id`

---

### @Indexed

```java
@Indexed
private String organizationId;
```

**Ubicación:** Campos de documento  
**Propósito:** Crea un índice en MongoDB para ese campo  
**Funcionamiento:** Mejora rendimiento de búsquedas por ese campo

---

## Anotaciones de Lombok

### @Data

```java
@Data
public class DistributionProgramResponse {
    private String id;
    private String name;
}
```

**Ubicación:** Clases DTO/Entity  
**Propósito:** Genera automáticamente:
- Getters para todos los campos
- Setters para campos no-final
- `toString()`
- `equals()` y `hashCode()`
- Constructor con campos `@NonNull` y `final`

---

### @Builder

```java
@Builder
public class DistributionProgram {
    private String id;
    private String name;
}

// Uso:
DistributionProgram program = DistributionProgram.builder()
    .id("123")
    .name("Programa 1")
    .build();
```

**Ubicación:** Clases  
**Propósito:** Implementa patrón Builder  
**Funcionamiento:** Genera clase interna Builder con métodos fluent

---

### @NoArgsConstructor

```java
@NoArgsConstructor
public class DistributionProgramDocument {
    // ...
}
```

**Ubicación:** Clases  
**Propósito:** Genera constructor sin argumentos  
**Uso:** Requerido por frameworks (Spring, MongoDB, etc.)

---

### @AllArgsConstructor

```java
@AllArgsConstructor
public class DistributionProgramDocument {
    private String id;
    private String name;
}
```

**Ubicación:** Clases  
**Propósito:** Genera constructor con todos los campos como parámetros

---

### @Slf4j

```java
@Slf4j
@Service
public class DistributionProgramServiceImpl {
    public void someMethod() {
        log.info("Mensaje de log");
        log.error("Error", exception);
    }
}
```

**Ubicación:** Clases  
**Propósito:** Genera campo `log` automáticamente  
**Funcionamiento:** Crea `private static final Logger log = LoggerFactory.getLogger(Class.class)`

---

## Anotaciones de Seguridad

### @EnableReactiveMethodSecurity

```java
@Configuration
@EnableReactiveMethodSecurity
public class SecurityConfig {
    // ...
}
```

**Ubicación:** Clase de configuración  
**Propósito:** Habilita seguridad a nivel de método en aplicaciones reactivas  
**Funcionamiento:** Permite usar `@PreAuthorize`, `@PostAuthorize`, etc.

---

### @PreAuthorize

```java
@PreAuthorize("hasRole('ADMIN')")
public Mono<ResponseDto<...>> create(...) {
    // ...
}
```

**Ubicación:** Métodos  
**Propósito:** Valida permisos ANTES de ejecutar el método  
**Expresiones:**
- `hasRole('ADMIN')` - Usuario tiene rol ADMIN
- `hasAnyRole('ADMIN', 'USER')` - Tiene al menos uno de los roles
- `isAuthenticated()` - Usuario está autenticado
- `permitAll()` - Permite a todos

---

## Anotaciones de Project Reactor

### @NonNull (Reactor)

```java
public Mono<String> process(@NonNull String input) {
    // ...
}
```

**Ubicación:** Parámetros  
**Propósito:** Indica que el parámetro no puede ser `null`  
**Funcionamiento:** Documentación + validación en tiempo de compilación

---

## Anotaciones de Testing

### @SpringBootTest

```java
@SpringBootTest
class DistributionProgramServiceTest {
    // ...
}
```

**Ubicación:** Clases de test  
**Propósito:** Carga contexto completo de Spring Boot  
**Funcionamiento:** Inicia aplicación completa para tests de integración

---

### @Test

```java
@Test
void shouldCreateProgram() {
    // ...
}
```

**Ubicación:** Métodos de test  
**Propósito:** Marca el método como test  
**Funcionamiento:** JUnit ejecuta el método como test

---

### @BeforeEach

```java
@BeforeEach
void setUp() {
    // Preparación antes de cada test
}
```

**Ubicación:** Métodos de test  
**Propósito:** Ejecuta antes de cada test  
**Uso:** Inicializar datos, mocks, etc.

---

### @Mock

```java
@Mock
private DistributionProgramRepository repository;
```

**Ubicación:** Campos en tests  
**Propósito:** Crea un mock de la dependencia  
**Funcionamiento:** Mockito crea objeto simulado

---

### @InjectMocks

```java
@InjectMocks
private DistributionProgramServiceImpl service;
```

**Ubicación:** Campos en tests  
**Propósito:** Inyecta mocks en el objeto  
**Funcionamiento:** Mockito inyecta los `@Mock` en el servicio

---

## Resumen de Anotaciones por Categoría

### Spring Core
- `@SpringBootApplication`, `@Configuration`, `@Bean`
- `@Service`, `@Repository`, `@Component`
- `@Autowired`, `@Value`

### Spring Web
- `@RestController`, `@RequestMapping`
- `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`
- `@RequestBody`, `@PathVariable`, `@RequestParam`, `@RequestHeader`

### Validación
- `@Valid`, `@NotNull`, `@NotBlank`, `@NotEmpty`
- `@Size`, `@Pattern`, `@Min`, `@Max`

### MongoDB
- `@Document`, `@Id`, `@Field`, `@Indexed`

### Lombok
- `@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
- `@Slf4j`, `@Getter`, `@Setter`

### Seguridad
- `@EnableReactiveMethodSecurity`, `@PreAuthorize`

### Testing
- `@SpringBootTest`, `@Test`, `@BeforeEach`
- `@Mock`, `@InjectMocks`

---

## Conclusión

Las anotaciones permiten:
- ✅ **Reducir código boilerplate** (Lombok)
- ✅ **Configuración declarativa** (Spring)
- ✅ **Validación automática** (Bean Validation)
- ✅ **Seguridad** (Spring Security)
- ✅ **Persistencia** (Spring Data MongoDB)
- ✅ **Testing** (JUnit + Mockito)
