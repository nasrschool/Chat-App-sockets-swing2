package Tools;

public class Statements {
    public static String newUser = "INSERT INTO users_table(user_id,user_password) values(?,?)";
    public static String selectUser = "SELECT * FROM users_table WHERE user_id = ?;";
    public static String getLastUserId = "SELECT MAX(user_id) as last_id FROM users_table;";
    public static String getLastGroupId = "SELECT MAX(group_id) as last_id FROM admins_of_groups;";
    public static String usersOfGroups = "SELECT * FROM users_of_groups WHERE user_id = ? ";
    public static String groupsChats = "SELECT * FROM groups_chats WHERE group_id = ?";
    public static String addGroupUser = "INSERT INTO users_of_groups(group_id,user_id,is_private) VALUES (?,?,?);";
    public static String addGroupAdmin = "INSERT INTO admins_of_groups(group_id,admin_id) values (?,?);";

}
