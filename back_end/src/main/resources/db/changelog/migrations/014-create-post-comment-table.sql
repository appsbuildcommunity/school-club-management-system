-- liquibase formatted sql

-- changeset abdessalam:1
CREATE TABLE post_comment
(
    post_id    BIGINT NOT NULL,
    comment_id BIGINT NOT NULL,

    CONSTRAINT pk_post_comments PRIMARY KEY (post_id, comment_id),
    CONSTRAINT fk_pc_post FOREIGN KEY (post_id) REFERENCES post (post_id) ON DELETE CASCADE,
    CONSTRAINT fk_pc_comment FOREIGN KEY (comment_id) REFERENCES comment (comment_id) ON DELETE CASCADE,

    CONSTRAINT uq_pc_comment_id UNIQUE (comment_id)
);

-- rollback DROP TABLE post_comment;

-- changeset abdessalam:2
CREATE INDEX idx_pc_post_id ON post_comment (post_id);

-- rollback DROP INDEX IF EXISTS idx_pc_post_id;