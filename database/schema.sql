CREATE TABLE IF NOT EXISTS users_table (
    user_id INTEGER PRIMARY KEY,
    user_password VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS users_of_groups (
    group_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    is_private BOOLEAN NOT NULL DEFAULT FALSE,
    group_name VARCHAR(100),
    PRIMARY KEY (group_id,user_id),
    CONSTRAINT fk_group_user FOREIGN KEY (user_id) REFERENCES users_table(user_id)
);

CREATE TABLE IF NOT EXISTS admins_of_groups (
    group_id INTEGER NOT NULL,
    admin_id INTEGER NOT NULL,
    PRIMARY KEY (group_id,admin_id),
    CONSTRAINT fk_group_admin FOREIGN KEY (admin_id) REFERENCES users_table(user_id)
);

CREATE TABLE IF NOT EXISTS groups_chats (
    message_id BIGSERIAL PRIMARY KEY,
    group_id INTEGER NOT NULL,
    user_id INTEGER NOT NULL,
    content VARCHAR(2000) NOT NULL,
    date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_message_user FOREIGN KEY (user_id) REFERENCES users_table(user_id)
);

CREATE INDEX IF NOT EXISTS idx_group_date ON groups_chats(group_id,date);
