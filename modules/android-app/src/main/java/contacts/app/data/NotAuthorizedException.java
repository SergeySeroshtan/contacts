package contacts.app.data;

/**
 * Thrown if user is not authorized to use network resource.
 */
public class NotAuthorizedException extends Exception {

    private static final long serialVersionUID = -1619686001692528827L;

    public NotAuthorizedException(String message) {
        super(message);
    }

    public NotAuthorizedException(String message, Throwable cause) {
        super(message, cause);
    }

}
