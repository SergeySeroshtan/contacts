package grytsenko.contacts.app.sync;

/**
 * Information about group.
 */
public class SyncedGroup {

    /**
     * Creates a group.
     */
    public static SyncedGroup create(long id, String uid, String title) {
        SyncedGroup group = new SyncedGroup();

        group.id = id;
        group.uid = uid;
        group.title = title;

        return group;
    }

    private long id;
    private String uid;
    private String title;

    public SyncedGroup() {
    }

    /**
     * Returns the internal identifier, assigned by Android.
     */
    public long getId() {
        return id;
    }

    /**
     * Returns the unique identifier, that is used to distinguish groups.
     */
    public String getUid() {
        return uid;
    }

    /**
     * Returns the title, which can be customized by user.
     */
    public String getTitle() {
        return title;
    }

}
