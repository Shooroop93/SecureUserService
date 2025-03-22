-- liquibase formatted sql

-- changeset user:add_login_column context:dev,prod labels:add_login_column
ALTER TABLE users
    ADD COLUMN login VARCHAR(255) NOT NULL UNIQUE;

-- rollback ALTER TABLE users DROP COLUMN login;