package pt.sirs.server;

import pt.sirs.server.Exceptions.AmountToHighException;
import pt.sirs.server.Exceptions.InvalidPasswordException;
import pt.sirs.server.Exceptions.InvalidUsernameException;

public class Account{
	
	private String iban;
	private int balance;
	private String username;
	private String password;

	public Account(String iban, int balance, String username, String password){
		if(password.length()<4 && password.length()>15)
		{ throw new InvalidPasswordException(password); }
		if(username.length()>10)
		{ throw new InvalidUsernameException(username); }
		this.iban = iban;
		this.balance = balance;
		this.username = username;
		this.password = password;
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

}