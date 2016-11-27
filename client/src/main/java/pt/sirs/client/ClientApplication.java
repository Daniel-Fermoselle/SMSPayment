package pt.sirs.client;

import java.io.*;
import java.net.*;
import java.util.Scanner;
import pt.sirs.smsPacket.SmsPacket;
import pt.sirs.client.Client;

public class ClientApplication {
	
	public static final String IBAN = "PT01234567890123456789012";
	public static final int INITMONEY = 0;
	
	public static void main(String[] args) {
		Socket requestSocket = null;//CUIDADO
		ObjectOutputStream out = null;//CUIDADO
		ObjectInputStream in = null;
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
	    	
	    	console.printf("Please enter your username: ");
	    	String username = console.readLine();
	    	
	    	console.printf("Please enter your password: ");
	    	char[] passwordChars = console.readPassword();
	    	String passwordString = new String(passwordChars);
	    	
	    	Client client = new Client(username, passwordString);
	    	
	    	String login = client.generateLoginSms();
	    	System.out.println(login + " TAMANHO: " + login.length());
    		out.writeObject(login);
            out.flush();
            
            String feedback = (String) in.readObject();
            System.out.println(feedback + " TAMANHO: " + feedback.length());
            System.out.println(client.processLoginFeedback(feedback));
            
	    	
	    	while(true){
	    		String iban, amount;
	    		
		    	console.printf("Please enter the IBAN to transfer: ");
		    	iban = console.readLine();
		    	
		    	console.printf("Please enter an amount to transfer: ");
		    	amount = console.readLine();
		    	
		    	String transaction = client.generateTransactionSms(iban, amount);
		    	System.out.println(transaction + " TAMANHO: " + transaction.length());
	    		out.writeObject(transaction);
	            out.flush();
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
    
}
