package grytsenko.contacts.app.sync;

/**
 * Information about group, that is synchronized.
 */
public class SyncedGroup {

    /**
     * Creates a group with given properties.
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
     * Returns the unique identifier.
     * 
     * <p>
     * We use unique identifier in order to distinguish the groups.
     */
    public String getUid() {
        return uid;
    }

    /**
     * Sets the unique identifier
     */
    public void setUid(String uid) {
        this.uid = uid;
    }

    /**
     * Returns the readable title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the readable title.
     */
    public void setTitle(String title) {
        this.title = title;
    }

}
