ALTER TABLE reports
    ALTER COLUMN reporter_id DROP NOT NULL,
ALTER COLUMN reported_user_id DROP NOT NULL;