package Server;

import Tools.*;
import org.json.JSONObject;


import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;

public class Manager implements Runnable{
    public HashMap<Integer,HashMap<Integer,ClientHandler>> usersGroups = new HashMap<>();
    public ArrayList<JSONObject> dataToTreat = new ArrayList<>();
    public Connection con;
    public PreparedStatement pt;
    public Manager(Connection con){
        this.con = con;
    }

    public void createClientHandler(Socket socket, Manager manager,int user_id){
        new ClientHandler(socket,user_id,manager);
    }

    public void addToTreatmentQueue(JSONObject msg){
        dataToTreat.add(msg);
    }

    public void groupsUsersDataToJson(int user_id){

    }


    public void run(){
        while(true){
            for (JSONObject msg : dataToTreat) {
                try {
                    //if(msg.getInt("msgType") == MsgTypes.GET_ALL_DATA) {
                    //    pt = con.prepareStatement(Statements.groupsUserIsIn);

                    //}
                }catch(Exception e){
                    System.out.println("error treating a msg: " + e);
                }
            }
        }
    }

}
