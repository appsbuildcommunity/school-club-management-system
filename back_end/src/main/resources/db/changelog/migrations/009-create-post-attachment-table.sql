-- liquibase formatted sql

-- changeset abdessalam:1
CREATE TABLE post_attachment
(
    post_id       BIGINT NOT NULL,
    attachment_id BIGINT NOT NULL,

    CONSTRAINT pk_post_attachment PRIMARY KEY (post_id, attachment_id),

    CONSTRAINT fk_pa_post FOREIGN KEY (post_id) REFERENCES post (post_id) ON DELETE CASCADE,
    CONSTRAINT fk_pa_attachment FOREIGN KEY (attachment_id) REFERENCES attachment (attachment_id) ON DELETE CASCADE
);

-- rollback DROP TABLE post_attachment;

-- changeset abdessalam:2
CREATE INDEX idx_pa_post_id ON post_attachment (post_id);

-- rollback DROP INDEX IF EXISTS idx_pa_post_id;