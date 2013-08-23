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
     * Returns all contacts from specified group.
     * 
     * @param account
     *            the account of user, who performs operation.
     * @param group
     *            the group where contacts are searched.
     * 
     * @return the found contacts.
     */
    public Map<String, SyncedContact> allFromGroup(SyncedGroup group) {
        String[] projection = new String[] { GroupMembership.CONTACT_ID };
        String selection = GroupMembership.GROUP_ROW_ID + "=? and "
                + GroupMembership.MIMETYPE + "=?";
        Cursor cursor = contentResolver.query(Data.CONTENT_URI, projection,
                selection, new String[] { Long.toString(group.getId()),
                        GroupMembership.CONTENT_ITEM_TYPE }, null);

        try {
            int contactsNum = cursor.getCount();
            Log.d(TAG,
                    format("Found {0} contacts in group {1}.", contactsNum,
                            group.getName()));
            if (!cursor.moveToFirst()) {
                return Collections.emptyMap();
            }

            Map<String, SyncedContact> contacts = new HashMap<String, SyncedContact>(
                    contactsNum);
            do {
                long contactId = cursor.getLong(cursor
                        .getColumnIndexOrThrow(GroupMembership.CONTACT_ID));
                SyncedContact contact = findContact(contactId);
                if (contact == null) {
                    continue;
                }
                contacts.put(contact.getUsername(), contact);
            } while (cursor.moveToNext());

            return contacts;
        } finally {
            cursor.close();
        }
    }

    /**
     * Finds a contact.
     * 
     * @param id
     *            the identifier of contact.
     * 
     * @return the found contact or <code>null</code> if contact was not found.
     */
    public SyncedContact findContact(long id) {
        String[] projection = new String[] { RawContacts.SYNC1,
                RawContacts.SYNC2, RawContacts.SYNC3 };
        Uri uri = ContentUris.withAppendedId(RawContacts.CONTENT_URI, id);
        Cursor cursor = contentResolver
                .query(uri, projection, null, null, null);

        try {
            if (!cursor.moveToFirst()) {
                return null;
            }

            String username = cursor.getString(cursor
                    .getColumnIndexOrThrow(RawContacts.SYNC1));
            String version = cursor.getString(cursor
                    .getColumnIndexOrThrow(RawContacts.SYNC2));
            String unsyncedPhotoUrl = cursor.getString(cursor
                    .getColumnIndexOrThrow(RawContacts.SYNC3));

            return SyncedContact
                    .create(id, username, version, unsyncedPhotoUrl);
        } finally {
            cursor.close();
        }
    }

    /**
     * Creates new contact in specified group.
     * 
     * @param account
     *            the account of user, who performs operation.
     * @param group
     *            the group for contact.
     * @param loadedContact
     *            the loaded contact with new data.
     * 
     * @return the created contact.
     * 
     * @throws SyncOperationException
     *             if contact could not be created.
     */
    public SyncedContact createContact(Account account, SyncedGroup group,
            Contact loadedContact) throws SyncOperationException {
        String username = loadedContact.getUsername();
        String version = loadedContact.getVersion();
        String unsyncedPhotoUrl = loadedContact.getPhotoUrl();

        Log.d(TAG,
                format("Create contact for {0} in group {1}.", username,
                        group.getName()));

        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
        batch.add(ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
                .withValue(RawContacts.ACCOUNT_NAME, account.name)
                .withValue(RawContacts.ACCOUNT_TYPE, account.type)
                .withValue(RawContacts.SYNC1, username)
                .withValue(RawContacts.SYNC2, version)
                .withValue(RawContacts.SYNC3, unsyncedPhotoUrl).build());

        ContentValues name = new ContentValues();
        name.put(StructuredName.GIVEN_NAME, loadedContact.getFirstName());
        name.put(StructuredName.FAMILY_NAME, loadedContact.getLastName());
        batch.add(doInsert(StructuredName.CONTENT_ITEM_TYPE, name));

        batch.add(doInsert(Email.CONTENT_ITEM_TYPE, Email.ADDRESS,
                loadedContact.getMail()));
        batch.add(doInsert(Phone.CONTENT_ITEM_TYPE, Phone.NUMBER,
                loadedContact.getPhone()));
        batch.add(doInsert(Organization.CONTENT_ITEM_TYPE,
                Organization.OFFICE_LOCATION, loadedContact.getLocation()));
        batch.add(doInsert(GroupMembership.CONTENT_ITEM_TYPE,
                GroupMembership.GROUP_ROW_ID, group.getId()));

        batch.add(doInsert(Photo.CONTENT_ITEM_TYPE, Photo.PHOTO, null));

        try {
            ContentProviderResult[] results = contentResolver.applyBatch(
                    ContactsContract.AUTHORITY, batch);
            long id = ContentUris.parseId(results[0].uri);

            Log.d(TAG, format("Contact for {0} was created.", username));
            return SyncedContact
                    .create(id, username, version, unsyncedPhotoUrl);
        } catch (Exception exception) {
            throw new SyncOperationException("Could not create contact.",
                    exception);
        }
    }

    /**
     * Updates existing contact.
     * 
     * @param account
     *            the account of user, who performs operation.
     * @param syncedContact
     *            the updated contact.
     * @param loadedContact
     *            the loaded contact with new data.
     * 
     * @throws SyncOperationException
     *             if contact could not be updated.
     */
    public SyncedContact updateContact(Account account,
            SyncedContact syncedContact, Contact loadedContact)
            throws SyncOperationException {
        long id = syncedContact.getId();
        String username = syncedContact.getUsername();
        String version = loadedContact.getVersion();
        String unsyncedPhotoUrl = loadedContact.getPhotoUrl();

        Log.d(TAG, format("Update contact for {0}.", username));

        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();

        batch.add(doUpdate(id, StructuredName.CONTENT_ITEM_TYPE,
                StructuredName.GIVEN_NAME, loadedContact.getFirstName()));
        batch.add(doUpdate(id, StructuredName.CONTENT_ITEM_TYPE,
                StructuredName.FAMILY_NAME, loadedContact.getLastName()));

        batch.add(doUpdate(id, Email.CONTENT_ITEM_TYPE, Email.ADDRESS,
                loadedContact.getMail()));
        batch.add(doUpdate(id, Phone.CONTENT_ITEM_TYPE, Phone.NUMBER,
                loadedContact.getPhone()));

        batch.add(doUpdate(id, Organization.CONTENT_ITEM_TYPE,
                Organization.OFFICE_LOCATION, loadedContact.getLocation()));

        Uri contactUri = ContentUris
                .withAppendedId(RawContacts.CONTENT_URI, id);
        batch.add(ContentProviderOperation.newUpdate(contactUri)
                .withValue(RawContacts.SYNC2, version)
                .withValue(RawContacts.SYNC3, unsyncedPhotoUrl).build());

        try {
            contentResolver.applyBatch(ContactsContract.AUTHORITY, batch);
            Log.d(TAG, format("Contact for {0} was updated.", username));

            return SyncedContact
                    .create(id, username, version, unsyncedPhotoUrl);
        } catch (Exception exception) {
            throw new SyncOperationException("Could not update photo.",
                    exception);
        }
    }

    /**
     * Updates photo of existing contact.
     * 
     * @param account
     *            the account of user, who performs operation.
     * @param syncedContact
     *            the updated contact.
     * @param photo
     *            the new photo for contact.
     * 
     * @throws SyncOperationException
     *             if contact could not be updated.
     */
    public void updateContactPhoto(Account account,
            SyncedContact syncedContact, byte[] photo)
            throws SyncOperationException {
        long id = syncedContact.getId();
        Log.d(TAG, format("Update photo for {0}.", syncedContact.getUsername()));

        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
        batch.add(doUpdate(id, Photo.CONTENT_ITEM_TYPE, Photo.PHOTO, photo));

        Uri contactUri = ContentUris
                .withAppendedId(RawContacts.CONTENT_URI, id);
        batch.add(ContentProviderOperation.newUpdate(contactUri)
                .withValue(RawContacts.SYNC3, null).build());

        try {
            contentResolver.applyBatch(ContactsContract.AUTHORITY, batch);
            Log.d(TAG,
                    format("Photo for {0} was updated.",
                            syncedContact.getUsername()));
        } catch (Exception exception) {
            throw new SyncOperationException("Could not update photo.",
                    exception);
        }
    }

    /**
     * Removes existing contact.
     * 
     * @param account
     *            the account of user, who performs operation.
     * @param syncedContact
     *            the removed contact.
     * 
     * @throws SyncOperationException
     *             if contact could not be removed.
     */
    public void removeContact(Account account, SyncedContact syncedContact)
            throws SyncOperationException {
        long id = syncedContact.getId();
        Log.d(TAG,
                format("Remove contact for {0}.", syncedContact.getUsername()));

        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
        Uri contactUri = ContentUris
                .withAppendedId(RawContacts.CONTENT_URI, id)
                .buildUpon()
                .appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER,
                        "true").build();
        batch.add(ContentProviderOperation.newDelete(contactUri).build());

        try {
            contentResolver.applyBatch(ContactsContract.AUTHORITY, batch);
            Log.d(TAG,
                    format("Photo for {0} was removed.",
                            syncedContact.getUsername()));
        } catch (Exception exception) {
            throw new SyncOperationException("Could not remove contact.",
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
