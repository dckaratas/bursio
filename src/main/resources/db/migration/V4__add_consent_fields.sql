ALTER TABLE users
    ADD COLUMN privacy_policy_accepted BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN terms_accepted             BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN consent_accepted_at        TIMESTAMP;