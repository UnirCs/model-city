-- Model City Monolith - consolidated schema

-- ===== core =====
-- DDL: model-city-users microservice
-- Database: modelcity
-- Dependency order: zones -> neighbourhoods -> users

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

-- ===== engagement =====
-- DDL: model-city-engagement microservice
-- Database: modelcity-engagement
-- Dependency order: civic_questions -> objectives -> answers
-- Note: zone_id and neighbourhood_id are soft references to an external service (no FK constraints).

-- Civic questions table
CREATE TABLE IF NOT EXISTS civic_questions (
    id               BIGSERIAL       NOT NULL,
    title            VARCHAR(255)    NOT NULL,
    description      TEXT            NOT NULL,
    image_url        VARCHAR(2048),
    open_date        DATE            NOT NULL,
    close_date       DATE            NOT NULL,
    zone_id          BIGINT          NOT NULL,
    neighbourhood_id BIGINT          NOT NULL,
    yes_count        BIGINT          NOT NULL DEFAULT 0,
    no_count         BIGINT          NOT NULL DEFAULT 0,

    CONSTRAINT pk_civic_questions        PRIMARY KEY (id),
    CONSTRAINT chk_civic_questions_dates CHECK (close_date > open_date),
    CONSTRAINT fk_civic_questions_zone
        FOREIGN KEY (zone_id)          REFERENCES zones (id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_civic_questions_neighbourhood
        FOREIGN KEY (neighbourhood_id) REFERENCES neighbourhoods (id)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

COMMENT ON TABLE  civic_questions                    IS 'Civic questions issued by administrators for citizen YES/NO voting.';
COMMENT ON COLUMN civic_questions.id                 IS 'Surrogate primary key.';
COMMENT ON COLUMN civic_questions.title              IS 'Short title of the question.';
COMMENT ON COLUMN civic_questions.description        IS 'Full body describing the proposal.';
COMMENT ON COLUMN civic_questions.image_url          IS 'URL of the representative image (CDN or external source).';
COMMENT ON COLUMN civic_questions.open_date          IS 'First day citizens can vote.';
COMMENT ON COLUMN civic_questions.close_date         IS 'Last day citizens can vote (exclusive after this date).';
COMMENT ON COLUMN civic_questions.zone_id            IS 'Soft ref to zones service — district the question belongs to.';
COMMENT ON COLUMN civic_questions.neighbourhood_id   IS 'Soft ref to zones service — specific neighbourhood within the zone.';
COMMENT ON COLUMN civic_questions.yes_count          IS 'Denormalised tally of YES votes, incremented atomically on each vote.';
COMMENT ON COLUMN civic_questions.no_count           IS 'Denormalised tally of NO votes, incremented atomically on each vote.';

-- Objectives table
CREATE TABLE IF NOT EXISTS objectives (
    id          BIGSERIAL   NOT NULL,
    question_id BIGINT      NOT NULL,
    objective   TEXT        NOT NULL,
    sort_order  INT         NOT NULL DEFAULT 0,

    CONSTRAINT pk_objectives PRIMARY KEY (id),
    CONSTRAINT fk_objectives_question
        FOREIGN KEY (question_id) REFERENCES civic_questions (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_objectives_question_id ON objectives (question_id);

COMMENT ON TABLE  objectives             IS 'Goals associated with a civic question, ordered by sort_order.';
COMMENT ON COLUMN objectives.question_id IS 'FK to civic_questions.';
COMMENT ON COLUMN objectives.objective   IS 'Text of the goal/objective.';
COMMENT ON COLUMN objectives.sort_order  IS 'Display order within the question (ascending).';

-- Answers table: deduplication ledger. One vote per verified DNI per question.
CREATE TABLE IF NOT EXISTS answers (
    id          BIGSERIAL       NOT NULL,
    question_id BIGINT          NOT NULL,
    citizen_id  VARCHAR(128)    NOT NULL,
    dni_hash    VARCHAR(64)     NOT NULL,
    vote        VARCHAR(3)      NOT NULL,
    answered_at TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_answers         PRIMARY KEY (id),
    CONSTRAINT uq_answers_dni     UNIQUE (question_id, dni_hash),
    CONSTRAINT chk_answers_vote   CHECK (vote IN ('YES', 'NO')),
    CONSTRAINT fk_answers_question
        FOREIGN KEY (question_id) REFERENCES civic_questions (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_answers_citizen
        FOREIGN KEY (citizen_id) REFERENCES users (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_answers_question_id ON answers (question_id);
CREATE INDEX IF NOT EXISTS idx_answers_citizen_id  ON answers (question_id, citizen_id);

COMMENT ON TABLE  answers             IS 'Citizen votes on civic questions. Deduplicated by verified DNI hash (one vote per DNI per question).';
COMMENT ON COLUMN answers.question_id IS 'FK to civic_questions.';
COMMENT ON COLUMN answers.citizen_id  IS 'Auth0 sub propagated by the gateway via X-Auth-Sub. Kept for the UI hint and audit.';
COMMENT ON COLUMN answers.dni_hash    IS 'Irreversible HMAC of the voter DNI. Deduplication key — no raw PII stored.';
COMMENT ON COLUMN answers.vote        IS 'Vote cast by the citizen: YES or NO.';
COMMENT ON COLUMN answers.answered_at IS 'UTC timestamp when the vote was cast.';

-- Security alerts table
CREATE TABLE IF NOT EXISTS security_alerts (
    id               BIGSERIAL        NOT NULL,
    title            VARCHAR(255)     NOT NULL,
    severity         VARCHAR(16)      NOT NULL,
    description      TEXT             NOT NULL,
    latitude         DOUBLE PRECISION NOT NULL,
    longitude        DOUBLE PRECISION NOT NULL,
    zone_id          BIGINT           NOT NULL,
    neighbourhood_id BIGINT,
    created_at       TIMESTAMPTZ      NOT NULL DEFAULT NOW(),
    expires_at       TIMESTAMPTZ      NOT NULL,

    CONSTRAINT pk_security_alerts           PRIMARY KEY (id),
    CONSTRAINT chk_security_alerts_severity CHECK (severity IN ('IMPORTANT', 'MEDIUM', 'MILD')),
    CONSTRAINT chk_security_alerts_latitude  CHECK (latitude  BETWEEN -90  AND 90),
    CONSTRAINT chk_security_alerts_longitude CHECK (longitude BETWEEN -180 AND 180),
    CONSTRAINT chk_security_alerts_expiry    CHECK (expires_at > created_at),
    CONSTRAINT fk_security_alerts_zone
        FOREIGN KEY (zone_id)          REFERENCES zones (id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_security_alerts_neighbourhood
        FOREIGN KEY (neighbourhood_id) REFERENCES neighbourhoods (id)
        ON UPDATE CASCADE ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_security_alerts_expires_at ON security_alerts (expires_at);
CREATE INDEX IF NOT EXISTS idx_security_alerts_zone_id    ON security_alerts (zone_id);

COMMENT ON TABLE  security_alerts                  IS 'Citizen security alerts issued by administrators or backoffice. Inactive when expires_at is in the past.';
COMMENT ON COLUMN security_alerts.id               IS 'Surrogate primary key.';
COMMENT ON COLUMN security_alerts.title            IS 'Short title of the alert.';
COMMENT ON COLUMN security_alerts.severity         IS 'Alert severity: IMPORTANT, MEDIUM or MILD.';
COMMENT ON COLUMN security_alerts.description      IS 'Human-readable description of the alert.';
COMMENT ON COLUMN security_alerts.latitude         IS 'WGS84 latitude of the affected location.';
COMMENT ON COLUMN security_alerts.longitude        IS 'WGS84 longitude of the affected location.';
COMMENT ON COLUMN security_alerts.zone_id          IS 'Soft ref to zones service. Null neighbourhood_id means the whole zone.';
COMMENT ON COLUMN security_alerts.neighbourhood_id IS 'Soft ref to a specific neighbourhood. NULL = applies to the whole zone.';
COMMENT ON COLUMN security_alerts.created_at       IS 'UTC timestamp when the alert was created.';
COMMENT ON COLUMN security_alerts.expires_at       IS 'UTC timestamp after which the alert is no longer shown.';


-- ===== core (OTP authorizations) =====
-- DDL: model-city-core (OTP authorizations)
-- Database: modelcity

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


-- ===== leisure =====
-- DDL: model-city-leisure microservice
-- Database: modelcity-leisure
-- Dependency order: city_places -> city_routes -> city_route_places

CREATE TABLE IF NOT EXISTS city_places (
    id                      BIGSERIAL       NOT NULL,
    name                    VARCHAR(255)    NOT NULL,
    latitude                DOUBLE PRECISION NOT NULL,
    longitude               DOUBLE PRECISION NOT NULL,
    description             TEXT            NOT NULL,
    address                 VARCHAR(512),
    photo_url_1             VARCHAR(2048),
    photo_url_2             VARCHAR(2048),
    photo_url_3             VARCHAR(2048),
    access_info             TEXT,
    accessibility_info      TEXT,
    category                VARCHAR(64),
    visit_duration_minutes  INT,

    CONSTRAINT pk_city_places              PRIMARY KEY (id),
    CONSTRAINT chk_city_places_latitude    CHECK (latitude  BETWEEN -90  AND 90),
    CONSTRAINT chk_city_places_longitude   CHECK (longitude BETWEEN -180 AND 180)
);

COMMENT ON TABLE  city_places                       IS 'Points of interest within the city (monuments, parks, museums...).';
COMMENT ON COLUMN city_places.id                    IS 'Surrogate primary key.';
COMMENT ON COLUMN city_places.name                  IS 'Display name of the place.';
COMMENT ON COLUMN city_places.latitude              IS 'WGS84 latitude in decimal degrees.';
COMMENT ON COLUMN city_places.longitude             IS 'WGS84 longitude in decimal degrees.';
COMMENT ON COLUMN city_places.description           IS 'Long description shown on the detail page.';
COMMENT ON COLUMN city_places.address               IS 'Postal address or human-readable location.';
COMMENT ON COLUMN city_places.photo_url_1           IS 'First photo URL (cover).';
COMMENT ON COLUMN city_places.photo_url_2           IS 'Second photo URL.';
COMMENT ON COLUMN city_places.photo_url_3           IS 'Third photo URL.';
COMMENT ON COLUMN city_places.access_info           IS 'General access indications (opening hours, transport...).';
COMMENT ON COLUMN city_places.accessibility_info    IS 'Indications for reduced-mobility access.';
COMMENT ON COLUMN city_places.category              IS 'Free-form category (MONUMENT, MUSEUM, PARK, SQUARE...).';
COMMENT ON COLUMN city_places.visit_duration_minutes IS 'Estimated visit duration in minutes.';

CREATE TABLE IF NOT EXISTS city_routes (
    id                          BIGSERIAL       NOT NULL,
    name                        VARCHAR(255)    NOT NULL,
    description                 TEXT            NOT NULL,
    target_audience             VARCHAR(64)     NOT NULL,
    image_url                   VARCHAR(2048),
    estimated_duration_minutes  INT,

    CONSTRAINT pk_city_routes PRIMARY KEY (id)
);

COMMENT ON TABLE  city_routes                            IS 'Themed itineraries composed of several ordered city places.';
COMMENT ON COLUMN city_routes.id                         IS 'Surrogate primary key.';
COMMENT ON COLUMN city_routes.name                       IS 'Display name of the route.';
COMMENT ON COLUMN city_routes.description                IS 'Long description of the route.';
COMMENT ON COLUMN city_routes.target_audience            IS 'Intended audience (FAMILY, KIDS, TOURIST, CULTURAL...).';
COMMENT ON COLUMN city_routes.image_url                  IS 'Cover image URL for the route.';
COMMENT ON COLUMN city_routes.estimated_duration_minutes IS 'Estimated total duration of the itinerary in minutes.';

CREATE TABLE IF NOT EXISTS city_route_places (
    id          BIGSERIAL   NOT NULL,
    route_id    BIGINT      NOT NULL,
    place_id    BIGINT      NOT NULL,
    sort_order  INT         NOT NULL DEFAULT 0,

    CONSTRAINT pk_city_route_places PRIMARY KEY (id),
    CONSTRAINT uq_city_route_places UNIQUE (route_id, place_id),
    CONSTRAINT fk_city_route_places_route
        FOREIGN KEY (route_id) REFERENCES city_routes (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_city_route_places_place
        FOREIGN KEY (place_id) REFERENCES city_places (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_city_route_places_route_id ON city_route_places (route_id);
CREATE INDEX IF NOT EXISTS idx_city_route_places_place_id ON city_route_places (place_id);

COMMENT ON TABLE  city_route_places             IS 'Ordered association between a route and its city places.';
COMMENT ON COLUMN city_route_places.route_id    IS 'FK to city_routes.';
COMMENT ON COLUMN city_route_places.place_id    IS 'FK to city_places.';
COMMENT ON COLUMN city_route_places.sort_order  IS 'Position of the place within the route (0-based, ascending).';

-- Public spaces: reservable facilities like sport centres (polideportivos).
-- Soft-deleted via the active flag. Tables: public_spaces -> reservable_resources -> space_reservations.

CREATE TABLE IF NOT EXISTS public_spaces (
    id              BIGSERIAL       NOT NULL,
    name            VARCHAR(255)    NOT NULL,
    description     TEXT            NOT NULL,
    address         VARCHAR(512),
    latitude        DOUBLE PRECISION,
    longitude       DOUBLE PRECISION,
    photo_url_1     VARCHAR(2048),
    photo_url_2     VARCHAR(2048),
    photo_url_3     VARCHAR(2048),
    active          BOOLEAN         NOT NULL DEFAULT TRUE,

    CONSTRAINT pk_public_spaces             PRIMARY KEY (id),
    CONSTRAINT chk_public_spaces_latitude   CHECK (latitude  IS NULL OR latitude  BETWEEN -90  AND 90),
    CONSTRAINT chk_public_spaces_longitude  CHECK (longitude IS NULL OR longitude BETWEEN -180 AND 180)
);

COMMENT ON TABLE  public_spaces             IS 'Public facilities that contain reservable resources (sport centres, libraries...).';
COMMENT ON COLUMN public_spaces.active      IS 'Soft-delete flag. Inactive rows are excluded from public listings.';

CREATE TABLE IF NOT EXISTS reservable_resources (
    id                  BIGSERIAL       NOT NULL,
    public_space_id     BIGINT          NOT NULL,
    name                VARCHAR(255)    NOT NULL,
    description         TEXT,
    resource_type       VARCHAR(64)     NOT NULL,
    active              BOOLEAN         NOT NULL DEFAULT TRUE,

    CONSTRAINT pk_reservable_resources              PRIMARY KEY (id),
    CONSTRAINT fk_reservable_resources_space
        FOREIGN KEY (public_space_id) REFERENCES public_spaces (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_reservable_resources_space ON reservable_resources (public_space_id);

COMMENT ON TABLE  reservable_resources                  IS 'Individual reservable units inside a public space (football pitch, padel court...).';
COMMENT ON COLUMN reservable_resources.resource_type    IS 'Free-form category (FOOTBALL_FIELD, BASKETBALL_COURT, TENNIS_COURT, PADEL_COURT...).';
COMMENT ON COLUMN reservable_resources.active           IS 'Soft-delete flag. Inactive rows are excluded from public listings.';

CREATE TABLE IF NOT EXISTS space_reservations (
    id                  BIGSERIAL       NOT NULL,
    resource_id         BIGINT          NOT NULL,
    citizen_sub         VARCHAR(255)    NOT NULL,
    citizen_name        VARCHAR(255),
    reservation_date    DATE            NOT NULL,
    start_time          TIME            NOT NULL,
    end_time            TIME            NOT NULL,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_space_reservations            PRIMARY KEY (id),
    CONSTRAINT fk_space_reservations_resource
        FOREIGN KEY (resource_id) REFERENCES reservable_resources (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_space_reservations_citizen
        FOREIGN KEY (citizen_sub) REFERENCES users (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT chk_space_reservations_window    CHECK (start_time >= TIME '09:00' AND end_time <= TIME '19:00'),
    CONSTRAINT chk_space_reservations_order     CHECK (end_time > start_time)
);

CREATE INDEX IF NOT EXISTS idx_space_reservations_resource_date ON space_reservations (resource_id, reservation_date);
CREATE INDEX IF NOT EXISTS idx_space_reservations_citizen      ON space_reservations (citizen_sub);

COMMENT ON TABLE  space_reservations             IS 'Citizen reservations of a reservable_resource on a given date and time range.';
COMMENT ON COLUMN space_reservations.citizen_sub IS 'Auth0 sub of the citizen that booked the reservation.';
COMMENT ON COLUMN space_reservations.citizen_name IS 'Snapshot of the citizen display name at the time of the reservation.';

-- Events: cultural / leisure events linked to a city_place. Soft-deleted via the active flag.
-- Tables: events -> event_tickets -> event_refunds.

CREATE TABLE IF NOT EXISTS events (
    id              BIGSERIAL       NOT NULL,
    place_id        BIGINT          NOT NULL,
    name            VARCHAR(255)    NOT NULL,
    description     TEXT            NOT NULL,
    event_type      VARCHAR(32)     NOT NULL,
    requires_ticket BOOLEAN         NOT NULL DEFAULT FALSE,
    paid            BOOLEAN         NOT NULL DEFAULT FALSE,
    price           NUMERIC(10,2)   NOT NULL DEFAULT 0,
    currency        VARCHAR(3)      NOT NULL DEFAULT 'EUR',
    capacity        INT,
    starts_at       TIMESTAMP       NOT NULL,
    ends_at         TIMESTAMP       NOT NULL,
    stripe_price_id VARCHAR(255),
    photo_url_1     VARCHAR(2048),
    photo_url_2     VARCHAR(2048),
    photo_url_3     VARCHAR(2048),
    active          BOOLEAN         NOT NULL DEFAULT TRUE,

    CONSTRAINT pk_events                PRIMARY KEY (id),
    CONSTRAINT fk_events_place
        FOREIGN KEY (place_id) REFERENCES city_places (id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT chk_events_type          CHECK (event_type IN
        ('MUSIC','NIGHTLIFE','PERFORMING_ARTS','HOBBIES','BUSINESS','FOOD_AND_DRINK','OTHER')),
    CONSTRAINT chk_events_price         CHECK (price >= 0),
    CONSTRAINT chk_events_paid_price    CHECK ((paid = TRUE AND price > 0) OR (paid = FALSE AND price = 0)),
    CONSTRAINT chk_events_ticket_paid   CHECK (paid = FALSE OR requires_ticket = TRUE),
    CONSTRAINT chk_events_dates         CHECK (ends_at > starts_at),
    CONSTRAINT chk_events_capacity      CHECK (capacity IS NULL OR capacity > 0)
);

CREATE INDEX IF NOT EXISTS idx_events_place_id  ON events (place_id);
CREATE INDEX IF NOT EXISTS idx_events_type      ON events (event_type);
CREATE INDEX IF NOT EXISTS idx_events_starts_at ON events (starts_at);

COMMENT ON TABLE  events                    IS 'Cultural / leisure events held at a city place.';
COMMENT ON COLUMN events.place_id           IS 'FK to city_places where the event takes place.';
COMMENT ON COLUMN events.event_type         IS 'Event category: MUSIC, NIGHTLIFE, PERFORMING_ARTS, HOBBIES, BUSINESS, FOOD_AND_DRINK, OTHER.';
COMMENT ON COLUMN events.requires_ticket    IS 'Whether attendees must buy a ticket.';
COMMENT ON COLUMN events.paid               IS 'Whether the ticket has a non-zero price.';
COMMENT ON COLUMN events.capacity           IS 'Optional max number of tickets that can be sold.';
COMMENT ON COLUMN events.active             IS 'Soft-delete flag. Inactive rows are excluded from public listings.';
COMMENT ON COLUMN events.stripe_price_id    IS 'Stripe Price ID for future payment integration. NULL until Stripe is set up.';

CREATE TABLE IF NOT EXISTS event_tickets (
    id              BIGSERIAL       NOT NULL,
    event_id        BIGINT          NOT NULL,
    citizen_sub     VARCHAR(255)    NOT NULL,
    citizen_name    VARCHAR(255),
    price_paid      NUMERIC(10,2)   NOT NULL DEFAULT 0,
    currency        VARCHAR(3)      NOT NULL DEFAULT 'EUR',
    status          VARCHAR(16)     NOT NULL DEFAULT 'PURCHASED',
    purchased_at    TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    refunded_at     TIMESTAMP,
    stripe_checkout_session_id VARCHAR(255),

    CONSTRAINT pk_event_tickets         PRIMARY KEY (id),
    CONSTRAINT fk_event_tickets_event
        FOREIGN KEY (event_id) REFERENCES events (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_event_tickets_citizen
        FOREIGN KEY (citizen_sub) REFERENCES users (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT chk_event_tickets_status CHECK (status IN ('PENDING','PAID','PURCHASED','CANCELLED','REFUNDED')),
    CONSTRAINT chk_event_tickets_price  CHECK (price_paid >= 0)
);

CREATE INDEX IF NOT EXISTS idx_event_tickets_event   ON event_tickets (event_id);
CREATE INDEX IF NOT EXISTS idx_event_tickets_citizen ON event_tickets (citizen_sub);

COMMENT ON TABLE  event_tickets             IS 'Tickets purchased by citizens for an event.';
COMMENT ON COLUMN event_tickets.status      IS 'PENDING, PAID, PURCHASED, CANCELLED or REFUNDED.';
COMMENT ON COLUMN event_tickets.citizen_sub IS 'Auth0 sub of the citizen that bought the ticket.';
COMMENT ON COLUMN event_tickets.stripe_checkout_session_id IS 'Stripe Checkout Session ID associated with this purchase (web flow).';

CREATE TABLE IF NOT EXISTS event_refunds (
    id              BIGSERIAL       NOT NULL,
    ticket_id       BIGINT          NOT NULL,
    amount          NUMERIC(10,2)   NOT NULL,
    currency        VARCHAR(3)      NOT NULL DEFAULT 'EUR',
    reason          VARCHAR(512),
    automatic       BOOLEAN         NOT NULL DEFAULT FALSE,
    issued_by_sub   VARCHAR(255),
    refunded_at     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_event_refunds             PRIMARY KEY (id),
    CONSTRAINT uq_event_refunds_ticket      UNIQUE (ticket_id),
    CONSTRAINT fk_event_refunds_ticket
        FOREIGN KEY (ticket_id) REFERENCES event_tickets (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_event_refunds_issued_by
        FOREIGN KEY (issued_by_sub) REFERENCES users (id)
        ON UPDATE CASCADE
        ON DELETE SET NULL,
    CONSTRAINT chk_event_refunds_amount     CHECK (amount >= 0)
);

CREATE INDEX IF NOT EXISTS idx_event_refunds_ticket ON event_refunds (ticket_id);

COMMENT ON TABLE  event_refunds             IS 'Refunds issued for event tickets (manual or automatic upon event deletion).';
COMMENT ON COLUMN event_refunds.automatic   IS 'TRUE when issued automatically (event soft-deletion); FALSE for manual refunds.';
COMMENT ON COLUMN event_refunds.issued_by_sub IS 'Auth0 sub of the staff member who triggered the refund (NULL for SYSTEM).';


-- ===== mobility =====
-- DDL: model-city-mobility microservice
-- Database: modelcity-mobility
-- Dependency order: cars -> street_reservations -> sanctions

CREATE TABLE IF NOT EXISTS cars (
    id              BIGSERIAL       NOT NULL,
    owner_sub       VARCHAR(255)    NOT NULL,
    license_plate   VARCHAR(32)     NOT NULL,
    nickname        VARCHAR(128),
    brand           VARCHAR(128),
    model           VARCHAR(128),
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT pk_cars              PRIMARY KEY (id),
    CONSTRAINT uq_cars_license      UNIQUE (license_plate),
    CONSTRAINT fk_cars_owner
        FOREIGN KEY (owner_sub) REFERENCES users (id)
        ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_cars_owner_sub ON cars (owner_sub);

COMMENT ON TABLE  cars                  IS 'Vehicles owned by core users (identified by Auth0 sub).';
COMMENT ON COLUMN cars.owner_sub        IS 'Auth0 sub of the owning user (core users table).';
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
    CONSTRAINT fk_street_reservations_user
        FOREIGN KEY (user_sub) REFERENCES users (id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
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
COMMENT ON COLUMN street_reservations.user_sub              IS 'Auth0 sub of the user that made the reservation.';
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
    CONSTRAINT chk_sanctions_longitude      CHECK (longitude BETWEEN -180 AND 180),
    CONSTRAINT fk_sanctions_agent
        FOREIGN KEY (agent_sub) REFERENCES users (id)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_sanctions_license_plate ON sanctions (license_plate);
CREATE INDEX IF NOT EXISTS idx_sanctions_created_at    ON sanctions (created_at);

COMMENT ON TABLE  sanctions                 IS 'Parking sanctions issued by operators or admins.';
COMMENT ON COLUMN sanctions.license_plate   IS 'License plate of the sanctioned vehicle.';
COMMENT ON COLUMN sanctions.image_base64    IS 'Base64-encoded evidence photo.';
COMMENT ON COLUMN sanctions.agent_sub       IS 'Auth0 sub of the operator or admin that issued the sanction.';

-- ============================================================================
-- System trails (audit log). One table per vertical. Admin-only read access.
-- Monolith: responsible_user_id / neighbourhood_id / zone_id are real FKs.
-- ============================================================================

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
    resource_id             VARCHAR(255),
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

CREATE TABLE IF NOT EXISTS engagement_trails (
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

    CONSTRAINT pk_engagement_trails PRIMARY KEY (event_id),
    CONSTRAINT chk_engagement_trails_operation CHECK (operation_type IN ('CREATE', 'UPDATE', 'DELETE')),
    CONSTRAINT fk_engagement_trails_user
        FOREIGN KEY (responsible_user_id) REFERENCES users (id) ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_engagement_trails_neighbourhood
        FOREIGN KEY (neighbourhood_id) REFERENCES neighbourhoods (id) ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_engagement_trails_zone
        FOREIGN KEY (zone_id) REFERENCES zones (id) ON UPDATE CASCADE ON DELETE SET NULL
);
CREATE INDEX IF NOT EXISTS idx_engagement_trails_type        ON engagement_trails (event_type);
CREATE INDEX IF NOT EXISTS idx_engagement_trails_user        ON engagement_trails (responsible_user_id);
CREATE INDEX IF NOT EXISTS idx_engagement_trails_occurred_at ON engagement_trails (occurred_at);

COMMENT ON TABLE engagement_trails IS 'Audit log of write operations in the engagement vertical.';

CREATE TABLE IF NOT EXISTS leisure_trails (
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

    CONSTRAINT pk_leisure_trails PRIMARY KEY (event_id),
    CONSTRAINT chk_leisure_trails_operation CHECK (operation_type IN ('CREATE', 'UPDATE', 'DELETE')),
    CONSTRAINT fk_leisure_trails_user
        FOREIGN KEY (responsible_user_id) REFERENCES users (id) ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_leisure_trails_neighbourhood
        FOREIGN KEY (neighbourhood_id) REFERENCES neighbourhoods (id) ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_leisure_trails_zone
        FOREIGN KEY (zone_id) REFERENCES zones (id) ON UPDATE CASCADE ON DELETE SET NULL
);
CREATE INDEX IF NOT EXISTS idx_leisure_trails_type        ON leisure_trails (event_type);
CREATE INDEX IF NOT EXISTS idx_leisure_trails_user        ON leisure_trails (responsible_user_id);
CREATE INDEX IF NOT EXISTS idx_leisure_trails_occurred_at ON leisure_trails (occurred_at);

COMMENT ON TABLE leisure_trails IS 'Audit log of write operations in the leisure vertical.';

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
    CONSTRAINT chk_mobility_trails_operation CHECK (operation_type IN ('CREATE', 'UPDATE', 'DELETE')),
    CONSTRAINT fk_mobility_trails_user
        FOREIGN KEY (responsible_user_id) REFERENCES users (id) ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_mobility_trails_neighbourhood
        FOREIGN KEY (neighbourhood_id) REFERENCES neighbourhoods (id) ON UPDATE CASCADE ON DELETE SET NULL,
    CONSTRAINT fk_mobility_trails_zone
        FOREIGN KEY (zone_id) REFERENCES zones (id) ON UPDATE CASCADE ON DELETE SET NULL
);
CREATE INDEX IF NOT EXISTS idx_mobility_trails_type        ON mobility_trails (event_type);
CREATE INDEX IF NOT EXISTS idx_mobility_trails_user        ON mobility_trails (responsible_user_id);
CREATE INDEX IF NOT EXISTS idx_mobility_trails_occurred_at ON mobility_trails (occurred_at);

COMMENT ON TABLE mobility_trails IS 'Audit log of write operations in the mobility vertical.';

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

CREATE TABLE IF NOT EXISTS civic_question_translations (
    question_id BIGINT NOT NULL, locale VARCHAR(10) NOT NULL, title VARCHAR(255), description TEXT,
    CONSTRAINT pk_civic_question_translations PRIMARY KEY (question_id, locale),
    CONSTRAINT fk_cq_translations_q FOREIGN KEY (question_id) REFERENCES civic_questions (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS objective_translations (
    objective_id BIGINT NOT NULL, locale VARCHAR(10) NOT NULL, objective TEXT,
    CONSTRAINT pk_objective_translations PRIMARY KEY (objective_id, locale),
    CONSTRAINT fk_objective_translations_o FOREIGN KEY (objective_id) REFERENCES objectives (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS security_alert_translations (
    alert_id BIGINT NOT NULL, locale VARCHAR(10) NOT NULL, title VARCHAR(255), description TEXT,
    CONSTRAINT pk_security_alert_translations PRIMARY KEY (alert_id, locale),
    CONSTRAINT fk_alert_translations_a FOREIGN KEY (alert_id) REFERENCES security_alerts (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS city_place_translations (
    place_id BIGINT NOT NULL, locale VARCHAR(10) NOT NULL, name VARCHAR(255), description TEXT,
    address VARCHAR(512), access_info TEXT, accessibility_info TEXT,
    CONSTRAINT pk_city_place_translations PRIMARY KEY (place_id, locale),
    CONSTRAINT fk_city_place_translations_p FOREIGN KEY (place_id) REFERENCES city_places (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS city_route_translations (
    route_id BIGINT NOT NULL, locale VARCHAR(10) NOT NULL, name VARCHAR(255), description TEXT,
    CONSTRAINT pk_city_route_translations PRIMARY KEY (route_id, locale),
    CONSTRAINT fk_city_route_translations_r FOREIGN KEY (route_id) REFERENCES city_routes (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS public_space_translations (
    space_id BIGINT NOT NULL, locale VARCHAR(10) NOT NULL, name VARCHAR(255), description TEXT, address VARCHAR(512),
    CONSTRAINT pk_public_space_translations PRIMARY KEY (space_id, locale),
    CONSTRAINT fk_public_space_translations_s FOREIGN KEY (space_id) REFERENCES public_spaces (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS reservable_resource_translations (
    resource_id BIGINT NOT NULL, locale VARCHAR(10) NOT NULL, name VARCHAR(255), description TEXT,
    CONSTRAINT pk_reservable_resource_translations PRIMARY KEY (resource_id, locale),
    CONSTRAINT fk_rr_translations_r FOREIGN KEY (resource_id) REFERENCES reservable_resources (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS event_translations (
    event_id BIGINT NOT NULL, locale VARCHAR(10) NOT NULL, name VARCHAR(255), description TEXT,
    CONSTRAINT pk_event_translations PRIMARY KEY (event_id, locale),
    CONSTRAINT fk_event_translations_e FOREIGN KEY (event_id) REFERENCES events (id) ON DELETE CASCADE
);

