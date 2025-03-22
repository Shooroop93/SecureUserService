-- liquibase formatted sql

-- changeset sergeev:create-table-user-roles context:dev,prod labels:create-table-user-roles
CREATE TABLE user_roles(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    role_id UUID NOT NULL
);

-- rollback DROP TABLE IF EXISTS user_roles CASCADE;