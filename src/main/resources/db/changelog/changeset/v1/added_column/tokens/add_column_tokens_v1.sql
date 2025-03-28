-- liquibase formatted sql

-- changeset sergeev:add_column_tokens_v1 context:dev,prod labels:add_column_tokens_v1
ALTER TABLE tokens ADD COLUMN token_type VARCHAR(20) NOT NULL

-- rollback ALTER TABLE tokens DROP COLUMN token_type