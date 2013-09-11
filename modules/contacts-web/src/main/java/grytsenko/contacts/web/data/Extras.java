package grytsenko.contacts.web.data;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

/**
 * Extra information about employee.
 */
@Entity
@Table(name = "extras")
public final class Extras implements Serializable {

    private static final long serialVersionUID = 7942426620723987505L;

    private static final int UID_LENGTH_MAX = 50;
    public static final int SKYPE_LENGTH_MAP = 32;
    private static final int POSITION_LENGTH_MAX = 50;

    @Id
    @Column(name = "uid", length = UID_LENGTH_MAX)
    private String uid;

    @Column(name = "skype", length = SKYPE_LENGTH_MAP)
    private String skype;

    @Column(name = "position", length = POSITION_LENGTH_MAX)
    private String position;

    @Version
    private Integer version;

    private Extras() {
    }

    /**
     * Returns the unique identifier of employee.
     */
    public String getUid() {
        return uid;
    }

    /**
     * Returns the Skype name.
     */
    public String getSkype() {
        return skype;
    }

    /**
     * Returns the position.
     */
    public String getPosition() {
        return position;
    }

    /**
     * Returns the version of data.
     */
    public String getVersion() {
        return version == null ? "" : Integer.toString(version);
    }

}
