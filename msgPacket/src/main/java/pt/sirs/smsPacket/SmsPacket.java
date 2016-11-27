package pt.sirs.smsPacket;
import java.io.Serializable;


import pt.sirs.smsPacket.Exceptions.InvalidSMSPacketValuesException;


public class SmsPacket implements  Serializable{
	private static final long serialVersionUID = 1L;
	private String sms;
	
	public SmsPacket(String sms){
		this.setSms(sms);
	}

	public String getSms() {
		return sms;
	}

	public void setSms(String sms) {
		this.sms = sms;
	}
	
	@Override
	public String toString(){
		return sms;
	}	
}
