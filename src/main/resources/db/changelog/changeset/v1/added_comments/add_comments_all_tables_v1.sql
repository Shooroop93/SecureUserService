-- liquibase formatted sql

-- changeset sergeev:add_comments_all_tables_v1 context:dev,prod labels:add_comments_all_tables_v1
COMMENT ON TABLE users IS 'Таблица хранения основных данных пользователей, включая email, пароль и дату создания.';
COMMENT ON COLUMN users.id IS 'Уникальный идентификатор пользователя.';
COMMENT ON COLUMN users.email IS 'Email пользователя (если зарегистрирован).';
COMMENT ON COLUMN users.password IS 'Хэшированный пароль пользователя (если зарегистрирован).';
COMMENT ON COLUMN users.created_at IS 'Дата создания аккаунта.';
COMMENT ON COLUMN users.updated_at IS 'Дата последнего обновления аккаунта.';

COMMENT ON TABLE platforms IS 'Таблица внешних платформ, таких как Telegram, Discord и др.';
COMMENT ON COLUMN platforms.id IS 'Уникальный идентификатор платформы.';
COMMENT ON COLUMN platforms.name IS 'Название платформы.';

COMMENT ON TABLE user_platforms IS 'Связывает пользователей с их уникальными идентификаторами на различных платформах.';
COMMENT ON COLUMN user_platforms.id IS 'Уникальный идентификатор записи.';
COMMENT ON COLUMN user_platforms.user_id IS 'Идентификатор пользователя (ссылка на таблицу users).';
COMMENT ON COLUMN user_platforms.platform_id IS 'Идентификатор платформы (ссылка на таблицу platforms).';
COMMENT ON COLUMN user_platforms.platform_user_id IS 'Уникальный идентификатор пользователя на платформе.';
COMMENT ON COLUMN user_platforms.created_at IS 'Дата создания записи.';

COMMENT ON TABLE roles IS 'Таблица хранения ролей пользователей для системы RBAC.';
COMMENT ON COLUMN roles.id IS 'Уникальный идентификатор роли.';
COMMENT ON COLUMN roles.name IS 'Название роли (например, USER, ADMIN).';

COMMENT ON TABLE user_roles IS 'Связывает пользователей с их ролями для контроля доступа.';
COMMENT ON COLUMN user_roles.id IS 'Уникальный идентификатор записи.';
COMMENT ON COLUMN user_roles.user_id IS 'Идентификатор пользователя (ссылка на таблицу users).';
COMMENT ON COLUMN user_roles.role_id IS 'Идентификатор роли (ссылка на таблицу roles).';

COMMENT ON TABLE tokens IS 'Таблица для хранения активных и отозванных JWT токенов пользователей.';
COMMENT ON COLUMN tokens.id IS 'Уникальный идентификатор токена.';
COMMENT ON COLUMN tokens.user_id IS 'Идентификатор пользователя, которому принадлежит токен.';
COMMENT ON COLUMN tokens.token IS 'JWT токен пользователя.';
COMMENT ON COLUMN tokens.revoked IS 'Признак soft-delete для токенов. Если true - токен отозван.';
COMMENT ON COLUMN tokens.created_at IS 'Дата создания токена.';
COMMENT ON COLUMN tokens.expires_at IS 'Дата истечения срока действия токена.';