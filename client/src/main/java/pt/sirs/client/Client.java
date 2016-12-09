package pt.sirs.client;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.sql.Timestamp;

import javax.crypto.spec.SecretKeySpec;

import pt.sirs.crypto.Crypto;

public class Client {
	public static final String SERVER_SUCCESSFUL_LOGIN_MSG = "LoginOk";	
	public static final String SERVER_SUCCESSFUL_LOGOUT_MSG = "LogoutOk";
	public static final String SUCCESSFUL_TRANSACTION_MSG = "TransOk";
	public static final String SUCCESS_FEEDBACK = "PogChamp";
	public static final String ERROR_MSG = "ChamPog";
	public static final String FRESHNESS_ERROR_MSG = "FreshKo";
	private static final String SERVER_PUBLIC_KEY_PATH = "keys/ServerPublicKey";

	
	private int myMoney; 
	private String myUsername;
	private String myPassword;
	private BigInteger p;
	private BigInteger g;
	private BigInteger secretValue;
	private BigInteger publicValue;
	private SecretKeySpec sharedKey;
	private String status;
	private int counter;
	private KeyPair keys;
	private String nonRepudiationString;
	private String mobile;
	
	/***
	 * This is the constructor for the Client this constructor can only 
	 * be called when we have generated a private and a public key for 
	 * the myUsername parameter. User main in crypto to perform this 
	 * action 
	 * @param myUsername
	 * @param myPassword
	 * @param mobile
	 * @throws Exception
	 */
	public Client(String myUsername, String myPassword, String mobile) throws Exception {
		this.myUsername = myUsername;
		this.myPassword = myPassword;
		this.mobile = mobile;
		BigInteger[] pair = Crypto.GeneratePandG();
		p = pair[0];
		g = pair[1];
		this.secretValue = Crypto.generateSecretValue();
		this.status = "Initialized";
		
		PublicKey pubKey = Crypto.readPubKeyFromFile("keys/" + this.myUsername +"PublicKey" );
		PrivateKey privKey = Crypto.readPrivKeyFromFile("keys/" + this.myUsername + "PrivateKey" );
		this.keys = new KeyPair(pubKey, privKey);
	}
	
	/***
	 * This function generates a login message, this message is composed by
	 * (mobile|)signature|cipheredText where 
	 * signature = {mobile + TS + password}Kcs and cipheredText = {TS|pass}Ks
	 * this function should only be called after establishing a shared key
	 * with the server.
	 * This function can be used to login one user into the server.
	 * Kcs = client private key
	 * Ks = shared key
	 * 
	 * @return String
	 * @throws Exception
	 */
	public String generateLoginSms() throws Exception{
		byte[] cipheredText;
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		
		//Generating signature
		String dataToSign = this.mobile +  timestamp.toString() + this.myPassword;
		byte[] signature = Crypto.sign(dataToSign, keys.getPrivate());
		
		//Ciphering text
		String toCipher  = timestamp.toString() + "|" + this.myPassword;
		cipheredText = Crypto.cipherSMS(toCipher, this.sharedKey);	
		
		//Concatenate mobile and signature with cipheredText --> (mobile|)signature|cipheredText
		//cipheredText = {TS|pass}Ks
		String mobile = Crypto.encode(this.mobile.getBytes());
		String stringSig = Crypto.encode(signature);
		String stringCiphertext = Crypto.encode(cipheredText);
		
		//Final message to be sent
		String toSend = mobile + "|" + "L" + "|" + stringSig + "|" + stringCiphertext;
	
		System.out.println("Size of login SMS message: " + ("L" + "|" + stringSig + "|" + stringCiphertext).length());
		return toSend;
	}
	
	/***
	 * This function is called when we want to generate a message of logout
	 * this message is composed by (mobile|)signature|cipheredText where
	 * signature = {mobile + logout + counter}Kcs
	 * cipheredText = {logout|counter}Ks this function should only be called
	 * when we are logged in to the server and have a shared key.
	 * This function can be used to logout one user from the server.
	 * Kcs = client private key
	 * Ks = shared key
	 * 
	 * @return String
	 * @throws Exception
	 */
	public String generateLogoutSms() throws Exception{
		byte[] cipheredText;
		
		//Generating signature
		String dataToSign = this.mobile +  "logout" + this.counter;
		byte[] signature = Crypto.sign(dataToSign, keys.getPrivate());
		
		//Ciphering text
		String toCipher  = "logout" + "|" + this.counter;
		cipheredText = Crypto.cipherSMS(toCipher, this.sharedKey);	
		
		//Concatenate mobile and signature with cipheredText --> (mobile|)signature|cipheredText
		//cipheredText = {logout|counter}Ks
		String mobile = Crypto.encode(this.mobile.getBytes());
		String stringSig = Crypto.encode(signature);
		String stringCiphertext = Crypto.encode(cipheredText);
		
		//Final message to be sent
		String toSend = mobile + "|" + "O" + "|" + stringSig + "|" + stringCiphertext;
		
		System.out.println("Size of logout SMS message: " + (stringSig + "|" + stringCiphertext).length());
		return toSend;
		
	}
	
