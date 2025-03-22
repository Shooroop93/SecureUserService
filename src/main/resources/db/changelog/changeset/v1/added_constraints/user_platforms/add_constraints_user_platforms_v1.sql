-- liquibase formatted sql

-- changeset sergeev:add_constraints_user_platforms_v1 context:dev,prod labels:add_constraints_user_platforms_v1
ALTER TABLE user_platforms
    ADD CONSTRAINT fk_user_platforms_user_id FOREIGN KEY (user_id) REFERENCES users(id),
    ADD CONSTRAINT fk_user_platforms_platform_id FOREIGN KEY (platform_id) REFERENCES platforms(id);


-- rollback ALTER TABLE user_platforms DROP CONSTRAINT IF EXISTS fk_user_platforms_user_id, DROP CONSTRAINT IF EXISTS fk_user_platforms_platform_id;