-- liquibase formatted sql

-- changeset abdessalam:1
CREATE TABLE event_post
(
    event_id BIGINT NOT NULL,
    post_id  BIGINT NOT NULL,

    CONSTRAINT pk_event_posts PRIMARY KEY (event_id, post_id),

    CONSTRAINT fk_ep_event FOREIGN KEY (event_id) REFERENCES event (event_id) ON DELETE CASCADE,
    CONSTRAINT fk_ep_post FOREIGN KEY (post_id) REFERENCES post (post_id) ON DELETE CASCADE,

    CONSTRAINT uq_ep_post_id UNIQUE (post_id)
);

-- rollback DROP TABLE event_post;

-- changeset abdessalam:2
CREATE INDEX idx_ep_event_id ON event_post (event_id);

-- rollback DROP INDEX IF EXISTS idx_ep_event_id;