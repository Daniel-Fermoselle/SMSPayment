package pt.sirs.server;

import java.math.BigInteger;
import java.security.KeyPair;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;

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

	private BigInteger secretValue;
	private BigInteger publicValue;
	private String status;
	private KeyPair keys;
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
	 * @param splittedMsg2 
	 * @param splittedMsg 
	 * @return String
	 * @throws Exception
	 */
    public String processLoginSms(String senderString, String stringSig, String stringCiphered) throws Exception{
		String decipheredMsg;
		Account sender;
		String stringTimestamp, password;

		byte[] byteMobile = Crypto.decode(senderString);
		byte[] byteSignature = Crypto.decode(stringSig);
		byte[] byteCipheredMsg = Crypto.decode(stringCiphered);

		//Getting user in msg
		sender = getAccountByMobile(new String(byteMobile));
		if(sender == null){
			return generateUnsuccessfulFeedbackOffSession("Sender mobile number unknown", 0);
		}
		sender.setCounter(0);
		sender.setTrys(sender.getTrys() + 1);
		
		if(sender.getTrys() == NUMBER_OF_UNSUCCESSFULL_LOGIN_TRYS){
			removeAccount(sender);
			return generateUnsuccessfulFeedbackOnSession(sender, "Sender tried to many time to login going to block account.", 0);
		}

		try{
		//Deciphering msg
		decipheredMsg = Crypto.decipherSMS(byteCipheredMsg, sender.getSharedKey());
		} catch (Exception e){
			return generateUnsuccessfulFeedbackOnSession(sender, "Cipher was corrupted in loggin message", 0);
		}
		
		//Obtaining time stamp and password
		String[] splitedMsg = decipheredMsg.split("\\|");
		if(splitedMsg.length != 2){
			return generateUnsuccessfulFeedbackOnSession(sender, "Deciphered content in loggin sms not well formated.", 0);
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
			return generateUnsuccessfulFeedbackOnSession(sender, "Signature compromised on loggin SMS received.", 0);
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
	 * @param splittedMsg2 
	 * @param splittedMsg 
	 * @return String
	 * @throws Exception
	 */
    public String processTransactionSms(String senderString, String stringSig, String stringCiphered) throws Exception{
		String decipheredMsg, receiver, amount = "", counter;
		Account sender;
		
		byte[] byteMobile = Crypto.decode(senderString);
		byte[] byteSignature = Crypto.decode(stringSig);
		byte[] byteCipheredMsg = Crypto.decode(stringCiphered);
		
		//Getting user in msg
		sender = getAccountByMobile(new String(byteMobile));
		if(sender == null){
			return generateUnsuccessfulFeedbackOffSession("User not registered in transaction.", 0);			
		}
		
		try{
			//Deciphering msg
			decipheredMsg = Crypto.decipherSMS(byteCipheredMsg, sender.getSharedKey());
		} catch (Exception e){
			return generateUnsuccessfulFeedbackOnSession(sender, "Cipher in transaction sms was corrupted", 0);
		}
		
		//Obtaining receiver amount and counter
		String[] splitedMsg = decipheredMsg.split("\\|");
		if(splitedMsg.length == 3){
			receiver = splitedMsg[0];
			amount   = splitedMsg[1];
			counter  = splitedMsg[2];
		}
		else{
			return generateUnsuccessfulFeedbackOnSession(sender, "Deciphered content in transaction sms not well formated.", 0);
		}
		
		//Verify user counter
		int smsCounter = Integer.parseInt(counter);
		if(smsCounter < sender.getCounter()) { 
			return generateUnsuccessfulFeedbackOnSession(sender, "Freshness compromised.", 0); 
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
		String msgToVerify = sender.getMobile() + receiver + amount + counter;
   
		if(Crypto.verifySign(msgToVerify, byteSignature, sender.getPubKey())){				
			
			return generateTransactionFeedback(sender, receiver, amount);
		}
		else{
			return generateUnsuccessfulFeedbackOnSession(sender, "Signature compromised on loggin SMS received", sender.getCounter());
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
				return generateUnsuccessfulFeedbackOnSession(sender, "Receiver not registered.", sender.getCounter());
			}
		} catch (AmountToHighException e){
			return generateUnsuccessfulFeedbackOnSession(sender, "Amount to damn high.", sender.getCounter());

		}

		//Msg to cipher
		String toCipher = this.status + "|" + sender.getCounter();
		byte[] cipheredText = Crypto.cipherSMS(toCipher, sender.getSharedKey());
		
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
			return generateUnsuccessfulFeedbackOnSession(a, "Wrong password or TS compromised.", 0);
		}
		
		//Add user counter to msg
		String feedback = this.status + "|" + a.getCounter();
		byte[] cipheredText = Crypto.cipherSMS(feedback, a.getSharedKey());
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
	public String generateUnsuccessfulFeedbackOnSession(Account sender, String msg,int counter) throws Exception{
		System.out.println(msg);
		this.status = ERROR_MSG;
		
		//Msg to cipher
		String toCipher = this.status + "|" + counter;
		byte[] cipheredText = Crypto.cipherSMS(toCipher, sender.getSharedKey());
		
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
	public String generateUnsuccessfulFeedbackOffSession(String msg,int counter) throws Exception{
		System.out.println(msg);
		this.status = ERROR_MSG;
		
		//Msg to cipher
		String toCipher = this.status + "|" + counter;
		
		//Generating signature
		String dataToSign = this.status + counter;
		byte[] signature = Crypto.sign(dataToSign, keys.getPrivate());
		
		//Concatenate signature with cipheredText --> signature|cipheredText
		//cipheredText = {status|counter}Ks
		String stringSig = Crypto.encode(signature);
		String toSend = stringSig + "|" + toCipher;
		
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
        String iban = "", username = "", password = "", mobile = "", p = "", g = "", np = "", sharedKey = "";
        int balance = 0, tries = 0, counter = 0;
		// Step 1: Allocate a database "Connection" object
        Connection conn = DriverManager.getConnection(
              "jdbc:mysql://localhost:3306/serverdbsms?useSSL=false", this.mysqlId, this.mysqlPassword); // MySQL

        // Step 2: Allocate a "Statement" object in the Connection
        Statement stmt = conn.createStatement();
        
        // Step 3: Execute a SQL SELECT query, the query result
        String strSelect = "select iban, balance, username, password, mobile, counter, tries, p, g, np, sharedKey from accountsms where username = '" + msg + "'";
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
            p = rset.getString("p");
            g = rset.getString("g");
            np = rset.getString("np");
            sharedKey = rset.getString("sharedKey");

           ++rowCount;
        }
        
        if(rowCount == 0){
        	return null;
        }
        else{
        	return new Account(iban, balance, username, password, mobile, tries, counter, p, g, np, sharedKey, this);
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
        String iban = "", username = "", password = "", mobile = "", p = "", g = "", np = "", sharedKey = "";
        int balance = 0, tries = 0, counter = 0;
		// Step 1: Allocate a database "Connection" object
        Connection conn = DriverManager.getConnection(
              "jdbc:mysql://localhost:3306/serverdbsms?useSSL=false", this.mysqlId, this.mysqlPassword); // MySQL

        // Step 2: Allocate a "Statement" object in the Connection
        Statement stmt = conn.createStatement();
        
        // Step 3: Execute a SQL SELECT query, the query result
        String strSelect = "select iban, balance, username, password, mobile, counter, tries, p, g, np, sharedKey from accountsms where mobile = '" + msg + "'";

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
            p = rset.getString("p");
            g = rset.getString("g");
            np = rset.getString("np");
            sharedKey = rset.getString("sharedKey");
           ++rowCount;
        }
        if(rowCount == 0){
        	return null;
        }
        else{
        	return new Account(iban, balance, username, password, mobile, tries, counter, p, g, np, sharedKey, this);
        }
	}
	

	public void generateSecretValue() {
		this.secretValue = Crypto.generateSecretValue();
	}
	
	public void generatePublicValue(Account sender){
	   publicValue = sender.getG().modPow(secretValue, sender.getP());
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

	
	public void savePforClient(String senderString, String p) throws Exception {
		Account sender;
		
		byte[] byteMobile = Crypto.decode(senderString);

		//Getting user in msg
		sender = getAccountByMobile(new String(byteMobile));
		if(sender == null){
			System.out.println("Client not registered");
		}
		
		sender.setP(p);
	}
	

	public void saveGforClient(String senderString, String g) throws Exception {
		Account sender;
		
		byte[] byteMobile = Crypto.decode(senderString);

		//Getting user in msg
		sender = getAccountByMobile(new String(byteMobile));
		if(sender == null){
			System.out.println("Client not registered");
		}
		
		sender.setG(g);		
	}
	

	public void saveNPforClient(String senderString, String stringSig, String TS) throws Exception {
		Account sender;
		
		byte[] byteMobile = Crypto.decode(senderString);

		//Getting user in msg
		sender = getAccountByMobile(new String(byteMobile));
		if(sender == null){
			System.out.println("Client not registered");
		}
		
		sender.setNP(senderString + "|" + stringSig + "|" + TS);				
	}
	

	public void savePVforClient(String senderString, String stringPublicValueSender) throws Exception{
		Account sender;
		
		byte[] bytePublicValue = Crypto.decode(stringPublicValueSender);
		BigInteger publicValue = new BigInteger(bytePublicValue);
		
		//Get sender
		byte[] byteMobile = Crypto.decode(senderString);

		//Getting user in msg
		sender = getAccountByMobile(new String(byteMobile));
		if(sender == null){
			System.out.println("Client not registered");
		}
		
		//Get nonRepudiationString from client
		String nonRepudiationString = sender.getnonRepudiationString();
		
		//Verify publicValue
		String[] splitedSms = nonRepudiationString.split("\\|");
		String stringSender = splitedSms[0];
		byte[] byteSig = Crypto.decode(splitedSms[1]);
		String stringTS  = splitedSms[2];

		//Verify TimeStamp
		if(!Crypto.validTS(stringTS)){
			System.out.println("Time stamp used in DH public value invalid, passed more than 1 minute");
			return;	
		}
		
		//Verify signature
		String msgToVerify = stringSender + publicValue + stringTS;
		if(!Crypto.verifySign(msgToVerify, byteSig, sender.getPubKey())){
			System.out.println("Signature compromised in DH public value msg");
			return;	
		}
		
		//Generate secret value
		generateSecretValue();
		generatePublicValue(sender);
		
		//Generate SharedKey
		BigInteger sharedKey = publicValue.modPow(secretValue, sender.getP());
		sender.setSharedKey(Crypto.generateKeyFromBigInt(sharedKey));
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
	

	public String getPublicValueForClient() {
		System.out.println("Size of Server public value used in DH SMS message: " + Crypto.encode(publicValue.toByteArray()).length());
		return Crypto.encode(publicValue.toByteArray());
	}

	public String processLogoutSms(String senderString, String stringSig, String stringCiphered) throws Exception {
		String decipheredMsg, operation, counter;
		Account sender;
		
		byte[] byteMobile = Crypto.decode(senderString);
		byte[] byteSignature = Crypto.decode(stringSig);
		byte[] byteCipheredMsg = Crypto.decode(stringCiphered);
		
		//Getting user in msg
		sender = getAccountByMobile(new String(byteMobile));
		if(sender == null){
			return generateUnsuccessfulFeedbackOffSession("User not registered.", 0);			
		}
		
		try{
			//Deciphering msg
			decipheredMsg = Crypto.decipherSMS(byteCipheredMsg, sender.getSharedKey());
		} catch (Exception e){
			return generateUnsuccessfulFeedbackOnSession(sender, "Cipher was corrupted", 0);
		}
		
		//Obtaining time stamp and password
		String[] splitedMsg = decipheredMsg.split("\\|");
		if(splitedMsg.length == 2){
			operation = splitedMsg[0];
			counter  = splitedMsg[1];
		}	
		else{
			return generateUnsuccessfulFeedbackOnSession(sender, "Deciphered content not well formated.", 0);
		}
		
		//Verify user counter
		int smsCounter = Integer.parseInt(counter);
		if(smsCounter < sender.getCounter()) { 
			return generateUnsuccessfulFeedbackOnSession(sender, "Freshness compromised.", 0); 
		}
		
		//Verify signature 
		String msgToVerify;
		msgToVerify = sender.getMobile() + operation + counter;
	
		if(Crypto.verifySign(msgToVerify, byteSignature, sender.getPubKey())){				
			//Check if it's a logout message
			if(operation.equals("logout")){
				return generateLogoutFeedback(sender); 
			}
			else{
				return generateUnsuccessfulFeedbackOnSession(sender, "Logout message but operation in message didn't match.", 0); 
			}
			
		}
		else{
			return generateUnsuccessfulFeedbackOnSession(sender, "Signature compromised on logout SMS received", sender.getCounter());
		}
	}
}