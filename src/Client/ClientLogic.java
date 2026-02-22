package Client;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class ClientLogic {
    public Socket socket;
    public BufferedReader bufferedReader;
    public BufferedWriter bufferedWriter;


    public ClientLogic(){
        try{
            socket = new Socket("localhost",1234);
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            JSONObject msg = new JSONObject();
            msg.put("msgType",true);
            msg.put("user_id",3);
            msg.put("user_password","0001");


            bufferedWriter.write(msg.toString() + "\n");
            bufferedWriter.flush();

            System.out.println("auth msg was written");

            System.out.println((bufferedReader.readLine()));

            socket.close();

        }catch(Exception e){
            System.out.println("error in Client Logic: " + e);
        }
    }

    public static void main(String[] args){
        new ClientLogic();
    }

}
