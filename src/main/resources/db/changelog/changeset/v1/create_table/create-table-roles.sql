-- liquibase formatted sql

-- changeset sergeev:create-table-roles context:dev,prod labels:create-table-roles
CREATE TABLE roles(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name varchar(100) UNIQUE NOT NULL
);

-- rollback DROP TABLE IF EXISTS roles CASCADE ;