package pt.sirs.server;

import java.security.PublicKey;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import pt.sirs.crypto.Crypto;
import pt.sirs.server.Exceptions.AmountToHighException;
import pt.sirs.server.Exceptions.InvalidPasswordException;
import pt.sirs.server.Exceptions.InvalidUsernameException;

public class Account{
	
	private String iban;
	private int balance;
	private String username;
	private String password;
	private int counter;
	private PublicKey pubKey;
	private String mobile;
	private int trys;
	private Server server;

	public Account(String iban, int balance, String username, String password, String mobile, int tries, int counter, Server server) throws Exception{
		if(password.length() < 4 || password.length() > 7)
		{ throw new InvalidPasswordException(password); }
		if(username.length() > 10)
		{ throw new InvalidUsernameException(username); }
		this.iban = iban;
		this.balance = balance;
		this.username = username;
		this.password = password;
		this.counter = counter;
		this.setMobile(mobile);
		this.pubKey = Crypto.readPubKeyFromFile("keys/" + username + "PublicKey" );
		this.trys = tries;
		this.server = server;
	}
	
	public void debit(int amount) throws Exception{
		if(this.balance < amount){
			Integer amountS = amount;
			throw new AmountToHighException(amountS.toString());
		}
		setBalance(this.balance - amount);
	}

	public void credit(int amount) throws Exception{
		setBalance(this.balance + amount);
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
	
	public void setBalance(int balance) throws Exception{
		this.balance = balance;

		// Step 1: Allocate a database "Connection" object
		Connection conn = DriverManager.getConnection(
				"jdbc:mysql://localhost:3306/serverdbsms?useSSL=false", server.getMysqlId(), server.getMysqlPassword()); // MySQL

		// Step 2: Allocate a "Statement" object in the Connection
		Statement stmt = conn.createStatement();

		// Step 3 & 4: Execute a SQL UPDATE via executeUpdate()
		String strUpdate = "update accountsms set balance = " + balance + " where mobile = '" + this.mobile + "'";
		stmt.executeUpdate(strUpdate);
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

	public int getCounter() {
		return counter;
	}

	public void setCounter(int counter) throws Exception{
		this.counter = counter;
		// Step 1: Allocate a database "Connection" object
		Connection conn = DriverManager.getConnection(
				"jdbc:mysql://localhost:3306/serverdbsms?useSSL=false", server.getMysqlId(), server.getMysqlPassword()); // MySQL

		// Step 2: Allocate a "Statement" object in the Connection
		Statement stmt = conn.createStatement();

		// Step 3 & 4: Execute a SQL UPDATE via executeUpdate()
		String strUpdate = "update accountsms set counter = " + counter + " where mobile = '" + this.mobile + "'";
		stmt.executeUpdate(strUpdate);
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

	public int getTrys() {
		return this.trys;
	}

	public void setTrys(int trys) throws Exception{
		this.trys = trys;
		
		// Step 1: Allocate a database "Connection" object
        Connection conn = DriverManager.getConnection(
              "jdbc:mysql://localhost:3306/serverdbsms?useSSL=false", server.getMysqlId(), server.getMysqlPassword()); // MySQL

        // Step 2: Allocate a "Statement" object in the Connection
        Statement stmt = conn.createStatement();
      
        // Step 3 & 4: Execute a SQL UPDATE via executeUpdate()
        String strUpdate = "update accountsms set tries = " + trys + " where mobile = '" + this.mobile + "'";
        stmt.executeUpdate(strUpdate);
	}

	public Server getServer() {
		return server;
	}

	public void setServer(Server server) {
		this.server = server;
	}

}