# Prompt para un nuevo proyecto con el mismo stack

Copia todo lo que está dentro del bloque de abajo y pégalo como primer mensaje en una sesión nueva de Claude Code,
abierta en una carpeta vacía. Antes, completa las partes marcadas con `<<...>>`. Si tienes el enunciado en PDF,
déjalo en esa carpeta y pon su nombre donde se indica.

---

````markdown
Vamos a construir un proyecto backend nuevo desde cero. Responde siempre en **español**.

## 1. El proyecto
- **Nombre:** <<NOMBRE DEL PROYECTO, ej. "MediTrack">>
- **Contexto:** <<2-4 líneas: qué empresa es, qué problema tiene y qué debe resolver el sistema>>
- **Enunciado:** <<"está en `ARCHIVO.pdf` en esta carpeta, léelo completo antes de empezar" o "no hay enunciado, me baso en lo que escribo aquí">>
- **Servicio principal** (`<<nombre-servicio-principal>>`, puerto 8080): <<qué gestiona, ej. "pacientes, citas y médicos">>
- **Servicio externo simulado** (`<<nombre-servicio-externo>>`, puerto 8081): <<qué simula, ej. "disponibilidad de laboratorios">>
  - Endpoint: `POST <<ruta, ej. /api/labs/check-availability>>`
  - Debe responder **siempre lo mismo para la misma entrada** (determinista, basado en un hash, sin azar ni estado).
- **Entidades principales:** <<ej. Paciente, Cita, Médico, Historial>>
- **Reglas de negocio:** <<lista: unicidad, valores > 0, estados que no se pueden saltar...>>
- **Estados y transiciones:** <<ej. PROGRAMADA → CONFIRMADA | CANCELADA; CONFIRMADA → ATENDIDA | CANCELADA>>
- **Operación crítica que debe ser transaccional:** <<ej. "atender una cita descuenta el insumo y registra el historial">>
- **Roles:** <<ej. PACIENTE (solo lo suyo), MEDICO, ADMIN>>; los usuarios nuevos se registran con el rol básico.

## 2. Stack (no lo cambies sin preguntarme)
Java 21 · Spring Boot 3.5.x · Maven · PostgreSQL 16 · Flyway · Spring Data JPA/Hibernate ·
Spring Security + JJWT 0.12 (JWT stateless) · MapStruct 1.6 · Lombok · Bean Validation · springdoc-openapi (Swagger) ·
Actuator + Micrometer (+ Prometheus) · JUnit 5 · Mockito · MockMvc · Testcontainers · Docker multi-stage + Docker Compose.

## 3. Arquitectura del servicio principal: hexagonal
Paquete base `com.<<empresa>>.<<modulo>>`:
- `domain/model`, `domain/exception`: **Java puro**. Prohibido importar Spring, JPA, Jakarta o Lombok. Las reglas de
  negocio viven en las entidades (métodos como `decreaseStock`, `approve`, fábricas `create(...)` que validan).
  Excepciones de negocio que extienden una `DomainException` base.
- `application/port/in`: interfaces de casos de uso, con los comandos como `record` anidados.
- `application/port/out`: lo que el dominio necesita del exterior (repositorios, cliente externo, password, token).
- `application/service`: implementan los puertos de entrada. Se permite `@Service` y `@Transactional`; nunca dependen
  de `infrastructure`.
- `infrastructure/adapter/in/rest`: controllers, DTOs (`record` con Bean Validation) y un `RestMapper` (MapStruct).
  Sin lógica de negocio.
- `infrastructure/adapter/out/persistence`: entidades JPA (`*Entity`), repositorios Spring Data (`*JpaRepository`),
  `PersistenceMapper` (MapStruct) y adapters (`*PersistenceAdapter`) que implementan los puertos de salida.
- `infrastructure/adapter/out/<<externo>>`: adapter HTTP con `RestClient` y timeouts (conexión 2 s, lectura 3 s).
  Cualquier falla → excepción de dominio `...UnavailableException` → 503. Registra un `Timer` de Micrometer.
