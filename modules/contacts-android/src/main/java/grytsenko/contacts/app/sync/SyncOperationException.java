package grytsenko.contacts.app.sync;

/**
 * Thrown if operation, that is required for synchronization, could not be
 * completed.
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
