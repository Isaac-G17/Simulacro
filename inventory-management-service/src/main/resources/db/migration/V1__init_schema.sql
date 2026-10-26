-- =========================================================
-- V1: esquema inicial de StockBridge
-- Nunca modifiques una migración ya aplicada: crea V2, V3...
-- =========================================================

CREATE TABLE users (
    id         BIGSERIAL    PRIMARY KEY,
    full_name  VARCHAR(120) NOT NULL,
    email      VARCHAR(150) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    role       VARCHAR(20)  NOT NULL CHECK (role IN ('EMPLEADO', 'BODEGA', 'ADMIN')),
    active     BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE products (
    id          BIGSERIAL      PRIMARY KEY,
    code        VARCHAR(50)    NOT NULL UNIQUE,
    name        VARCHAR(150)   NOT NULL,
    description VARCHAR(500),
    category    VARCHAR(80)    NOT NULL,
    price       NUMERIC(12, 2) NOT NULL CHECK (price > 0),
    stock       INTEGER        NOT NULL CHECK (stock >= 0),
    active      BOOLEAN        NOT NULL DEFAULT TRUE,
    version     BIGINT         NOT NULL DEFAULT 0   -- bloqueo optimista (@Version)
);

CREATE TABLE inventory_requests (
    id          BIGSERIAL    PRIMARY KEY,
    user_id     BIGINT       NOT NULL REFERENCES users (id),
    product_id  BIGINT       NOT NULL REFERENCES products (id),
    quantity    INTEGER      NOT NULL CHECK (quantity > 0),
    status      VARCHAR(20)  NOT NULL CHECK (status IN ('PENDIENTE', 'APROBADA', 'RECHAZADA', 'ENTREGADA', 'CANCELADA')),
    observation VARCHAR(500),
    created_at  TIMESTAMP    NOT NULL,
    updated_at  TIMESTAMP
);

CREATE TABLE inventory_movements (
    id         BIGSERIAL    PRIMARY KEY,
    product_id BIGINT       NOT NULL REFERENCES products (id),
    request_id BIGINT       REFERENCES inventory_requests (id),
    user_id    BIGINT       NOT NULL REFERENCES users (id),
    type       VARCHAR(20)  NOT NULL CHECK (type IN ('ENTRADA', 'SALIDA', 'AJUSTE')),
    quantity   INTEGER      NOT NULL CHECK (quantity > 0),
    note       VARCHAR(255),
    created_at TIMESTAMP    NOT NULL
);

CREATE INDEX idx_requests_user    ON inventory_requests (user_id);
CREATE INDEX idx_requests_product ON inventory_requests (product_id);
CREATE INDEX idx_requests_status  ON inventory_requests (status);
CREATE INDEX idx_movements_product ON inventory_movements (product_id);