- `infrastructure/security`: JWT stateless; el principal autenticado es un record `CurrentUser(id, email, role)` del
  dominio (en los controllers: `@AuthenticationPrincipal CurrentUser`).
- `infrastructure/config`: `GlobalExceptionHandler` (ProblemDetail), `RestClientConfig`, `OpenApiConfig` (botón
  Authorize con bearer JWT), `MetricsConfig`.

Dirección de dependencias: `infrastructure → application → domain`, nunca al revés.
El servicio externo simulado usa capas simples (controller, dto, service).

## 4. Convenciones
- **Errores** con `ProblemDetail` centralizado: 400 validación (con `errors` por campo) · 401 autenticación ·
  403 rol sin permiso o recurso ajeno · 404 no encontrado · 409 duplicado, conflicto de estado, stock o concurrencia ·
  422 regla de dominio violada · 503 servicio externo · 500 inesperado (con `log.error`).
  También: `DataIntegrityViolationException` → 409 y `ObjectOptimisticLockingFailureException` → 409.
- **Esquema solo con Flyway** (`ddl-auto: validate`). V1 esquema, V2 datos semilla (usuarios con hash BCrypt y datos de
  ejemplo). **Nunca editar una migración aplicada**: los cambios van en V3, V4...
- **JPA**: relaciones siempre `FetchType.LAZY`; `@EntityGraph` en los listados que muestran relaciones (evitar N+1);
  al guardar, asignar las FK con `getReferenceById`; `@Version` en la entidad que sufre concurrencia; `open-in-view: false`.
- **Seguridad**: `@PreAuthorize` en los controllers por rol; la **propiedad del recurso** (un usuario solo ve lo suyo)
  se valida en el caso de uso, no en el controller. BCrypt. El login responde el mismo mensaje si el email no existe o
  la contraseña es incorrecta. 401 y 403 de Spring Security también en ProblemDetail.
- **Operación crítica** en un único `@Transactional`. El caso de uso que llama al servicio externo **no** es
  `@Transactional` (no retener una conexión de BD durante una llamada HTTP).
- **Métricas** con prefijo `<<prefijo>>.`: contadores por operación, un gauge de lo que interese vigilar (con una
  consulta COUNT), el timer del servicio externo y un contador `<<prefijo>>.errors{status, exception}` en el
  `GlobalExceptionHandler` para distinguir 500 (BD/código) de 503 (externo). `log.info` en operaciones clave.
- **Actuator**: health e info públicos; el resto solo ADMIN.
- **Pruebas**: unitarias `*Test` con `@ExtendWith(MockitoExtension.class)` simulando los puertos (y pruebas de dominio
  sin mocks); de integración `*IT` que extienden una `AbstractIntegrationTest` (`@SpringBootTest`,
  `@AutoConfigureMockMvc`, `@Testcontainers`, PostgreSQL con `@ServiceConnection`), cada prueba con sus propios datos
  únicos. Las IT no corren con `mvn test`; se ejecutan con `mvn test -Dtest=ApplicationIT`.
- **Docker**: Dockerfile multi-stage por servicio (build con Maven, runtime JRE liviano), `docker-compose.yml` con
  postgres (healthcheck) + los dos servicios en una red interna; variables de entorno con valores por defecto
  (`DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`, URL del servicio externo usando el **nombre del servicio**).
- **Commits**: uno por bloque terminado, mensajes en español con prefijo (`feat:`, `test:`, `docs:`, `chore:`).
  **Sin atribución a Claude** (nada de `Co-Authored-By: Claude` ni "Generated with Claude Code").

## 5. Problemas que ya me pasaron (evítalos desde el inicio)
- **Testcontainers y Docker Engine 29**: la versión que trae Spring Boot 3.5.6 (1.21.3) falla con "Could not find a
  valid Docker environment / Status 400". Fija `<testcontainers.version>1.21.4</testcontainers.version>` en el pom.
