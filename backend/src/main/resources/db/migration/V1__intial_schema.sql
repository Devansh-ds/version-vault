-- ============================================================
-- VersionVault - Initial Database Schema
-- V1
-- ============================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;


-- ============================================================
-- USERS
-- ============================================================

CREATE TABLE users (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                       username VARCHAR(50) NOT NULL,
                       email VARCHAR(255) NOT NULL,

                       created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                       CONSTRAINT uq_users_username UNIQUE (username),
                       CONSTRAINT uq_users_email UNIQUE (email)
);


-- ============================================================
-- OBJECTS
-- Immutable file content
-- ============================================================

CREATE TABLE objects (
                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                         content_hash VARCHAR(64) NOT NULL,
                         size BIGINT NOT NULL,
                         storage_key VARCHAR(500) NOT NULL,

                         created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                         CONSTRAINT uq_objects_content_hash UNIQUE (content_hash),
                         CONSTRAINT uq_objects_storage_key UNIQUE (storage_key),

                         CONSTRAINT chk_objects_size_non_negative
                             CHECK (size >= 0)
    );


-- ============================================================
-- REPOSITORIES
-- ============================================================

CREATE TABLE repositories (
                              id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                              owner_id UUID NOT NULL,
                              name VARCHAR(100) NOT NULL,
                              description TEXT,

                              created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                              CONSTRAINT uq_repositories_owner_name
                                  UNIQUE (owner_id, name),

                              CONSTRAINT fk_repositories_owner
                                  FOREIGN KEY (owner_id)
                                      REFERENCES users(id)
                                      ON DELETE CASCADE
);


-- ============================================================
-- MANIFESTS
-- Immutable snapshot of repository files
-- ============================================================

CREATE TABLE manifests (
                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                           repository_id UUID NOT NULL,

                           created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                           CONSTRAINT fk_manifests_repository
                               FOREIGN KEY (repository_id)
                                   REFERENCES repositories(id)
                                   ON DELETE CASCADE
);


-- ============================================================
-- COMMITS
-- ============================================================

CREATE TABLE commits (
                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                         repository_id UUID NOT NULL,

                         parent_commit_id UUID,

                         manifest_id UUID NOT NULL,

                         author_id UUID NOT NULL,

                         message VARCHAR(500) NOT NULL,

                         created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                         CONSTRAINT uq_commits_manifest
                             UNIQUE (manifest_id),

                         CONSTRAINT fk_commits_repository
                             FOREIGN KEY (repository_id)
                                 REFERENCES repositories(id)
                                 ON DELETE CASCADE,

                         CONSTRAINT fk_commits_parent
                             FOREIGN KEY (parent_commit_id)
                                 REFERENCES commits(id)
                                 ON DELETE RESTRICT,

                         CONSTRAINT fk_commits_manifest
                             FOREIGN KEY (manifest_id)
                                 REFERENCES manifests(id)
                                 ON DELETE CASCADE,

                         CONSTRAINT fk_commits_author
                             FOREIGN KEY (author_id)
                                 REFERENCES users(id)
                                 ON DELETE RESTRICT
);


-- ============================================================
-- COMPOSITE KEY SUPPORT
--
-- Used to ensure:
-- branch.repository_id == branch.head_commit.repository_id
-- ============================================================

ALTER TABLE commits
    ADD CONSTRAINT uq_commits_id_repository
        UNIQUE (id, repository_id);


-- ============================================================
-- BRANCHES
-- ============================================================

CREATE TABLE branches (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                          repository_id UUID NOT NULL,

                          name VARCHAR(100) NOT NULL,

                          head_commit_id UUID,

                          created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                          CONSTRAINT uq_branches_repository_name
                              UNIQUE (repository_id, name),

                          CONSTRAINT fk_branches_repository
                              FOREIGN KEY (repository_id)
                                  REFERENCES repositories(id)
                                  ON DELETE CASCADE,

                          CONSTRAINT fk_branches_head_commit
                              FOREIGN KEY (head_commit_id, repository_id)
                                  REFERENCES commits(id, repository_id)
                                  ON DELETE RESTRICT
);


-- ============================================================
-- WORKING ENTRIES
-- Current mutable state of a branch
-- ============================================================

CREATE TABLE working_entries (
                                 id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                                 branch_id UUID NOT NULL,

                                 path VARCHAR(1000) NOT NULL,

                                 object_id UUID NOT NULL,

                                 created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                 updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                 CONSTRAINT uq_working_entries_branch_path
                                     UNIQUE (branch_id, path),

                                 CONSTRAINT fk_working_entries_branch
                                     FOREIGN KEY (branch_id)
                                         REFERENCES branches(id)
                                         ON DELETE CASCADE,

                                 CONSTRAINT fk_working_entries_object
                                     FOREIGN KEY (object_id)
                                         REFERENCES objects(id)
                                         ON DELETE RESTRICT
);


-- ============================================================
-- MANIFEST ENTRIES
-- Immutable files belonging to a manifest
-- ============================================================

CREATE TABLE manifest_entries (
                                  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

                                  manifest_id UUID NOT NULL,

                                  path VARCHAR(1000) NOT NULL,

                                  object_id UUID NOT NULL,

                                  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                  CONSTRAINT uq_manifest_entries_manifest_path
                                      UNIQUE (manifest_id, path),

                                  CONSTRAINT fk_manifest_entries_manifest
                                      FOREIGN KEY (manifest_id)
                                          REFERENCES manifests(id)
                                          ON DELETE CASCADE,

                                  CONSTRAINT fk_manifest_entries_object
                                      FOREIGN KEY (object_id)
                                          REFERENCES objects(id)
                                          ON DELETE RESTRICT
);


-- ============================================================
-- INDEXES
-- ============================================================

CREATE INDEX idx_manifests_repository_id
    ON manifests(repository_id);

CREATE INDEX idx_commits_repository_created_at
    ON commits(repository_id, created_at);

CREATE INDEX idx_working_entries_branch_id
    ON working_entries(branch_id);

CREATE INDEX idx_manifest_entries_manifest_id
    ON manifest_entries(manifest_id);

CREATE INDEX idx_branches_repository_id
    ON branches(repository_id);

CREATE INDEX idx_commits_parent_commit_id
    ON commits(parent_commit_id);