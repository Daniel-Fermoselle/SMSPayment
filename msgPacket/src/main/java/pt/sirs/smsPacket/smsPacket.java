package pt.sirs.smsPacket;
import java.io.Serializable;
import pt.sirs.smsPacket.Exceptions.InvalidSMSPacketValuesException;


public class smsPacket implements  Serializable{
	private static final long serialVersionUID = 1L;
	private String iban;
	private String amount;
	//private cena de cifra
	
	public smsPacket(String ib, String am) throws InvalidSMSPacketValuesException{
		if(ib.length()==25 && am.length()==8){
			iban=ib;
			amount=am;
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
	
	@Override
	public String toString(){
		return "Sou a mensagem com o iban: " + getIban() + "e com o amount: " + getAmount() + ".";
	}
}
