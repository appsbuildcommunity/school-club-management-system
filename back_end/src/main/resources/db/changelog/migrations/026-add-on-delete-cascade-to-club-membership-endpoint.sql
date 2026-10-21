-- liquibase formatted sql

-- changeset tawfiq_akdir:1
-- The club-profile-endpoints join table (022) already cascades on endpoint delete;
-- make the membership grants behave the same so the EndpointRegistrar can prune
-- endpoints that no longer exist in the code without hitting FK violations.
ALTER TABLE club_membership_endpoint DROP CONSTRAINT fk_cme_endpoint;
ALTER TABLE club_membership_endpoint
    ADD CONSTRAINT fk_cme_endpoint FOREIGN KEY (endpoint_id)
        REFERENCES endpoint (endpoint_id) ON DELETE CASCADE;

-- rollback ALTER TABLE club_membership_endpoint DROP CONSTRAINT fk_cme_endpoint;
-- rollback ALTER TABLE club_membership_endpoint ADD CONSTRAINT fk_cme_endpoint FOREIGN KEY (endpoint_id) REFERENCES endpoint (endpoint_id);