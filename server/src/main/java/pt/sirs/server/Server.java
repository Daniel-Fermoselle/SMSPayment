package pt.sirs.server;

import java.math.BigInteger;
import java.security.KeyPair;
import java.sql.Timestamp;
import java.util.ArrayList;

import javax.crypto.spec.SecretKeySpec;


import pt.sirs.crypto.Crypto;
import pt.sirs.server.Exceptions.AmountToHighException;
import pt.sirs.server.Exceptions.IBANAlreadyExistsException;
import pt.sirs.server.Exceptions.ServerException;
import pt.sirs.server.Exceptions.UserAlreadyExistsException;

public class Server {
	public static final String SERVER_SUCCESSFUL_LOGIN_MSG = "LoginOk";
	public static final String SUCCESSFUL_TRANSACTION_MSG = "TransOk";
	public static final String SERVER_SUCCESSFUL_LOGOUT_MSG = "LogoutOk";
	public static final String SERVER_BEGGINING = "Initialized";
	public static final String ERROR_MSG = "ChamPog";
	public static final String SERVER_LOST_CONNECTION_MSG = "ConnectionKO";
	private static final String PRIVATE_KEY_PATH = "keys/ServerPrivateKey";
	private static final String PUBLIC_KEY_PATH = "keys/ServerPublicKey";
	public static final String ERROR_MSG_DH = "Blocked";

	
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
    	addAccount(new Account("PT12345678901234567890123", 100, 	  "nasTyMSR",   "12345",   "913534674"));
    	addAccount(new Account("PT12345678901234567890124", 100, 	  "sigmaJEM",   "12345",   "915667357"));
    	addAccount(new Account("PT12345678901234567890125", 100, 	  "Alpha"   ,   "12345",   "912436744"));
    	addAccount(new Account("PT12345678901234567890126", 100, 	  "jse"     ,   "12345",   "912456434"));
    	addAccount(new Account("PT12345678901234567890127", 10000000, "aaaaaaaaaa", "1234567", "912456423"));
    	this.status = SERVER_BEGGINING;
    	keys = new KeyPair(Crypto.readPubKeyFromFile(PUBLIC_KEY_PATH), Crypto.readPrivKeyFromFile(PRIVATE_KEY_PATH));

    }    
    
    public String processLoginSms(String sms) throws Exception{
		String decipheredMsg;
		Account sender;
		String stringTimestamp, password;

		String[] splitedSms = sms.split("\\|");
		if(splitedSms.length != 3){
			//TODO Generate error msg client not registered
			return generateUnsuccessfulFeedback("Wrong message format.", 0);
		}
		byte[] byteMobile = Crypto.decode(splitedSms[0]);
		byte[] byteSignature = Crypto.decode(splitedSms[1]);
		byte[] byteCipheredMsg = Crypto.decode(splitedSms[2]);

		//Getting user in msg
		sender = getAccountByMobile(new String(byteMobile));
		if(sender == null){
			return generateUnsuccessfulFeedback("Sender mobile number unknown", 0);
		}
		sender.setCounter(0);
		sender.setTrys(sender.getTrys() + 1);
		
		if(sender.getTrys() == 2){
			accounts.remove(sender);
			return generateUnsuccessfulFeedback("Sender tried to many time to login going to block account.", 0);
		}

		//Deciphering msg
		decipheredMsg = Crypto.decipherSMS(byteCipheredMsg, this.sharedKey);

		//Obtaining time stamp and password
		String[] splitedMsg = decipheredMsg.split("\\|");
		if(splitedMsg.length != 2){
			//TODO Generate error msg client not registered
			return generateUnsuccessfulFeedback("Deciphered content not expected.", 0);
		}
		stringTimestamp = splitedMsg[0];
		password = splitedMsg[1];
		
		System.out.println("Password is:" + password + "   len " + password.length());
		
		//Verify signature 
		String msgToVerify = sender.getMobile() + stringTimestamp + password;
		
		if(Crypto.verifySign(msgToVerify, byteSignature, sender.getPubKey())){		
			return generateLoginFeedback(sender, password, stringTimestamp);
		}
		else{
			//TODO Generate error msg for feedback signature compromised
			return generateUnsuccessfulFeedback("Signature compromised on loggin SMS received.", 0);
		}
    }
    
    public String processTransactionSms(String sms) throws Exception{
		String decipheredMsg, receiver, amount = "", counter;
		Account sender;
		SecretKeySpec sharedKey;
		
		String[] splitedSms = sms.split("\\|");
		if(splitedSms.length != 3){
			//TODO Generate error msg client not registered
			return generateUnsuccessfulFeedback("Wrong message format.", 0);
		}
		byte[] byteMobile = Crypto.decode(splitedSms[0]);
		byte[] byteSignature = Crypto.decode(splitedSms[1]);
		byte[] byteCipheredMsg = Crypto.decode(splitedSms[2]);
		
		//Getting user in msg
		sender = getAccountByMobile(new String(byteMobile));
		if(sender == null){
			//TODO Generate error msg client not registered
			return generateUnsuccessfulFeedback("User not registered.", 0);			
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
		else if(splitedMsg.length == 3){
			receiver = splitedMsg[0];
			amount   = splitedMsg[1];
			counter  = splitedMsg[2];
		}
		else{
			return generateUnsuccessfulFeedback("Deciphered content not expected.", 0);
		}
		
		//Verify user counter
		int smsCounter = Integer.parseInt(counter);
		if(smsCounter < sender.getCounter()) { return generateUnsuccessfulFeedback("Freshness compromised.", 0); }
		else { 
			if(sender.getCounter() < Integer.MAX_VALUE)
				sender.setCounter(smsCounter + 1);
			else{
				sender.setCounter(0);
				System.out.println("User sent too many messages. Going to force logout");
			}
		}
		
		//Verify signature 
		String msgToVerify;
		if(splitedMsg.length == 3){
			msgToVerify = sender.getMobile() + receiver + amount + counter;
		}
		else{
			msgToVerify = sender.getMobile() + receiver + counter;
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
			return generateUnsuccessfulFeedback("Signature compromised on loggin SMS received", sender.getCounter());
		}
    }

	public String generateTransactionFeedback(Account sender, String receiver, String amount) throws Exception{
		Account receiverAcc;

		receiverAcc = getAccountByUsername(receiver);
		try{
			if(receiverAcc != null){
				sender.debit(Integer.parseInt(amount));
				receiverAcc.credit(Integer.parseInt(amount));
				this.status = SUCCESSFUL_TRANSACTION_MSG;
			}
			else{
				//TODO Generate error msg receiver not registered
				return generateUnsuccessfulFeedback("Receiver not registered.", sender.getCounter());
			}
		} catch (AmountToHighException e){
			return generateUnsuccessfulFeedback("Amount to damn high.", sender.getCounter());

		}

		sender.setCounter(Integer.MAX_VALUE);
		//Msg to cipher
		String toCipher = this.status + "|" + sender.getCounter();
		byte[] cipheredText = Crypto.cipherSMS(toCipher, this.sharedKey);
		
		//Generating signature
		String dataToSign = this.status + sender.getCounter();
		byte[] signature = Crypto.sign(dataToSign, keys.getPrivate());
				
		System.out.println(this.status);
		if(this.status.equals(SUCCESSFUL_TRANSACTION_MSG)){
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
	//		this.status = SERVER_FAILED_LOGIN_MSG;

		if(password.equals(a.getPassword()) && Crypto.validTS(stringTS)){
			this.status = SERVER_SUCCESSFUL_LOGIN_MSG;
			a.setSharedKey(sharedKey);
			a.setTrys(0);
		}
		else{
			return generateUnsuccessfulFeedback("Wrong password.", 0);
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
		this.status = SERVER_SUCCESSFUL_LOGOUT_MSG;
		sender.setCounter(0);

		//Msg to cipher
		String toCipher = this.status + "|" + sender.getCounter();
		byte[] cipheredText = Crypto.cipherSMS(toCipher, sender.getSharedKey());
		
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
	
	public String generateUnsuccessfulFeedback(String msg,int counter) throws Exception{
		System.out.println(msg);
		this.status = ERROR_MSG;
		
		//Msg to cipher
		String toCipher = this.status + "|" + counter;
		byte[] cipheredText = Crypto.cipherSMS(toCipher, this.sharedKey);
		
		//Generating signature
		String dataToSign = this.status + counter;
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
	
	public Account getAccountByMobile(String msg){
		for(Account user : this.accounts){
			if(msg.equals(user.getMobile())){
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
        
        if(this.status.equals(ERROR_MSG_DH)){
			return generateUnsuccessfulFeedback("This sender was blocked", 0);
        }

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
		
		Account sender = getAccountByMobile(stringSender);
		if(sender == null){
			//TODO Generate error msg client not registered
			this.status = ERROR_MSG_DH;
			return;			
		}
		
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