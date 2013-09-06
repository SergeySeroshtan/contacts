package grytsenko.contacts.api;

import java.io.Serializable;

/**
 * Contact information for person.
 */
public final class Contact implements Serializable {

    private static final long serialVersionUID = 1393219047960946953L;

    private String username;

    private String firstName;
    private String lastName;

    private String photoUrl;

    private String mail;
    private String phone;

    private String location;

    private String version;

    public Contact() {
    }

    /**
     * Returns the unique name, which can be used to distinguish contacts.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the unique name.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the first name.
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the first name.
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Returns the last name.
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the last name.
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
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
     * Returns the work email address.
     */
    public String getMail() {
        return mail;
    }

    /**
     * Sets the work email address.
     */
    public void setMail(String mail) {
        this.mail = mail;
    }

    /**
     * Returns the mobile phone number.
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Sets the mobile phone number.
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Returns the location of office.
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the location of office.
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Returns the version.
     * 
     * <p>
     * Versions can be compared for equality only.
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the version.
     */
    public void setVersion(String version) {
        this.version = version;
    }

}
