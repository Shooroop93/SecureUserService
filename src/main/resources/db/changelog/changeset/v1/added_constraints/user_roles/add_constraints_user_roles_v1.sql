-- liquibase formatted sql

-- changeset sergeev:add_constraints_user_roles_v1 context:dev,prod labels:add_constraints_user_roles_v1
ALTER TABLE user_roles
    ADD CONSTRAINT fk_user_roles_user_id FOREIGN KEY (user_id) REFERENCES users(id),
    ADD CONSTRAINT fk_user_roles_role_id FOREIGN KEY (role_id) REFERENCES roles(id);

-- rollback ALTER TABLE user_roles DROP CONSTRAINT IF EXISTS fk_user_roles_user_id, DROP CONSTRAINT IF EXISTS fk_user_roles_role_id