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
package grytsenko.contacts.app.sync;

/**
 * Information about contact.
 */
public class SyncedContact {

    /**
     * Creates a contact.
     */
    public static SyncedContact create(long id, String uid, String version,
            String photoUrl, boolean photoSynced) {
        SyncedContact contact = new SyncedContact();

        contact.id = id;
        contact.uid = uid;
        contact.version = version;
        contact.photoUrl = photoUrl;
        contact.photoSynced = photoSynced;

        return contact;
    }

    private long id;
    private String uid;
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
     * Returns the unique identifier, that is used to distinguish contacts.
     */
    public String getUid() {
        return uid;
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
