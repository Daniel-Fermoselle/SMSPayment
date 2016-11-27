package pt.sirs.server;

import java.math.BigInteger;
import java.security.Key;
import java.util.ArrayList;
import java.util.Arrays;

import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

import pt.sirs.crypto.Crypto;
import pt.sirs.server.Exceptions.IBANAlreadyExistsException;
import pt.sirs.server.Exceptions.ServerException;
import pt.sirs.server.Exceptions.UserAlreadyExistsException;

public class Server {
	public static final String KEYSTORE_LOCATION = "keys/aes-keystore.jck";
	public static final String KEYSTORE_PASS = "mypass";
	public static final String ALIAS = "aes";
	public static final String KEY_PASS = "mypass";
	
	private ArrayList<Account> accounts;
	private BigInteger p;
	private BigInteger g;
	private BigInteger secretValue;
	private BigInteger publicValue;
	private SecretKeySpec sharedKey;
	
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
    
    public String processLoginSms(String cipheredSms) throws Exception{
		byte[] iv, msg;
		Key sharedKey;
		String decipheredSms;
		Account a;
		
		byte[] decodedCipheredSms =  Crypto.decode(cipheredSms);
		
		a = checkUsername(new String(decodedCipheredSms));
		
		iv = Arrays.copyOfRange(decodedCipheredSms, 0, 16);
		//Possible problem if encoding used more than 1 byte in 1 character
		msg = Arrays.copyOfRange(decodedCipheredSms, 16 + 2 + a.getUsername().length(), decodedCipheredSms.length);
		
		//sharedKey = Crypto.getKeyFromKeyStore(KEYSTORE_LOCATION, KEYSTORE_PASS, ALIAS, KEY_PASS);
		decipheredSms = Crypto.decipherSMS(msg, this.sharedKey, new IvParameterSpec(iv));
		System.out.println("Password is:" + decipheredSms + "   len " + decipheredSms.length());
		
		return generateLoginFeedback(a.getPassword(), decipheredSms);
    }
	
	public String generateLoginFeedback(String aPassword, String smsPassword) throws Exception{
		String feedback = "ChamPog";
		
		if(smsPassword.contains(aPassword)){
			feedback = "PogChamp";
		}
		
		IvParameterSpec ivspec = Crypto.generateIV();
		//Key sharedKey = Crypto.getKeyFromKeyStore(KEYSTORE_LOCATION, KEYSTORE_PASS, ALIAS, KEY_PASS);	
		byte[] cipheredText = Crypto.cipherSMS(feedback, this.sharedKey, ivspec);
		
		byte[] finalMsg = new byte[ivspec.getIV().length + cipheredText.length];
		System.arraycopy(ivspec.getIV(), 0, finalMsg, 0, ivspec.getIV().length);
		System.arraycopy(cipheredText, 0, finalMsg, ivspec.getIV().length, cipheredText.length);
		
		System.out.println(feedback);
		
		return Crypto.encode(finalMsg);
	}
	
	public Account checkUsername(String msg){
		for(Account user : this.accounts){
			if(msg.contains(user.getUsername())){
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