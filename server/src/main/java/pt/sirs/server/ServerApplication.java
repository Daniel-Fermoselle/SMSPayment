package pt.sirs.server;

import java.io.*;
import java.net.*;
import pt.sirs.server.Server;

public class ServerApplication {
	
	public static final int SERVER_PORT = 10000;
	//Number of connects that the server will have on his queue
	public static final int QUEUE_SIZE = 10;

	public static void main(String args[])
    {
        run();
    }
	
    private static void run() {
    	ServerSocket providerSocket = null;
    	ObjectOutputStream out = null;
    	ObjectInputStream in = null;
        try{
            //1. creating a server socket
            providerSocket = new ServerSocket(SERVER_PORT, QUEUE_SIZE);
            //2. Wait for connection
            System.out.println("Waiting for connection");
            Socket connection = providerSocket.accept();
            System.out.println("Connection received from " + connection.getInetAddress().getHostName());
            //3. get Input and Output streams
            out = new ObjectOutputStream(connection.getOutputStream());
            out.flush();
            in = new ObjectInputStream(connection.getInputStream());
            Server server = new Server();
            while(true){
	        	String sms = (String) in.readObject();
	        	String feedback = server.processSms(sms);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
            //4: Closing connection
            try{
                in.close();
                out.close();
                providerSocket.close();
            }
            catch(IOException ioException){
                ioException.printStackTrace();
            }
        }
    }

}