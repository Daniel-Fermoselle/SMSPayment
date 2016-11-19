package pt.sirs.smsPacket;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;

import pt.sirs.smsPacket.Exceptions.InvalidSMSPacketValuesException;


public class smsPacket implements  Serializable{
	private static final long serialVersionUID = 1L;
	private String iban;
	private String amount;
	private Signature sig;
	
	public smsPacket(String ib, String am) throws InvalidSMSPacketValuesException{
		if(ib.length()==25 && am.length()<=8){
			iban=ib;
			amount=am;
			//sig=null;//CUIDADO
			
		}
		else{
			throw new InvalidSMSPacketValuesException(ib,am);
		}
	}
	
	public void setIban(String ib){
		iban=ib;
	}
	public String getIban(){
		return iban;
	}
	public void setAmount(String am){
		amount=am;
	}
	public String getAmount(){
		return amount;
	}
	
	public String cipherMobile(/*recebe uma/duas key*/){
		String message;
		message=iban+"-"+amount+"-"+"digest";//inserir o digest cifrado com a chave privada do sender assumindo '-' nao usados no digest
		//cifrar message com chave publica do receiver
		return message;
	}
	
	public void setSignature(/*recebe private key para signature*/){
		String elements=iban.concat(amount);
		try {
			
			sig = Signature.getInstance("SHA256withRSA"); //tem de ser 256 porque e o suportado pelo java
			//sig.initSign(/*recebe private key para signature*/);
			sig.update(elements.getBytes());
			
		} catch (SignatureException e) {
			System.out.println("Error while creating the signature");
			e.printStackTrace();
		}
		catch (NoSuchAlgorithmException e) {
			System.out.println("Error while creating the signature");
			e.printStackTrace();
		}
	}
	
	@Override
	public String toString(){
		return "Sou a mensagem com o iban: " + getIban() + " e com o amount: " + getAmount() + ".";
	}
}
