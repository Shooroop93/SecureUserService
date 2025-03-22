-- liquibase formatted sql

-- changeset sergeev:create-table-platforms context:dev,prod labels:create-table-platforms
CREATE TABLE platforms (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name varchar UNIQUE NOT NULL
);

-- rollback DROP TABLE IF EXISTS platforms CASCADE;