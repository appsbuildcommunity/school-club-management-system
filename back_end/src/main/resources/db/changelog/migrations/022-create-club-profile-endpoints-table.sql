-- liquibase formatted sql

-- changeset tawfiq_akdir:1
CREATE TABLE club_profile_endpoints
(
    club_profile_id BIGINT NOT NULL,
    endpoint_id     BIGINT NOT NULL,

    CONSTRAINT pk_club_profile_endpoints PRIMARY KEY (club_profile_id, endpoint_id),
    CONSTRAINT fk_cpe_profile FOREIGN KEY (club_profile_id) REFERENCES club_profile (club_profile_id) ON DELETE CASCADE,
    CONSTRAINT fk_cpe_endpoint FOREIGN KEY (endpoint_id) REFERENCES endpoint (endpoint_id) ON DELETE CASCADE
);

-- rollback DROP TABLE club_profile_endpoints;

-- changeset tawfiq_akdir:2
CREATE INDEX idx_cpe_profile_id ON club_profile_endpoints (club_profile_id);
CREATE INDEX idx_cpe_endpoint_id ON club_profile_endpoints (endpoint_id);

-- rollback DROP INDEX IF EXISTS idx_cpe_endpoint_id;
-- rollback DROP INDEX IF EXISTS idx_cpe_profile_id;
