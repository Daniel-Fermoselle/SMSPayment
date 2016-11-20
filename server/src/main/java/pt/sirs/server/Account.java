package pt.sirs.server;

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
	
	

}