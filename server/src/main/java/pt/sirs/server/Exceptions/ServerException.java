package pt.sirs.server.Exceptions;

public abstract class ServerException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ServerException() {
    }

    public ServerException(String msg) {
        super(msg);
    }
}