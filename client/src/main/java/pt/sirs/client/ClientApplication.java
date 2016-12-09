package pt.sirs.client;

import java.io.*;
import java.net.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pt.sirs.client.Client;

public class ClientApplication {
	
	private static DatagramSocket clientSocket = null;
	private static InetAddress IPAddress = null;
	private static int port;
	private static byte[] sendData = new byte[1024];
	private static byte[] receiveData = new byte[1024];
	
	public ClientApplication() {	}
	
	public static void main(String[] args) {
		ClientApplication ca = new ClientApplication();
		ca.run(args[0], args[1]);

	}
	
	public void run(String stringPort, String host) {
		Client client = null;
		String feedback = Client.ERROR_MSG;
	    
		try{
			Console console = System.console();
			if (console == null) {
				System.out.println("Couldn't get Console instance");
				System.exit(0);
			}
			
			//1. Criar o socket para falar com o server
			clientSocket = new DatagramSocket();
		    IPAddress = InetAddress.getByName(host);
		    port = Integer.parseInt(stringPort);
		    System.out.println("Connected to localhost in port " + port);
	        
	        System.out.println("Started...");
	        while(true){
		        while(!feedback.equals(Client.SERVER_SUCCESSFUL_LOGIN_MSG)){
		        	String mobile 	= readMobile(console, "Please enter your mobilenumber: ");	
		        	String username = readUsername(console, "Please enter your username: ");	
					String passwordString = readPassword(console, "Please enter your password: ");
					
					client = new Client(username, passwordString, mobile);

			    	client = DiffieHellman(client);
			    	
			    	if(client.getStatus().equals(Client.SERVER_SUCCESSFUL_LOGOUT_MSG) || client.getSharedKey() == null){
			    		return;
			    	}
			    	client = Login(client);
			    	feedback = client.getStatus();
		        }
		    	
	            while(!client.getStatus().equals(Client.SERVER_SUCCESSFUL_LOGOUT_MSG)){
		    		System.out.println("Choose one of the following options");
		    		System.out.println("1 - Transaction");
		    		System.out.println("2 - Logout");
		    		
		    		String choice = console.readLine();

		    		if(choice.equals("1")){
		    			client = Transaction(client, console);
		    		}
		    		else if(choice.equals("2")){
		    			client = Logout(client);
		    			feedback = Client.SERVER_SUCCESSFUL_LOGOUT_MSG;
		    			return;
		    		}
		    		if(client.getStatus().equals(Client.SERVER_SUCCESSFUL_LOGOUT_MSG)){
		    			return;
		    		}
		    	}
	        }
		}
		
		catch(UnknownHostException e){
            System.err.println("Attempt to connect an unknown server.");
        }
		catch(FileNotFoundException e){
			System.err.println("Username not registered in this phone. Run the application again.");
		}
        catch(Exception e){
            e.printStackTrace();
        }
		
    	finally{
            if(clientSocket != null){
			    clientSocket.close();
			}
        }
    }
	
	public static Client DiffieHellman(Client client) throws Exception{
		
		//Sharing values p and g
		sendMsg(client.generateValueSharingSMS("p"));
		sendMsg(client.generateValueSharingSMS("g"));

        //Generate secret value
        client.generateSecretValue();

        //Generate client public value
        sendMsg(client.getNonRepudiationMsgForPublicValue());
        sendMsg(client.generatePublicValue());

        //Generate sharedKey
        client.receiveNonRepudiationMsgForPublicValue(receiveMsg());
        client.generateSharedKey(receiveMsg());
		
		return client;
	}
	
	public static Client Login(Client client) throws Exception{
		
        sendMsg(client.generateLoginSms());
        
        String feedback = receiveMsg();
        System.out.println(client.processFeedback(feedback, "login"));
        
		return client;
	}
	
	public static Client Transaction(Client client, Console console) throws Exception{
		
		String iban, amount;
		
    	iban = readUsername(console, "Please enter the username to transfer: ");
    	
    	amount = readAmount(console, "Please enter an amount to transfer: ");
    	
    	String transaction = client.generateTransactionSms(iban, amount);
		sendMsg(transaction);
        
        String feedback = receiveMsg();
        String feedbackProcessed = client.processFeedback(feedback, "transaction");
        System.out.println(feedbackProcessed);
        
        if(feedbackProcessed.equals(Client.FRESHNESS_ERROR_MSG)){
        	client.setStatus(Client.SERVER_SUCCESSFUL_LOGOUT_MSG);
        }
		
		return client;
	}
	
	public static Client Logout(Client client) throws Exception{
		
        sendMsg(client.generateLogoutSms());
        
        String feedback = receiveMsg();
        System.out.println(client.processFeedback(feedback, "logout"));
		
		return client;
	}
	
	public static String readMobile(Console console, String msg) throws Exception{
		console.printf(msg);
		String mobile = console.readLine();
		Pattern p = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
    	Matcher m = p.matcher(mobile);
    	boolean b = m.find();
		while(mobile.length() != 9 || b){
			System.out.println("The inserted mobilenumber has to be 9 digits long, no letters or special char alowed. Try again!");
			console.printf(msg);
			mobile = console.readLine();
			m = p.matcher(mobile);
	    	b = m.find();
		}
		return mobile;
	}
	
	public static String readUsername(Console console, String msg) throws Exception{
		console.printf(msg);
		String username = console.readLine();
		Pattern p = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
    	Matcher m = p.matcher(username);
    	boolean b = m.find();
		while(username.length() > 10 || b){
			System.out.println("The inserted username is too big, usernames only have at most 10 characters, no special char alowed. Try again!");
			console.printf(msg);
			username = console.readLine();
			m = p.matcher(username);
	    	b = m.find();
		}
		return username;
	}
	
	public static String readPassword(Console console, String msg) throws Exception{
		console.printf(msg);
    	char[] passwordChars = console.readPassword();
    	String passwordString = new String(passwordChars);
    	Pattern p = Pattern.compile("[^0-9 ]", Pattern.CASE_INSENSITIVE);
    	Matcher m = p.matcher(passwordString);
    	boolean b = m.find();
		while(passwordString.length() > 7 || passwordString.length() < 4 || b){
			System.out.println("The inserted password is incorrect, passwords only have at most 8 and at least 4 digits, no letters or special char alowed. Try again!");
			console.printf(msg);
			passwordChars = console.readPassword();
	    	passwordString = new String(passwordChars);
	    	m = p.matcher(passwordString);
	    	b = m.find();
		}
		return passwordString;
	}
	
	public static String readAmount(Console console, String msg) throws Exception{
		console.printf(msg);
		String amount = console.readLine();
		Pattern p = Pattern.compile("[^0-9 ]", Pattern.CASE_INSENSITIVE);
    	Matcher m = p.matcher(amount);
    	boolean b = m.find();
		while(amount.length() > 10 || b){
			System.out.println("The inserted amount is too big, you can oly transfer up to 99.999.999, no letters or special char alowed. Try again!");
			console.printf(msg);
			amount = console.readLine();
			m = p.matcher(amount);
	    	b = m.find();
		}
		return amount;
	}
	
	  
    public static void sendMsg(String msg) throws IOException{
    	sendData = msg.getBytes();
	    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
	    clientSocket.send(sendPacket);
    }
    
    public static String receiveMsg() throws IOException{
    	DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
	    clientSocket.receive(receivePacket);
	    return new String(receivePacket.getData());
    }
}
