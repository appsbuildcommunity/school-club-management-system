-- liquibase formatted sql

-- changeset tawfiq_akdir:1
ALTER TABLE assistant_member_privilege DROP COLUMN privilege;

-- rollback ALTER TABLE assistant_member_privilege ADD COLUMN privilege VARCHAR(50) NOT NULL;

-- changeset tawfiq_akdir:2
ALTER TABLE assistant_member_privilege ADD COLUMN endpoint_id BIGINT NOT NULL;
ALTER TABLE assistant_member_privilege ADD CONSTRAINT fk_privilege_endpoint FOREIGN KEY (endpoint_id) REFERENCES endpoint (endpoint_id);

-- rollback ALTER TABLE assistant_member_privilege DROP CONSTRAINT fk_privilege_endpoint;
-- rollback ALTER TABLE assistant_member_privilege DROP COLUMN endpoint_id;

-- changeset tawfiq_akdir:3
CREATE INDEX idx_privilege_endpoint_id ON assistant_member_privilege (endpoint_id);

-- rollback DROP INDEX IF EXISTS idx_privilege_endpoint_id;
