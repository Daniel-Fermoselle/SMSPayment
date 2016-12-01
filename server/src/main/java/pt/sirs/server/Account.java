package pt.sirs.server;

import java.security.PublicKey;

import javax.crypto.spec.SecretKeySpec;

import pt.sirs.crypto.Crypto;
import pt.sirs.server.Exceptions.AmountToHighException;
import pt.sirs.server.Exceptions.InvalidPasswordException;
import pt.sirs.server.Exceptions.InvalidUsernameException;

public class Account{
	
	private String iban;
	private int balance;
	private String username;
	private String password;
	private SecretKeySpec sharedKey;
	private int counter;
	private PublicKey pubKey;
	private String mobile;

	public Account(String iban, int balance, String username, String password, String mobile) throws Exception{
		if(password.length()<4 || password.length()>8)
		{ throw new InvalidPasswordException(password); }
		if(username.length()>10)
		{ throw new InvalidUsernameException(username); }
		this.iban = iban;
		this.balance = balance;
		this.username = username;
		this.password = password;
		this.counter = 0;
		this.setMobile(mobile);
		this.pubKey = Crypto.readPubKeyFromFile("keys/" + username + "PublicKey" );
	}
	
	public void debit(int amount){
		if(this.balance < amount){
			Integer amountS = amount;
			throw new AmountToHighException(amountS.toString());
		}
		this.balance = this.balance - amount;
	}

	public void credit(int amount){
		this.balance = this.balance + amount;
	}
	
	public String getIban() {
		return iban;
	}
	
	public void setIban(String iban) {
		this.iban = iban;
	}
	
	public int getBalance() {
		return balance;
	}
	
	public void setBalance(int balance) {
		this.balance = balance;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
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

	public PublicKey getPubKey() {
		return pubKey;
	}

	public void setPubKey(PublicKey pubKey) {
		this.pubKey = pubKey;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

}