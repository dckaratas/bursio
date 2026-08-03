-- V1__init_schema.sql

CREATE TABLE users
(
    id                 BIGSERIAL PRIMARY KEY,
    email              VARCHAR(255) NOT NULL UNIQUE,
    password           VARCHAR(255) NOT NULL,
    role               VARCHAR(20)  NOT NULL,
    status             VARCHAR(30)  NOT NULL DEFAULT 'PENDING_VERIFICATION',
    first_name         VARCHAR(100),
    last_name          VARCHAR(100),
    email_verified     BOOLEAN      NOT NULL DEFAULT FALSE,
    verification_token VARCHAR(255),
    token_expiry       TIMESTAMP,
    created_at         TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE universities
(
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    city       VARCHAR(100),
    active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE university_email_domains
(
    university_id BIGINT       NOT NULL REFERENCES universities (id),
    domain        VARCHAR(100) NOT NULL,
    PRIMARY KEY (university_id, domain)
);

CREATE TABLE student_profiles
(
    id                 BIGSERIAL PRIMARY KEY,
    user_id            BIGINT    NOT NULL UNIQUE REFERENCES users (id),
    university_id      BIGINT REFERENCES universities (id),
    department         VARCHAR(255),
    grade              INTEGER,
    gpa                NUMERIC(3, 2),
    bio                TEXT,
    motivation         TEXT,
    contact_preference VARCHAR(20),
    contact_value      VARCHAR(255),
    profile_complete   BOOLEAN   NOT NULL DEFAULT FALSE,
    created_at         TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE match_requests
(
    id            BIGSERIAL PRIMARY KEY,
    donor_id      BIGINT      NOT NULL REFERENCES users (id),
    student_id    BIGINT      NOT NULL REFERENCES users (id),
    status        VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    donor_message TEXT,
    responded_at  TIMESTAMP,
    expires_at    TIMESTAMP,
    created_at    TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMP   NOT NULL DEFAULT NOW()
);

CREATE TABLE reports
(
    id               BIGSERIAL PRIMARY KEY,
    reporter_id      BIGINT      NOT NULL REFERENCES users (id),
    reported_user_id BIGINT      NOT NULL REFERENCES users (id),
    reason           VARCHAR(50),
    description      TEXT,
    report_status    VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    created_at       TIMESTAMP   NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP   NOT NULL DEFAULT NOW()
);