package pt.sirs.client;

import pt.sirs.smsPacket.smsPacket;
import pt.sirs.smsPacket.Exceptions.InvalidSMSPacketValuesException;

public class Client {
	private String myIban;
	private int myMoney; //Nao iremo usar isto na versao basica. Vamos comecar com ints e depois se quisermos mudamos para floats/doubles
	
	public Client(String mib, int mm) {
		myIban=mib;
		myMoney=mm;
	}
	
	public void setMyIban(String mib){
		myIban=mib;
	}
	
	public String getMyIban(){
		return myIban;
	}
	
	public void setMyMoney(int mm){
		myMoney=mm;
	}
	
	public int getMyMoney(){
		return myMoney;
	}
	
	
	public smsPacket getSmsPacket(String iban, String amount){
		smsPacket sms=null;//CUIDADO
		try {
			sms = new smsPacket(iban,amount);
			return sms;
		} catch (InvalidSMSPacketValuesException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
			return sms;//CUIDADO
		}
	}
}
