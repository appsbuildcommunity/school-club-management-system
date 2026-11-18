-- liquibase formatted sql

-- changeset developer:1
ALTER TABLE post ADD COLUMN hidden_reason VARCHAR(500);

-- rollback ALTER TABLE post DROP COLUMN hidden_reason;
