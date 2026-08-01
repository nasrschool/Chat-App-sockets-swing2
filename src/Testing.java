import Tools.*;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.xml.crypto.Data;
import java.sql.*;
import java.util.ArrayList;

public class Testing{
    public static ArrayList<Integer> arr = new ArrayList<>();
    public static void main(String[] args) throws Exception {
        String url = System.getenv().getOrDefault(
                "CHAT_DB_URL", "jdbc:mysql://localhost:3306/chat_app_server_side");
        String uName = System.getenv().getOrDefault("CHAT_DB_USER", "root");
        String password = System.getenv().getOrDefault("CHAT_DB_PASSWORD", "");
        Connection con = DriverManager.getConnection(url, uName, password);
        PreparedStatement pt = con.prepareStatement(Statements.usersOfGroups);
        pt.setInt(1,3);
        ResultSet rs = pt.executeQuery();

        //JSONObject groupsUsersData = DataToJson.groupsUsersDataToJson(DataToJson.resultSetToArray(rs));
        //System.out.println(groupsUsersData);

        arr.add(12);
        arr.add(5);
        arr.add(13);
        arr.add(17);

        ArrayList arr2 =(ArrayList<Integer>) arr.clone();

        System.out.println(arr2);
        arr.clear();
        System.out.println(arr2);
        System.out.println(arr);

    }

    public static void func(){

        for(int i : arr){
            System.out.println(i);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }


}
