-- liquibase formatted sql

-- changeset sergeev:create-table-user-platforms context:dev,prod labels:create-table-user-platforms
CREATE TABLE user_platforms(
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    platform_id UUID NOT NULL,
    platform_user_id varchar NOT NULL,
    created_at timestamp NOT NULL DEFAULT NOW()
);

-- rollback DROP TABLE IF EXISTS user_platforms CASCADE;