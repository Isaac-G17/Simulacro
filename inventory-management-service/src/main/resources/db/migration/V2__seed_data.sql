-- =========================================================
-- V2: datos de prueba
-- Contraseña de ambos usuarios: Admin123*  (hash BCrypt)
-- Los EMPLEADOS se crean con POST /auth/register
-- =========================================================

INSERT INTO users (full_name, email, password, role, active) VALUES
    ('Administrador', 'admin@stockbridge.com',
     '$2a$10$J0Wi3NewfhNOUdU1bwjR8u55T9LM8pvmzE7AoUTMXkfkA3556Q48S', 'ADMIN', TRUE),
    ('Encargado Bodega', 'bodega@stockbridge.com',
     '$2a$10$J0Wi3NewfhNOUdU1bwjR8u55T9LM8pvmzE7AoUTMXkfkA3556Q48S', 'BODEGA', TRUE);

INSERT INTO products (code, name, description, category, price, stock, active) VALUES
    ('MON-001', 'Monitor 24"',        'Monitor LED Full HD 24 pulgadas', 'MONITORES',   650000.00, 20, TRUE),
    ('TEC-001', 'Teclado mecánico',   'Teclado mecánico switch red',     'PERIFERICOS', 180000.00,  3, TRUE),
    ('MOU-001', 'Mouse inalámbrico',  'Mouse óptico inalámbrico',        'PERIFERICOS',  75000.00, 50, TRUE),
    ('LAP-001', 'Portátil 14"',       'Portátil 16GB RAM 512GB SSD',     'PORTATILES', 3200000.00,  2, TRUE),
    ('CAB-001', 'Cable HDMI 2m',      'Cable HDMI 2.1',                  'ACCESORIOS',   25000.00,  0, FALSE);
