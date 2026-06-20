-- SHA-256 hex çıktısı her zaman 64 karakterdir
ALTER TABLE users
    ADD COLUMN password_reset_token VARCHAR(64),
    ADD COLUMN password_reset_expiry TIMESTAMP;