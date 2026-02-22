package Server;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;

import org.json.*;

public class ClientHandler{
    public static HashMap<Integer,ClientHandler> clientHandlers = new HashMap<>();
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private JSONObject[] dataToSend;
    private String UserId;
    private HashMap<String, List<Integer>> groupsUsers = new HashMap<>();
    private HashMap<String,JSONObject[]> groupsChats = new HashMap<>();

    public ClientHandler(Socket socket,int user_id){
        clientHandlers.put(user_id,this);
        this.socket = socket;
        try{
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        }catch(IOException e){
            System.out.println("error constructing ClientHandler: " + e);
        }
    }
    //these first 2 will be run in threads
    public void writeDataToClient(){}

    public void readDataFromClient(){}

    public void writeDataToManager(){}

    public void readDataFromManager(){}
}
