package pt.sirs.server;

import java.math.BigInteger;
import java.security.KeyPair;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import javax.crypto.spec.SecretKeySpec;


import pt.sirs.crypto.Crypto;
import pt.sirs.server.Exceptions.IBANAlreadyExistsException;
import pt.sirs.server.Exceptions.ServerException;
import pt.sirs.server.Exceptions.UserAlreadyExistsException;

public class Server {

	private static final int SIZE_OF_TIMESTAMP = 23;
	private static final int SIGNATURE_SIZE = 47;
	public static final String SERVER_FAILED_LOGIN_MSG = "ChamPog";
	public static final String SERVER_SUCCESSFUL_LOGIN_MSG = "PogChamp";
	private static final String FAILED_TRANSACTION_MSG = "Transaction Failed";
	private static final String PRIVATE_KEY_PATH = "keys/PrivateKeyServer";
	private static final String PUBLIC_KEY_PATH = "keys/PublicKeyServer";

	
	private ArrayList<Account> accounts;
	private BigInteger p;
	private BigInteger g;
	private BigInteger secretValue;
	private BigInteger publicValue;
	private SecretKeySpec sharedKey;
	private String status;
	private KeyPair keys;
	private String nonRepudiationString;
	
    public Server() throws Exception {
    	
    	this.accounts = new ArrayList<Account>();
    	addAccount(new Account("PT12345678901234567890123", 100, "nasTyMSR", "12345"));
    	addAccount(new Account("PT12345678901234567890124", 100, "sigmaJEM", "12345"));
    	addAccount(new Account("PT12345678901234567890125", 100, "Alpha", "12345"));
    	addAccount(new Account("PT12345678901234567890126", 100, "jse", "12345"));
    	addAccount(new Account("PT12345678901234567890127", 10000000, "aaaaaaaaaa", "1234567"));
    	this.status = "Initialized";
    	keys = new KeyPair(Crypto.readPubKeyFromFile(PUBLIC_KEY_PATH), Crypto.readPrivKeyFromFile(PRIVATE_KEY_PATH));

    }    
    
    public String processLoginSms(String sms) throws Exception{
		String decipheredMsg;
		Account sender;
		String stringTimestamp, password;

		String[] splitedSms = sms.split("\\|");
		byte[] byteUsername = Crypto.decode(splitedSms[0]);
		byte[] byteSignature = Crypto.decode(splitedSms[1]);
		byte[] byteCipheredMsg = Crypto.decode(splitedSms[2]);

		//Getting user in msg
		sender = getAccountByUsername(new String(byteUsername));
		if(sender == null){
			//TODO Generate error msg client not registered
			System.out.println("User not registered");
		}
		
		//Deciphering msg
		decipheredMsg = Crypto.decipherSMS(byteCipheredMsg, this.sharedKey);

		//Obtaining time stamp and password
		String[] splitedMsg = decipheredMsg.split("\\|");
		stringTimestamp = splitedMsg[0];
		password = splitedMsg[1];
		
		System.out.println("Password is:" + password + "   len " + password.length());
		
		//Verify signature 
		String msgToVerify = sender.getUsername() + stringTimestamp + password;
		
		if(Crypto.verifySign(msgToVerify, byteSignature, sender.getPubKey())){		
			return generateLoginFeedback(sender, password, stringTimestamp);
		}
		else{
			//TODO Generate error msg for feedback signature compromised
			System.out.println("Signature compromised on loggin SMS received");
			return generateLoginFeedback(sender, "sdadjan", stringTimestamp);
		}
    }
    
    public String processTransactionSms(String sms) throws Exception{
		String decipheredMsg, receiver, amount = "", counter;
		Account sender;
		SecretKeySpec sharedKey;
		
		String[] splitedSms = sms.split("\\|");
		byte[] byteUsername = Crypto.decode(splitedSms[0]);
		byte[] byteSignature = Crypto.decode(splitedSms[1]);
		byte[] byteCipheredMsg = Crypto.decode(splitedSms[2]);
		
		//Getting user in msg
		sender = getAccountByUsername(new String(byteUsername));
		if(sender == null){
			//TODO Generate error msg client not registered
			System.out.println("User not registered");
		}
		sharedKey = sender.getSharedKey();
		
		//Deciphering msg
		decipheredMsg = Crypto.decipherSMS(byteCipheredMsg, sharedKey);
		
		//Obtaining time stamp and password
		String[] splitedMsg = decipheredMsg.split("\\|");
		if(splitedMsg.length == 2){
			receiver = splitedMsg[0];
			counter  = splitedMsg[1];
		}
		else{
			receiver = splitedMsg[0];
			amount   = splitedMsg[1];
			counter  = splitedMsg[2];
		}
		
		//Verify user counter
		int smsCounter = Integer.parseInt(counter);
		if(smsCounter < sender.getCounter()){
			return FAILED_TRANSACTION_MSG;
		}
		else{
			sender.setCounter(smsCounter + 1);
		}
		
		String msgToVerify;
		//Verify signature 
		if(splitedMsg.length != 2){
			msgToVerify = sender.getUsername() + receiver + amount + counter;
		}
		else{
			msgToVerify = sender.getUsername() + receiver + counter;
		}
    
		if(Crypto.verifySign(msgToVerify, byteSignature, sender.getPubKey())){		
		
			//Check if it's a logout message
			if(receiver.equals("logout")){
				return generateLogoutFeedback(sender); 
			}
			
			return generateTransactionFeedback(sender, receiver, amount);
		}
		else{
			//TODO Generate error msg for feedback signature compromised
			System.out.println("Signature compromised on loggin SMS received");
			return generateLogoutFeedback(sender); 
		}
    }

