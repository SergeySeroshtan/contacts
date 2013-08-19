package contacts.app.data;

/**
 * Thrown if network resource is not available.
 */
public class NotAvailableException extends Exception {

    private static final long serialVersionUID = -717789096856215702L;

    public NotAvailableException(String message) {
        super(message);
    }

    public NotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }

}
