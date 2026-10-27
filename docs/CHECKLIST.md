# Checklist del simulacro – StockBridge

Marca cada ítem (`[x]`) a medida que lo completes.

> **Prioridad si falta tiempo:** 1 → 2 → 3 → 8. Con eso hay un proyecto funcional, demostrable y que se levanta con un comando. Pruebas y métricas suman, pero sin lo anterior no se pueden mostrar.

## 1. Dominio
- [x] `RequestStatus.canTransitionTo()`: tabla de transiciones
- [x] `Product.create()`: precio > 0 y stock ≥ 0
- [x] `Product.updateInfo()`, `decreaseStock()`, `increaseStock()`
- [x] `InventoryRequest.create()`: cantidad > 0, estado PENDIENTE y fecha actual
- [x] `InventoryRequest.changeStatus()` y `reject()` (guardar el motivo)

## 2. Casos de uso
- [x] `ProductService`: crear (validar código duplicado), actualizar, buscar por id, activar/desactivar
- [x] `RequestService.create()`: rechazar producto inactivo y cantidad inválida
- [x] `RequestService.findById()`: un EMPLEADO solo ve sus propias solicitudes
- [x] `RequestService.approve()`, `reject()`, `cancel()`
- [x] `RequestService.deliver()`: stock + movimiento + estado en **una sola** transacción
- [x] `SupplierAvailabilityService.check()`

## 3. Seguridad JWT
- [x] `SecurityConfig`, BCrypt, `@PreAuthorize`, respuestas 401/403 en ProblemDetail
- [x] `JwtTokenProvider.generateToken()` y `parse()`
- [x] `JwtAuthenticationFilter`: poner la autenticación en el `SecurityContext`
- [x] `AuthService.register()` y `login()`
- [ ] Probar en Swagger: login → Authorize → endpoint protegido

## 4. Servicio de proveedores
- [x] Controller, DTOs, Dockerfile
- [x] `AvailabilityService.check()` con lógica determinista basada en un hash
- [x] Cliente con timeouts y error 503 en el servicio principal
- [ ] Probar con el supplier apagado → debe responder 503, no colgarse

## 5. Persistencia
- [x] Entidades, relaciones LAZY, `@EntityGraph`, JPQL, paginación, `@Version`
- [x] Flyway V1 (esquema) y V2 (datos de prueba)
- [ ] Arrancar con PostgreSQL y verificar que Flyway migra sin errores

## 6. Observabilidad
- [x] Actuator (health, info, metrics, prometheus) y Timer del proveedor
- [ ] Contadores `requests.created`, `requests.approved`, `requests.rejected` en `RequestService`
- [ ] Gauge de productos con stock bajo
- [ ] Logs `log.info` en operaciones clave y `log.error` en fallos

## 7. Pruebas
### Unitarias (JUnit 5 + Mockito)
- [ ] Registrar producto válido
- [ ] Rechazar código duplicado
- [ ] Rechazar cantidad inválida
- [ ] Impedir entrega sin stock suficiente
- [ ] Rechazar transición de estado inválida
- [ ] Proveedor caído (mock que lanza `SupplierUnavailableException`)

### Integración (MockMvc + Testcontainers)
- [x] Health responde y un endpoint sin token devuelve 401
- [ ] Login correcto devuelve token
- [ ] Un EMPLEADO que crea un producto recibe 403
- [ ] Un empleado que consulta la solicitud de otro es rechazado
- [ ] Flujo completo crear → aprobar → entregar (baja el stock y queda el movimiento)

## 8. Docker
- [x] Dockerfiles multi-stage y `docker-compose.yml`
- [ ] Encender Docker Desktop y correr `docker compose up --build`
- [ ] Verificar que el servicio principal llama al supplier por la red interna

## 9. Entregables
- [x] Repositorio en GitHub
- [ ] Swagger mostrando todos los endpoints
- [ ] README con las decisiones técnicas completas y TODOs marcados
- [ ] Commits frecuentes (uno por bloque terminado)

## 10. Preparar la defensa
- [ ] Explicar el recorrido de una petición: controller → caso de uso → dominio → puerto → adapter → BD
- [ ] ¿Qué cambia si se reemplaza PostgreSQL? Solo el adaptador de persistencia y la configuración.
- [ ] ¿Por qué `@Transactional` en `deliver()`? Si falla el movimiento, el stock no se descuenta.
- [ ] ¿Cómo se genera y valida un JWT? ¿Qué pasa si alguien modifica el token?
- [ ] N+1: cómo detectarlo (`show-sql: true`) y cómo resolverlo (`@EntityGraph`)
- [ ] Mockito frente a Testcontainers: qué prueba cada uno
- [ ] Error de BD o del proveedor: cómo distinguirlos (500 frente a 503, métricas, logs)
- [ ] Concurrencia: qué hace `@Version` cuando dos personas entregan las últimas unidades a la vez
