package pt.sirs.server.Exceptions;

public class InvalidPasswordException extends ServerException {

    private static final long serialVersionUID = 1L;

    private String conflictingPassword;

    public InvalidPasswordException(String conflictingPassword) {
        this.conflictingPassword = conflictingPassword;
    }

    public String getConflictingPassword() {
        return this.conflictingPassword;

    }

    @Override
    public String getMessage() {
        return "The password must have at least 4 characters and at most 15 characters";
    }
}
