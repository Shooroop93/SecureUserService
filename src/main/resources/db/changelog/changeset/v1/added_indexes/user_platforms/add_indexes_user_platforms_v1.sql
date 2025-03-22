-- liquibase formatted sql

-- changeset sergeev:add_indexes_user_platforms_v1 context:dev,prod labels:add_indexes_user_platforms_v1
CREATE INDEX IF NOT EXISTS idx_user_platforms_user_id ON user_platforms(user_id);
CREATE UNIQUE INDEX IF NOT EXISTS idx_user_platforms_unique ON user_platforms(user_id, platform_id);

-- rollback DROP INDEX IF EXISTS idx_user_platforms_user_id;
-- rollback DROP INDEX IF EXISTS idx_user_platforms_unique;