package contacts.app.service.sync;

/**
 * Thrown if synchronization should be interrupted.
 */
public class SyncException extends Exception {

    private static final long serialVersionUID = 6740791355283868178L;

    public SyncException(String message) {
        super(message);
    }

    public SyncException(String message, Throwable cause) {
        super(message, cause);
    }

}
