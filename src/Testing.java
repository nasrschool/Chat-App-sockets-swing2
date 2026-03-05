import Tools.*;

import javax.xml.crypto.Data;
import java.sql.*;

public class Testing{
    public static void main(String[] args) throws SQLException {
        String url = "jdbc:mysql://localhost:3306/chat_app_server_side";
        String uName = "root";
        String password = "200608";
        Connection con = DriverManager.getConnection(url, uName, password);
        PreparedStatement pt = con.prepareStatement("SELECT * FROM groups_chats");
        ResultSet rs = pt.executeQuery();

        System.out.println(DataToJson.groupsMsgsDataToJson(DataToJson.resultSetToArray(rs)));

    }
}
