ALTER TABLE users
ADD COLUMN owner_id BIGINT UNIQUE,
ADD CONSTRAINT fk_users_owner
    FOREIGN KEY (owner_id)
    REFERENCES owners (id)
    ON DELETE SET NULL;
