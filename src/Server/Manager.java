package Server;

import Tools.*;
import org.json.JSONArray;
import org.json.JSONObject;


import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class Manager implements Runnable{
    public static Manager manager;
    public HashMap<Integer,HashMap<Integer,ClientHandler>> usersGroups = new HashMap<>();
    public ArrayList<JSONObject> msgsToTreat = new ArrayList<>();
    public Connection con;
    public PreparedStatement pt;
    public Manager(Connection con){
        this.con = con;
        Manager.manager = this;
    }

    public void createClientHandler(Socket socket,int user_id){
        new ClientHandler(socket,user_id);
    }

    public void addToTreatmentQueue(JSONObject msg){
        System.out.println("adding msg to manager treatment queue");
        synchronized (msgsToTreat){
            msgsToTreat.add(msg);
        }
    }


    public void run(){
        System.out.println("did the run function even start???");
        while(true){
            try{
                Thread.sleep(50);
            }catch(Exception e){
                System.out.println("manager thread interrupted while waiting");
            }

            synchronized (msgsToTreat){
                for(JSONObject msg: msgsToTreat){
                    System.out.println("hello? from manager run");
                    try {
                        System.out.println("from Manager: " + msg);
                        MsgTypes msgType = MsgTypes.valueOf(msg.getString("msgType"));
                        JSONObject response = new JSONObject();
                        boolean sendResponse = true;
                        switch(msgType){
                            case GET_ALL_GROUP_DATA -> response = getAllGroupData(msg);
                            case GET_ALL_GROUP_CHAT -> response = getAllGroupChats(msg);
                            case INVITE_TO_DM -> response = inviteToDm(msg);
                            case MAKE_GROUP -> response = makeGroup(msg);
                            case INVITE_TO_GROUP -> {}
                            case ACCEPT_GROUP_INVITE -> {}
                            case SEND_MSG -> { sendMessage(msg); sendResponse = false; }
                            case LOG_OUT -> { sendResponse = false; }
                            case ERROR -> {}

                        }

                        //System.out.println("response 2: " + response);
                        if(sendResponse){
                            if(!response.has("msgType")) response.put("msgType",msgType);
                            ClientHandler ch = ClientHandler.clientHandlers.get(msg.getInt("msgSource"));
                            if(ch != null) ch.addToQueue(response);
                        }

                    }catch(Exception e){
                        System.out.println("error treating a msg: " + e);
                    }
                }
                msgsToTreat.clear();
            }

        }
    }
    //so as to avoid clutter within the switch statement, each case will be written as its own function
    private JSONObject getAllGroupData(JSONObject msg){
        int msgSource = msg.getInt("msgSource");
        JSONObject response = new JSONObject();
        try{
            pt = con.prepareStatement(Statements.usersOfGroups);
            pt.setInt(1,msgSource);
            ResultSet rs = pt.executeQuery();

            response = DataToJson.groupsUsersDataToJson(DataToJson.resultSetToArray(rs));
        }catch(Exception e){
            System.out.println("error when getting group data");
            response.put("msgType",MsgTypes.ERROR);
        }

        return response;
    }

    private JSONObject getAllGroupChats(JSONObject msg){
        int msgDestination = msg.getInt("msgDestination");
        JSONObject response = new JSONObject();
        try{
            if(!isGroupMember(msgDestination,msg.getInt("msgSource"))) return error("conversation not found");
            pt = con.prepareStatement(Statements.groupsChats);
            pt.setInt(1,msgDestination);

            ResultSet rs = pt.executeQuery();

            response = DataToJson.groupMsgsDataToJson(DataToJson.resultSetToArray(rs),msgDestination);
            System.out.println("resultSet: " + rs);
            System.out.println("response: " + response);
        }catch(Exception e){
            System.out.println("error when getting group messages" + e);
        }

        return response;
    }

    private JSONObject makeGroup(JSONObject msg){
        int msgSource = msg.getInt("msgSource");
        JSONObject response = new JSONObject();

        try {
            pt = con.prepareStatement(Statements.getLastGroupId);
            ResultSet rs = pt.executeQuery();
            int groupId = (rs.next())?(rs.getInt("last_id") + 1):1;
            String groupName = msg.optString("group_name","").trim();
            if(groupName.isEmpty()) return error("group name is required");
            ArrayList<Integer> users = new ArrayList<>();
            users.add(msgSource);
            JSONArray userIds = msg.optJSONArray("users_id");
            if(userIds != null){
                for(int i = 0; i < userIds.length(); i++){
                    int userId = userIds.getInt(i);
                    if(!users.contains(userId) && userExists(userId)) users.add(userId);
                }
            }
            if(users.size() < 2) return error("group has no valid members");
            for(int userId: users) addGroupUser(groupId,userId,false,groupName);

            pt = con.prepareStatement(Statements.addGroupAdmin);
            pt.setInt(1,groupId);
            pt.setInt(2,msgSource);
            pt.executeUpdate();

            response.put("group_id",groupId);
            response.put("group_name",groupName);
        } catch (Exception e) {
            System.out.println("error creating new group!" + e);
            return error("failed database operation");
        }
        response.put("msg", "the group has been created");

        return response;
    }

    private JSONObject inviteToDm(JSONObject msg){
        int msgSource = msg.getInt("msgSource");
        int otherUser = msg.optInt("user_id",-1);
        try{
            if(otherUser < 0 || otherUser == msgSource || !userExists(otherUser)) return error("invalid direct-chat user ID");
            pt = con.prepareStatement(Statements.existingPrivateGroup);
            pt.setInt(1,msgSource);
            pt.setInt(2,otherUser);
            ResultSet rs = pt.executeQuery();
            int groupId;
            if(rs.next()){
                groupId = rs.getInt("group_id");
            }else{
                pt = con.prepareStatement(Statements.getLastGroupId);
                rs = pt.executeQuery();
                groupId = (rs.next())?(rs.getInt("last_id") + 1):1;
                addGroupUser(groupId,msgSource,true,null);
                addGroupUser(groupId,otherUser,true,null);
            }
            JSONObject response = new JSONObject();
            response.put("group_id",groupId);
            response.put("is_private",true);
            response.put("group_name",JSONObject.NULL);
            response.put("users_id",new JSONArray().put(msgSource).put(otherUser));
            return response;
        }catch(Exception e){
            System.out.println("error creating direct conversation: " + e);
            return error("failed database operation");
        }
    }

    private void sendMessage(JSONObject msg){
        int userId = msg.getInt("msgSource");
        int groupId = msg.getInt("msgDestination");
        String content = msg.optString("content","").trim();
        try{
            if(content.isEmpty() || !isGroupMember(groupId,userId)){
                ClientHandler ch = ClientHandler.clientHandlers.get(userId);
                if(ch != null) ch.addToQueue(error("invalid message data"));
                return;
            }
            pt = con.prepareStatement(Statements.insertMessage);
            pt.setInt(1,groupId);
            pt.setInt(2,userId);
            pt.setString(3,content);
            pt.executeUpdate();
            JSONObject broadcast = new JSONObject();
            broadcast.put("msgType",MsgTypes.SEND_MSG);
            broadcast.put("group_id",groupId);
            broadcast.put("user_id",userId);
            broadcast.put("content",content);
            broadcast.put("date",java.time.LocalDateTime.now().toString());
            pt = con.prepareStatement(Statements.getGroupUsers);
            pt.setInt(1,groupId);
            ResultSet rs = pt.executeQuery();
            while(rs.next()){
                ClientHandler ch = ClientHandler.clientHandlers.get(rs.getInt("user_id"));
                if(ch != null) ch.addToQueue(broadcast);
            }
        }catch(Exception e){
            System.out.println("error sending message: " + e);
            ClientHandler ch = ClientHandler.clientHandlers.get(userId);
            if(ch != null) ch.addToQueue(error("failed database operation"));
        }
    }

    private boolean userExists(int userId) throws SQLException{
        pt = con.prepareStatement(Statements.userExists);
        pt.setInt(1,userId);
        ResultSet rs = pt.executeQuery();
        return rs.next();
    }

    private boolean isGroupMember(int groupId,int userId) throws SQLException{
        pt = con.prepareStatement(Statements.isGroupMember);
        pt.setInt(1,groupId);
        pt.setInt(2,userId);
        ResultSet rs = pt.executeQuery();
        return rs.next();
    }

    private void addGroupUser(int groupId,int userId,boolean isPrivate,String groupName) throws SQLException{
        pt = con.prepareStatement(Statements.addGroupUser);
        pt.setInt(1,groupId);
        pt.setInt(2,userId);
        pt.setBoolean(3,isPrivate);
        pt.setString(4,groupName);
        pt.executeUpdate();
    }

    private JSONObject error(String content){
        JSONObject response = new JSONObject();
        response.put("msgType",MsgTypes.ERROR);
        response.put("content",content);
        return response;
    }


    //private JSONObject inviteToGroup(){

    //}


}
