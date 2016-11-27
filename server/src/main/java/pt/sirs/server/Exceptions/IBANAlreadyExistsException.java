package pt.sirs.server.Exceptions;

public class IBANAlreadyExistsException extends ServerException {

    private static final long serialVersionUID = 1L;

    private String conflictingIBAN;

    public IBANAlreadyExistsException(String conflictingIBAN) {
        this.conflictingIBAN = conflictingIBAN;
    }

    public String getConflictingIBAN() {
        return this.conflictingIBAN;

    }

    @Override
    public String getMessage() {
        return "The IBAN " + this.conflictingIBAN + " already exists";
    }
}
