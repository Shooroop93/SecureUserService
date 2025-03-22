-- liquibase formatted sql

-- changeset sergeev:create-table-users context:dev,prod labels:create-table-users
CREATE TABLE users(
    id         UUID PRIMARY KEY             DEFAULT gen_random_uuid(),
    email      varchar(255) UNIQUE NULL,
    password   varchar(255)        NULL,
    created_at timestamp           NOT NULL DEFAULT now(),
    updated_at timestamp           NOT NULL DEFAULT now()
);

-- rollback DROP TABLE IF EXISTS users CASCADE;