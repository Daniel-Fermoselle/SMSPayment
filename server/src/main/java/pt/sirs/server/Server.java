package pt.sirs.server;

import java.math.BigInteger;
import java.security.KeyPair;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;

import javax.crypto.spec.SecretKeySpec;

import pt.sirs.crypto.Crypto;
import pt.sirs.server.Exceptions.AmountToHighException;

public class Server {
	public  static final String SERVER_BEGGINING = "Initialized";
	public  static final String SERVER_SUCCESSFUL_LOGIN_MSG = "LoginOk";
	public  static final String SERVER_SUCCESSFUL_LOGOUT_MSG = "LogoutOk";
	public  static final String SUCCESSFUL_TRANSACTION_MSG = "TransOk";
	public  static final String ERROR_MSG = "ChamPog";
	public  static final String ERROR_MSG_DH = "ErrorDH";
	public  static final String SERVER_LOST_CONNECTION_MSG = "ConnectionKO";
	private static final String PRIVATE_KEY_PATH = "keys/ServerPrivateKey";
	private static final String PUBLIC_KEY_PATH = "keys/ServerPublicKey";
	//Care if you want 3 chances the number of tries must be 4
	private static final int    NUMBER_OF_UNSUCCESSFULL_LOGIN_TRYS = 4;
	public  static final String MYSQL_ID = "root";
	public  static final String MYSQL_PASSWORD = "root";

	private BigInteger p;
	private BigInteger g;
	private BigInteger secretValue;
	private BigInteger publicValue;
	private SecretKeySpec sharedKey;
	private String status;
	private KeyPair keys;
	private String nonRepudiationString;
	private String mysqlId;
	private String mysqlPassword;
	
	/***
	 * This is the constructor for the Server, this constructor 
	 * will be called at the start of the ServerApplication. 
	 * action 
	 * @param mysqlId
	 * @param mysqlId
	 * @throws Exception
	 */
    public Server(String mysqlId, String mysqlPassword) throws Exception {
    	this.mysqlId = mysqlId;
    	this.mysqlPassword = mysqlPassword;
    	this.status = SERVER_BEGGINING;
    	keys = new KeyPair(Crypto.readPubKeyFromFile(PUBLIC_KEY_PATH), Crypto.readPrivKeyFromFile(PRIVATE_KEY_PATH));

    }        
    
