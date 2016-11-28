package pt.sirs.server;

import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


import pt.sirs.crypto.Crypto;
import pt.sirs.server.Exceptions.IBANAlreadyExistsException;
import pt.sirs.server.Exceptions.ServerException;
import pt.sirs.server.Exceptions.UserAlreadyExistsException;

public class Server {
	private static final int SIZE_OF_TIMESTAMP = 23;
	private static final long MINUTE_IN_MILLIS = 60000;//one minute in millisecs
	private static final String SERVER_FAILED_LOGIN_MSG = "ChamPog";
	private static final String SERVER_SUCCESSFUL_LOGIN_MSG = "PogChamp";
	private static final String FAILED_TRANSACTION_MSG = "Transaction Failed";
	
	private ArrayList<Account> accounts;
	private BigInteger p;
	private BigInteger g;
	private BigInteger secretValue;
	private BigInteger publicValue;
	private SecretKeySpec sharedKey;
	
    public Server() throws ServerException {
    	
    	this.accounts = new ArrayList<Account>();
    	addAccount(new Account("PT12345678901234567890123", 100, "nasTyMSR", "12345"));
    	addAccount(new Account("PT12345678901234567890124", 100, "sigmaJEM", "12345"));
    	addAccount(new Account("PT12345678901234567890125", 100, "Alpha", "12345"));
    	addAccount(new Account("PT12345678901234567890126", 100, "jse", "12345"));
    	
    }    
    
    public String processLoginSms(String cipheredSms) throws Exception{
		byte[] msg;
		String decipheredSms;
		Account a;
		String stringTimestamp, password;

		byte[] decodedCipheredSms =  Crypto.decode(cipheredSms);
		
		a = getAccountByUsername(new String(decodedCipheredSms));
		
		//Possible problem if encoding used more than 1 byte in 1 character
		msg = Arrays.copyOfRange(decodedCipheredSms, 1 + a.getUsername().length(), decodedCipheredSms.length);
		
		decipheredSms = Crypto.decipherSMS(msg, this.sharedKey);
		//Verify TS
		stringTimestamp = String.copyValueOf(decipheredSms.toCharArray(), 0, SIZE_OF_TIMESTAMP);
		password = String.copyValueOf(decipheredSms.toCharArray(), SIZE_OF_TIMESTAMP + 1, 
									  decipheredSms.toCharArray().length - SIZE_OF_TIMESTAMP - 1);
		System.out.println("Password is:" + password + "   len " + password.length());
		
		return generateLoginFeedback(a, password, stringTimestamp);
    }
    
    public String processTransactionSms(String cipheredSms) throws Exception{
		byte[] msg;
		String decipheredSms;
		Account sender, receiver;
		SecretKeySpec sharedKey;
		
		byte[] decodedCipheredSms =  Crypto.decode(cipheredSms);
		
		sender = getAccountByUsername(new String(decodedCipheredSms));
		sharedKey = sender.getSharedKey();
		
		//Possible problem if encoding used more than 1 byte in 1 character
		msg = Arrays.copyOfRange(decodedCipheredSms, 1 + sender.getUsername().length(), decodedCipheredSms.length);
		
		decipheredSms = Crypto.decipherSMS(msg, sharedKey);

		receiver = getAccountByIban(decipheredSms);
		String[] parts = decipheredSms.split("-");
		
		//Verify user counter
		int smsCounter = Integer.parseInt(parts[2]);
		if(smsCounter > sender.getCounter()){
			return FAILED_TRANSACTION_MSG;
		}
		else{
			sender.setCounter(smsCounter + 1);
		}
		
		sender.debit(Integer.parseInt(parts[1]));
		receiver.credit(Integer.parseInt(parts[1]));
		
		//Should send user.conter
		return ((Integer) sender.getBalance()).toString();
    }
	
	public String generateLoginFeedback(Account a, String password, String stringTS) throws Exception{
		String feedback = SERVER_FAILED_LOGIN_MSG;
		
		if(password.equals(a.getPassword()) && validTS(stringTS)){
			feedback = SERVER_SUCCESSFUL_LOGIN_MSG;
			a.setSharedKey(sharedKey);
		}
		
		//Add user conter should be at 0
		feedback += "-" + a.getCounter();
		byte[] cipheredText = Crypto.cipherSMS(feedback, sharedKey);
		System.out.println(feedback);
		
		return Crypto.encode(cipheredText);
	}
	
    private boolean validTS(String stringTS) throws ParseException {
    	System.out.println(stringTS); //TODO Prints the time when sms received, remove if you please
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    	Date ts = sdf.parse(stringTS);
    	
    	//Generate current date plus and less one minute
    	Calendar date = Calendar.getInstance();
    	long t= date.getTimeInMillis();
    	Date afterAddingOneMin = new Date(t + (MINUTE_IN_MILLIS));
    	Date afterReducingOneMin = new Date(t - (MINUTE_IN_MILLIS));    	

    	if(ts.before(afterAddingOneMin) && ts.after(afterReducingOneMin))
    		return true;
    	else 
    		return false;
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

	
}