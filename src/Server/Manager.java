package Server;

import Tools.*;
import org.json.JSONObject;


import javax.xml.crypto.Data;
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
        msgsToTreat.add(msg);
    }


    public void run(){
        System.out.println("did the run function even start???");
        while(true){
            try{
                System.out.println("dataToTreat size: " + msgsToTreat.size());
                Thread.sleep(3000);
            }catch(Exception e){
                System.out.println("manager thread interrupted while waiting");
            }

            synchronized (msgsToTreat){
                for(JSONObject msg: msgsToTreat){
                    System.out.println("hello? from manager run");
                    try {
                        System.out.println("from Manager: " + msg);
                        Thread.sleep(1000);
                        MsgTypes msgType = MsgTypes.valueOf(msg.getString("msgType"));
                        JSONObject response = new JSONObject();
                        switch(msgType){
                            case GET_ALL_GROUP_DATA -> response = getAllGroupData(msg);
                            case GET_ALL_GROUP_CHAT -> response = getAllGroupChats(msg);
                            //case INVITE_TO_DM -> {}
                            case MAKE_GROUP -> {response = makeGroup(msg);}
                            case INVITE_TO_GROUP -> {}
                            case ACCEPT_GROUP_INVITE -> {}
                            case SEND_MSG -> {}
                            case LOG_OUT -> {}
                            case ERROR -> {}

                        }

                        //System.out.println("response 2: " + response);
                        response.put("msgType",msgType);

                        ClientHandler ch = ClientHandler.clientHandlers.get(msg.getInt("msgSource"));
                        ch.addToQueue(response);

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
        int msgSource = msg.getInt("msgSource");
        int msgDestination = msg.getInt("msgDestination");
        JSONObject response = new JSONObject();
        try{
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
            rs.next();

            int GroupId = (rs.next())?(rs.getInt("last_id") + 1):0;

            pt = con.prepareStatement(Statements.addGroupUser);
            pt.setInt(1,GroupId);
            pt.setInt(2,msgSource);
            pt.setInt(3,0);
            pt.executeUpdate();

            pt = con.prepareStatement(Statements.addGroupAdmin);
            pt.setInt(1,GroupId);
            pt.setInt(2,msgSource);
            pt.executeUpdate();

        } catch (SQLException e) {
            System.out.println("error creating new group!" + e);
        }
        response.put("msg", "the group has been created");

        return response;
    }


    //private JSONObject inviteToGroup(){

    //}


}
