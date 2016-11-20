package pt.sirs.server.Exceptions;

public class AmountToHighException extends ServerException {

    private static final long serialVersionUID = 1L;

    private String conflictingAmount;

    public AmountToHighException(String conflictingAmount) {
        this.conflictingAmount = conflictingAmount;
    }

    public String getConflictingAmount() {
        return this.conflictingAmount;

    }

    @Override
    public String getMessage() {
        return "The amount " + this.conflictingAmount + " is to high";
    }
}