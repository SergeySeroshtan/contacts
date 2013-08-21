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
     * Provides information about contacts, that present in address book.
     * 
     * @param account
     *            the account of user, who performs operation.
     * 
     * @return the known contacts.
     */
    public Map<String, KnownContact> getKnownContacts(Account account) {
        Uri uri = RawContacts.CONTENT_URI.buildUpon()
                .appendQueryParameter(RawContacts.ACCOUNT_NAME, account.name)
                .appendQueryParameter(RawContacts.ACCOUNT_TYPE, account.type)
                .build();

        String[] projection = new String[] { RawContacts._ID,
                RawContacts.SYNC1, RawContacts.SYNC2 };
        Cursor cursor = contentResolver
                .query(uri, projection, null, null, null);

        try {
            int contactsNum = cursor.getCount();
            Log.d(TAG,
                    format("Found {0} contacts in address book.", contactsNum));
            if (contactsNum == 0) {
                return Collections.emptyMap();
            }

            Map<String, KnownContact> knownContacts = new HashMap<String, KnownContact>(
                    contactsNum);
            do {
                cursor.moveToNext();

                long id = cursor.getLong(cursor
                        .getColumnIndexOrThrow(RawContacts._ID));
                String username = cursor.getString(cursor
                        .getColumnIndexOrThrow(RawContacts.SYNC1));
                String version = cursor.getString(cursor
                        .getColumnIndexOrThrow(RawContacts.SYNC2));

                KnownContact knownContact = KnownContact.create(id, username,
                        version);
                knownContacts.put(knownContact.getUsername(), knownContact);
            } while (!cursor.isLast());

            return knownContacts;
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
    public KnownContact createContact(Account account, long groupId,
            Contact contact) throws NotCompletedException {
        String username = contact.getUsername();
        String version = contact.getVersion();

        Log.d(TAG, format("Create new contact for {0}.", username));

        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
        batch.add(ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
                .withValue(RawContacts.ACCOUNT_NAME, account.name)
                .withValue(RawContacts.ACCOUNT_TYPE, account.type)
                .withValue(RawContacts.SYNC1, username)
                .withValue(RawContacts.SYNC2, version).build());

        ContentValues name = new ContentValues();
        name.put(StructuredName.GIVEN_NAME, contact.getFirstName());
        name.put(StructuredName.FAMILY_NAME, contact.getLastName());
        batch.add(doInsert(StructuredName.CONTENT_ITEM_TYPE, name));

        batch.add(doInsert(Email.CONTENT_ITEM_TYPE, Email.ADDRESS,
                contact.getMail()));
        batch.add(doInsert(Phone.CONTENT_ITEM_TYPE, Phone.NUMBER,
                contact.getPhone()));
        batch.add(doInsert(Organization.CONTENT_ITEM_TYPE,
                Organization.OFFICE_LOCATION, contact.getLocation()));
        batch.add(doInsert(GroupMembership.CONTENT_ITEM_TYPE,
                GroupMembership.GROUP_ROW_ID, groupId));

        batch.add(doInsert(Photo.CONTENT_ITEM_TYPE, Photo.PHOTO, null));

        try {
            ContentProviderResult[] results = contentResolver.applyBatch(
                    ContactsContract.AUTHORITY, batch);
            long id = ContentUris.parseId(results[0].uri);
            return KnownContact.create(id, username, version);
        } catch (Exception exception) {
            throw new NotCompletedException("Could not create contact.",
                    exception);
        }
    }

    /**
     * Updates existing contact.
     * 
     * @param account
     *            the account of user, who performs operation.
     * @param knownContact
     *            the information about existing contact.
     * @param syncedContact
     *            the synchronized contact.
     * 
     * @throws NotCompletedException
     *             if contact could not be updated.
     */
    public void updateContact(Account account, KnownContact knownContact,
            Contact syncedContact) throws NotCompletedException {
        long id = knownContact.getId();
        Log.d(TAG, format("Update photo of contact {0}.", id));

        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();

        batch.add(doUpdate(id, StructuredName.CONTENT_ITEM_TYPE,
                StructuredName.GIVEN_NAME, syncedContact.getFirstName()));
        batch.add(doUpdate(id, StructuredName.CONTENT_ITEM_TYPE,
                StructuredName.FAMILY_NAME, syncedContact.getLastName()));

        batch.add(doUpdate(id, Email.CONTENT_ITEM_TYPE, Email.ADDRESS,
                syncedContact.getMail()));
        batch.add(doUpdate(id, Phone.CONTENT_ITEM_TYPE, Phone.NUMBER,
                syncedContact.getPhone()));

        batch.add(doUpdate(id, Organization.CONTENT_ITEM_TYPE,
                Organization.OFFICE_LOCATION, syncedContact.getLocation()));

        Uri contactUri = ContentUris
                .withAppendedId(RawContacts.CONTENT_URI, id);
        batch.add(ContentProviderOperation.newUpdate(contactUri)
                .withValue(RawContacts.SYNC2, syncedContact.getVersion())
                .build());

        try {
            contentResolver.applyBatch(ContactsContract.AUTHORITY, batch);
        } catch (Exception exception) {
            throw new NotCompletedException("Could not update photo.",
                    exception);
        }
    }

    /**
     * Updates photo of existing contact.
     * 
     * @param account
     *            the account of user, who performs operation.
     * @param knownContact
     *            the information about existing contact.
     * @param photo
     *            the new photo for contact.
     * 
     * @throws NotCompletedException
     *             if contact could not be updated.
     */
    public void updateContactPhoto(Account account, KnownContact knownContact,
            byte[] photo) throws NotCompletedException {
        long id = knownContact.getId();
        Log.d(TAG, format("Update photo of contact {0}.", id));

        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
        batch.add(doUpdate(id, Photo.CONTENT_ITEM_TYPE, Photo.PHOTO, photo));

        try {
            contentResolver.applyBatch(ContactsContract.AUTHORITY, batch);
        } catch (Exception exception) {
            throw new NotCompletedException("Could not update photo.",
                    exception);
        }
    }

    /**
     * Removes existing contact.
     * 
     * @param account
     *            the account of user, who performs operation.
     * @param knownContact
     *            the information about existing contact.
     * 
     * @throws NotCompletedException
     *             if contact could not be removed.
     */
    public void removeContact(Account account, KnownContact knownContact)
            throws NotCompletedException {
        long id = knownContact.getId();
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

    private static <T> ContentProviderOperation doInsert(String mime,
            String key, T value) {
        return ContentProviderOperation.newInsert(Data.CONTENT_URI)
                .withValue(Data.MIMETYPE, mime).withValue(key, value)
                .withValueBackReference(Data.RAW_CONTACT_ID, 0).build();
    }

    private static <T> ContentProviderOperation doInsert(String mime,
            ContentValues values) {
        return ContentProviderOperation.newInsert(Data.CONTENT_URI)
                .withValue(Data.MIMETYPE, mime).withValues(values)
                .withValueBackReference(Data.RAW_CONTACT_ID, 0).build();
    }

    private static <T> ContentProviderOperation doUpdate(long id, String mime,
            String key, T value) {
        String selection = Data.CONTACT_ID + "=? and " + Data.MIMETYPE + "=?";
        return ContentProviderOperation
                .newUpdate(Data.CONTENT_URI)
                .withSelection(selection,
                        new String[] { Long.toString(id), mime })
                .withValue(key, value).build();
    }

}
