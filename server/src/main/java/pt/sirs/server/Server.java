package pt.sirs.server;

import java.security.Key;
import java.util.ArrayList;
import pt.sirs.crypto.Crypto;

public class Server {
	
	private static final String KEYSTORE_LOCATION = "keys/aes-keystore.jck";
	private static final String KEYSTORE_PASS = "mypass";
	private static final String ALIAS = "aes";
	private static final String KEY_PASS = "mypass";
	private ArrayList<Account> accounts;
	
    public Server(){
    	this.accounts = new ArrayList<Account>();
    	this.accounts.add(new Account("PT12345678901234567890123", 100, "nasTyMSR", "1"));
    	this.accounts.add(new Account("PT12345678901234567890124", 100, "sigmaJEM", "12"));
    	this.accounts.add(new Account("PT12345678901234567890125", 100, "Alpha", "123"));
    	this.accounts.add(new Account("PT12345678901234567890126", 100, "jse", "1234"));
    	
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