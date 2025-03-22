-- liquibase formatted sql

-- changeset sergeev:create-extension-pgcrypto context:dev,prod labels:create-extension-pgcrypto
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- rollback DROP EXTENSION IF EXISTS "pgcrypto";