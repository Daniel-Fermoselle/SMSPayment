package pt.sirs.smsPacket.Exceptions;

public class InvalidSMSPacketValuesException extends Exception {
	private static final long serialVersionUID = 1L;

    private String myIban;
    private String otherIban;
    private String amount;

    public InvalidSMSPacketValuesException(String mib, String oib, String am) {
    	myIban=mib;
    	otherIban=oib;
        amount=am;
    }

    public String getMyIban() {
        return myIban;
    }
    public String getOtherIban() {
        return otherIban;
    }
    
    public String getAmount() {
        return amount;
    }


    public String getMessage() {
        return "Amount or Iban values wrong check values\nMyIban: " + getMyIban() + "\nOtherIban: " +  getOtherIban() + "\nAmount: " + getAmount();
    }

}
