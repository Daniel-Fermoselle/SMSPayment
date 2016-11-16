package pt.sirs.smsPacket;
import java.io.Serializable;
public class smsPacket implements  Serializable{
	private String iban;
	private String amount;
	//private cena de cifra
	
	public smsPacket(String ib, String am){
		iban=ib;
		amount=am;
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
	
}
