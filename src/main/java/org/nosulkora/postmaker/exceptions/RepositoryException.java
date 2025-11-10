package org.nosulkora.postmaker.exceptions;

public class RepositoryException  extends RuntimeException{
    public RepositoryException() {
        super();
    }

    public RepositoryException(String message) {
        super(message);
    }

    public RepositoryException(String message, Throwable cause) {
        super(message, cause);
    }

    public RepositoryException(Throwable cause) {
        super(cause);
    }

    protected RepositoryException(
            String message,
            Throwable cause,
            boolean enableSuppression,
            boolean writableStackTrace
    ) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public static RepositoryException of(String format, Object... args) {
        return new RepositoryException(String.format(format, args));
    }

    public static RepositoryException of(Throwable cause, String format, Object... args) {
        return new RepositoryException(String.format(format, args), cause);
    }
}
