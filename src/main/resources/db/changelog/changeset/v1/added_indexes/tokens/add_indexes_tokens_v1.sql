-- liquibase formatted sql

-- changeset sergeev:add_indexes_tokens_v1 context:dev,prod labels:add_indexes_tokens_v1
CREATE INDEX IF NOT EXISTS idx_tokens_user_id ON tokens(user_id);

-- rollback DROP INDEX IF EXISTS idx_tokens_user_id;