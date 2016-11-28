package pt.sirs.client;

import java.math.BigInteger;
import java.util.Arrays;

import javax.crypto.spec.SecretKeySpec;

import pt.sirs.crypto.Crypto;

public class Client {
	public static final String SUCCESS_FEEDBACK = "PogChamp";
	public static final String FAILED_FEEDBACK = "ChamPog";
	
	private int myMoney; 
	private String myUsername;
	private String myPassword;
	private BigInteger p;
	private BigInteger g;
	private BigInteger secretValue;
	private BigInteger publicValue;
	private SecretKeySpec sharedKey;
	private String status;
	
	
	public Client(String myUsername, String myPassword) {
		this.myUsername = myUsername;
		this.myPassword = myPassword;
		BigInteger[] pair = Crypto.GeneratePandG();
		p = pair[0];
		g = pair[1];
		this.secretValue = Crypto.generateSecretValue();
		this.status = "Initialized";
	}
	
	public String generateLoginSms() throws Exception{
		byte[] cipheredText;
		String usernameS = "-" + this.myUsername + "-";
		
		cipheredText = Crypto.cipherSMS(this.myPassword, this.sharedKey);	
		

		//Concatenate IV with username with cipheredText --> IV-username-cipheredText
		byte[] usernameB = usernameS.getBytes();
		byte[] finalMsg = new byte[cipheredText.length + usernameB.length];
		System.arraycopy(usernameB, 0, finalMsg, 0, usernameB.length);
		System.arraycopy(cipheredText, 0, finalMsg, usernameB.length, cipheredText.length);
		
		return Crypto.encode(finalMsg);
		
	}
	
	public String generateTransactionSms(String iban, String amount) throws Exception{
		byte[] cipheredText;
		String usernameS = "-" + this.myUsername + "-";
		String msgToCipher = iban + "-" + amount;
		
		cipheredText = Crypto.cipherSMS(msgToCipher, this.sharedKey);		

		//Concatenate IV with username with cipheredText --> IV-username-cipheredText
		byte[] usernameB = usernameS.getBytes();
		byte[] finalMsg = new byte[cipheredText.length + usernameB.length];
		System.arraycopy(usernameB, 0, finalMsg, 0, usernameB.length);
		System.arraycopy(cipheredText, 0, finalMsg, usernameB.length, cipheredText.length);
		
		return Crypto.encode(finalMsg);
		
	}
	
	public String processLoginFeedback(String cipheredSms) throws Exception{
		byte[] msg;
		String decipheredSms;
		
		byte[] decodedCipheredSms =  Crypto.decode(cipheredSms);
		
		msg = Arrays.copyOfRange(decodedCipheredSms, 0, decodedCipheredSms.length);
		
		decipheredSms = Crypto.decipherSMS(msg, this.sharedKey);
		
		this.status = decipheredSms;
		
		return decipheredSms;
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
	
