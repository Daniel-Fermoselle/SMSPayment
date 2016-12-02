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
		this.trys = 0;
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
		
        Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/serverdbsms?useSSL=false", Server.MYSQL_ID, Server.MYSQL_PASSWORD); // MySQL

          // Step 2: Allocate a "Statement" object in the Connection
          Statement stmt = conn.createStatement();
        
          // Step 3 & 4: Execute a SQL UPDATE via executeUpdate()
          //   which returns an int indicating the number of rows affected.
          // Increase the price by 7% and qty by 1 for id=1001
          String strUpdate = "update accountsms set balance = " + balance + " where mobile = '" + this.mobile + "'";
          System.out.println("The SQL query is: " + strUpdate);  // Echo for debugging
          int countUpdated = stmt.executeUpdate(strUpdate);
          System.out.println(countUpdated + " records affected.");
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
		
        Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/serverdbsms?useSSL=false", Server.MYSQL_ID, Server.MYSQL_PASSWORD); // MySQL

          // Step 2: Allocate a "Statement" object in the Connection
          Statement stmt = conn.createStatement();
        
          // Step 3 & 4: Execute a SQL UPDATE via executeUpdate()
          //   which returns an int indicating the number of rows affected.
          // Increase the price by 7% and qty by 1 for id=1001
          String strUpdate = "update accountsms set counter = " + counter + " where mobile = '" + this.mobile + "'";
          System.out.println("The SQL query is: " + strUpdate);  // Echo for debugging
          int countUpdated = stmt.executeUpdate(strUpdate);
          System.out.println(countUpdated + " records affected.");
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
		return trys;
	}

	public void setTrys(int trys) throws Exception{
		this.trys = trys;
		

        Connection conn = DriverManager.getConnection(
              "jdbc:mysql://localhost:3306/serverdbsms?useSSL=false", Server.MYSQL_ID, Server.MYSQL_PASSWORD); // MySQL

        // Step 2: Allocate a "Statement" object in the Connection
        Statement stmt = conn.createStatement();
      
        // Step 3 & 4: Execute a SQL UPDATE via executeUpdate()
        //   which returns an int indicating the number of rows affected.
        // Increase the price by 7% and qty by 1 for id=1001
        String strUpdate = "update accountsms set tries = " + trys + " where mobile = '" + this.mobile + "'";
        System.out.println("The SQL query is: " + strUpdate);  // Echo for debugging
        int countUpdated = stmt.executeUpdate(strUpdate);
        System.out.println(countUpdated + " records affected.");
	}

}