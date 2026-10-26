# CLAUDE.md

Simulacro de preparación (Módulo 6 – Java Spring Boot): **StockBridge**, gestión de inventario y solicitudes internas.
El enunciado completo está en `Enunciado Prueba de Desempeño - M 6.1 Java Spring Boot - Simulacro.pdf` y el avance en `docs/CHECKLIST.md`.

## Contexto de trabajo
- Es un proyecto de **estudio**: el objetivo es que el autor entienda y pueda defender cada decisión.
- **La lógica de negocio la implementa el autor.** Los métodos marcados con `// TODO` son suyos: guía, explica, revisa o da pistas, pero no los implementes salvo que lo pida explícitamente.
- Responde en **español**.
- **Commits sin atribución a Claude**: nunca agregues `Co-Authored-By: Claude` ni "Generated with Claude Code" en commits o PRs.
- Al terminar un bloque, actualiza las casillas de `docs/CHECKLIST.md`.

## Estructura
```
inventory-management-service/   servicio principal (puerto 8080) – arquitectura hexagonal
supplier-availability-service/  simulador de proveedores (puerto 8081) – capas simples
docker-compose.yml              postgres + ambos servicios en la red stockbridge-net
docs/CHECKLIST.md               lo que falta para completar el simulacro
```

Stack: Java 21, Spring Boot 3.5.6, Maven, PostgreSQL 16, Flyway, Spring Security + JJWT 0.12, MapStruct 1.6, Lombok, springdoc, Actuator/Micrometer, JUnit 5, Mockito, Testcontainers.

## Comandos
Ejecutar dentro de la carpeta de cada servicio:
```bash
mvn package -DskipTests           # compilar
mvn test                          # pruebas unitarias (*Test)
mvn test -Dtest=ApplicationIT     # integración con Testcontainers (requiere Docker Desktop)
mvn spring-boot:run               # ejecutar en local
```
Desde la raíz:
```bash
docker compose up -d postgres     # solo la BD, para desarrollo local
docker compose up --build         # todo el sistema
```
- Swagger: http://localhost:8080/swagger-ui.html
- Usuarios semilla (V2): `admin@stockbridge.com` / `bodega@stockbridge.com`, contraseña `Admin123*`. Los EMPLEADOS se crean con `POST /auth/register`.

## Arquitectura hexagonal (inventory-management-service)
Paquete base `com.stockbridge.inventory`:
- `domain/model`, `domain/exception`: **Java puro**. Prohibido importar Spring, JPA, Jakarta o Lombok aquí. Las reglas de negocio viven en las entidades (`Product.decreaseStock`, `InventoryRequest.approve`...).
- `application/port/in`: interfaces de casos de uso, con los comandos como `record` anidados.
- `application/port/out`: lo que el dominio necesita del exterior (repositorios, proveedor, password, token).
- `application/service`: implementan los puertos de entrada. Se permite `@Service` y `@Transactional`; nunca dependen de clases de `infrastructure`.
- `infrastructure/adapter/in/rest`: controllers, DTOs (`record` con Bean Validation) y `RestMapper` (MapStruct). Sin lógica de negocio.
- `infrastructure/adapter/out/persistence`: entidades JPA (`*Entity`), repositorios Spring Data (`*JpaRepository`), `PersistenceMapper` y adapters (`*PersistenceAdapter`) que implementan los puertos de salida.
- `infrastructure/adapter/out/supplier`: `SupplierRestAdapter` (RestClient con timeouts). Cualquier falla se convierte en `SupplierUnavailableException`.
- `infrastructure/security`: JWT stateless; el principal autenticado es `CurrentUser` (en los controllers: `@AuthenticationPrincipal CurrentUser`).
- `infrastructure/config`: `GlobalExceptionHandler` (ProblemDetail), `RestClientConfig`, `OpenApiConfig`.

Dirección de dependencias: `infrastructure → application → domain`, nunca al revés.

## Convenciones
- **Excepciones**: las de negocio extienden `DomainException` y se mapean en `GlobalExceptionHandler`: 404 no encontrado · 409 duplicado, stock o transición · 422 regla violada · 401 credenciales · 403 sin permiso o recurso ajeno · 503 proveedor · 500 inesperado. Si creas una nueva, regístrala ahí.
- **Esquema de BD**: solo mediante Flyway (`ddl-auto: validate`). **Nunca edites una migración aplicada**: crea `V3__...sql`, `V4__...`.
- **Relaciones JPA**: siempre `FetchType.LAZY`; en listados que muestran relaciones, usa `@EntityGraph` para evitar N+1. Al guardar, asigna las FK con `getReferenceById`.
- **Concurrencia**: `ProductEntity` tiene `@Version`; `ObjectOptimisticLockingFailureException` responde 409.
- **Roles**: `@PreAuthorize` en los controllers (`EMPLEADO`, `BODEGA`, `ADMIN`). La propiedad del recurso (un empleado solo ve lo suyo) se valida en el caso de uso, no en el controller.
- **Transiciones de solicitud**: PENDIENTE → APROBADA | RECHAZADA | CANCELADA; APROBADA → ENTREGADA | CANCELADA; el resto son estados finales.
- **Entrega**: descontar stock, guardar el movimiento y marcar ENTREGADA ocurren en un único `@Transactional`.
- **Métricas**: los nombres llevan el prefijo `inventory.` (por ejemplo `inventory.requests.created`, `inventory.supplier.requests`).
- **Pruebas**: las unitarias se llaman `*Test` y usan `@ExtendWith(MockitoExtension.class)` simulando los puertos. Las de integración se llaman `*IT`, extienden `AbstractIntegrationTest` y no corren con `mvn test` por defecto.

## Cuidado
- No uses `@Validated` a nivel de clase en los controllers: haría que las violaciones de parámetros terminen como 500 en lugar de 400.
- El secreto JWT debe tener al menos 32 caracteres (HS256).
- Docker Desktop debe estar encendido para Compose y para Testcontainers.
