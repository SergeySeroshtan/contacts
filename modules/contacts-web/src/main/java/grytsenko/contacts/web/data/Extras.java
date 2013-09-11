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

    @Id
    @Column(name = "uid")
    private String uid;

    @Column(name = "skype")
    private String skype;

    @Column(name = "position")
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
    public Integer getVersion() {
        return version;
    }

}
