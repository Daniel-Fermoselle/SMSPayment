package pt.sirs.client;

import java.io.*;
import java.net.*;
import pt.sirs.client.Client;
import pt.sirs.crypto.Crypto;

public class ClientApplication {
	
	public static final String IBAN = "PT01234567890123456789012";
	public static final int INITMONEY = 0;
	
	public static void main(String[] args) {
		Socket requestSocket = null;//CUIDADO
		ObjectOutputStream out = null;//CUIDADO
		ObjectInputStream in = null;
		Client client = null;
		String feedback = Client.FAILED_FEEDBACK;
		try{
			
			//1. Criar o socket para falar com o server
			requestSocket = new Socket("localhost", 10000);
	        System.out.println("Connected to localhost in port 10000");//Just debugging prints
	        
	        //2. Criar o socket para enviar coisas para o server
	        out = new ObjectOutputStream(requestSocket.getOutputStream());
	        out.flush();
            in = new ObjectInputStream(requestSocket.getInputStream());
	   
	        Console console = System.console();
	        if (console == null) {
	            System.out.println("Couldn't get Console instance");
	            System.exit(0);
	        }
	        System.out.println("Started...");//Just debugging prints	
	        while(true){
		        //TODO Make Deffie Hellman happen once
		        while(!feedback.equals(Client.SUCCESS_FEEDBACK)){
		        	console.printf("Please enter your mobile: ");
			    	String mobile = console.readLine();	    
			    	console.printf("Please enter your username: ");
			    	String username = console.readLine();	    	
			    	console.printf("Please enter your password: ");
			    	char[] passwordChars = console.readPassword();
			    	String passwordString = new String(passwordChars);
			    	
			    	client = new Client(username, passwordString, mobile);
			    	
			    	client = DiffieHellman(client, out, in);	
			    	client = Login(client, out, in);
			    	feedback = client.getStatus();
		        }
		    	
		    	while(client.getStatus().equals(Client.SUCCESS_FEEDBACK)){
		    		System.out.println("Choose one of the following options");
		    		System.out.println("1 - Transaction");
		    		System.out.println("2 - Logout");
		    		String choice = console.readLine();
		    		if(choice.equals("1")){
		    			client = Transaction(client, out, in, console);
		    		}
		    		else if(choice.equals("2")){
		    			client = Logout(client, out, in);
		    			feedback = Client.FAILED_FEEDBACK;
		    		}
		    	}
	        }
		}
		
		catch(UnknownHostException unknownHost){
            System.err.println("Tentativa de conexao com server desconhecido");
        }
        catch(Exception Exception){
            Exception.printStackTrace();
        }
		
    	finally{
            //4: Closing connection
            try{
                out.close();
                requestSocket.close();
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
		
    	console.printf("Please enter the username to transfer: ");
    	iban = console.readLine();
    	
    	console.printf("Please enter an amount to transfer: ");
    	amount = console.readLine();
    	
    	String transaction = client.generateTransactionSms(iban, amount);
    	System.out.println(transaction + " TAMANHO: " + transaction.length());
		out.writeObject(transaction);
        out.flush();
        
        String feedback = (String) in.readObject();
        System.out.println(feedback + " TAMANHO: " + feedback.length());
        System.out.println(client.processFeedback(feedback, "transaction"));
		
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
    
}
