-- liquibase formatted sql

-- changeset sergeev:add_column_tokens_v2 context:dev,prod labels:add_column_tokens_v2
ALTER TABLE tokens ADD COLUMN session_id UUID NOT NULL

-- rollback ALTER TABLE tokens DROP COLUMN session_id