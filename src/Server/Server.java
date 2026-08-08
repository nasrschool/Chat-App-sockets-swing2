package Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class Server {
    public ServerSocket serverSocket;
    public Connection con;
    public String url = System.getenv().getOrDefault(
            "CHAT_DB_URL", "jdbc:postgresql://localhost:5432/chat_app_server_side");
    public String uName = System.getenv().getOrDefault("CHAT_DB_USER", "chatuser");
    public String password = System.getenv().getOrDefault("CHAT_DB_PASSWORD", "chatpass");
    public PreparedStatement st;
    public Manager manager;


    public Server(){
        try{
            serverSocket = new ServerSocket(1234);
            con = DriverManager.getConnection(url, uName, password);
            manager = new Manager(con);
            (new Thread(manager)).start();
            System.out.println("server is on!");

            while (true) {
                Thread thread = new Thread(new LoginAuthenticator(serverSocket.accept(),con,manager));
                thread.start();
                System.out.println("thread has started!");
            }
        }catch(Exception e){
            System.out.println("error constructing a server: " + e);
        }
    }

    public static void main(String[] args){
        new Server();
    }

}
