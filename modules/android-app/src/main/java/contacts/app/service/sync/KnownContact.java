package contacts.app.service.sync;

/**
 * Information about contact, that is already exists in address book.
 */
public class KnownContact {

    /**
     * Creates known contact.
     */
    public static KnownContact create(long id, String username, String version) {
        KnownContact knownContact = new KnownContact();

        knownContact.id = id;
        knownContact.username = username;
        knownContact.version = version;

        return knownContact;
    }

    private long id;
    private String username;
    private String version;

    public KnownContact() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

}
