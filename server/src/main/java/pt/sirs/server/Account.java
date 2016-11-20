package pt.sirs.server;

import pt.sirs.server.Exceptions.AmountToHighException;

public class Account{
	
	private String iban;
	private int balance;

	public Account(String iban, int balance){
		this.setIban(iban);
		this.setBalance(balance);
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

}