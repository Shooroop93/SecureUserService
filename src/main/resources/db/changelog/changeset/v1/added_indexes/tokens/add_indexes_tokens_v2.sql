-- liquibase formatted sql

-- changeset sergeev:add_indexes_tokens_v2 context:dev,prod labels:add_indexes_tokens_v2
CREATE INDEX IF NOT EXISTS idx_tokens_token ON tokens(token);

-- rollback DROP INDEX IF EXISTS idx_tokens_token;