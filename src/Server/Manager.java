package Server;

import java.net.Socket;

public class Manager implements Runnable{

    public void createClientHandler(Socket socket, boolean isOld,int user_id){
        new ClientHandler(socket,user_id);
    }



    public void run(){

    }

}
