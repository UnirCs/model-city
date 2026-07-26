-- DDL: model-city-engagement microservice
-- Database: modelcity-engagement
-- Dependency order: civic_questions -> objectives -> answers
-- Note: zone_id, neighbourhood_id and citizen_id are soft references to the core service (no FK constraints).

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
    CONSTRAINT chk_civic_questions_dates CHECK (close_date > open_date)
);

COMMENT ON TABLE  civic_questions                    IS 'Civic questions issued by administrators for citizen YES/NO voting.';
COMMENT ON COLUMN civic_questions.id                 IS 'Surrogate primary key.';
COMMENT ON COLUMN civic_questions.title              IS 'Short title of the question.';
COMMENT ON COLUMN civic_questions.description        IS 'Full body describing the proposal.';
COMMENT ON COLUMN civic_questions.image_url          IS 'URL of the representative image (CDN or external source).';
COMMENT ON COLUMN civic_questions.open_date          IS 'First day citizens can vote.';
COMMENT ON COLUMN civic_questions.close_date         IS 'Last day citizens can vote (exclusive after this date).';
COMMENT ON COLUMN civic_questions.zone_id            IS 'Soft ref to core service — district the question belongs to.';
COMMENT ON COLUMN civic_questions.neighbourhood_id   IS 'Soft ref to core service — specific neighbourhood within the zone.';
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
        ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_answers_question_id ON answers (question_id);
CREATE INDEX IF NOT EXISTS idx_answers_citizen_id  ON answers (question_id, citizen_id);

COMMENT ON TABLE  answers             IS 'Citizen votes on civic questions. Deduplicated by verified DNI hash (one vote per DNI per question).';
COMMENT ON COLUMN answers.question_id IS 'FK to civic_questions.';
COMMENT ON COLUMN answers.citizen_id  IS 'Soft ref to core service — Auth0 sub propagated by the gateway via X-Auth-Sub. Kept for the UI hint and audit.';
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
    CONSTRAINT chk_security_alerts_expiry    CHECK (expires_at > created_at)
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
COMMENT ON COLUMN security_alerts.zone_id          IS 'Soft ref to core service. Null neighbourhood_id means the whole zone.';
COMMENT ON COLUMN security_alerts.neighbourhood_id IS 'Soft ref to a specific neighbourhood in core service. NULL = applies to the whole zone.';
COMMENT ON COLUMN security_alerts.created_at       IS 'UTC timestamp when the alert was created.';
COMMENT ON COLUMN security_alerts.expires_at       IS 'UTC timestamp after which the alert is no longer shown.';

-- System trails (audit log) for the engagement vertical. Admin-only read access.
-- Microservice: zone/neighbourhood/responsible user are soft references (no FK).
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
    CONSTRAINT chk_engagement_trails_operation CHECK (operation_type IN ('CREATE', 'UPDATE', 'DELETE'))
);
CREATE INDEX IF NOT EXISTS idx_engagement_trails_type        ON engagement_trails (event_type);
CREATE INDEX IF NOT EXISTS idx_engagement_trails_user        ON engagement_trails (responsible_user_id);
CREATE INDEX IF NOT EXISTS idx_engagement_trails_occurred_at ON engagement_trails (occurred_at);

COMMENT ON TABLE engagement_trails IS 'Audit log of write operations in the engagement vertical.';

-- i18n translation side tables (default-locale value stays in the base table).
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
