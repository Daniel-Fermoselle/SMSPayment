package pt.sirs.server;

import java.io.*;
import java.net.*;
import java.security.KeyPair;

import pt.sirs.crypto.Crypto;
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
	            while(!server.getStatus().equals(Server.SERVER_SUCCESSFUL_LOGIN_MSG)){
	            	server = DiffieHellman(server, out, in);            
	            	server = Login(server, out, in);
	            }
	            
	            while(server.getStatus().equals(Server.SERVER_SUCCESSFUL_LOGIN_MSG)){
	            	server = Transaction(server, out, in);
	            }
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
    
	public static Server DiffieHellman(Server server, ObjectOutputStream out, ObjectInputStream in) throws Exception{
		
		//Receive p and g values for DH
        server.setP((String) in.readObject());
        server.setG((String) in.readObject());
        //Generate secret value
        server.generateSecretValue();
        //Generate server public value
        server.generatePublicValue();
        
        //Receive public value from client and generate sharedKey
        server.generateSharedKey((String) in.readObject());
        
        //Send public value to client
        out.writeObject(server.getPublicValue());
        out.flush();
		
		return server;
	}
	
	public static Server Login(Server server, ObjectOutputStream out, ObjectInputStream in) throws Exception{
		
        String sms = (String) in.readObject();
    	System.out.println(sms + " TAMANHO: " + sms.length());
        String feedback = server.processLoginSms(sms);
        System.out.println(feedback + " TAMANHO: " + feedback.length());
		out.writeObject(feedback);
        out.flush();
        
		return server;
	}
	
	public static Server Transaction(Server server, ObjectOutputStream out, ObjectInputStream in) throws Exception{
		
        String transaction = (String) in.readObject();
    	System.out.println(transaction + " TAMANHO: " + transaction.length());
        String feedback = server.processTransactionSms(transaction);
        System.out.println(feedback + " TAMANHO: " + feedback.length());
		out.writeObject(feedback);
        out.flush();
		
		return server;
	}
    
}