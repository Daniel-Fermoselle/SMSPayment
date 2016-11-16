package pt.sirs.smsPacket.Exceptions;

public class InvalidSMSPacketValuesException extends Exception {
	private static final long serialVersionUID = 1L;

    private String iban;
    private String amount;

    public InvalidSMSPacketValuesException(String ib, String am) {
        iban=ib;
        amount=am;
    }

    public String getIban() {
        return iban;
    }
    
    public String getAmount() {
        return amount;
    }


    public String getMessage() {
        return "Amount or Iban values wrong check values\nIban: " + getIban() + "\nAmount: " + getAmount();
    }

}
