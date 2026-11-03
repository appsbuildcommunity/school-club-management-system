-- liquibase formatted sql

-- changeset tawfiq_akdir:032-user-onboarding-completed
ALTER TABLE users
    ADD COLUMN onboarding_completed BOOLEAN NOT NULL DEFAULT FALSE;

-- Existing users have already used the app; only newly provisioned users need onboarding.
UPDATE users
SET onboarding_completed = TRUE;

-- rollback ALTER TABLE users DROP COLUMN onboarding_completed;
