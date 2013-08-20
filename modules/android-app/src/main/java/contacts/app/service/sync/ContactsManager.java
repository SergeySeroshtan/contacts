package contacts.app.service.sync;

import static java.text.MessageFormat.format;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import android.accounts.Account;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;
import contacts.model.Contact;

/**
 * Helps to manage contacts.
 */
public class ContactsManager {

    private static final String TAG = ContactsManager.class.getName();

    private ContentResolver contentResolver;

    /**
     * Creates manager in the specified context.
     * 
     * @param context
     *            the context, where manager is used.
     */
    public ContactsManager(Context context) {
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

    /**
     * Gets the existing contacts.
     * 
     * @param account
     *            the account of user, who performs operation.
     * 
     * @return the set of names of users.
     */
    public Map<String, Long> getExistingContacts(Account account) {
        Uri uri = RawContacts.CONTENT_URI.buildUpon()
                .appendQueryParameter(RawContacts.ACCOUNT_NAME, account.name)
                .appendQueryParameter(RawContacts.ACCOUNT_TYPE, account.type)
                .build();

        String[] projection = new String[] { RawContacts.SYNC1, RawContacts._ID };
        Cursor cursor = contentResolver
                .query(uri, projection, null, null, null);

        try {
            int contactsNum = cursor.getCount();
            Log.d(TAG,
                    format("Found {0} contacts in address book.", contactsNum));
            if (contactsNum == 0) {
                return Collections.emptyMap();
            }

            Map<String, Long> existingContacts = new HashMap<String, Long>(
                    contactsNum);
            for (int i = 0; i < contactsNum; ++i) {
                cursor.moveToNext();
                existingContacts.put(cursor.getString(0), cursor.getLong(1));
            }

            return existingContacts;
        } finally {
            cursor.close();
        }
    }

    /**
     * Creates new contact.
     * 
     * @param account
     *            the account of user, who performs operation.
     * @param groupId
     *            the identifier of group for contact.
     * @param contact
     *            the data of contact.
     * @param photo
     *            the photo for contact (optional).
     * 
     * @throws NotCompletedException
     *             if contact could not be created.
     */
    public void createContact(Account account, long groupId, Contact contact,
            byte[] photo) throws NotCompletedException {
        String userName = contact.getUserName();

        Log.d(TAG, format("Create contact for {0}.", userName));

        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
        batch.add(ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
                .withValue(RawContacts.ACCOUNT_NAME, account.name)
                .withValue(RawContacts.ACCOUNT_TYPE, account.type)
                .withValue(RawContacts.SYNC1, userName).build());

        Map<String, Object> name = new HashMap<String, Object>();
        name.put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
                contact.getFirstName());
        name.put(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
                contact.getLastName());
        batch.add(insertOperation(
                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE,
                name));

        batch.add(insertOperation(
                ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                contact.getMail()));
        batch.add(insertOperation(
                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                contact.getPhone()));
        batch.add(insertOperation(
                ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE,
                ContactsContract.CommonDataKinds.Organization.OFFICE_LOCATION,
                contact.getLocation()));
        batch.add(insertOperation(
                ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE,
                ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID,
                groupId));

        if (photo != null) {
            batch.add(insertOperation(
                    ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE,
                    ContactsContract.CommonDataKinds.Photo.PHOTO, photo));
        }

        try {
            contentResolver.applyBatch(ContactsContract.AUTHORITY, batch);
        } catch (Exception exception) {
            throw new NotCompletedException(format(
                    "Could not create contact for {0}.", userName), exception);
        }
    }

    /**
     * Helps build insert operation.
     */
    private static <T> ContentProviderOperation insertOperation(String mime,
            String key, T value) {
        ContentProviderOperation.Builder builder = ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI);

        builder.withValue(ContactsContract.Data.MIMETYPE, mime);
        builder.withValue(key, value);

        builder.withValueBackReference(
                ContactsContract.CommonDataKinds.StructuredName.RAW_CONTACT_ID,
                0);

        return builder.build();
    }

    /**
     * Helps build insert operation.
     */
    private static <T> ContentProviderOperation insertOperation(String mime,
            Map<String, ?> entries) {
        ContentProviderOperation.Builder builder = ContentProviderOperation
                .newInsert(ContactsContract.Data.CONTENT_URI);

        builder.withValue(ContactsContract.Data.MIMETYPE, mime);
        for (Map.Entry<String, ?> entry : entries.entrySet()) {
            builder.withValue(entry.getKey(), entry.getValue());
        }

        builder.withValueBackReference(
                ContactsContract.CommonDataKinds.StructuredName.RAW_CONTACT_ID,
                0);

        return builder.build();
    }

    /**
     * Removes existing contact.
     * 
     * @param account
     *            the account of user, who performs operation.
     * @param id
     *            the identifier of contact.
     * 
     * @throws NotCompletedException
     *             if contact could not be removed.
     */
    public void removeContact(Account account, long id)
            throws NotCompletedException {
        Log.d(TAG, format("Remove contact {0}.", id));

        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
        Uri contactUri = ContentUris
                .withAppendedId(RawContacts.CONTENT_URI, id)
                .buildUpon()
                .appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER,
                        "true").build();
        batch.add(ContentProviderOperation.newDelete(contactUri).build());

        try {
            contentResolver.applyBatch(ContactsContract.AUTHORITY, batch);
        } catch (Exception exception) {
            throw new NotCompletedException("Could not remove contact.",
                    exception);
        }
    }

}
