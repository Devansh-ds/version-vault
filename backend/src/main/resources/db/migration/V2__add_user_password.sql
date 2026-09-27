-- ============================================================
-- VersionVault - Add User Authentication
-- V2
-- ============================================================

ALTER TABLE users
    ADD COLUMN password VARCHAR(255) NOT NULL;