-- liquibase formatted sql

-- changeset sergeev:add_constraints_tokens_v1 context:dev,prod labels:add_constraints_tokens_v1
ALTER TABLE tokens
    ADD CONSTRAINT fk_tokens_user_id FOREIGN KEY (user_id) REFERENCES users(id);

-- rollback ALTER TABLE tokens DROP CONSTRAINT IF EXISTS fk_tokens_user_id;