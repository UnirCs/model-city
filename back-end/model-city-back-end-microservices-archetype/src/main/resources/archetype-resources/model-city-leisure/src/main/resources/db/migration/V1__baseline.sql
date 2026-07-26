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
    CONSTRAINT chk_event_refunds_amount     CHECK (amount >= 0)
);

CREATE INDEX IF NOT EXISTS idx_event_refunds_ticket ON event_refunds (ticket_id);

COMMENT ON TABLE  event_refunds             IS 'Refunds issued for event tickets (manual or automatic upon event deletion).';
COMMENT ON COLUMN event_refunds.automatic   IS 'TRUE when issued automatically (event soft-deletion); FALSE for manual refunds.';
COMMENT ON COLUMN event_refunds.issued_by_sub IS 'Auth0 sub of the staff member who triggered the refund (NULL for SYSTEM).';


-- System trails (audit log) for the leisure vertical. Admin-only read access.
-- Microservice: zone/neighbourhood/responsible user are soft references (no FK).
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
    CONSTRAINT chk_leisure_trails_operation CHECK (operation_type IN ('CREATE', 'UPDATE', 'DELETE'))
);
CREATE INDEX IF NOT EXISTS idx_leisure_trails_type        ON leisure_trails (event_type);
CREATE INDEX IF NOT EXISTS idx_leisure_trails_user        ON leisure_trails (responsible_user_id);
CREATE INDEX IF NOT EXISTS idx_leisure_trails_occurred_at ON leisure_trails (occurred_at);

COMMENT ON TABLE leisure_trails IS 'Audit log of write operations in the leisure vertical.';


-- i18n translation side tables (default-locale value stays in the base table).
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
