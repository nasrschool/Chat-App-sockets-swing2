package Tools;

public class Statements {
    public static String newUser = "INSERT INTO users_table(user_id,user_password) values(?,?)";
    public static String selectUser = "SELECT * FROM users_table WHERE user_id = ?;";
    public static String getLastUserId = "SELECT MAX(user_id) as last_id FROM users_table;";
    public static String getLastGroupId = "SELECT MAX(group_id) as last_id FROM users_of_groups;";
    public static String usersOfGroups = "SELECT all_members.group_id, all_members.user_id, all_members.is_private, all_members.group_name FROM users_of_groups member_groups JOIN users_of_groups all_members ON member_groups.group_id = all_members.group_id WHERE member_groups.user_id = ? ORDER BY all_members.group_id, all_members.user_id;";
    public static String groupsChats = "SELECT group_id,user_id,content,date FROM groups_chats WHERE group_id = ? ORDER BY date";
    public static String addGroupUser = "INSERT INTO users_of_groups(group_id,user_id,is_private,group_name) VALUES (?,?,?,?);";
    public static String addGroupAdmin = "INSERT INTO admins_of_groups(group_id,admin_id) values (?,?);";
    public static String insertMessage = "INSERT INTO groups_chats(group_id,user_id,content,date) VALUES (?,?,?,CURRENT_TIMESTAMP);";
    public static String getGroupUsers = "SELECT user_id FROM users_of_groups WHERE group_id = ?;";
    public static String isGroupMember = "SELECT 1 FROM users_of_groups WHERE group_id = ? AND user_id = ?;";
    public static String userExists = "SELECT 1 FROM users_table WHERE user_id = ?;";
    public static String existingPrivateGroup = "SELECT group_id FROM users_of_groups WHERE is_private = TRUE GROUP BY group_id HAVING COUNT(*) = 2 AND SUM(user_id = ?) = 1 AND SUM(user_id = ?) = 1 LIMIT 1;";

}
