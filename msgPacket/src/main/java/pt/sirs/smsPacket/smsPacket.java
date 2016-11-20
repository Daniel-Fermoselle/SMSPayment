package pt.sirs.smsPacket;
import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;

import pt.sirs.smsPacket.Exceptions.InvalidSMSPacketValuesException;


public class smsPacket implements  Serializable{
	private static final long serialVersionUID = 1L;
	private String myIban;
	private String otherIban;
	private String amount;
	private String digest;
	
	public smsPacket(String mib, String oib, String am) throws InvalidSMSPacketValuesException{
		if(mib.length()==25 && oib.length()==25 && am.length()<=8){
			myIban=mib;
			otherIban=oib;
			amount=am;
			//digest="";//CUIDADO
			
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
	
	public String cipherMobile(/*recebe uma/duas key*/){
		String message;
		message=myIban+"-"+amount+"-"+digest;//inserir o digest cifrado com a chave privada do sender assumindo '-' nao usados no digest
		//cifrar message com chave publica do receiver
		return message;
	}
	
	public void setSignature(PrivateKey key){
		String elements=myIban.concat(otherIban).concat(amount);
		try {
			
			Signature sig = Signature.getInstance("SHA256withRSA"); //tem de ser 256 porque e o suportado pelo java
			sig.initSign(key);
			sig.update(elements.getBytes());
			byte[] signature = sig.sign();
    		digest = printBase64Binary(signature);
			
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
	
	@Override
	public String toString(){
		return "Sou a mensagem com o MyIban: " + this.getMyIban() + " com o OtherIban: " + this.getOtherIban() +" e com o amount: " + this.getAmount() + ".";
	}
}
