package Tools;


import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class DataToJson {
    //each json contains other jsons that represent groups, and each group contains an array of
    // its users, if i recall correctly
    public static JSONObject groupsUsersDataToJson(ArrayList<HashMap<String,String>> data) throws SQLException {
        // TO DO: add the admin property too!
        // TO DO: clean this function
        JSONObject json = new JSONObject();
        JSONArray jsonArr = new JSONArray();
        HashMap<Integer,Integer> groupIdToIndex = new HashMap<>();
        ArrayList<ArrayList<Integer>> arrayOfUsersInGroups = new ArrayList<>();
        ArrayList<JSONObject> arrayOfGroupJsons = new ArrayList<>();

        for(HashMap<String,String> line: data){
            System.out.println("from groupsUsersDataToJson: " +line);
            int groupId = Integer.parseInt(line.get("group_id"));
            int userId = Integer.parseInt(line.get("user_id"));
            boolean isPrivate = Boolean.parseBoolean(line.get("is_private"));
            if(groupIdToIndex.get(groupId) == null){
                JSONObject tmpJson = new JSONObject();
                tmpJson.put("group_id",groupId);
                tmpJson.put("is_private",isPrivate);

                arrayOfUsersInGroups.add(new ArrayList<>());

                arrayOfGroupJsons.add(tmpJson);

                groupIdToIndex.put(groupId,arrayOfGroupJsons.size() - 1);
            }


            int indexOfGroupIdJson = groupIdToIndex.get(groupId);
            arrayOfUsersInGroups.get(indexOfGroupIdJson).add(userId);
        }

        for(int groupId: groupIdToIndex.keySet()){
            JSONObject groupJson = arrayOfGroupJsons.get(groupIdToIndex.get(groupId));
            ArrayList<Integer> arrayOfUsersForGroup = arrayOfUsersInGroups.get(groupIdToIndex.get(groupId));
            groupJson.put("users_id",arrayOfUsersForGroup);
            jsonArr.put(groupJson);
        }


        json.put("content",jsonArr);
        return json;
    }


    public static JSONObject groupMsgsDataToJson(ArrayList<HashMap<String,String>> data, int groupId) throws SQLException{
        //each group is a json that contains its propper array of msgs
        JSONObject json = new JSONObject();
        JSONArray arrayOfMsgs = new JSONArray();

        for(int i = 0; i < data.size(); i++){
            HashMap<String,String> row = data.get(i);
            if(row.get("group_id").equals(groupId + "")){
                JSONObject tmpObj = new JSONObject();
                tmpObj.put("group_id",row.get("group_id"));
                tmpObj.put("user_id",row.get("user_id"));
                tmpObj.put("content",row.get("content"));
                tmpObj.put("date",row.get("date"));

                arrayOfMsgs.put(tmpObj);
            }
        }

        json.put("group_id",groupId);
        json.put("msgs",arrayOfMsgs);

        return json;
    }

    public static JSONObject groupsMsgsDataToJson(ArrayList<HashMap<String,String>> data) throws SQLException{
        JSONObject json = new JSONObject();
        JSONArray arrayOfGroupsMsgs = new JSONArray();
        HashMap<Integer,Integer> groupIds = new HashMap<>();

        for(int i = 0; i < data.size();i++){
            HashMap<String,String> row = data.get(i);
            int groupId = Integer.parseInt(row.get("group_id"));
            if(groupIds.get(groupId) == null){
                System.out.println("prossesing groupId: " + groupId);
                System.out.println("groupId: " + groupId);
                groupIds.put(groupId,0);
                arrayOfGroupsMsgs.put(groupMsgsDataToJson(data,groupId));
            }
            i++;
        }

        json.put("groups_msgs", arrayOfGroupsMsgs);
        return json;
    }

    public static ArrayList<HashMap<String,String>> resultSetToArray(ResultSet rs) throws SQLException {//lines, then columns through name
        ArrayList<HashMap<String,String>> output = new ArrayList<>();// each index in the array list
        // represents a row, and each row has its columns

        ResultSetMetaData metaData = rs.getMetaData();
        int count = metaData.getColumnCount();

        while(rs.next()){
            HashMap<String,String> row = new HashMap<>();
            for(int i = 1; i <= count; i++){
                String colName = metaData.getColumnName(i);
                row.put(colName,rs.getString(colName));
            }
            output.add(row);
        }

        return output;
    }


}
