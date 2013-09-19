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
