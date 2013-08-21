package contacts.app.service.sync;

import static java.text.MessageFormat.format;

import java.util.ArrayList;

import android.accounts.Account;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
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
     * Finds or creates group for contacts.
     * 
     * @param account
     *            the account of user, who performs operation.
     * @param title
     *            the title of group.
     * 
     * @return the identifier of group.
     * 
     * @throws NotCompletedException
     *             if group could not be found or created.
     */
    public long getGroup(Account account, String title)
            throws NotCompletedException {
        Long id = findGroup(account, title);
        if (id != null) {
            Log.d(TAG, format("Group {0} was found.", title));
            return id;
        }

        return createGroup(account, title);
    }

    /**
     * Finds group by title.
     * 
     * @return identifier of group or <code>null</code> if group was not found.
     */
    private Long findGroup(Account account, String title) {
        String[] projection = new String[] { ContactsContract.Groups._ID,
                ContactsContract.Groups.TITLE };
        String selection = ContactsContract.Groups.TITLE + "=? and "
                + ContactsContract.Groups.ACCOUNT_NAME + "=? and "
                + ContactsContract.Groups.ACCOUNT_TYPE + "=?";
        Cursor cursor = contentResolver.query(
                ContactsContract.Groups.CONTENT_URI, projection, selection,
                new String[] { title, account.name, account.type }, null);

        try {
            if (cursor.getCount() <= 0) {
                Log.d(TAG, format("Could not find group {0}.", title));
                return null;
            }

            cursor.moveToNext();
            return cursor.getLong(cursor
                    .getColumnIndex(ContactsContract.Groups._ID));
        } finally {
            cursor.close();
        }
    }

    /**
     * Creates a group with the specified title.
     * 
     * @return identifier of created group.
     * 
     * @throws NotCompletedException
     *             if group could not be created.
     */
    private Long createGroup(Account account, String title)
            throws NotCompletedException {
        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();

        ops.add(ContentProviderOperation
                .newInsert(ContactsContract.Groups.CONTENT_URI)
                .withValue(ContactsContract.Groups.TITLE, title)
                .withValue(ContactsContract.Groups.ACCOUNT_NAME, account.name)
                .withValue(ContactsContract.Groups.ACCOUNT_TYPE, account.type)
                .withValue(ContactsContract.Groups.GROUP_VISIBLE, 1).build());

        try {
            ContentProviderResult[] results = contentResolver.applyBatch(
                    ContactsContract.AUTHORITY, ops);
            return ContentUris.parseId(results[0].uri);
        } catch (Exception exception) {
            throw new NotCompletedException(format(
                    "Could not create group {0}.", title), exception);
        }
    }

}
