-- liquibase formatted sql

-- changeset tawfiq_akdir:1
ALTER TABLE club ADD COLUMN is_coordination_club BOOLEAN NOT NULL DEFAULT FALSE;

-- rollback ALTER TABLE club DROP COLUMN is_coordination_club;
