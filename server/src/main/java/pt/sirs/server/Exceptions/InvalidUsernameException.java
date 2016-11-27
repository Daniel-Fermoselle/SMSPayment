package pt.sirs.server.Exceptions;

public class InvalidUsernameException extends ServerException {

    private static final long serialVersionUID = 1L;

    private String conflictingUsername;

    public InvalidUsernameException(String conflictingUsername) {
        this.conflictingUsername = conflictingUsername;
    }

    public String getConflictingIBAN() {
        return this.conflictingUsername;

    }

    @Override
    public String getMessage() {
        return "The username " + this.conflictingUsername + " is not valid, must have at most 10 characters and at least 4 characters";
    }
}
