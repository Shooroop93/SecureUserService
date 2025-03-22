-- liquibase formatted sql

-- changeset sergeev:add_indexes_user_roles_v1 context:dev,prod labels:add_indexes_user_roles_v1
CREATE INDEX IF NOT EXISTS idx_user_roles_user_id ON user_roles(user_id);

CREATE UNIQUE INDEX IF NOT EXISTS idx_user_roles_unique ON user_roles(user_id, role_id);

-- rollback DROP INDEX IF EXISTS idx_user_roles_user_id;
-- rollback DROP INDEX IF EXISTS idx_user_roles_unique;