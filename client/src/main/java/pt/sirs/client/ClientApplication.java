package pt.sirs.client;

import java.io.*;
import java.net.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pt.sirs.client.Client;

public class ClientApplication {
	
	public static void main(String[] args) {
		Socket requestSocket = null;
		ObjectOutputStream out = null;
		ObjectInputStream in = null;
		Client client = null;
		String feedback = Client.ERROR_MSG;
		try{
			Console console = System.console();
			if (console == null) {
				System.out.println("Couldn't get Console instance");
				System.exit(0);
			}
						
			
			//1. Criar o socket para falar com o server
			requestSocket = new Socket("localhost", 10000);
	        System.out.println("Connected to localhost in port 10000");//Just debugging prints
	        
	        //2. Criar o socket para enviar coisas para o server
	        out = new ObjectOutputStream(requestSocket.getOutputStream());
	        out.flush();
            in = new ObjectInputStream(requestSocket.getInputStream());
	   
	        System.out.println("Started...");//Just debugging prints	
	        while(true){
		        //TODO Make Deffie Hellman happen once
		        while(!feedback.equals(Client.SERVER_SUCCESSFUL_LOGIN_MSG)){
		        	String mobile 	= readMobile(console, "Please enter your mobilenumber: ");	
		        	String username = readUsername(console, "Please enter your username: ");	
					String passwordString = readPassword(console, "Please enter your password: ");
					
					client = new Client(username, passwordString, mobile);

			    	client = DiffieHellman(client, out, in);	
			    	client = Login(client, out, in);
			    	feedback = client.getStatus();
		        }
		    	
	            while(!client.getStatus().equals(Client.SERVER_SUCCESSFUL_LOGOUT_MSG)){
		    		System.out.println("Choose one of the following options");
		    		System.out.println("1 - Transaction");
		    		System.out.println("2 - Logout");
		    		String choice = console.readLine();
		    		if(choice.equals("1")){
		    			client = Transaction(client, out, in, console);
		    		}
		    		else if(choice.equals("2")){
		    			client = Logout(client, out, in);
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
            //4: Closing connection
            try{
            	if(in != null && out != null && requestSocket != null){
	            	in.close();
	                out.close();
	                requestSocket.close();
            	}
            }
            catch(IOException ioException){
                ioException.printStackTrace();
            }
        }
    }
	
	public static Client DiffieHellman(Client client, ObjectOutputStream out, ObjectInputStream in) throws Exception{
		
		//Sharing values p and g
    	out.writeObject(client.generateValueSharingSMS("p"));
        out.flush();
    	out.writeObject(client.generateValueSharingSMS("g"));
        out.flush();
        //Generate secret value
        client.generateSecretValue();
        
        //Generate client public value
        out.writeObject(client.getNonRepudiationMsgForPublicValue());
        out.flush();
        out.writeObject(client.generatePublicValue());
        out.flush();
 
        //Generate sharedKey
        client.receiveNonRepudiationMsgForPublicValue((String) in.readObject());
        client.generateSharedKey((String) in.readObject());
		
		return client;
	}
	
	public static Client Login(Client client, ObjectOutputStream out, ObjectInputStream in) throws Exception{
		
    	String login = client.generateLoginSms();
    	System.out.println(login + " TAMANHO: " + login.length());
		out.writeObject(login);
        out.flush();
        
        String feedback = (String) in.readObject();
        System.out.println(feedback + " TAMANHO: " + feedback.length());
        System.out.println(client.processFeedback(feedback, "login"));
        
		return client;
	}
	
	public static Client Transaction(Client client, ObjectOutputStream out, ObjectInputStream in, Console console) throws Exception{
		
		String iban, amount;
		
    	iban = readUsername(console, "Please enter the username to transfer: ");
    	
    	amount = readAmount(console, "Please enter an amount to transfer: ");
    	
    	String transaction = client.generateTransactionSms(iban, amount);
    	System.out.println(transaction + " TAMANHO: " + transaction.length());
		out.writeObject(transaction);
        out.flush();
        
        String feedback = (String) in.readObject();
        System.out.println(feedback + " TAMANHO: " + feedback.length());
        String feedbackProcessed = client.processFeedback(feedback, "transaction");
        System.out.println(feedbackProcessed);
        
        if(feedbackProcessed.equals(Client.FRESHNESS_ERROR_MSG)){
        	client.setStatus(Client.SERVER_SUCCESSFUL_LOGOUT_MSG);
        }
		
		return client;
	}
	
	public static Client Logout(Client client, ObjectOutputStream out, ObjectInputStream in) throws Exception{
		
    	String logout = client.generateLogoutSms();
    	System.out.println(logout + " TAMANHO: " + logout.length());
		out.writeObject(logout);
        out.flush();
        
        String feedback = (String) in.readObject();
        System.out.println(feedback + " TAMANHO: " + feedback.length());
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
			System.out.println("The inserted mobilenumber has to be 9 digits long or has special char. Try again!");
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
			System.out.println("The inserted username is too big, usernames only have at most 10 characters or has special char. Try again!");
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
    	Pattern p = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
    	Matcher m = p.matcher(passwordString);
    	boolean b = m.find();
		while(passwordString.length() > 8 || passwordString.length() < 4 || b){
			System.out.println("The inserted password is incorrect, passwords only have at most 8 and at least 4 characters or has special char. Try again!");
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
			System.out.println("The inserted amount is too big, you can oly transfer up to 99.999.999 or has letters / special char. Try again!");
			console.printf(msg);
			amount = console.readLine();
			m = p.matcher(amount);
	    	b = m.find();
		}
		return amount;
	}
    
}