- **`.gitignore`**: no uses `out/` ni `bin/` sueltos (la regla de IntelliJ ignoraría los paquetes `port/out` y
  `adapter/out`). Ánclalos: `/out/`, `/*/out/`, `/bin/`, `/*/bin/`. Ignora `.env` y `.env.*` excepto `.env.example`.
  Compruébalo con `git ls-files -ci --exclude-standard` (debe salir vacío).
- **No uses `@Validated` a nivel de clase en los controllers**: las violaciones de parámetros terminarían en 500.
- **JWT**: el secreto debe tener al menos 32 caracteres. Firma con `.signWith(key, Jwts.SIG.HS256)`: si solo pones
  `.signWith(key)`, JJWT elige HS256/384/512 según el largo de la clave (60 caracteres → HS384). Agrega una prueba
  unitaria que verifique `"alg":"HS256"` en la cabecera del token.
- **Hash del servicio simulado**: usa `Math.floorMod(hash, ...)`, no `Math.abs` (falla con `Integer.MIN_VALUE`), y
  normaliza mayúsculas/espacios.
- **Mockito estricto**: no pongas stubs en `@BeforeEach` que algún test no use (UnnecessaryStubbingException).
- **Maven**: si `mvn` no aparece en la shell de la sesión, usa la ruta completa
  (`& "$env:USERPROFILE\tools\apache-maven-3.9.16\bin\mvn.cmd"` en PowerShell). Estoy en Windows 11.
- Docker Desktop debe estar encendido para Compose y para Testcontainers.

## 6. Cómo quiero trabajar
<<ELIGE UNA y borra la otra>>

**Opción A – Modo estudio (yo implemento la lógica):**
Genera el esqueleto completo y funcional (pom, configuración, migraciones, seguridad, adapters, DTOs, mappers,
manejo de errores, Docker, pruebas base), pero deja los métodos de **reglas de negocio y casos de uso** con
`// TODO` y una guía paso a paso en comentarios, lanzando `UnsupportedOperationException("TODO")`. Esos TODO son
míos: guíame, revisa lo que haga y dame pistas, pero no los implementes salvo que te lo pida explícitamente.

**Opción B – Proyecto completo:**
Implementa todo, bloque por bloque, compilando y probando cada uno antes de hacer commit.

En ambos casos:
1. Primero lee el enunciado (si hay) y propónme el modelo de dominio, los estados, los endpoints con sus roles y las
   decisiones de diseño. **Espera mi visto bueno antes de generar código.**
2. Crea un `CLAUDE.md` con el contexto, la estructura, los comandos y estas convenciones, y un `docs/CHECKLIST.md`
   por bloques (dominio, casos de uso, seguridad, servicio externo, persistencia, observabilidad, pruebas, Docker,
   entregables y preparación de la defensa). Ve marcando las casillas al terminar cada bloque.
3. Verifica de verdad, no supongas: `mvn package`, `mvn test`, las IT con Testcontainers, `docker compose up --build`,
   la llamada al servicio externo por la red interna, el 503 con el servicio externo apagado, Flyway migrando y el
   flujo login → Authorize → endpoint protegido en Swagger.
4. Al final, un README con: cómo ejecutarlo, usuarios semilla, tabla de endpoints con roles, tabla de códigos de
   error, métricas, pruebas, diagramas mermaid (arquitectura hexagonal, secuencia de la operación crítica, estados)
   y decisiones técnicas que yo pueda defender.
5. Explícame las decisiones que tomes por tu cuenta (cosas fuera de lo que pedí), para que pueda defenderlas.
````

---

## Consejos de uso
- Cuanto más concretas sean las **reglas de negocio** y los **estados**, menos tendrá que adivinar Claude.
- Si la prueba real trae enunciado, pásale el PDF: el prompt le pide leerlo completo y proponer el diseño antes de
  programar.
- La **Opción A** es la mejor para practicar antes de la prueba de desempeño, porque te obliga a escribir la lógica
  que luego vas a tener que explicar.
