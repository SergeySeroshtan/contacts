package contacts.app.service.sync;

import contacts.util.StringUtils;

/**
 * Information about contact, that is synchronized.
 */
public class SyncedContact {

    /**
     * Creates a contact with given properties.
     */
    public static SyncedContact create(long id, String username,
            String version, String unsyncedPhotoUrl) {
        SyncedContact contact = new SyncedContact();

        contact.id = id;
        contact.username = username;
        contact.version = version;
        contact.unsyncedPhotoUrl = unsyncedPhotoUrl;

        return contact;
    }

    private long id;
    private String username;
    private String version;
    private String unsyncedPhotoUrl;

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
     * Returns the URL of photo for contact, that was not synced together with
     * contact.
     */
    public String getUnsyncedPhotoUrl() {
        return unsyncedPhotoUrl;
    }

    /**
     * Sets the URL of photo.
     */
    public void setUnsyncedPhotoUrl(String unsyncedPhotoUrl) {
        this.unsyncedPhotoUrl = unsyncedPhotoUrl;
    }

    /**
     * Checks that photo for contact synchronized.
     * 
     * @return <code>true</code> if photo synchronized and <code>false</code>
     *         otherwise.
     */
    public boolean isPhotoSynced() {
        return StringUtils.isNullOrEmpty(unsyncedPhotoUrl);
    }

}
