ALTER TABLE commits
    ADD COLUMN second_parent_commit_id UUID;

ALTER TABLE commits
    ADD CONSTRAINT fk_commits_second_parent
        FOREIGN KEY (second_parent_commit_id)
            REFERENCES commits(id)
            ON DELETE RESTRICT;

CREATE INDEX idx_commits_second_parent_commit_id
    ON commits(second_parent_commit_id);