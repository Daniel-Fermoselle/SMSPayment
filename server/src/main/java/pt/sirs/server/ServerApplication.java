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
            	String msg = new String(receivePacket.getData());
            	InetAddress IPAddress = receivePacket.getAddress();
                int senderPort = receivePacket.getPort();
                //verify type of message
            	String[] splittedMsg = msg.split("\\|");
            	
            	if(splittedMsg.length < 3){
         		   System.out.println("Invalid message field");
         		   continue;
            	}
            	String typeOfMsg = splittedMsg[1];
            	
            	for(int i = 0; i < splittedMsg.length; i++){
            		System.out.println(splittedMsg[i]);
            	}
                
                String feedback = "";
                switch (typeOfMsg) {
                    case "P":  server.savePforClient(splittedMsg[0], splittedMsg[2]);
                             break;
                    case "G":  server.saveGforClient(splittedMsg[0], splittedMsg[2]);
                             break;
                    case "NP": server.saveNPforClient(splittedMsg[0], splittedMsg[2], splittedMsg[3]);
                             break;
                    case "PV": server.savePVforClient(splittedMsg[0], splittedMsg[2]);
                    		   SendMsg(server.getNonRepudiationMsgForPublicValue(), IPAddress, senderPort);
                    		   SendMsg(server.getPublicValueForClient(), IPAddress, senderPort);
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
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    
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