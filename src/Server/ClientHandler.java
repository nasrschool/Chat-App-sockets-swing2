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
    private int userId;
    private HashMap<String, List<Integer>> groupsUsers = new HashMap<>();
    private HashMap<String,ArrayList<JSONObject>> groupsChats = new HashMap<>();
    private ArrayList<JSONObject> msgsToSend = new ArrayList<>();
    private boolean connected = true;

    public ClientHandler(Socket socket,int userId){
        synchronized (clientHandlers){ clientHandlers.put(userId,this); }
        this.socket = socket;
        this.userId = userId;
        this.manager = Manager.manager;
        try{
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        }catch(IOException e){
            System.out.println("error constructing ClientHandler: " + e);
        }

        (new Thread(this::writeDataToClient)).start();
        (new Thread(this::readDataFromClient)).start();

    }
    //these first 2 will be run in threads
    public void writeDataToClient(){
        while(connected){
            try{
                synchronized (msgsToSend){
                    for(JSONObject msg: msgsToSend){
                        bufferedWriter.write(msg.toString());
                        bufferedWriter.write("\n");
                        bufferedWriter.flush();
                        System.out.println("sending msg from client Hanlder " + userId + ": " + msg);
                    }
                    msgsToSend.clear();
                }
                Thread.sleep(25);
            }catch(Exception e){
                if(connected) System.out.println("error within the write function of user " + userId + ": " + e);
                disconnect();
            }
        }
    }

    public void readDataFromClient(){
        while(connected){
            try{
                String line = bufferedReader.readLine();
                if(line == null){ disconnect(); break; }
                JSONObject msg = new JSONObject(line);
                System.out.println("from client Handler: " + msg);
                manager.addToTreatmentQueue(msg);

            }catch(Exception e){
                if(connected) System.out.println("error reading data from user " + this.userId + " :" + e);
                disconnect();
            }
        }
    }

    public void addToQueue(JSONObject msg){
        System.out.println("added to ch queue: " + msg);
        synchronized (msgsToSend){ msgsToSend.add(msg); }
    }

    public void disconnect(){
        if(!connected) return;
        connected = false;
        synchronized (clientHandlers){
            if(clientHandlers.get(userId) == this) clientHandlers.remove(userId);
        }
        try{ socket.close(); }catch(Exception ignored){}
    }

}
