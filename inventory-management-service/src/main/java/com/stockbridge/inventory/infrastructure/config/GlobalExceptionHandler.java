package com.stockbridge.inventory.infrastructure.config;

import com.stockbridge.inventory.domain.exception.*;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manejo centralizado de errores con ProblemDetail (RFC 9457).
 * Diferencia: validación (400) · autenticación (401) · autorización (403) · no encontrado (404)
 *             reglas de negocio (409/422) · proveedor caído (503) · fallas internas (500).
 * Cada error incrementa la métrica inventory.errors{status, exception}: así se distingue en /actuator/metrics
 * si las fallas vienen de la BD/código (500) o del proveedor externo (503).
 */
@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final MeterRegistry meterRegistry;

    @ExceptionHandler(ResourceNotFoundException.class)
    ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "Recurso no encontrado", ex.getMessage(), ex);
    }

    @ExceptionHandler({DuplicateResourceException.class, InsufficientStockException.class,
            InvalidStateTransitionException.class})
    ProblemDetail handleConflict(DomainException ex) {
        return problem(HttpStatus.CONFLICT, "Conflicto con el estado actual", ex.getMessage(), ex);
    }

    @ExceptionHandler({InvalidDomainDataException.class, InactiveProductException.class})
    ProblemDetail handleBusinessRule(DomainException ex) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "Regla de negocio violada", ex.getMessage(), ex);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    ProblemDetail handleInvalidCredentials(InvalidCredentialsException ex) {
        return problem(HttpStatus.UNAUTHORIZED, "Credenciales inválidas", ex.getMessage(), ex);
    }

    @ExceptionHandler({ResourceAccessDeniedException.class, AccessDeniedException.class})
    ProblemDetail handleForbidden(RuntimeException ex) {
        return problem(HttpStatus.FORBIDDEN, "Acceso denegado", ex.getMessage(), ex);
    }

    @ExceptionHandler(SupplierUnavailableException.class)
    ProblemDetail handleSupplier(SupplierUnavailableException ex) {
        return problem(HttpStatus.SERVICE_UNAVAILABLE, "Proveedor externo no disponible", ex.getMessage(), ex);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    ProblemDetail handleOptimisticLock(ObjectOptimisticLockingFailureException ex) {
        log.warn("Conflicto de concurrencia: {}", ex.getMessage());
        return problem(HttpStatus.CONFLICT, "Modificación concurrente",
                "El recurso fue modificado por otra operación. Intenta de nuevo.", ex);
    }

    /** Carrera entre dos altas con el mismo código/email: ambas pasan el existsBy... y la BD rechaza la segunda. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Violación de integridad en BD: {}", ex.getMostSpecificCause().getMessage());
        return problem(HttpStatus.CONFLICT, "Conflicto de datos",
                "La operación viola una restricción de datos (posible duplicado)", ex);
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleUnexpected(Exception ex) {
        log.error("Error inesperado", ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno", "Ocurrió un error inesperado", ex);
    }

    /** Errores de Bean Validation (@Valid): devuelve el detalle por campo. */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatusCode status,
                                                                  WebRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Datos inválidos",
                "La petición tiene campos inválidos", ex);
        problem.setProperty("errors", errors);
        return ResponseEntity.badRequest().body(problem);
    }

    private ProblemDetail problem(HttpStatus status, String title, String detail, Exception ex) {
        meterRegistry.counter("inventory.errors",
                "status", String.valueOf(status.value()),
                "exception", ex.getClass().getSimpleName()).increment();
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setProperty("timestamp", Instant.now());
        return problem;
    }
}
