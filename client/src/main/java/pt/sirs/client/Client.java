package pt.sirs.client;

import pt.sirs.smsPacket.SmsPacket;
import java.security.Key;


import pt.sirs.crypto.Crypto;
import pt.sirs.smsPacket.Exceptions.InvalidSMSPacketValuesException;

public class Client {
	public static final String KEYSTORE_LOCATION = "keys/aes-keystore.jck";
	public static final String KEYSTORE_PASS = "mypass";
	public static final String ALIAS = "aes";
	public static final String KEY_PASS = "mypass";
	
	private String myIban;
	private int myMoney; 
	
	public Client(String myIban, int myMoney) {
		this.myIban  = myIban;
		this.myMoney = myMoney;
	}
	
	public SmsPacket getSmsPacket(String OtherIban, String amount) throws InvalidSMSPacketValuesException{
		SmsPacket sms;
		
		sms = new SmsPacket(myIban,OtherIban,amount);		
		return sms;
	}

	public String getToSend(SmsPacket sms) throws Exception {
		String cipherText;
		Key sharedKey;
		Crypto.GenerateKey();
		sharedKey = Crypto.getKeyFromKeyStore(KEYSTORE_LOCATION, KEYSTORE_PASS, ALIAS, KEY_PASS);
		cipherText = Crypto.cipherSMS(sms.getConcatSmsFields(), sharedKey);
		return cipherText;

	}
	
	public void setMyIban(String myIban){
		this.myIban = myIban;
	}
	
	public String getMyIban(){
		return myIban;
	}
	
	public void setMyMoney(int myMoney){
		this.myMoney = myMoney;
	}
	
	public int getMyMoney(){
		return myMoney;
	}
}
	
