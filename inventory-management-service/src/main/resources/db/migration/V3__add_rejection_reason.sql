-- El motivo de rechazo se guarda aparte para no perder la observación original del empleado
ALTER TABLE inventory_requests
    ADD COLUMN rejection_reason VARCHAR(500);
