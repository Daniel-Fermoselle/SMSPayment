package pt.sirs.server;

import java.security.Key;
import java.util.ArrayList;
import pt.sirs.crypto.Crypto;
import pt.sirs.server.Exceptions.IBANAlreadyExistsException;
import pt.sirs.server.Exceptions.ServerException;
import pt.sirs.server.Exceptions.UserAlreadyExistsException;

public class Server {
	
	private static final String KEYSTORE_LOCATION = "keys/aes-keystore.jck";
	private static final String KEYSTORE_PASS = "mypass";
	private static final String ALIAS = "aes";
	private static final String KEY_PASS = "mypass";
	private ArrayList<Account> accounts;
	
    public Server() throws ServerException {
    	this.accounts = new ArrayList<Account>();
    	addAccount(new Account("PT12345678901234567890123", 100, "nasTyMSR", "1"));
    	addAccount(new Account("PT12345678901234567890124", 100, "sigmaJEM", "12"));
    	addAccount(new Account("PT12345678901234567890125", 100, "Alpha", "123"));
    	addAccount(new Account("PT12345678901234567890126", 100, "jse", "1234"));
    	
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
    

	public String processSms(String cipheredSms) throws Exception {
		Key sharedKey;
		String decipheredSms;
		String feedback = "pog";
		System.out.println(cipheredSms + "   len " + cipheredSms.length());
		sharedKey = Crypto.getKeyFromKeyStore(KEYSTORE_LOCATION, KEYSTORE_PASS, ALIAS, KEY_PASS);
		decipheredSms = Crypto.decipherSMS(cipheredSms, sharedKey);
		System.out.println(decipheredSms + "   len " + decipheredSms.length());

		//Verify signature
		//Execute operation
		//Generate Feedback
		return feedback;
	}
    
}