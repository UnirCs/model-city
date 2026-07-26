-- DDL: model-city-mobility microservice
-- Database: modelcity-mobility
-- Dependency order: cars -> street_reservations -> sanctions
-- Note: owner_sub/user_sub/agent_sub are soft references to the core service (no FK constraints).

CREATE TABLE IF NOT EXISTS cars (
    id              BIGSERIAL       NOT NULL,
    owner_sub       VARCHAR(255)    NOT NULL,
    license_plate   VARCHAR(32)     NOT NULL,
    nickname        VARCHAR(128),
    brand           VARCHAR(128),
    model           VARCHAR(128),
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_cars              PRIMARY KEY (id),
    CONSTRAINT uq_cars_license      UNIQUE (license_plate)
);

CREATE INDEX IF NOT EXISTS idx_cars_owner_sub ON cars (owner_sub);

COMMENT ON TABLE  cars                  IS 'Vehicles owned by core users (identified by Auth0 sub).';
COMMENT ON COLUMN cars.owner_sub        IS 'Soft ref to core service — Auth0 sub of the owning user.';
COMMENT ON COLUMN cars.license_plate    IS 'Unique license plate of the vehicle.';
COMMENT ON COLUMN cars.nickname         IS 'Personalized name given by the owner.';


CREATE TABLE IF NOT EXISTS street_reservations (
    id                          BIGSERIAL           NOT NULL,
    user_sub                    VARCHAR(255)        NOT NULL,
    car_id                      BIGINT              NOT NULL,
    latitude                    DOUBLE PRECISION    NOT NULL,
    longitude                   DOUBLE PRECISION    NOT NULL,
    created_at                  TIMESTAMPTZ         NOT NULL,
    expires_at                  TIMESTAMPTZ         NOT NULL,
    renewed_from_id             BIGINT,
    stripe_checkout_session_id  VARCHAR(255),
    status                      VARCHAR(16)         NOT NULL DEFAULT 'PENDING',
    price_paid                  NUMERIC(10,2)       NOT NULL DEFAULT 0,
    currency                    VARCHAR(3)          NOT NULL DEFAULT 'EUR',

    CONSTRAINT pk_street_reservations               PRIMARY KEY (id),
    CONSTRAINT fk_street_reservations_car
        FOREIGN KEY (car_id) REFERENCES cars (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT fk_street_reservations_renewed_from
        FOREIGN KEY (renewed_from_id) REFERENCES street_reservations (id)
        ON UPDATE CASCADE
        ON DELETE SET NULL,
    CONSTRAINT chk_street_reservations_dates        CHECK (expires_at > created_at),
    CONSTRAINT chk_street_reservations_latitude     CHECK (latitude  BETWEEN -90  AND 90),
    CONSTRAINT chk_street_reservations_longitude    CHECK (longitude BETWEEN -180 AND 180),
    CONSTRAINT chk_street_reservations_status       CHECK (status IN ('PENDING', 'PAID', 'CANCELLED')),
    CONSTRAINT chk_street_reservations_price        CHECK (price_paid >= 0)
);

CREATE INDEX IF NOT EXISTS idx_street_reservations_user_sub    ON street_reservations (user_sub);
CREATE INDEX IF NOT EXISTS idx_street_reservations_car_id      ON street_reservations (car_id);
CREATE INDEX IF NOT EXISTS idx_street_reservations_expires_at  ON street_reservations (expires_at);

COMMENT ON TABLE  street_reservations                       IS 'Parking reservations on SER zones made by citizens.';
COMMENT ON COLUMN street_reservations.user_sub              IS 'Soft ref to core service — Auth0 sub of the user that made the reservation.';
COMMENT ON COLUMN street_reservations.car_id                IS 'FK to the reserved vehicle in cars.';
COMMENT ON COLUMN street_reservations.renewed_from_id       IS 'Optional FK to the original reservation when this is a renewal.';
COMMENT ON COLUMN street_reservations.stripe_checkout_session_id IS 'Stripe Checkout Session ID associated with this reservation.';
COMMENT ON COLUMN street_reservations.status                IS 'Payment lifecycle: PENDING, PAID or CANCELLED.';
COMMENT ON COLUMN street_reservations.price_paid            IS 'Amount charged for this reservation in minor currency units.';
COMMENT ON COLUMN street_reservations.currency              IS 'ISO 4217 currency code (e.g. EUR).';

CREATE TABLE IF NOT EXISTS sanctions (
    id              BIGSERIAL           NOT NULL,
    license_plate   VARCHAR(32)         NOT NULL,
    latitude        DOUBLE PRECISION    NOT NULL,
    longitude       DOUBLE PRECISION    NOT NULL,
    image_base64    TEXT                NOT NULL,
    agent_sub       VARCHAR(255)        NOT NULL,
    created_at      TIMESTAMPTZ         NOT NULL,

    CONSTRAINT pk_sanctions                 PRIMARY KEY (id),
    CONSTRAINT chk_sanctions_latitude       CHECK (latitude  BETWEEN -90  AND 90),
    CONSTRAINT chk_sanctions_longitude      CHECK (longitude BETWEEN -180 AND 180)
);

CREATE INDEX IF NOT EXISTS idx_sanctions_license_plate ON sanctions (license_plate);
CREATE INDEX IF NOT EXISTS idx_sanctions_created_at    ON sanctions (created_at);

COMMENT ON TABLE  sanctions                 IS 'Parking sanctions issued by operators or admins.';
COMMENT ON COLUMN sanctions.license_plate   IS 'License plate of the sanctioned vehicle.';
COMMENT ON COLUMN sanctions.image_base64    IS 'Base64-encoded evidence photo.';
COMMENT ON COLUMN sanctions.agent_sub       IS 'Soft ref to core service — Auth0 sub of the operator or admin that issued the sanction.';

-- System trails (audit log) for the mobility vertical. Admin-only read access.
-- Microservice: zone/neighbourhood/responsible user are soft references (no FK).
CREATE TABLE IF NOT EXISTS mobility_trails (
    event_id                UUID                NOT NULL,
    event_type              VARCHAR(100)        NOT NULL,
    operation_type          VARCHAR(10)         NOT NULL,
    occurred_at             TIMESTAMPTZ         NOT NULL,
    correlation_id          VARCHAR(100),
    responsible_user_id     VARCHAR(128),
    responsible_user_role   VARCHAR(50),
    neighbourhood_id        BIGINT,
    zone_id                 BIGINT,
    resource_type           VARCHAR(100)        NOT NULL,
    resource_id             VARCHAR(255),
    payload                 JSONB,

    CONSTRAINT pk_mobility_trails PRIMARY KEY (event_id),
    CONSTRAINT chk_mobility_trails_operation CHECK (operation_type IN ('CREATE', 'UPDATE', 'DELETE'))
);
CREATE INDEX IF NOT EXISTS idx_mobility_trails_type        ON mobility_trails (event_type);
CREATE INDEX IF NOT EXISTS idx_mobility_trails_user        ON mobility_trails (responsible_user_id);
CREATE INDEX IF NOT EXISTS idx_mobility_trails_occurred_at ON mobility_trails (occurred_at);

COMMENT ON TABLE mobility_trails IS 'Audit log of write operations in the mobility vertical.';
