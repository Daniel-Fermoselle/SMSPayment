package pt.sirs.client;

import java.security.Key;
import java.util.Arrays;

import javax.crypto.spec.IvParameterSpec;

import pt.sirs.crypto.Crypto;

public class Client {
	public static final String KEYSTORE_LOCATION = "keys/aes-keystore.jck";
	public static final String KEYSTORE_PASS = "mypass";
	public static final String ALIAS = "aes";
	public static final String KEY_PASS = "mypass";
	
	private int myMoney; 
	private String myUsername;
	private String myPassword;
	
	public Client(String myUsername, String myPassword) {
		this.myUsername = myUsername;
		this.myPassword = myPassword;
	}
	
	public String generateLoginSms() throws Exception{
		IvParameterSpec ivspec;
		byte[] cipheredText;
		Key sharedKey;
		String usernameS = "-" + this.myUsername + "-";
		
		ivspec = Crypto.generateIV();
		sharedKey = Crypto.getKeyFromKeyStore(KEYSTORE_LOCATION, KEYSTORE_PASS, ALIAS, KEY_PASS);	
		cipheredText = Crypto.cipherSMS(this.myPassword, sharedKey, ivspec);	
		

		//Concatenate IV with username with cipheredText --> IV-username-cipheredText
		byte[] usernameB = usernameS.getBytes();
		byte[] finalMsg = new byte[ivspec.getIV().length + cipheredText.length + usernameB.length];
		System.arraycopy(ivspec.getIV(), 0, finalMsg, 0, ivspec.getIV().length);
		System.arraycopy(usernameB, 0, finalMsg, ivspec.getIV().length, usernameB.length);
		System.arraycopy(cipheredText, 0, finalMsg, ivspec.getIV().length + usernameB.length, cipheredText.length);
		
		return Crypto.encode(finalMsg);
		
	}
	
	public String generateTransactionSms(String iban, String amount) throws Exception{
		IvParameterSpec ivspec;
		byte[] cipheredText;
		Key sharedKey;
		String usernameS = "-" + this.myUsername + "-";
		String msgToCipher = iban + "-" + amount;
		
		ivspec = Crypto.generateIV();
		sharedKey = Crypto.getKeyFromKeyStore(KEYSTORE_LOCATION, KEYSTORE_PASS, ALIAS, KEY_PASS);	
		cipheredText = Crypto.cipherSMS(msgToCipher, sharedKey, ivspec);		

		//Concatenate IV with username with cipheredText --> IV-username-cipheredText
		byte[] usernameB = usernameS.getBytes();
		byte[] finalMsg = new byte[ivspec.getIV().length + cipheredText.length + usernameB.length];
		System.arraycopy(ivspec.getIV(), 0, finalMsg, 0, ivspec.getIV().length);
		System.arraycopy(usernameB, 0, finalMsg, ivspec.getIV().length, usernameB.length);
		System.arraycopy(cipheredText, 0, finalMsg, ivspec.getIV().length + usernameB.length, cipheredText.length);
		
		return Crypto.encode(finalMsg);
		
	}
	
	public String processLoginFeedback(String cipheredSms) throws Exception{
		byte[] iv, msg;
		Key sharedKey;
		String decipheredSms;
		
		byte[] decodedCipheredSms =  Crypto.decode(cipheredSms);
		
		iv = Arrays.copyOfRange(decodedCipheredSms, 0, 16);
		msg = Arrays.copyOfRange(decodedCipheredSms, 16, decodedCipheredSms.length);
		
		sharedKey = Crypto.getKeyFromKeyStore(KEYSTORE_LOCATION, KEYSTORE_PASS, ALIAS, KEY_PASS);
		decipheredSms = Crypto.decipherSMS(msg, sharedKey, new IvParameterSpec(iv));
		
		return decipheredSms;
		
		
	}
	
	public void setMyMoney(int myMoney){
		this.myMoney = myMoney;
	}
	
	public int getMyMoney(){
		return myMoney;
	}

	public String getMyUsername() {
		return myUsername;
	}

	public void setMyUsername(String myUsername) {
		this.myUsername = myUsername;
	}

	public String getMyPassword() {
		return myPassword;
	}

	public void setMyPassword(String myPassword) {
		this.myPassword = myPassword;
	}
}
	
