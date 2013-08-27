package grytsenko.contacts.app.service.sync;

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
 * Helps manage groups.
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
     * @param name
     *            the unique name of group.
     * 
     * @return the found group or <code>null</code> if group not found.
     */
    public SyncedGroup findGroup(Account account, String name) {
        String[] projection = new String[] { Groups._ID, Groups.TITLE };
        String selection = Groups.SYNC1 + "=? and " + Groups.ACCOUNT_NAME
                + "=? and " + Groups.ACCOUNT_TYPE + "=?";
        Cursor cursor = contentResolver.query(Groups.CONTENT_URI, projection,
                selection, new String[] { name, account.name, account.type },
                null);

        try {
            if (!cursor.moveToFirst()) {
                Log.d(TAG, format("Group {0} not found.", name));
                return null;
            }

            int idColumn = cursor.getColumnIndexOrThrow(Groups._ID);
            long id = cursor.getLong(idColumn);

            int titleColumn = cursor.getColumnIndexOrThrow(Groups.TITLE);
            String title = cursor.getString(titleColumn);

            Log.d(TAG, format("Found group {0} with title {1}", id, title));

            return SyncedGroup.create(id, name, title);
        } finally {
            cursor.close();
        }
    }

    /**
     * Creates a group.
     * 
     * @param account
     *            the account of user, who performs operation.
     * @param name
     *            the name of group.
     * @param title
     *            the title of group.
     * 
     * @return the created group.
     * 
     * @throws SyncOperationException
     *             if group could not be created.
     */
    public SyncedGroup createGroup(Account account, String name, String title)
            throws SyncOperationException {
        Log.d(TAG, format("Create group {0}.", name));

        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();

        batch.add(ContentProviderOperation.newInsert(Groups.CONTENT_URI)
                .withValue(Groups.SYNC1, name).withValue(Groups.TITLE, title)
                .withValue(Groups.ACCOUNT_NAME, account.name)
                .withValue(Groups.ACCOUNT_TYPE, account.type)
                .withValue(Groups.GROUP_VISIBLE, 1).build());

        try {
            ContentProviderResult[] results = contentResolver.applyBatch(
                    ContactsContract.AUTHORITY, batch);
            long id = ContentUris.parseId(results[0].uri);

            Log.d(TAG, format("Group {0} was created.", name));
            return SyncedGroup.create(id, name, title);
        } catch (Exception exception) {
            throw new SyncOperationException(format(
                    "Could not create group {0}.", name), exception);
        }
    }

    /**
     * Updates title of group.
     * 
     * @param account
     *            the account of user, who performs operation.
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
        String name = syncedGroup.getName();
        Log.d(TAG, format("Update title of group {0} to {1}.", name, title));

        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();

        Uri groupUri = ContentUris.withAppendedId(Groups.CONTENT_URI, id);
        batch.add(ContentProviderOperation.newUpdate(groupUri)
                .withValue(Groups.TITLE, title).build());

        try {
            contentResolver.applyBatch(ContactsContract.AUTHORITY, batch);
            Log.d(TAG, format("Title for {0} was updated.", name));

            return SyncedGroup.create(id, name, title);
        } catch (Exception exception) {
            throw new SyncOperationException("Could not update photo.",
                    exception);
        }
    }

}
