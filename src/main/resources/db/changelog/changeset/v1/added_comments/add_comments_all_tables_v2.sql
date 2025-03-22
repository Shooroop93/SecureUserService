-- liquibase formatted sql

-- changeset sergeev:add_comments_all_tables_v2 context:dev,prod labels:add_comments_all_tables_v2
COMMENT ON COLUMN users.login IS 'Уникальный логин пользователя для авторизации';