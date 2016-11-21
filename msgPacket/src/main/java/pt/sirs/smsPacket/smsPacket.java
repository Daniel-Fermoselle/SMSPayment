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


public class smsPacket implements  Serializable{
	private static final long serialVersionUID = 1L;
	private String myIban;
	private String otherIban;
	private String amount;
	private String signature;
	
	public smsPacket(String mib, String oib, String am) throws InvalidSMSPacketValuesException{
		if(mib.length()==25 && oib.length()==25 && am.length()<=8){
			myIban=mib;
			otherIban=oib;
			amount=am;
			//signature="";//CUIDADO
			
		}
		else{
			throw new InvalidSMSPacketValuesException(mib,oib,am);
		}
	}
	
	public void setMyIban(String ib){
		myIban=ib;
	}
	public String getMyIban(){
		return myIban;
	}
	public void setOtherIban(String ib){
		otherIban=ib;
	}
	public String getOtherIban(){
		return otherIban;
	}
	
	public void setAmount(String am){
		amount=am;
	}
	public String getAmount(){
		return amount;
	}
	
	public String cipherMobile(){
		String message;
		message=myIban+"-"+ otherIban + "-"+amount+"-"+signature;//inserir o digest cifrado com a chave privada do sender assumindo '-' nao usados na signature
		//cifrar message com chave publica do receiver
		return message;
	}
	
	public smsPacket decipherMobile(String message, PublicKey key){
		//TODO TEST MESSAGE FORMATING
		String [] divided= message.split("-");
		smsPacket sms;
		try {
			sms = new smsPacket(divided[0],divided[1],divided[2]);
			if(sms.verifySign(divided[3], key)){
				return sms;
			}
			
			return null;//CUIDADO
			
		} 
		//TODO CHECK THIS CATCH
		catch (InvalidSMSPacketValuesException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;//CUIDADO
		}
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
	
	
	
	@Override
	public String toString(){
		return "Sou a mensagem com o MyIban: " + this.getMyIban() + " com o OtherIban: " + this.getOtherIban() +" e com o amount: " + this.getAmount() + ".";
	}
}
