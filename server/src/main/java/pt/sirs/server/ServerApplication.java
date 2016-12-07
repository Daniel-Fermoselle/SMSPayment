package pt.sirs.server;

import java.io.*;
import java.net.*;
import pt.sirs.server.Server;

public class ServerApplication {
	
	public static final int SERVER_PORT = 10000;
	//Number of connects that the server will have on his queue
	public static final int QUEUE_SIZE = 10;
	public static final int TIME_WAITING = 40000;

	public static void main(String args[])
    {
        run(args[0]);
    }

	private static DatagramSocket serverSocket = null;
	private static byte[] receiveData = new byte[130];
	private static byte[] sendData = new byte[130];
    private static void run(String port) {
        try{  
        	Server server = getServerDatabase();
        	
            //1. creating a server socket
        	serverSocket = new DatagramSocket(Integer.parseInt(port));
            
            while(true){
            	
            	DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            	serverSocket.receive(receivePacket);
            	String msg = new String( receivePacket.getData());
            	InetAddress IPAddress = receivePacket.getAddress();
                int senderPort = receivePacket.getPort();
                //verify type of message
            	String[] splittedMsg = msg.split("\\|");
            	
            	if(splittedMsg.length < 3){
         		   System.out.println("Invalid message field");
         		   continue;
            	}
            	String typeOfMsg = splittedMsg[1];
                
                String feedback = "";
                switch (typeOfMsg) {
                    case "P":  server.savePforClient(splittedMsg[0], splittedMsg[2]);
                             break;
                    case "G":  server.saveGforClient(splittedMsg[0], splittedMsg[2]);
                             break;
                    case "NR": server.saveNPforClient(splittedMsg[0], splittedMsg[2], splittedMsg[3]);
                             break;
                    case "PV": server.savePVforClient(splittedMsg[0], splittedMsg[2]);
                    		   SendMsg(server.getNonRepudiationMsgForPublicValue(splittedMsg[0]), IPAddress, senderPort);
                    		   SendMsg(server.getPublicValueForClient(splittedMsg[0]), IPAddress, senderPort);
                             break;
                    case "L":  feedback = server.processLoginSms(splittedMsg[0], splittedMsg[2], splittedMsg[3]);
                    		   SendMsg(feedback, IPAddress, senderPort);
                             break;
                    case "T":  feedback = server.processTransactionSms(splittedMsg[0], splittedMsg[2], splittedMsg[3]);
         		   			   SendMsg(feedback, IPAddress, senderPort);
                             break;
                    case "O":  feedback = server.processLogoutSms(splittedMsg[0], splittedMsg[2], splittedMsg[3]);
		   			   		   SendMsg(feedback, IPAddress, senderPort);
                             break;
                    default:   
                    		   System.out.println("Invalid message field");
                    		   SendMsg(feedback, IPAddress, senderPort);
                             break;
                }
                System.out.println(feedback);
     /*
                
            	if(server.getStatus().equals(Server.SERVER_SUCCESSFUL_LOGOUT_MSG)  || server.getStatus().equals(Server.SERVER_LOST_CONNECTION_MSG)){
                    server.setStatus(Server.SERVER_BEGGINING);
                    
                    //2. Wait for connection
                    System.out.println("Waiting for connection");
                    connection = providerSocket.accept();
                    connection.setSoTimeout(TIME_WAITING);
                    System.out.println("Connection received from " + connection.getInetAddress().getHostName());
                    
                    //3. get Input and Output streams
                    out = new ObjectOutputStream(connection.getOutputStream());
                    out.flush();
                    in = new ObjectInputStream(connection.getInputStream());
            	}
	            while(!server.getStatus().equals(Server.SERVER_SUCCESSFUL_LOGIN_MSG) && !server.getStatus().equals(Server.SERVER_LOST_CONNECTION_MSG)){
	            	server = DiffieHellman(server, out, in);            
	            	server = Login(server, out, in);
	            }
	            
	            while(!server.getStatus().equals(Server.SERVER_SUCCESSFUL_LOGOUT_MSG) && !server.getStatus().equals(Server.SERVER_LOST_CONNECTION_MSG)){
	            	server = Transaction(server, out, in);
	            }*/
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally{
            //4: Closing connection
            
        }
    }
    
   /* public static Server DiffieHellman(Server server, ObjectOutputStream out, ObjectInputStream in) throws Exception{
    	try{
    		//Receive p and g values for DH
    		server.setP((String) in.readObject());
    		server.setG((String) in.readObject());
    		//Generate secret value
    		server.generateSecretValue();
    		//Generate server public value
    		server.generatePublicValue();

    		//Receive public value from client and generate sharedKey
            server.receiveNonRepudiationMsgForPublicValue((String) in.readObject());
    		server.generateSharedKey((String) in.readObject());

    		//Send public value to client
    		out.writeObject(server.getNonRepudiationMsgForPublicValue());
            out.flush();
    		out.writeObject(server.getPublicValue());
    		out.flush();
    	}catch (Exception e){
    		server.setStatus(Server.SERVER_LOST_CONNECTION_MSG); }
    	return server;
    }

    public static Server Login(Server server, ObjectOutputStream out, ObjectInputStream in) throws Exception{
    	try{
    		String sms = (String) in.readObject();
    		String feedback = server.processLoginSms(sms);
    		out.writeObject(feedback);
    		out.flush();
    	}catch (Exception e){
    		server.setStatus(Server.SERVER_LOST_CONNECTION_MSG); }
    	return server;
    }
	
	public static Server Transaction(Server server, ObjectOutputStream out, ObjectInputStream in) {
		try{
	        String transaction = (String) in.readObject();
	        String feedback = server.processTransactionSms(transaction);
			out.writeObject(feedback);
	        out.flush();
			
		}catch (Exception e){
			server.setStatus(Server.SERVER_LOST_CONNECTION_MSG); }
		return server;
	}*/
    
	private static Server getServerDatabase() throws Exception{
		Console console = System.console();
		if (console == null) {
			System.out.println("Couldn't get Console instance");
			System.exit(0);
		}
		console.printf("Insert your mysql id: ");
		String id = console.readLine();
		console.printf("Insert your mysql password: ");
    	char[] passwordChars = console.readPassword();
    	String passwordString = new String(passwordChars);
    	
    	return new Server(id, passwordString);
	}
	
	private static void SendMsg(String msg, InetAddress IPAddress, int port) throws Exception{
		sendData = msg.getBytes();
        DatagramPacket sendPacket =
        new DatagramPacket(sendData, sendData.length, IPAddress, port);
        serverSocket.send(sendPacket);
	}
}