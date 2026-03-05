package Server;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.*;

public class ClientHandler{
    public static HashMap<Integer,ClientHandler> clientHandlers = new HashMap<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private Manager manager;
    private ArrayList<JSONObject> dataToSend;
    private int userId;
    private HashMap<String, List<Integer>> groupsUsers = new HashMap<>();
    private HashMap<String,ArrayList<JSONObject>> groupsChats = new HashMap<>();

    public ClientHandler(Socket socket,int userId,Manager manager){
        clientHandlers.put(userId,this);
        this.socket = socket;
        this.userId = userId;
        this.manager = manager;
        try{
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        }catch(IOException e){
            System.out.println("error constructing ClientHandler: " + e);
        }


    }
    //these first 2 will be run in threads
    public void writeDataToClient(){

    }

    public void readDataFromClient(){
        while(true){
            try{
                JSONObject msg = new JSONObject(bufferedReader.readLine());
                manager.addToTreatmentQueue(msg);

            }catch(Exception e){
                System.out.println("error reading data from user " + this.userId + " :" + e);
            }
        }
    }

}
