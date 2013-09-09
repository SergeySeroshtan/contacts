package grytsenko.contacts.app.sync;

/**
 * Information about contact.
 */
public class SyncedContact {

    /**
     * Creates a contact.
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
     * Returns the internal identifier, assigned by Android.
     */
    public long getId() {
        return id;
    }

    /**
     * Returns the unique name of user, that is used to distinguish contacts.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Returns the version of data.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Returns the URL of photo for contact.
     */
    public String getPhotoUrl() {
        return photoUrl;
    }

    /**
     * Returns the flag, which shows that photo is synchronized or not.
     */
    public boolean isPhotoSynced() {
        return photoSynced;
    }

}