	public String generateTransactionFeedback(Account sender, String receiver, String amount) throws Exception{
		Account receiverAcc;
		this.status = SERVER_FAILED_LOGIN_MSG;
		
		receiverAcc = getAccountByUsername(receiver);
		if(receiverAcc != null){
			sender.debit(Integer.parseInt(amount));
			receiverAcc.credit(Integer.parseInt(amount));
			this.status = SERVER_SUCCESSFUL_LOGIN_MSG;
		}
		else{
			//TODO Generate error msg receiver not registered
			System.out.println("Receiver not registered");
		}
		
		//Msg to cipher
		String toCipher = this.status + "|" + sender.getCounter();
		byte[] cipheredText = Crypto.cipherSMS(toCipher, this.sharedKey);
		
		//Generating signature
		String dataToSign = this.status + sender.getCounter();
		byte[] signature = Crypto.sign(dataToSign, keys.getPrivate());
				
		System.out.println(this.status);
		if(this.status.equals(SERVER_SUCCESSFUL_LOGIN_MSG)){
			System.out.println("Receiver: " + ((Integer) receiverAcc.getBalance()).toString() + " <------- " +
				"Sender: " + ((Integer) sender.getBalance()).toString());
		}
		
		//Concatenate signature with cipheredText --> signature|cipheredText
		//cipheredText = {status|counter}Ks
		String stringSig = Crypto.encode(signature);
		String stringCiphertext = Crypto.encode(cipheredText);
		String toSend = stringSig + "|" + stringCiphertext;
		
		System.out.println("Size of transaction feedback SMS message: " + toSend.length());

		return toSend;		
	}

	public String generateLoginFeedback(Account a, String password, String stringTS) throws Exception{
		this.status = SERVER_FAILED_LOGIN_MSG;
		
		if(password.equals(a.getPassword()) && Crypto.validTS(stringTS)){
			this.status = SERVER_SUCCESSFUL_LOGIN_MSG;
			a.setSharedKey(sharedKey);
		}
		
		//Add user counter to msg
		String feedback = this.status + "|" + a.getCounter();
		byte[] cipheredText = Crypto.cipherSMS(feedback, sharedKey);
		System.out.println(feedback);
		
		//Generating signature
		String dataToSign = this.status + a.getCounter();
		byte[] signature = Crypto.sign(dataToSign, keys.getPrivate());
		
		//Concatenate signature with cipheredText --> signature|cipheredText
		//cipheredText = {status|counter}Ks
		String stringSig = Crypto.encode(signature);
		String stringCiphertext = Crypto.encode(cipheredText);
		String toSend = stringSig + "|" + stringCiphertext;
		
		System.out.println("Size of login feedback SMS message: " + toSend.length());

		return toSend;
	}
	

	public String generateLogoutFeedback(Account sender) throws Exception{
		this.status = SERVER_FAILED_LOGIN_MSG;
		sender.setCounter(0);
		
		//Msg to cipher
		String toCipher = this.status + "|" + sender.getCounter();
		byte[] cipheredText = Crypto.cipherSMS(toCipher, this.sharedKey);
		
		//Generating signature
		String dataToSign = this.status + sender.getCounter();
		byte[] signature = Crypto.sign(dataToSign, keys.getPrivate());
		
		//Concatenate signature with cipheredText --> signature|cipheredText
		//cipheredText = {status|counter}Ks
		String stringSig = Crypto.encode(signature);
		String stringCiphertext = Crypto.encode(cipheredText);
		String toSend = stringSig + "|" + stringCiphertext;
		
		System.out.println("Size of logout feedback SMS message: " + toSend.length());

		return toSend;		
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
			if(msg.equals(user.getUsername())){
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
	
	public String getNonRepudiationMsgForPublicValue() throws Exception {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

		//Generating signature
		String dataToSign = publicValue + timestamp.toString();
		byte[] signature = Crypto.sign(dataToSign, keys.getPrivate());

		String stringSig = Crypto.encode(signature);
		String toSend =  stringSig + "|" + timestamp.toString();
		
		System.out.println("Size of non repudiation msg for public value used in DH SMS message: " + toSend.length());

		return toSend;
	}
	
	public void generatePublicValue(){
	   publicValue = g.modPow(secretValue, p);
	}
	
	public String getPublicValue(){
		return Crypto.encode(publicValue.toByteArray());
	}
	
	public void receiveNonRepudiationMsgForPublicValue(String readObject) {
		this.nonRepudiationString = readObject;
	}
	
	public void generateSharedKey(String stringPublicValue) throws Exception{
		byte[] bytePublicValue = Crypto.decode(stringPublicValue);
		BigInteger publicValue = new BigInteger(bytePublicValue);
		
		//Verify publicValue
		String[] splitedSms = this.nonRepudiationString.split("\\|");
		String stringSender = splitedSms[0];
		byte[] byteSig = Crypto.decode(splitedSms[1]);
		String stringTS  = splitedSms[2];
		
		Account sender = getAccountByUsername(stringSender);
		
		//Verify TimeStamp
		if(!Crypto.validTS(stringTS)){
			//TODO send proper error
			System.out.println("Time stamp used in DH public value invalid, passed more than 1 minute");
		}
		
		//Verify signature
		String msgToVerify = stringSender + publicValue + stringTS;
		if(!Crypto.verifySign(msgToVerify, byteSig, sender.getPubKey())){
			//TODO send proper error
			System.out.println("Signature compromised ins DH public value msg");
		}
		
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