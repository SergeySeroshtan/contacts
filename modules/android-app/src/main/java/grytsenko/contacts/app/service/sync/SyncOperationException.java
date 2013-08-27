package grytsenko.contacts.app.service.sync;

/**
 * Thrown if operation, that is required for synchronization, could not be
 * performed.
 */
public class SyncOperationException extends Exception {

    private static final long serialVersionUID = -7368186952748292754L;

    public SyncOperationException(String message) {
        super(message);
    }

    public SyncOperationException(String message, Throwable cause) {
        super(message, cause);
    }

}
