package Tools;

public class Statements {
    public static String newUser = "INSERT INTO users_table(user_id,user_password) values(?,?)";
    public static String selectUser = "SELECT * FROM users_table WHERE user_id = ?;";
    public static String getLastUserId = "SELECT MAX(user_id) as last_id FROM users_table;";

}
