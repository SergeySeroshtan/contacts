package contacts.app.service.sync;

/**
 * Thrown if operation could not be completed.
 */
public class NotCompletedException extends Exception {

    private static final long serialVersionUID = -7368186952748292754L;

    public NotCompletedException(String message) {
        super(message);
    }

    public NotCompletedException(String message, Throwable cause) {
        super(message, cause);
    }

}
