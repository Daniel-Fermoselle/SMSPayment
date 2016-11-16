package pt.sirs.client;

import java.io.*;
import java.net.*;
import java.util.Scanner;
import pt.sirs.smsPacket.smsPacket;
import pt.sirs.smsPacket.Exceptions.InvalidSMSPacketValuesException;
import pt.sirs.client.Client;

public class ClientApplication {
	
	public static final String IBAN = "PT0123456789012345678901234";
	public static final int INITMONEY = 0;
	
	public static void main(String[] args) {
		Socket requestSocket=null;//CUIDADO
		ObjectOutputStream out=null;//CUIDADO
		try{
			//1. Criar o socket para falar com o server
			requestSocket = new Socket("localhost", 10000);
	        System.out.println("Connected to localhost in port 10000");//Just debugging prints
	        //2. Criar o socket para enviar coisas para o server
	        out = new ObjectOutputStream(requestSocket.getOutputStream());
	        out.flush();
	    
	    	Client c = new Client(IBAN,INITMONEY);
	    	Scanner s = new Scanner(System.in);
	    	String command = "";
	    	System.out.println("Started...");//Just debugging prints
	    	command=s.nextLine();
	    	if(command==null){
	    		System.out.println("Error:Exiting...");//Just debugging prints
	    		System.exit(1);//VERIFICAR
	    	}
	    	while(!command.equals("exit")){
	    		String [] line = command.split(" ");
	    		if(line.length!=2){
	    			System.out.println("Wrong format, try again");//Just debugging prints
	    			continue;
	    			//System.exit(2);//VERIFICAR
	    		}
	    		//Line processing
	    		String [] temp = line[0].split("Iban:");
	    		String iban = temp[1];
	    		temp = line[1].split("Amount:");
	    		String amount = temp[1];
	    		
	    		smsPacket sms = c.getSmsPacket(iban, amount);
	    		out.writeObject(sms);
	            out.flush();
	            System.out.println("SMS sent... " + sms.toString());//Just debugging prints
	    		command=s.nextLine();
	    	}
	    	System.out.println("Exiting...");//Just debugging prints
		}
		
		//Sera necessario ter assim as excepcoes tendo em conta que isto vai ser um servico de sms (question mark)
		catch(UnknownHostException unknownHost){
            System.err.println("Tentativa de conexao com server desconhecido");
        }
        catch(IOException ioException){
            ioException.printStackTrace();
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
