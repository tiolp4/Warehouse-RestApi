-- ============================================================
-- Logistics standalone schema
-- Shares 'users' and 'suppliers' tables with the main warehouse DB.
-- Run once against the PostgreSQL database where users/suppliers exist.
-- ============================================================

CREATE TABLE IF NOT EXISTS transit_shipments (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tracking_number   VARCHAR(64)  NOT NULL UNIQUE,
    carrier           VARCHAR(128) NOT NULL,
    supplier_id       UUID         REFERENCES suppliers(id) ON DELETE SET NULL,
    origin            VARCHAR(255) NOT NULL,
    destination       VARCHAR(255) NOT NULL,
    departure_date    DATE         NOT NULL,
    expected_arrival  DATE         NOT NULL,
    actual_arrival    DATE,
    status            VARCHAR(16)  NOT NULL DEFAULT 'PLANNED'
                      CHECK (status IN ('PLANNED','IN_TRANSIT','DELIVERED','DELAYED','CANCELLED')),
    notes             TEXT,
    created_at        TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at        TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_transit_status   ON transit_shipments(status);
CREATE INDEX IF NOT EXISTS idx_transit_expected ON transit_shipments(expected_arrival);

CREATE TABLE IF NOT EXISTS logistics_schedules (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_name   VARCHAR(128) NOT NULL,
    vehicle       VARCHAR(64)  NOT NULL,
    route         VARCHAR(255) NOT NULL,
    work_date     DATE         NOT NULL,
    shift_start   TIME         NOT NULL,
    shift_end     TIME         NOT NULL,
    status        VARCHAR(16)  NOT NULL DEFAULT 'SCHEDULED'
                  CHECK (status IN ('SCHEDULED','IN_PROGRESS','COMPLETED','CANCELLED')),
    shipment_id   UUID         REFERENCES transit_shipments(id) ON DELETE SET NULL,
    notes         TEXT,
    created_at    TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_sched_date   ON logistics_schedules(work_date);
CREATE INDEX IF NOT EXISTS idx_sched_driver ON logistics_schedules(driver_name);

CREATE OR REPLACE FUNCTION trg_transit_autostatus() RETURNS trigger AS $$
BEGIN
    IF NEW.actual_arrival IS NOT NULL AND NEW.status <> 'CANCELLED' THEN
        NEW.status := 'DELIVERED';
    ELSIF NEW.status = 'IN_TRANSIT'
       AND NEW.expected_arrival < CURRENT_DATE
       AND NEW.actual_arrival IS NULL THEN
        NEW.status := 'DELAYED';
    END IF;
    NEW.updated_at := now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_transit_autostatus ON transit_shipments;
CREATE TRIGGER trg_transit_autostatus
BEFORE INSERT OR UPDATE ON transit_shipments
FOR EACH ROW EXECUTE FUNCTION trg_transit_autostatus();
