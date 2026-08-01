package Client;

import Tools.MsgTypes;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;

public class ClientLogic {
    public static Socket socket;
    public static BufferedReader bufferedReader;
    public static BufferedWriter bufferedWriter;
    public static ArrayList<JSONObject> msgQuery = new ArrayList<>();


    public static void initialiseClientLogic(){
        try{
            socket = new Socket("localhost",1234);
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            (new Thread(ClientLogic::treatMsgQuery)).start();
            (new Thread(ClientLogic::treatIncMsg)).start();


            JSONObject msg = new JSONObject();
            msg.put("msgType",true);
            msg.put("user_id",3);
            msg.put("user_password","0000");

            msgQuery.add(msg);
            System.out.println("from initial: " + msgQuery);
            Thread.sleep(3000);
            msg = new JSONObject();
            msg.put("msgSource",3);
            msg.put("msgType", MsgTypes.MAKE_GROUP);
            //msg.put("msgDestination",2);
            msgQuery.add(msg);

            //socket.close();

        }catch(Exception e){
            System.out.println("error in Client Logic: " + e);
        }
    }


    public static void treatMsgQuery(){
        while(true){
            try{
                //System.out.println("sup");
                synchronized (msgQuery){
                    for(JSONObject msg: msgQuery) {

                        bufferedWriter.write(msgQuery.getFirst().toString());
                        bufferedWriter.write("\n");
                        bufferedWriter.flush();

                        System.out.println("sent msg: " + msg);
                    }
                    msgQuery.clear();
                }

            }catch(Exception e){
                System.out.println("error treating query msg in client: " + e);
            }
        }

    }

    public static void treatIncMsg(){
        while(true){
            try {
                if(!socket.isClosed()){
                    JSONObject msg = new JSONObject(bufferedReader.readLine());

                    System.out.println("received msg: " + msg);
                }
            }catch(Exception e){
                System.out.println("error treating an inc message: " + e);
            }
        }
    }


    public static void main(String[] args){
        ClientLogic.initialiseClientLogic();
    }

}
