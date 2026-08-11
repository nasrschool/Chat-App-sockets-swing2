-- Local demonstration accounts only.
INSERT INTO users_table(user_id,user_password) VALUES
    (1,'demo1'),
    (2,'demo2'),
    (3,'demo3'),
    (4,'demo4')
ON CONFLICT (user_id) DO NOTHING;

-- One demonstration group containing all four users.
INSERT INTO users_of_groups(group_id,user_id,is_private,group_name) VALUES
    (1,1,FALSE,'Demo Group'),
    (1,2,FALSE,'Demo Group'),
    (1,3,FALSE,'Demo Group'),
    (1,4,FALSE,'Demo Group')
ON CONFLICT (group_id,user_id) DO NOTHING;

INSERT INTO admins_of_groups(group_id,admin_id) VALUES (1,1)
ON CONFLICT (group_id,admin_id) DO NOTHING;
