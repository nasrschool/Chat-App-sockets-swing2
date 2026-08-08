-- Local demonstration accounts only.
INSERT INTO users_table(user_id,user_password) VALUES
    (1,'demo1'),
    (2,'demo2'),
    (3,'demo3'),
    (4,'demo4')
ON CONFLICT (user_id) DO NOTHING;

-- Example direct conversation between users 1 and 2.
INSERT INTO users_of_groups(group_id,user_id,is_private,group_name) VALUES
    (1,1,TRUE,NULL),
    (1,2,TRUE,NULL)
ON CONFLICT (group_id,user_id) DO NOTHING;

-- Example normal group containing users 1, 2, and 3.
INSERT INTO users_of_groups(group_id,user_id,is_private,group_name) VALUES
    (2,1,FALSE,'Test Group'),
    (2,2,FALSE,'Test Group'),
    (2,3,FALSE,'Test Group')
ON CONFLICT (group_id,user_id) DO NOTHING;

INSERT INTO admins_of_groups(group_id,admin_id) VALUES (2,1)
ON CONFLICT (group_id,admin_id) DO NOTHING;

INSERT INTO groups_chats(group_id,user_id,content)
SELECT 1,1,'Hello User 2'
WHERE NOT EXISTS (SELECT 1 FROM groups_chats WHERE group_id = 1 AND user_id = 1 AND content = 'Hello User 2');

INSERT INTO groups_chats(group_id,user_id,content)
SELECT 1,2,'Hello User 1'
WHERE NOT EXISTS (SELECT 1 FROM groups_chats WHERE group_id = 1 AND user_id = 2 AND content = 'Hello User 1');

INSERT INTO groups_chats(group_id,user_id,content)
SELECT 2,3,'Hello everyone'
WHERE NOT EXISTS (SELECT 1 FROM groups_chats WHERE group_id = 2 AND user_id = 3 AND content = 'Hello everyone');
