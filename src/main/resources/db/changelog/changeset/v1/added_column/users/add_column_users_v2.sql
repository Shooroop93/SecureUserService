-- liquibase formatted sql

-- changeset user:add column is_verified context:dev,prod labels:add_column_users_v2
ALTER TABLE users
    ADD COLUMN is_verified boolean NOT NULL DEFAULT false;

-- rollback ALTER TABLE users DROP COLUMN is_verified;