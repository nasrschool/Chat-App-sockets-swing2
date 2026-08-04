CREATE DATABASE IF NOT EXISTS chat_app_server_side;
USE chat_app_server_side;

CREATE TABLE IF NOT EXISTS users_table (
    user_id INT PRIMARY KEY,
    user_password VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS users_of_groups (
    group_id INT NOT NULL,
    user_id INT NOT NULL,
    is_private BOOLEAN NOT NULL DEFAULT FALSE,
    group_name VARCHAR(100) NULL,
    PRIMARY KEY (group_id,user_id),
    CONSTRAINT fk_group_user FOREIGN KEY (user_id) REFERENCES users_table(user_id)
);

CREATE TABLE IF NOT EXISTS admins_of_groups (
    group_id INT NOT NULL,
    admin_id INT NOT NULL,
    PRIMARY KEY (group_id,admin_id),
    CONSTRAINT fk_group_admin FOREIGN KEY (admin_id) REFERENCES users_table(user_id)
);

CREATE TABLE IF NOT EXISTS groups_chats (
    message_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id INT NOT NULL,
    user_id INT NOT NULL,
    content VARCHAR(2000) NOT NULL,
    date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_message_user FOREIGN KEY (user_id) REFERENCES users_table(user_id),
    INDEX idx_group_date (group_id,date)
);
