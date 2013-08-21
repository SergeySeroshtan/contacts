package contacts.app.service.sync;

import static java.text.MessageFormat.format;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import android.accounts.Account;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.provider.ContactsContract.CommonDataKinds.GroupMembership;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;
import contacts.model.Contact;

/**
 * Helps manage contacts.
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

        ContentValues name = new ContentValues();
        name.put(StructuredName.GIVEN_NAME, contact.getFirstName());
        name.put(StructuredName.FAMILY_NAME, contact.getLastName());
        batch.add(insertValues(StructuredName.CONTENT_ITEM_TYPE, name));

        batch.add(insertValue(Email.CONTENT_ITEM_TYPE, Email.ADDRESS,
                contact.getMail()));
        batch.add(insertValue(Phone.CONTENT_ITEM_TYPE, Phone.NUMBER,
                contact.getPhone()));
        batch.add(insertValue(Organization.CONTENT_ITEM_TYPE,
                Organization.OFFICE_LOCATION, contact.getLocation()));
        batch.add(insertValue(GroupMembership.CONTENT_ITEM_TYPE,
                GroupMembership.GROUP_ROW_ID, groupId));

        if (photo != null) {
            batch.add(insertValue(Photo.CONTENT_ITEM_TYPE, Photo.PHOTO, photo));
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
    private static <T> ContentProviderOperation insertValue(String mime,
            String key, T value) {
        return ContentProviderOperation.newInsert(Data.CONTENT_URI)
                .withValue(Data.MIMETYPE, mime).withValue(key, value)
                .withValueBackReference(Data.RAW_CONTACT_ID, 0).build();
    }

    /**
     * Helps build insert operation.
     */
    private static <T> ContentProviderOperation insertValues(String mime,
            ContentValues values) {
        return ContentProviderOperation.newInsert(Data.CONTENT_URI)
                .withValue(Data.MIMETYPE, mime).withValues(values)
                .withValueBackReference(Data.RAW_CONTACT_ID, 0).build();
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
