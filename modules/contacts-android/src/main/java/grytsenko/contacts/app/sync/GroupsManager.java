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

import static java.text.MessageFormat.format;

import java.util.ArrayList;

import android.accounts.Account;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Groups;
import android.util.Log;

/**
 * Manages sync of groups.
 */
public class GroupsManager {

    private static final String TAG = GroupsManager.class.getName();

    private ContentResolver contentResolver;

    /**
     * Creates manager in the specified context.
     * 
     * @param context
     *            the context, where manager is used.
     */
    public GroupsManager(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context not defined.");
        }

        this.contentResolver = context.getContentResolver();
    }

    /**
     * Finds group by its name.
     * 
     * @param account
     *            the account of user, who performs operation.
     * @param uid
     *            the unique identifier of group.
     * 
     * @return the found group or <code>null</code> if group not found.
     */
    public SyncedGroup findGroup(Account account, String uid) {
        Log.d(TAG, format("Search group {0}.", uid));

        String[] projection = new String[] { Groups._ID, Groups.TITLE };
        String selection = Groups.SYNC1 + "=? and " + Groups.ACCOUNT_NAME
                + "=? and " + Groups.ACCOUNT_TYPE + "=?";
        Cursor cursor = contentResolver.query(Groups.CONTENT_URI, projection,
                selection, new String[] { uid, account.name, account.type },
                null);

        try {
            if (!cursor.moveToFirst()) {
                Log.d(TAG, format("Group {0} not found.", uid));
                return null;
            }

            int idColumn = cursor.getColumnIndexOrThrow(Groups._ID);
            long id = cursor.getLong(idColumn);

            int titleColumn = cursor.getColumnIndexOrThrow(Groups.TITLE);
            String title = cursor.getString(titleColumn);

            Log.d(TAG, format("Found group {0} - {1}", id, title));

            return SyncedGroup.create(id, uid, title);
        } finally {
            cursor.close();
        }
    }

    /**
     * Creates a group.
     * 
     * @param account
     *            the account of user, who performs operation.
     * @param uid
     *            the unique identifier of group.
     * @param title
     *            the title of group.
     * 
     * @return the created group.
     * 
     * @throws SyncOperationException
     *             if group could not be created.
     */
    public SyncedGroup createGroup(Account account, String uid, String title)
            throws SyncOperationException {
        Log.d(TAG, format("Create group {0}.", uid));

        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();

        batch.add(ContentProviderOperation.newInsert(Groups.CONTENT_URI)
                .withValue(Groups.SYNC1, uid).withValue(Groups.TITLE, title)
                .withValue(Groups.ACCOUNT_NAME, account.name)
                .withValue(Groups.ACCOUNT_TYPE, account.type)
                .withValue(Groups.GROUP_VISIBLE, 1).build());

        try {
            ContentProviderResult[] results = contentResolver.applyBatch(
                    ContactsContract.AUTHORITY, batch);
            long id = ContentUris.parseId(results[0].uri);

            Log.d(TAG, format("Group {0} was created.", uid));
            return SyncedGroup.create(id, uid, title);
        } catch (Exception exception) {
            throw new SyncOperationException(format(
                    "Could not create group {0}.", uid), exception);
        }
    }

    /**
     * Updates title of group.
     * 
     * @param syncedGroup
     *            the synchronized group.
     * @param title
     *            the new title for group.
     * 
     * @return the updated group.
     * 
     * @throws SyncOperationException
     *             if contact could not be updated.
     */
    public SyncedGroup updateTitle(SyncedGroup syncedGroup, String title)
            throws SyncOperationException {
        long id = syncedGroup.getId();
        String uid = syncedGroup.getUid();
        Log.d(TAG, format("Update title of group {0} to {1}.", uid, title));

        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();

        Uri groupUri = ContentUris.withAppendedId(Groups.CONTENT_URI, id);
        batch.add(ContentProviderOperation.newUpdate(groupUri)
                .withValue(Groups.TITLE, title).build());

        try {
            contentResolver.applyBatch(ContactsContract.AUTHORITY, batch);
            Log.d(TAG, format("Title of group {0} was updated.", uid));

            return SyncedGroup.create(id, uid, title);
        } catch (Exception exception) {
            throw new SyncOperationException("Could not update photo.",
                    exception);
        }
    }

}
