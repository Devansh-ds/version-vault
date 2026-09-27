-- ============================================================
-- VersionVault - JWT Token Storage
-- V3
-- ============================================================

CREATE TABLE jwt_token (
   id BIGSERIAL PRIMARY KEY,

   token VARCHAR(2000) NOT NULL,

   token_type VARCHAR(50) NOT NULL,

   expired BOOLEAN NOT NULL DEFAULT FALSE,

   revoked BOOLEAN NOT NULL DEFAULT FALSE,

   user_id UUID NOT NULL,

   CONSTRAINT fk_jwt_token_user
       FOREIGN KEY (user_id)
           REFERENCES users(id)
           ON DELETE CASCADE,

   CONSTRAINT chk_jwt_token_type
       CHECK (token_type IN ('BEARER'))
);


-- ============================================================
-- INDEXES
-- ============================================================

CREATE INDEX idx_jwt_token_user_id
    ON jwt_token(user_id);

CREATE INDEX idx_jwt_token_token
    ON jwt_token(token);