	/***
	 * This function is called when we want to generate a transaction message
	 * this message is composed by (mobile|)signature|cipheredText where
	 * signature = {mobile + receiver + amount + counter}Kcs
	 * cipheredText = {receiver|amount|counter}Ks this function should only be called
	 * when we are logged in to the server and have a shared key. 
	 * This function can be used to transfer money from one user to other.
	 * Kcs = client private key
	 * Ks = shared key
	 * 
	 * @return String
	 * @throws Exception
	 */
	public String generateTransactionSms(String receiver, String amount) throws Exception{
		byte[] cipheredText;
		String toCipher = receiver + "|" + amount + "|" + this.counter;
		
		//Ciphering Msg
		cipheredText = Crypto.cipherSMS(toCipher, this.sharedKey);		

		//Generating signature
		String dataToSign = this.mobile + receiver + amount + this.counter;
		byte[] signature = Crypto.sign(dataToSign, keys.getPrivate());
	
		//Concatenate mobile and signature with cipheredText --> (mobile|)signature|cipheredText
		//cipheredText = {receiver|amount|counter}Ks
		String mobile = Crypto.encode(this.mobile.getBytes());
		String stringSig = Crypto.encode(signature);
		String stringCiphertext = Crypto.encode(cipheredText);
		
		String toSend = mobile + "|" + "T" + "|" + stringSig + "|" + stringCiphertext;
		
		System.out.println("Size of transaction SMS message: " + (stringSig + "|" + stringCiphertext).length());
		return toSend;
	}
	
	/***
	 * This function is used to process any feedback received from the server
	 * once we have established a shared key.
	 * This function changes the state where the client finds himself
	 * and to update his counter that is used to assure message freshness.
	 * 
	 * @param sms
	 * @param state
	 * @return String
	 * @throws Exception
	 */
	public String processFeedback(String sms, String state) throws Exception{
		String decipheredMsg;	
		
		String[] splitedSms = sms.split("\\|");
		byte[] byteSignature = Crypto.decode(splitedSms[0]);
		byte[] byteCipheredMsg = Crypto.decode(splitedSms[1]);
		
		try{
			//Deciphering Msg
			decipheredMsg = Crypto.decipherSMS(byteCipheredMsg, this.sharedKey);
		} catch (Exception e){
			return ERROR_MSG;
		}
		
		String[] splitedMsg = decipheredMsg.split("\\|");
		
		//Verify Counter
		if(!verifyCounter(state, Integer.parseInt(splitedMsg[1]))){
			System.out.println("Freshness compromised in " + state + " feedback!!");
			return FRESHNESS_ERROR_MSG;
		}
				
		this.counter = Integer.parseInt(splitedMsg[1]);
		this.status = splitedMsg[0];
		
		//Verifying signature
		String msgToVerify = splitedMsg[0] + splitedMsg[1];

		if(Crypto.verifySign(msgToVerify, byteSignature, Crypto.readPubKeyFromFile(SERVER_PUBLIC_KEY_PATH))){		
			
			return splitedMsg[0];

		}
		else{
			System.out.println("Signature compromised in " + state + " feedback!!");
			return ERROR_MSG;
		}
	}
	
	/***
	 * This function is used to verify if the counter received by the client is
	 * valid.
	 * In login and logout counter should be 0.
	 * In transaction the counter should always be bigger than the one the client
	 * currently has.
	 * 
	 * @param state
	 * @param counter
	 * @return
	 */
	private boolean verifyCounter(String state, int counter) {
		if(state.equals("login") || state.equals("logout")){
			if(counter != 0){
				return false;
			}
			else{
				return true;
			}
		}
		else if(state.equals("transaction")){
			if(this.counter >= counter){
				return false;
			}
			else{
				return true;
			}
		}
		return false;
	}
	
