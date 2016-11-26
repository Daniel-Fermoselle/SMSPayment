package pt.sirs.smsPacket;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;

import pt.sirs.smsPacket.Exceptions.InvalidSMSPacketValuesException;


public class SmsPacket implements  Serializable{
	private static final long serialVersionUID = 1L;
	private String myIban;
	private String otherIban;
	private String amount;
	private String signature;
	
	public SmsPacket(String mib, String oib, String am) throws InvalidSMSPacketValuesException{
		if(mib.length()==25 && oib.length()==25 && am.length()<=8){
			myIban = mib;
			otherIban = oib;
			amount = am;			
		}
		else{
			throw new InvalidSMSPacketValuesException(mib,oib,am);
		}
	}
	
	
	public String getConcatSmsFields(){
		String message;
		message = myIban + "-" + otherIban + "-" + amount;
		return message;
	}
	
	public void setSignature(PrivateKey key){
		String elements=myIban.concat(otherIban).concat(amount);
		try {
			System.out.println("elements: " + elements.length());//DEBUG
			/*MessageDigest md = MessageDigest.getInstance("SHA-256");

			md.update(elements.getBytes()); // Change this to "UTF-16" if needed
			byte[] digest = md.digest();
			System.out.println("digest: " + digest.length);//DEBUG*/
			Signature sig = Signature.getInstance("SHA256withRSA"); //tem de ser 256 porque e o suportado pelo java
			sig.initSign(key);
			sig.update(elements.getBytes());
			byte[] signBytes = sig.sign();
			System.out.println("signBytes: " + signBytes.length);//DEBUG
    		signature = printBase64Binary(signBytes);
    		System.out.println("signature: " + signature.length());//DEBUG
			
		}
		//TODO REVER TODOS OS CATCH	
		catch (SignatureException e) {
			System.out.println("Error while creating the signature");
			e.printStackTrace();
		}
		
		catch (NoSuchAlgorithmException e) {
			System.out.println("Error while creating the signature");
			e.printStackTrace();
		} 
		
		catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean verifySign(String toVerify,PublicKey key){
		System.out.println(this.toString());//DEBUGING
		String elements=myIban.concat(otherIban).concat(amount);
		String digestToCheck="";
		try {
			
			Signature sig = Signature.getInstance("SHA256withRSA"); //tem de ser 256 porque e o suportado pelo java
			sig.initSign((PrivateKey) key);
			sig.update(elements.getBytes());
			byte[] signature = sig.sign();
			digestToCheck = printBase64Binary(signature);
			if(toVerify.equals(digestToCheck)){
				return true;
			}
			return false;
		}
		//TODO REVER TODOS OS CATCH	
		catch (SignatureException e) {
			System.out.println("Error while creating the signature");
			e.printStackTrace();
			return false;//CUIDADO
		}
		
		catch (NoSuchAlgorithmException e) {
			System.out.println("Error while creating the signature");
			e.printStackTrace();
			return false;//CUIDADO
		} 
		
		catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;//CUIDADO
		}
		
	}
	
	public void setMyIban(String myIban){
		this.myIban = myIban;
	}
	public String getMyIban(){
		return myIban;
	}
	public void setOtherIban(String otherIban){
		this.otherIban = otherIban;
	}
	public String getOtherIban(){
		return otherIban;
	}
	
	public void setAmount(String amount){
		this.amount = amount;
	}
	public String getAmount(){
		return amount;
	}
	
	@Override
	public String toString(){
		return "Sou a mensagem com o MyIban: " + this.getMyIban() + " com o OtherIban: " + this.getOtherIban() +" e com o amount: " + this.getAmount() + ".";
	}
}
