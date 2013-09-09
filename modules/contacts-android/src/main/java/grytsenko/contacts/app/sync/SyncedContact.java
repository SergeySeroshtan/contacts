package grytsenko.contacts.app.sync;

/**
 * Information about contact, that is synchronized.
 */
public class SyncedContact {

    /**
     * Creates a contact with given properties.
     */
    public static SyncedContact create(long id, String username,
            String version, String photoUrl, boolean photoSynced) {
        SyncedContact contact = new SyncedContact();

        contact.id = id;
        contact.username = username;
        contact.version = version;
        contact.photoUrl = photoUrl;
        contact.photoSynced = photoSynced;

        return contact;
    }

    private long id;
    private String username;
    private String version;
    private String photoUrl;
    private boolean photoSynced;

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

    /**
     * Returns the URL of photo.
     */
    public String getPhotoUrl() {
        return photoUrl;
    }

    /**
     * Sets the URL of photo.
     */
    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    /**
     * Returns the flag, which shows that photo is synced.
     */
    public boolean isPhotoSynced() {
        return photoSynced;
    }

    /**
     * Sets the flag, which shows that photo is synced.
     */
    public void setPhotoSynced(boolean photoSynced) {
        this.photoSynced = photoSynced;
    }

}
