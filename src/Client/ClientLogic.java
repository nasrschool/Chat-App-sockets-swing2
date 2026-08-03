package Client;

import org.json.JSONObject;
import javax.swing.SwingUtilities;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientLogic {
    public static Socket socket;
    public static BufferedReader bufferedReader;
    public static BufferedWriter bufferedWriter;
    public static ArrayList<JSONObject> msgQuery = new ArrayList<>();
    public static ChatFrame chatFrame;
    public static int userId;
    public static boolean running;

    public static JSONObject login(int loginUserId,String password) throws Exception{
        close();
        socket = new Socket(System.getenv().getOrDefault("CHAT_SERVER_HOST","localhost"),1234);
        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        JSONObject msg = new JSONObject();
        msg.put("msgType",true); msg.put("user_id",loginUserId); msg.put("user_password",password);
        bufferedWriter.write(msg.toString()); bufferedWriter.write("\n"); bufferedWriter.flush();
        String line = bufferedReader.readLine();
        if(line == null) throw new IOException("server disconnected");
        JSONObject response = new JSONObject(line);
        if(!"ERROR".equals(response.optString("msgType"))){
            userId = loginUserId; running = true;
            (new Thread(ClientLogic::treatMsgQuery)).start();
            (new Thread(ClientLogic::treatIncMsg)).start();
        }
        return response;
    }

    public static void send(JSONObject msg){ synchronized (msgQuery){ msgQuery.add(msg); } }

    public static void treatMsgQuery(){
        while(running){
            try{
                synchronized (msgQuery){
                    for(JSONObject msg: msgQuery){ bufferedWriter.write(msg.toString()); bufferedWriter.write("\n"); bufferedWriter.flush(); }
                    msgQuery.clear();
                }
                Thread.sleep(25);
            }catch(Exception e){ if(running) System.out.println("error treating query msg in client: " + e); close(); }
        }
    }

    public static void treatIncMsg(){
        while(running){
            try{
                String line = bufferedReader.readLine();
                if(line == null){ close(); break; }
                JSONObject msg = new JSONObject(line);
                if(chatFrame != null) SwingUtilities.invokeLater(() -> chatFrame.receiveMessage(msg));
            }catch(Exception e){ if(running) System.out.println("error treating an inc message: " + e); close(); }
        }
    }

    public static void close(){
        running = false; chatFrame = null;
        synchronized (msgQuery){ msgQuery.clear(); }
        try{ if(socket != null) socket.close(); }catch(Exception ignored){}
    }
}