	/***
	 * This function is used to process login sms' received from the client and
	 * it checks if the message is composed by
	 * (mobile|)signature|cipheredText where 
	 * signature = {mobile + TS + password}Kcs and cipheredText = {TS|pass}Ks
	 * and if this parameters are correct
	 * Kcs = client private key
	 * Ks = shared key
	 * @param sms
	 * @return String
	 * @throws Exception
	 */
    public String processLoginSms(String sms) throws Exception{
		String decipheredMsg;
		Account sender;
		String stringTimestamp, password;

		String[] splitedSms = sms.split("\\|");
		if(splitedSms.length != 3){
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
		
		if(sender.getTrys() == NUMBER_OF_UNSUCCESSFULL_LOGIN_TRYS){
			removeAccount(sender);
			return generateUnsuccessfulFeedback("Sender tried to many time to login going to block account.", 0);
		}

		try{
		//Deciphering msg
		decipheredMsg = Crypto.decipherSMS(byteCipheredMsg, this.sharedKey);
		} catch (Exception e){
			return generateUnsuccessfulFeedback("Cipher was corrupted", 0);
		}
		
		//Obtaining time stamp and password
		String[] splitedMsg = decipheredMsg.split("\\|");
		if(splitedMsg.length != 2){
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
			return generateUnsuccessfulFeedback("Signature compromised on loggin SMS received.", 0);
		}
    }
    
	/***
	 * This function is used to process transactions sms' received from the client and
	 * it checks if the message is composed by (mobile|)signature|cipheredText where
	 * signature = {mobile + receiver + amount + counter}Kcs
	 * cipheredText = {receiver|amount|counter}Ks
	 * OR
	 * signature = {mobile + logout + counter}Kcs
	 * cipheredText = {logout|counter}Ks
	 * depending if the message is a logout or transaction
	 * Kcs = client private key
	 * Ks = shared key
	 * @param sms
	 * @return String
	 * @throws Exception
	 */
    public String processTransactionSms(String sms) throws Exception{
		String decipheredMsg, receiver, amount = "", counter;
		Account sender;
		
		String[] splitedSms = sms.split("\\|");
		if(splitedSms.length != 3){
			return generateUnsuccessfulFeedback("Wrong message format.", 0);
		}
		byte[] byteMobile = Crypto.decode(splitedSms[0]);
		byte[] byteSignature = Crypto.decode(splitedSms[1]);
		byte[] byteCipheredMsg = Crypto.decode(splitedSms[2]);
		
		//Getting user in msg
		sender = getAccountByMobile(new String(byteMobile));
		if(sender == null){
			return generateUnsuccessfulFeedback("User not registered.", 0);			
		}
		
		try{
			//Deciphering msg
			decipheredMsg = Crypto.decipherSMS(byteCipheredMsg, this.sharedKey);
		} catch (Exception e){
			return generateUnsuccessfulFeedback("Cipher was corrupted", 0);
		}
		
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
		if(smsCounter < sender.getCounter()) { 
			return generateUnsuccessfulFeedback("Freshness compromised.", 0); 
		}
		else { 
			if(sender.getCounter() < Integer.MAX_VALUE){
				sender.setCounter(smsCounter + 1);
			}
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
			return generateUnsuccessfulFeedback("Signature compromised on loggin SMS received", sender.getCounter());
		}
    }

	/***
	 * This function is used to generate transactions feedback to the client
	 * the message is composed by signature|cipheredText where
	 * signature = {status + counter}Kcs
	 * cipheredText = {status|counter}Ks
	 * Kcs = client private key
	 * Ks = shared key
	 * @param sender
	 * @param receiver
	 * @param amount
	 * @return String
	 * @throws Exception
	 */
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
				return generateUnsuccessfulFeedback("Receiver not registered.", sender.getCounter());
			}
		} catch (AmountToHighException e){
			return generateUnsuccessfulFeedback("Amount to damn high.", sender.getCounter());

		}

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

	/***
	 * This function is used to generate login feedback to the client
	 * the message is composed by signature|cipheredText where
	 * signature = {status + counter}Kcs
	 * cipheredText = {status|counter}Ks
	 * Kcs = client private key
	 * Ks = shared key
	 * @param a
	 * @param password
	 * @param stringTS
	 * @return String
	 * @throws Exception
	 */
	public String generateLoginFeedback(Account a, String password, String stringTS) throws Exception{		

		if(password.equals(a.getPassword()) && Crypto.validTS(stringTS)){
			this.status = SERVER_SUCCESSFUL_LOGIN_MSG;
			a.setTrys(0);
		}
		else{
			return generateUnsuccessfulFeedback("Wrong password or TS compromised.", 0);
		}
		
		//Add user counter to msg
		String feedback = this.status + "|" + a.getCounter();
		byte[] cipheredText = Crypto.cipherSMS(feedback, this.sharedKey);
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
	
	/***
	 * This function is used to generate logout feedback to the client
	 * the message is composed by signature|cipheredText where
	 * signature = {status + counter}Kcs
	 * cipheredText = {status|counter}Ks
	 * Kcs = client private key
	 * Ks = shared key
	 * @param sender
	 * @return String
	 * @throws Exception
	 */
	public String generateLogoutFeedback(Account sender) throws Exception{
		this.status = SERVER_SUCCESSFUL_LOGOUT_MSG;
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
	
	/***
	 * This function is used to generate an unsuccessful feedback when 
	 * an error occurs to the client
	 * the message is composed by signature|cipheredText where
	 * signature = {status + counter}Kcs
	 * cipheredText = {status|counter}Ks
	 * Kcs = client private key
	 * Ks = shared key
	 * @param msg
	 * @param counter
	 * @return String
	 * @throws Exception
	 */
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
	
	/***
	 * This function is used to generate an unsuccessful feedback when 
	 * an error occurs to the client
	 * the message is composed by signature|status where
	 * signature = {status}Kcs
	 * Kcs = client private key
	 * Ks = shared key
	 * @param msg
	 * @param counter
	 * @return String
	 * @throws Exception
	 */
	public String generateUnsuccessfulFeedbackDH(String msg) throws Exception{
		System.out.println(msg);
		this.status = ERROR_MSG;
		//Generating signature
		String dataToSign = this.status;
		byte[] signature = Crypto.sign(dataToSign, keys.getPrivate());
		
		//Concatenate signature with status --> signature|status
		//cipheredText = {status|counter}Ks
		String stringSig = Crypto.encode(signature);
		String toSend = stringSig + "|" + this.status;
		
		this.status = SERVER_BEGGINING;
		
		System.out.println("Size of logout feedback SMS message: " + toSend.length());

		return toSend;
	}
	
	/***
	 * This function sends a query to the database asking 
	 * for an account with the username msg
	 * @param msg
	 * @return String
	 * @throws Exception
	 */
	public Account getAccountByUsername(String msg) throws Exception{
        String iban = "", username = "", password = "", mobile = "";
        int balance = 0, tries = 0, counter = 0;
		// Step 1: Allocate a database "Connection" object
        Connection conn = DriverManager.getConnection(
              "jdbc:mysql://localhost:3306/serverdbsms?useSSL=false", this.mysqlId, this.mysqlPassword); // MySQL

        // Step 2: Allocate a "Statement" object in the Connection
        Statement stmt = conn.createStatement();
        
        // Step 3: Execute a SQL SELECT query, the query result
        String strSelect = "select iban, balance, username, password, mobile, counter, tries from accountsms where username = '" + msg + "'";
        ResultSet rset = stmt.executeQuery(strSelect);

        // Step 4: Process the ResultSet by scrolling the cursor forward via next().
        int rowCount = 0;
        while(rset.next()) {   // Move the cursor to the next row
            iban = rset.getString("iban");
            balance = rset.getInt("balance");
            username = rset.getString("username");
            password = rset.getString("password");
            mobile = rset.getString("mobile");
            tries = rset.getInt("tries");
            counter = rset.getInt("counter");
           ++rowCount;
        }
        
        if(rowCount == 0){
        	return null;
        }
        else{
        	return new Account(iban, balance, username, password, mobile, tries, counter, this);
        }
	}
	
	/***
	 * This function sends a query to the database asking 
	 * for an account with the mobile number msg
	 * @param msg
	 * @return String
	 * @throws Exception
	 */
	public Account getAccountByMobile(String msg) throws Exception{
        String iban = "", username = "", password = "", mobile = "";
        int balance = 0, tries = 0, counter = 0;
		// Step 1: Allocate a database "Connection" object
        Connection conn = DriverManager.getConnection(
              "jdbc:mysql://localhost:3306/serverdbsms?useSSL=false", this.mysqlId, this.mysqlPassword); // MySQL

        // Step 2: Allocate a "Statement" object in the Connection
        Statement stmt = conn.createStatement();
        
        // Step 3: Execute a SQL SELECT query, the query result
        String strSelect = "select iban, balance, username, password, mobile, counter, tries from accountsms where mobile = '" + msg + "'";

        // Step 4: Process the ResultSet by scrolling the cursor forward via next().
        ResultSet rset = stmt.executeQuery(strSelect);
        int rowCount = 0;
        while(rset.next()) {   // Move the cursor to the next row
            iban = rset.getString("iban");
            balance = rset.getInt("balance");
            username = rset.getString("username");
            password = rset.getString("password");
            mobile = rset.getString("mobile");
            tries = rset.getInt("tries");
            counter = rset.getInt("counter");
           ++rowCount;
        }
        if(rowCount == 0){
        	return null;
        }
        else{
        	return new Account(iban, balance, username, password, mobile, tries, counter, this);
        }
	}

	public void generateSecretValue() {
		this.secretValue = Crypto.generateSecretValue();
	}
	
	public String getNonRepudiationMsgForPublicValue() throws Exception {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        
        if(this.status.equals(ERROR_MSG_DH)){
			return generateUnsuccessfulFeedbackDH("This sender was blocked");
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
		System.out.println("Size of Server public value used in DH SMS message: " + Crypto.encode(publicValue.toByteArray()).length());
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
		System.out.println("OLA1");
		if(sender == null){			
			this.status = ERROR_MSG_DH;
			return;			
		}
		//Verify TimeStamp
		if(!Crypto.validTS(stringTS)){
			System.out.println("Time stamp used in DH public value invalid, passed more than 1 minute");
			this.status = ERROR_MSG_DH;
			return;	
		}
		
		//Verify signature
		String msgToVerify = stringSender + publicValue + stringTS;
		if(!Crypto.verifySign(msgToVerify, byteSig, sender.getPubKey())){
			System.out.println("Signature compromised ins DH public value msg");
			this.status = ERROR_MSG_DH;
			return;	
		}
		
		BigInteger sharedKey = publicValue.modPow(secretValue, p);
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
	
	public SecretKeySpec getSharedKey() {
		return sharedKey;
	}

	public void setSharedKey(SecretKeySpec sharedKey) {
		this.sharedKey = sharedKey;
	}
	
	/***
	 * This function sends a query to the database 
	 * to remove an account a
	 * @param a
	 * @return String
	 * @throws Exception
	 */
	public void removeAccount(Account a) throws Exception{
		// Step 1: Allocate a database "Connection" object
		Connection conn = DriverManager.getConnection(
				"jdbc:mysql://localhost:3306/serverdbsms?useSSL=false", this.mysqlId, this.mysqlPassword); // MySQL

		// Step 2: Allocate a "Statement" object in the Connection
		Statement stmt = conn.createStatement();

		// Step 3: Execute a SQL DELETE query, the query result
		String sqlDelete = "delete from accountsms where mobile = '" + a.getMobile() + "'";
		stmt.executeUpdate(sqlDelete);
	}

	public String getMysqlId() {
		return mysqlId;
	}

	public void setMysqlId(String mysqlId) {
		this.mysqlId = mysqlId;
	}

	public String getMysqlPassword() {
		return mysqlPassword;
	}

	public void setMysqlPassword(String mysqlPassword) {
		this.mysqlPassword = mysqlPassword;
	}
}