package contacts.app.service.sync;

/**
 * Information about group, that is synchronized.
 */
public class SyncedGroup {

    /**
     * Creates a group with given properties.
     */
    public static SyncedGroup create(long id, String name, String title) {
        SyncedGroup group = new SyncedGroup();

        group.id = id;
        group.name = name;
        group.title = title;

        return group;
    }

    private long id;
    private String name;
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
     * Returns the unique name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the unique name.
     */
    public void setName(String name) {
        this.name = name;
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
