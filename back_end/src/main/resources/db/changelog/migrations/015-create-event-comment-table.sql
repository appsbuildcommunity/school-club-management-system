-- liquibase formatted sql

-- changeset abdessalam:1
CREATE TABLE event_comment
(
    event_id   BIGINT NOT NULL,
    comment_id BIGINT NOT NULL,

    CONSTRAINT pk_event_comment PRIMARY KEY (event_id, comment_id),
    CONSTRAINT fk_ec_event FOREIGN KEY (event_id) REFERENCES event (event_id) ON DELETE CASCADE,
    CONSTRAINT fk_ec_comment FOREIGN KEY (comment_id) REFERENCES comment (comment_id) ON DELETE CASCADE,

    CONSTRAINT uq_ec_comment_id UNIQUE (comment_id)
);

-- rollback DROP TABLE event_comment;

-- changeset abdessalam:2
CREATE INDEX idx_ec_event_id ON event_comment (event_id);

-- rollback DROP INDEX IF EXISTS idx_ec_event_id;
