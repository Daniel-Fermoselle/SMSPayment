package pt.sirs.server;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;

import javax.crypto.spec.SecretKeySpec;


import pt.sirs.crypto.Crypto;
import pt.sirs.server.Exceptions.IBANAlreadyExistsException;
import pt.sirs.server.Exceptions.ServerException;
import pt.sirs.server.Exceptions.UserAlreadyExistsException;

public class Server {
	public static final String SUCCESS_FEEDBACK = "PogChamp";
	public static final String FAILED_FEEDBACK = "ChamPog";
	
	private ArrayList<Account> accounts;
	private BigInteger p;
	private BigInteger g;
	private BigInteger secretValue;
	private BigInteger publicValue;
	private SecretKeySpec sharedKey;
	private String status;
	
    public Server() throws ServerException {
    	
    	this.accounts = new ArrayList<Account>();
    	addAccount(new Account("PT12345678901234567890123", 100, "nasTyMSR", "1"));
    	addAccount(new Account("PT12345678901234567890124", 100, "sigmaJEM", "12"));
    	addAccount(new Account("PT12345678901234567890125", 100, "Alpha", "123"));
    	addAccount(new Account("PT12345678901234567890126", 100, "jse", "1234"));
    	this.status = "Initialized";
    }    
    
    public String processLoginSms(String cipheredSms) throws Exception{
		byte[] msg;
		String decipheredSms;
		Account a;
		
		byte[] decodedCipheredSms =  Crypto.decode(cipheredSms);
		
		a = getAccountByUsername(new String(decodedCipheredSms));
		
		//Possible problem if encoding used more than 1 byte in 1 character
		msg = Arrays.copyOfRange(decodedCipheredSms, 2 + a.getUsername().length(), decodedCipheredSms.length);
		
		decipheredSms = Crypto.decipherSMS(msg, this.sharedKey);
		System.out.println("Password is:" + decipheredSms + "   len " + decipheredSms.length());
		
		return generateLoginFeedback(a, decipheredSms);
    }
    
    public String processTransactionSms(String cipheredSms) throws Exception{
		byte[] msg;
		String decipheredSms;
		Account sender;
		SecretKeySpec sharedKey;
		
		byte[] decodedCipheredSms =  Crypto.decode(cipheredSms);
		
		sender = getAccountByUsername(new String(decodedCipheredSms));
		sharedKey = sender.getSharedKey();
		
		//Possible problem if encoding used more than 1 byte in 1 character
		msg = Arrays.copyOfRange(decodedCipheredSms, 2 + sender.getUsername().length(), decodedCipheredSms.length);
		
		decipheredSms = Crypto.decipherSMS(msg, sharedKey);
		
		return generateTransactionFeedback(sender, decipheredSms);
    }
	
	public String generateLoginFeedback(Account a, String smsPassword) throws Exception{
		this.status = FAILED_FEEDBACK;
		
		if(smsPassword.contains(a.getPassword())){
			this.status = SUCCESS_FEEDBACK;
			a.setSharedKey(sharedKey);
		}
		
		byte[] cipheredText = Crypto.cipherSMS(this.status, this.sharedKey);
		
		byte[] finalMsg = new byte[cipheredText.length];
		System.arraycopy(cipheredText, 0, finalMsg, 0, cipheredText.length);
		
		System.out.println(this.status);
		
		return Crypto.encode(finalMsg);
	}
	
	public String generateTransactionFeedback(Account sender, String decipheredSms) throws Exception{
		Account receiver;
		this.status = FAILED_FEEDBACK;
		
		receiver = getAccountByIban(decipheredSms);
		if(receiver != null){
			String[] parts = decipheredSms.split("-");
			sender.debit(Integer.parseInt(parts[1]));
			receiver.credit(Integer.parseInt(parts[1]));
			this.status = SUCCESS_FEEDBACK;
		}
		
		byte[] cipheredText = Crypto.cipherSMS(this.status, this.sharedKey);
		
		byte[] finalMsg = new byte[cipheredText.length];
		System.arraycopy(cipheredText, 0, finalMsg, 0, cipheredText.length);
		
		System.out.println(this.status);
		if(this.status.equals(SUCCESS_FEEDBACK)){
			System.out.println("Receiver: " + ((Integer) receiver.getBalance()).toString() + " <------- " +
				"Sender: " + ((Integer) sender.getBalance()).toString());
		}
		
		return Crypto.encode(finalMsg);
	}
	
    public void addAccount(Account account) throws ServerException{
    	for(Account a : this.accounts){
    		if(a.getIban().equals(account.getIban())){
    			throw new IBANAlreadyExistsException(account.getIban());
    		}
    		if(a.getUsername().equals(account.getUsername())){
    			throw new UserAlreadyExistsException(account.getUsername());
    		}
    	}
    	this.accounts.add(account);
    }
	
	public Account getAccountByUsername(String msg){
		for(Account user : this.accounts){
			if(msg.contains(user.getUsername())){
				return user;
			}
		}
		return null;
	}
	
	public Account getAccountByIban(String msg){
		for(Account user : this.accounts){
			if(msg.contains(user.getIban())){
				return user;
			}
		}
		return null;
	}

	public void generateSecretValue() {
		this.secretValue = Crypto.generateSecretValue();
	}
	
	public void generatePublicValue(){
	   publicValue = g.modPow(secretValue, p);
	}
	
	public String getPublicValue(){
		return Crypto.encode(publicValue.toByteArray());
	}
	
	public void generateSharedKey(String stringPublicValue) throws Exception{
		byte[] bytePublicValue = Crypto.decode(stringPublicValue);
		BigInteger publicValue = new BigInteger(bytePublicValue);
		BigInteger sharedKey = publicValue.modPow(secretValue, p);
		System.out.println(Crypto.encode(sharedKey.toByteArray()) + "  LENG SharedKey: " + Crypto.encode(sharedKey.toByteArray()).length());
		this.sharedKey = Crypto.generateKeyFromBigInt(sharedKey);
	}
    
	public void setP(String p) {
		byte[] byteP = Crypto.decode(p);
		this.p = new BigInteger(byteP);
	}

	public void setG(String g) {
		byte[] byteG = Crypto.decode(g);
		this.g = new BigInteger(byteG);		
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	
}