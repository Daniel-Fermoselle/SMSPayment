package pt.sirs.client;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.sql.Timestamp;
import java.util.Arrays;

import javax.crypto.spec.SecretKeySpec;

import pt.sirs.crypto.Crypto;

public class Client {
	public static final String SUCCESS_FEEDBACK = "PogChamp";
	public static final String FAILED_FEEDBACK = "ChamPog";
	private static final String SERVER_PUBLIC_KEY_PATH = "keys/PublicKeyServer";
	
	private int myMoney; 
	private String myUsername;
	private String myPassword;
	private BigInteger p;
	private BigInteger g;
	private BigInteger secretValue;
	private BigInteger publicValue;
	private SecretKeySpec sharedKey;
	private String status;
	//TODO this is only 32bit can be changed to long
	private int counter;
	private KeyPair keys;
	
	
	public Client(String myUsername, String myPassword) throws Exception {
		this.myUsername = myUsername;
		this.myPassword = myPassword;
		BigInteger[] pair = Crypto.GeneratePandG();
		p = pair[0];
		g = pair[1];
		this.secretValue = Crypto.generateSecretValue();
		this.status = "Initialized";
		
		PublicKey pubKey = Crypto.readPubKeyFromFile("keys/" + "PublicKey" + this.myUsername);
		PrivateKey privKey = Crypto.readPrivKeyFromFile("keys/" + "PrivateKey" + this.myUsername);
		this.keys = new KeyPair(pubKey, privKey);
	}
	
	public String generateLoginSms() throws Exception{
		byte[] cipheredText;
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
		
		//Generating signature
		String dataToSign = this.myUsername +  timestamp.toString() + this.myPassword;
		byte[] signature = Crypto.sign(dataToSign, keys.getPrivate());
		
		//Ciphering text
		String toCipher  = timestamp.toString() + "|" + this.myPassword;
		cipheredText = Crypto.cipherSMS(toCipher, this.sharedKey);	
		
		//Concatenate username and signature with cipheredText --> (username|)signature|cipheredText
		//cipheredText = {TS|pass}Ks
		String user = Crypto.encode(this.myUsername.getBytes());
		String stringSig = Crypto.encode(signature);
		String stringCiphertext = Crypto.encode(cipheredText);
		
		String toSend = user + "|" + stringSig + "|" + stringCiphertext;
		
		System.out.println("Size of login SMS message: " + (stringSig + "|" + stringCiphertext).length());
		return toSend;
	}
	
	public String generateLogoutSms() throws Exception{
		byte[] cipheredText;
		String usernameS = this.myUsername + "-";
		String msgToCipher = "logout" + "-" + this.counter;

		cipheredText = Crypto.cipherSMS(msgToCipher, this.sharedKey);			

		//Concatenate username with cipheredText --> username-cipheredText
		byte[] usernameB = usernameS.getBytes();
		byte[] finalMsg = new byte[cipheredText.length + usernameB.length];
		System.arraycopy(usernameB, 0, finalMsg, 0, usernameB.length);
		System.arraycopy(cipheredText, 0, finalMsg, usernameB.length, cipheredText.length);
		
		return Crypto.encode(finalMsg);
		
	}
	
	public String generateTransactionSms(String receiver, String amount) throws Exception{
		byte[] cipheredText;
		String toCipher = receiver + "|" + amount + "|" + this.counter;
		
		//Ciphering Msg
		cipheredText = Crypto.cipherSMS(toCipher, this.sharedKey);		

		//Generating signature
		String dataToSign = this.myUsername + receiver + amount + this.counter;
		byte[] signature = Crypto.sign(dataToSign, keys.getPrivate());
	
		//Concatenate username and signature with cipheredText --> (username|)signature|cipheredText
		//cipheredText = {receiver|amount|counter}Ks
		String user = Crypto.encode(this.myUsername.getBytes());
		String stringSig = Crypto.encode(signature);
		String stringCiphertext = Crypto.encode(cipheredText);
		
		String toSend = user + "|" + stringSig + "|" + stringCiphertext;
				
		System.out.println("Size of transaction SMS message: " + (stringSig + "|" + stringCiphertext).length());
		return toSend;
	}
	
