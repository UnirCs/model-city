-- DDL: model-city-core microservice
-- Database: modelcity-core
-- Dependency order: zones -> neighbourhoods -> users -> operation_authorizations

-- Zones table
CREATE TABLE IF NOT EXISTS zones (
    id              BIGSERIAL                           NOT NULL,
    display_name    VARCHAR(255)                        NOT NULL,
    name            VARCHAR(100)                        NOT NULL,

    CONSTRAINT pk_zones      PRIMARY KEY (id),
    CONSTRAINT uq_zones_name UNIQUE      (name)
);

COMMENT ON TABLE  zones              IS 'Zones (districts) grouping neighbourhoods. Reference table.';
COMMENT ON COLUMN zones.id           IS 'Surrogate primary key.';
COMMENT ON COLUMN zones.display_name IS 'Human-readable label shown in the UI.';
COMMENT ON COLUMN zones.name         IS 'Unique internal identifier (kebab-case). Used by the application logic.';

-- Neighbourhoods table
CREATE TABLE IF NOT EXISTS neighbourhoods (
    id              BIGSERIAL                           NOT NULL,
    display_name    VARCHAR(255)                        NOT NULL,
    name            VARCHAR(100)                        NOT NULL,
    zone_id         BIGINT                              NOT NULL,

    CONSTRAINT pk_neighbourhoods      PRIMARY KEY (id),
    CONSTRAINT uq_neighbourhoods_name UNIQUE      (name),
    CONSTRAINT fk_neighbourhoods_zone
        FOREIGN KEY (zone_id) REFERENCES zones (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

COMMENT ON TABLE  neighbourhoods              IS 'Neighbourhoods of the city, grouped by zone. Reference table.';
COMMENT ON COLUMN neighbourhoods.id           IS 'Surrogate primary key.';
COMMENT ON COLUMN neighbourhoods.display_name IS 'Human-readable label shown in the UI.';
COMMENT ON COLUMN neighbourhoods.name         IS 'Unique internal identifier (kebab-case). Used by the application logic.';
COMMENT ON COLUMN neighbourhoods.zone_id      IS 'FK to the zone this neighbourhood belongs to. Mandatory.';

CREATE INDEX IF NOT EXISTS idx_neighbourhoods_zone_id ON neighbourhoods (zone_id);

-- Citizens table
CREATE TABLE IF NOT EXISTS users (
    id                  VARCHAR(128)                    NOT NULL,
    name                VARCHAR(255),
    email               VARCHAR(320),
    address             VARCHAR(500),
    neighbourhood_id    BIGINT,
    role                VARCHAR(50)                     NOT NULL DEFAULT 'MODEL-CITY-CITIZEN',
    status              VARCHAR(20)                     NOT NULL DEFAULT 'ACTIVE',
    dni_hash            VARCHAR(64),
    created_at          TIMESTAMPTZ                     NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_users             PRIMARY KEY (id),
    CONSTRAINT chk_users_status     CHECK (status IN ('ACTIVE', 'DISABLED')),
    CONSTRAINT uq_users_email       UNIQUE      (email),
    CONSTRAINT fk_users_neighbourhood
        FOREIGN KEY (neighbourhood_id) REFERENCES neighbourhoods (id)
        ON UPDATE CASCADE
        ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_users_email            ON users (email);
CREATE INDEX IF NOT EXISTS idx_users_neighbourhood_id ON users (neighbourhood_id);

COMMENT ON TABLE  users                    IS 'Citizens registered in Model City. PK = Auth0 sub claim.';
COMMENT ON COLUMN users.id                 IS 'Unique Auth0 identifier (sub claim). Immutable. E.g. auth0|65f3a1b2c3d4e5f6';
COMMENT ON COLUMN users.name               IS 'Full name of the citizen';
COMMENT ON COLUMN users.email              IS 'Email address. Unique. May be null if the email scope was not granted in Auth0';
COMMENT ON COLUMN users.address            IS 'Postal address of the citizen (user-editable field)';
COMMENT ON COLUMN users.neighbourhood_id   IS 'FK to neighbourhoods. Null until the citizen selects their neighbourhood.';
COMMENT ON COLUMN users.role               IS 'User role in the system. One of: MODEL_CITY_PLATFORM_ADMIN, MODEL_CITY_OPERATOR, MODEL_CITY_BACKOFFICE, MODEL_CITY_MOBILITY_AGENT, MODEL_CITY_CITIZEN. Defaults to MODEL_CITY_CITIZEN.';
COMMENT ON COLUMN users.dni_hash           IS 'Irreversible HMAC of the citizen DNI, set on first mTLS certificate verification. Locks the account to a single DNI.';
COMMENT ON COLUMN users.created_at         IS 'UTC timestamp of the first sign-in. Immutable after INSERT.';

-- OTP authorizations table
CREATE TABLE IF NOT EXISTS operation_authorizations (
    operation_authorization_id  UUID                        NOT NULL,
    operation_type              VARCHAR(100)                NOT NULL,
    resource_type               VARCHAR(100)                NOT NULL,
    resource_id                 VARCHAR(255)                NOT NULL,
    user_id                     VARCHAR(128)                NOT NULL,
    expires_at                  TIMESTAMPTZ                 NOT NULL,
    status                      VARCHAR(20)                 NOT NULL DEFAULT 'PENDING',
    otp_hash                    VARCHAR(64)                 NOT NULL,
    dni_hash                    VARCHAR(64),
    attempts_remaining          INTEGER                     NOT NULL DEFAULT 3,
    created_at                  TIMESTAMPTZ                 NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_operation_authorizations PRIMARY KEY (operation_authorization_id),
    CONSTRAINT chk_status CHECK (status IN ('PENDING', 'VERIFIED', 'BURNT', 'EXPIRED')),
    CONSTRAINT chk_attempts CHECK (attempts_remaining >= 0),
    CONSTRAINT fk_operation_authorizations_user
        FOREIGN KEY (user_id) REFERENCES users (id)
        ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_op_auth_user_id   ON operation_authorizations (user_id);
CREATE INDEX IF NOT EXISTS idx_op_auth_status    ON operation_authorizations (status);
CREATE INDEX IF NOT EXISTS idx_op_auth_expires   ON operation_authorizations (expires_at);

COMMENT ON TABLE  operation_authorizations                          IS 'OTP challenges for authorizing sensitive operations.';
COMMENT ON COLUMN operation_authorizations.operation_authorization_id IS 'UUID primary key, generated by the application.';
COMMENT ON COLUMN operation_authorizations.operation_type           IS 'Type of operation being authorized (e.g. CONFIRM_ANSWER).';
COMMENT ON COLUMN operation_authorizations.resource_type            IS 'Type of resource the operation targets (e.g. public-question).';
COMMENT ON COLUMN operation_authorizations.resource_id              IS 'Identifier of the specific resource.';
COMMENT ON COLUMN operation_authorizations.user_id                  IS 'FK to users.id — the user who owns this challenge.';
COMMENT ON COLUMN operation_authorizations.expires_at               IS 'UTC timestamp after which the challenge is no longer valid.';
COMMENT ON COLUMN operation_authorizations.status                   IS 'Lifecycle state: PENDING → VERIFIED → BURNT or EXPIRED.';
COMMENT ON COLUMN operation_authorizations.otp_hash                 IS 'SHA-256 hex-encoded hash of the OTP code.';
COMMENT ON COLUMN operation_authorizations.dni_hash                 IS 'Irreversible HMAC of the verified DNI bound to this challenge, from the verification token. Null if not DNI-bound.';
COMMENT ON COLUMN operation_authorizations.attempts_remaining       IS 'Remaining validation attempts. Defaults to 3.';
COMMENT ON COLUMN operation_authorizations.created_at               IS 'UTC timestamp when the challenge was created.';

-- System trails (audit log) for the core vertical. Admin-only read access.
-- Microservice: zone/neighbourhood/user are owned by this same service, so real FKs are kept.
CREATE TABLE IF NOT EXISTS core_trails (
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
    resource_id              VARCHAR(255),
    payload                 JSONB,

    CONSTRAINT pk_core_trails PRIMARY KEY (event_id),
    CONSTRAINT chk_core_trails_operation CHECK (operation_type IN ('CREATE', 'UPDATE', 'DELETE')),
    CONSTRAINT fk_core_trails_user
        FOREIGN KEY (responsible_user_id) REFERENCES users (id) ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_core_trails_neighbourhood
        FOREIGN KEY (neighbourhood_id) REFERENCES neighbourhoods (id) ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_core_trails_zone
        FOREIGN KEY (zone_id) REFERENCES zones (id) ON UPDATE CASCADE ON DELETE SET NULL
);
CREATE INDEX IF NOT EXISTS idx_core_trails_type        ON core_trails (event_type);
CREATE INDEX IF NOT EXISTS idx_core_trails_user        ON core_trails (responsible_user_id);
CREATE INDEX IF NOT EXISTS idx_core_trails_occurred_at ON core_trails (occurred_at);

COMMENT ON TABLE core_trails IS 'Audit log of write operations in the core vertical (users + OTP).';

-- i18n translation side tables (default-locale value stays in the base table).
CREATE TABLE IF NOT EXISTS zone_translations (
    zone_id BIGINT NOT NULL, locale VARCHAR(10) NOT NULL, display_name VARCHAR(255),
    CONSTRAINT pk_zone_translations PRIMARY KEY (zone_id, locale),
    CONSTRAINT fk_zone_translations_zone FOREIGN KEY (zone_id) REFERENCES zones (id) ON DELETE CASCADE
);
CREATE TABLE IF NOT EXISTS neighbourhood_translations (
    neighbourhood_id BIGINT NOT NULL, locale VARCHAR(10) NOT NULL, display_name VARCHAR(255),
    CONSTRAINT pk_neighbourhood_translations PRIMARY KEY (neighbourhood_id, locale),
    CONSTRAINT fk_neighbourhood_translations_n FOREIGN KEY (neighbourhood_id) REFERENCES neighbourhoods (id) ON DELETE CASCADE
);
