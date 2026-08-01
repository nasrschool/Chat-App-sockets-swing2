package Server;

import Tools.MsgTypes;
import Tools.Statements;
import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LoginAuthenticator implements Runnable{
    private Socket socket;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;
    private Connection con;
    private PreparedStatement pt;
    private Manager manager;

    public LoginAuthenticator(Socket socket,Connection con,Manager manager){
        this.socket = socket;
        this.con = con;
        this.manager = manager;
        try{
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        }catch(IOException e){
            System.out.println("error constructing ClientHandler: " + e);
        }
    }

    public void sendMessage(String msg) throws Exception{
        bufferedWriter.write(msg);
        bufferedWriter.write("\n");
        bufferedWriter.flush();
    }

    public void run(){
        JSONObject authenticationMsg;
        ResultSet rs;
        int user_id;
        boolean msgType;
        String password;
        JSONObject errorMsg;
        boolean socketIsConnected = true;

        System.out.println("entered login authenticator");
        while(socketIsConnected){
            try{
                System.out.println("waiting for auth msg");
                String tmp = bufferedReader.readLine();
                System.out.println("received an auth msg");
                authenticationMsg = new JSONObject(tmp);

                System.out.println("authentication msg: " + authenticationMsg);
                msgType = (boolean) authenticationMsg.get("msgType");
                if(!msgType){//msgType == false => sign up
                    pt = con.prepareStatement(Statements.getLastUserId);
                    rs = pt.executeQuery();

                    user_id = (rs.next())?(rs.getInt("last_id") + 1):0;
                    rs.close();

                    pt = con.prepareStatement(Statements.newUser);
                    pt.setInt(1,user_id);
                    pt.setString(2,authenticationMsg.getString("user_password"));
                    pt.executeUpdate();

                }else{//msgType == true => login
                    user_id = authenticationMsg.getInt("user_id");
                    password = authenticationMsg.getString("user_password");
                    errorMsg = new JSONObject();
                    pt = con.prepareStatement(Statements.selectUser);
                    pt.setInt(1, user_id);
                    rs = pt.executeQuery();
                    if(!rs.next()){
                        errorMsg.put("msgType", MsgTypes.ERROR);
                        errorMsg.put("content","user_Id not found");
                        sendMessage(errorMsg.toString());
                        continue;
                       }

                    if(!rs.getString("user_password").equals(password)){
                        errorMsg.put("msgType",MsgTypes.ERROR);
                        errorMsg.put("content","password incorrect");
                        sendMessage(errorMsg.toString());
                        continue;
                    }

                    if(ClientHandler.clientHandlers.get(user_id) != null){
                        errorMsg.put("msgType",MsgTypes.ERROR);
                        errorMsg.put("content","already logged from another device");
                        sendMessage(errorMsg.toString());
                        continue;
                    }
                }

                System.out.println("a user has logged in!");
                sendMessage("{'msg':'client handler is about to be created!'}");

                manager.createClientHandler(socket,user_id);
                break;// get out of the loop and the run function

            }catch(NullPointerException e){
                System.out.println("user has left the login process!");
                socketIsConnected = false;
            }
            catch(Exception e){
                System.out.println("error in the authenticator: " + e);
            }
        }

    }

}