	//TODO In future make this return bool/void
	public String processFeedback(String sms, String state) throws Exception{
		String decipheredMsg;
		
		String[] splitedSms = sms.split("\\|");
		byte[] byteSignature = Crypto.decode(splitedSms[0]);
		byte[] byteCipheredMsg = Crypto.decode(splitedSms[1]);
		
		//Deciphering Msg
		decipheredMsg = Crypto.decipherSMS(byteCipheredMsg, this.sharedKey);
		
		String[] splitedMsg = decipheredMsg.split("\\|");
		
		//Verify Counter
		if(!verifyCounter(state, Integer.parseInt(splitedMsg[1]))){
			//TODO generate error msg
			return "ChampPog";
		}
		
		this.counter = Integer.parseInt(splitedMsg[1]);
		this.status = splitedMsg[0];
		
		//Verifying signature
		String msgToVerify = splitedMsg[0] + splitedMsg[1];

		if(Crypto.verifySign(msgToVerify, byteSignature, Crypto.readPubKeyFromFile(SERVER_PUBLIC_KEY_PATH))){		
			return splitedMsg[0];

		}
		else{
			//TODO Generate error signature compromised
			System.out.println("Signature compromised in login feed back!!");
			return "ChampPog";
		}
	}
	
	public String processLogoutFeedback(String cipheredSms) throws Exception{
		byte[] msg;
		String decipheredSms;
		
		byte[] decodedCipheredSms =  Crypto.decode(cipheredSms);
		
		msg = Arrays.copyOfRange(decodedCipheredSms, 0, decodedCipheredSms.length);
		
		decipheredSms = Crypto.decipherSMS(msg, this.sharedKey);
		
		this.status = decipheredSms;
		
		return decipheredSms;
	}
	
	private boolean verifyCounter(String state, int counter) {
		if(state.equals("login") || state.equals("logout")){
			if(counter != 0){
				System.out.println("Freshness of log operation feedback compromised");
				return false;
			}
			else{
				return true;
			}
		}
		else if(state.equals("transaction")){
			if(this.counter >= counter){
				System.out.println("Freshness of transaction feedback compromised");
				return false;
			}
			else{
				return true;
			}
		}
		return false;
	}
	
	public String generateValueSharingSMS(String value){
		if(value.equals("p")){
			System.out.println(Crypto.encode(this.p.toByteArray()) + "  LENG P: " + Crypto.encode(this.p.toByteArray()).length());
			return Crypto.encode(this.p.toByteArray());
		}
		if(value.equals("g")){
			System.out.println(Crypto.encode(this.g.toByteArray()) + "  LENG G: " + Crypto.encode(this.g.toByteArray()).length());
			return Crypto.encode(this.g.toByteArray());
		}
		else
			//TODO throw invalid char exception
			return null;
	}
	
	public void generateSecretValue() {
		this.secretValue = Crypto.generateSecretValue();
	}
	
	public String generatePublicValue(){
		publicValue = g.modPow(secretValue, p);
		System.out.println(Crypto.encode(publicValue.toByteArray()) + "  LENG PublicValue: " + Crypto.encode(publicValue.toByteArray()).length());
		return Crypto.encode(publicValue.toByteArray());
	}
	
	public void generateSharedKey(String stringPublicValue) throws Exception{
		byte[] bytePublicValue = Crypto.decode(stringPublicValue);
		BigInteger publicValue = new BigInteger(bytePublicValue);
		BigInteger sharedKey = publicValue.modPow(secretValue, p);
		System.out.println(Crypto.encode(sharedKey.toByteArray()) + "  LENG SharedKey: " + Crypto.encode(sharedKey.toByteArray()).length());
		this.sharedKey = Crypto.generateKeyFromBigInt(sharedKey);
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

}
	
