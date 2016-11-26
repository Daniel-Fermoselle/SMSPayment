package pt.sirs.smsPacket;
import java.io.Serializable;


import pt.sirs.smsPacket.Exceptions.InvalidSMSPacketValuesException;


public class SmsPacket implements  Serializable{
	private static final long serialVersionUID = 1L;
	private String myIban;
	private String otherIban;
	private String amount;
	
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