	/***
	 * This function when called with "p" generates a prime value for DH that is used a module
	 * if this function is called with "g" generates a prime value with "p" that is used as a 
	 * base in DH in the following manner g^x mod p.
	 * The generation of this values is achieved using a lib in crypto project check it out
	 * for more info.
	 * 
	 * @param value
	 * @return
	 */
	public String generateValueSharingSMS(String value){
		if(value.equals("p")){
			System.out.println("Size of public value p (prime) used in DH SMS message: " +  ("P" + "|" + Crypto.encode(this.p.toByteArray())).length());
			return this.mobile + "|" + "P" + "|" + Crypto.encode(this.p.toByteArray());
		}
		if(value.equals("g")){
			System.out.println("Size of public value g (module) used in DH SMS message: " + ("G" + "|" + Crypto.encode(this.g.toByteArray())).length());
			return this.mobile + "|" + "G" + "|" + Crypto.encode(this.g.toByteArray());
		}
		else{
			System.out.println("Function not used properly pass as argument p or g");
			return null;
		}
	}
	
	/***
	 * This function makes a call to a method in crypto to generate a secret value for the
	 * DH algorithm and then stores that value in a private variable of the client.
	 * 
	 */
	public void generateSecretValue() {
		this.secretValue = Crypto.generateSecretValue();
	}
	
	/***
	 * This function generates a message that grants authenticity in the DH algorithm
	 * this message is sent to the server before sending the public value of the client
	 * the message is compossed by (mobile|)signature|TS where signature = {mobile + publicValue
	 * + TS}Kcs
	 * Kcs = client private key
	 * 
	 * @return
	 * @throws Exception
	 */
	public String getNonRepudiationMsgForPublicValue() throws Exception {
		this.publicValue = g.modPow(secretValue, p);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

		//Generating signature
		String dataToSign = this.mobile + this.publicValue + timestamp.toString();
		byte[] signature = Crypto.sign(dataToSign, keys.getPrivate());

		String stringSig = Crypto.encode(signature);
		System.out.println(stringSig);
		String toSend = this.mobile + "|" + "NP" + "|" + stringSig + "|" + timestamp.toString();
		
		System.out.println("Size of non repudiation msg for public value used in DH SMS message: " + ("NP" + "|" + stringSig + "|" + timestamp.toString()).length());

		return toSend;
	}
	
	/***
	 * This function is only used to get the public value of the client.
	 * 
	 * @return
	 */
	public String generatePublicValue(){
		System.out.println("Size of Client public value used in DH SMS message: " + ("PV" + "|" + Crypto.encode(this.publicValue.toByteArray())).length());
		return this.mobile + "|" + "PV" + "|" + Crypto.encode(this.publicValue.toByteArray());
	}
	
	/***
	 * This function is used to generate a shared key when we receive a 
	 * public value from the server 
	 * 
	 * @param stringPublicValue
	 * @throws Exception
	 */
	public void generateSharedKey(String stringPublicValue) throws Exception{
		byte[] bytePublicValue = Crypto.decode(stringPublicValue);
		BigInteger publicValue = new BigInteger(bytePublicValue);
		
		//Verify publicValue
		String[] splitedSms = this.nonRepudiationString.split("\\|");
		byte[] byteSig = Crypto.decode(splitedSms[0]);
		String stringTS  = splitedSms[1];
		
		if(splitedSms[1].equals(ERROR_MSG)){
			System.out.println("There was a problem establishing shared. This user was blocked.");
			this.status = SERVER_SUCCESSFUL_LOGOUT_MSG;
			return;
		}
		try{
			//Verify TimeStamp
			if(!Crypto.validTS(stringTS)){
				System.out.println("Time stamp used in DH public value invalid, passed more than 1 minute");
				this.status = SERVER_SUCCESSFUL_LOGOUT_MSG;
				return;
			}
		}
		catch (java.text.ParseException e){
			System.out.println("There was a problem establishing shared. Message integrity send violated");
			this.status = SERVER_SUCCESSFUL_LOGOUT_MSG;
			return;
		}
		
		//Verify signature
		String msgToVerify = publicValue + stringTS;
		if(!Crypto.verifySign(msgToVerify, byteSig, Crypto.readPubKeyFromFile(SERVER_PUBLIC_KEY_PATH))){
			System.out.println("Signature compromised in DH public value msg");
			this.status = SERVER_SUCCESSFUL_LOGOUT_MSG;
			return;
		}
		
		BigInteger sharedKey = publicValue.modPow(secretValue, p);
		this.sharedKey = Crypto.generateKeyFromBigInt(sharedKey);
	}
	
	public void receiveNonRepudiationMsgForPublicValue(String readObject) {
		this.nonRepudiationString = readObject;		
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

	public int getCounter() {
		return counter;
	}

	public void setCounter(int counter) {
		this.counter = counter;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

}
	
