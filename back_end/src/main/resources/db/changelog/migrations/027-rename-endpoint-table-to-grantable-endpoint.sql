-- liquibase formatted sql

-- changeset tawfiq_akdir:1
ALTER TABLE endpoint RENAME TO grantable_endpoint;

-- rollback ALTER TABLE grantable_endpoint RENAME TO endpoint;

-- changeset tawfiq_akdir:2
ALTER INDEX idx_endpoint_category RENAME TO idx_grantable_endpoint_category;

-- rollback ALTER INDEX idx_grantable_endpoint_category RENAME TO idx_endpoint_category;

-- changeset tawfiq_akdir:3
ALTER TABLE grantable_endpoint RENAME CONSTRAINT uq_endpoint_name TO uq_grantable_endpoint_name;

-- rollback ALTER TABLE grantable_endpoint RENAME CONSTRAINT uq_grantable_endpoint_name TO uq_endpoint_name;
