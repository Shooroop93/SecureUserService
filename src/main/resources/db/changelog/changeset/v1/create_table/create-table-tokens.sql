-- liquibase formatted sql

-- changeset sergeev:create-table-tokens context:dev,prod labels:create-table-tokens
CREATE TABLE IF NOT EXISTS public.tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    token VARCHAR NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    expires_at TIMESTAMP NOT NULL
);

-- rollback DROP TABLE IF EXISTS tokens CASCADE;
