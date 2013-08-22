package contacts.app.service.sync;

/**
 * Information about group, that exists in address book.
 */
public class KnownGroup {

    /**
     * Creates a group with given properties.
     */
    public static KnownGroup create(long id, String name, String title) {
        KnownGroup knownGroup = new KnownGroup();

        knownGroup.id = id;
        knownGroup.name = name;
        knownGroup.title = title;

        return knownGroup;
    }

    private long id;
    private String name;
    private String title;

    public KnownGroup() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}
