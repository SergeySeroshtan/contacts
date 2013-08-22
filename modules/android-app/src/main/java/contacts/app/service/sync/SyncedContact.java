package contacts.app.service.sync;

/**
 * Information about contact, that is synchronized.
 */
public class SyncedContact {

    /**
     * Creates a contact with given properties.
     */
    public static SyncedContact create(long id, String username, String version) {
        SyncedContact contact = new SyncedContact();

        contact.id = id;
        contact.username = username;
        contact.version = version;

        return contact;
    }

    private long id;
    private String username;
    private String version;

    public SyncedContact() {
    }

    /**
     * Returns the internal identifier.
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the internal identifier.
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * Returns the unique name of user.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the unique name of user.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the current version.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the current version.
     */
    public void setVersion(String version) {
        this.version = version;
    }

}
