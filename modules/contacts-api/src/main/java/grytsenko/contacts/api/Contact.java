/**
 * Copyright (C) 2013 Anton Grytsenko (anthony.grytsenko@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grytsenko.contacts.api;

import java.io.Serializable;

/**
 * Contact information for person.
 */
public final class Contact implements Serializable {

    private static final long serialVersionUID = 1393219047960946953L;

    private String uid;

    private String firstName;
    private String lastName;

    private String photoUrl;

    private String mail;
    private String phone;
    private String skype;

    private String position;
    private String location;

    private String version;

    public Contact() {
    }

    /**
     * Returns the unique identifier of user.
     */
    public String getUid() {
        return uid;
    }

    /**
     * Sets the unique identifier of user.
     */
    public void setUid(String uid) {
        this.uid = uid;
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
     * Returns the Skype name.
     */
    public String getSkype() {
        return skype;
    }

    /**
     * Sets the Skype name.
     */
    public void setSkype(String skype) {
        this.skype = skype;
    }

    /**
     * Returns the position in company.
     */
    public String getPosition() {
        return position;
    }

    /**
     * Sets the position in company.
     */
    public void setPosition(String position) {
        this.position = position;
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
