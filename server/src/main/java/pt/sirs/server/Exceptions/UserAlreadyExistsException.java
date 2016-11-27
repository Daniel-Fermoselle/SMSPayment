package pt.sirs.server.Exceptions;

public class UserAlreadyExistsException extends ServerException {

    private static final long serialVersionUID = 1L;

    private String conflictingUsername;

    public UserAlreadyExistsException(String conflictingUsername) {
        this.conflictingUsername = conflictingUsername;
    }

    public String getConflictingUsername() {
        return this.conflictingUsername;

    }

    @Override
    public String getMessage() {
        return "The username " + this.conflictingUsername + " already exists";
    }
}
