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

    public ClientHandler(Socket socket,int userId){
        clientHandlers.put(userId,this);
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
        while(true){
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
            }catch(Exception e){
                System.out.println("error within the write function of user " + userId + ": " + e);
            }
        }
    }

    public void readDataFromClient(){
        while(true){
            try{
                JSONObject msg = new JSONObject(bufferedReader.readLine());
                System.out.println("from client Handler: " + msg);
                manager.addToTreatmentQueue(msg);

            }catch(Exception e){
                System.out.println("error reading data from user " + this.userId + " :" + e);
            }
        }
    }

    public void addToQueue(JSONObject msg){
        System.out.println("added to ch queue: " + msg);
        msgsToSend.add(msg);
    }

}
