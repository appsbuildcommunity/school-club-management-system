-- liquibase formatted sql

-- changeset abdessalam:1
CREATE TABLE event_attachment
(
    event_id      BIGINT NOT NULL,
    attachment_id BIGINT NOT NULL,

    CONSTRAINT pk_event_attachment PRIMARY KEY (event_id, attachment_id),

    CONSTRAINT fk_ea_event FOREIGN KEY (event_id) REFERENCES event (event_id) ON DELETE CASCADE,
    CONSTRAINT fk_ea_attachment FOREIGN KEY (attachment_id) REFERENCES attachment (attachment_id) ON DELETE CASCADE
);

-- rollback DROP TABLE event_attachment;

-- changeset abdessalam:2
CREATE INDEX idx_ea_event_id ON event_attachment (event_id);
CREATE INDEX idx_ea_attachment_id ON event_attachment (attachment_id);

-- rollback DROP INDEX IF EXISTS idx_ea_attachment_id;
-- rollback DROP INDEX IF EXISTS idx_ea_event_id;