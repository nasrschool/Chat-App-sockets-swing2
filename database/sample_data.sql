USE chat_app_server_side;

-- Local demonstration accounts only. Change these passwords for your own setup.
INSERT INTO users_table(user_id,user_password) VALUES
    (1,'demo1'),
    (2,'demo2'),
    (3,'demo3')
ON DUPLICATE KEY UPDATE user_id = VALUES(user_id);

-- Example direct conversation between users 1 and 2.
INSERT IGNORE INTO users_of_groups(group_id,user_id,is_private,group_name) VALUES
    (1,1,TRUE,NULL),
    (1,2,TRUE,NULL);

-- Example group containing all three users. The creator is its administrator.
INSERT IGNORE INTO users_of_groups(group_id,user_id,is_private,group_name) VALUES
    (2,1,FALSE,'Test Group'),
    (2,2,FALSE,'Test Group'),
    (2,3,FALSE,'Test Group');
INSERT IGNORE INTO admins_of_groups(group_id,admin_id) VALUES (2,1);

INSERT INTO groups_chats(group_id,user_id,content) VALUES
    (1,1,'Hello User 2'),
    (2,3,'Hello everyone');
