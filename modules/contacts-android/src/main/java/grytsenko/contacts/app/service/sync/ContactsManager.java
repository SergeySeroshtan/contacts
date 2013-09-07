package grytsenko.contacts.app.service.sync;

import static java.text.MessageFormat.format;
import grytsenko.contacts.api.Contact;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import android.accounts.Account;
import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
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
     * @param group
     *            the group where contacts are searched.
     * 
     * @return the found contacts.
     */
    public Map<String, SyncedContact> allFromGroup(SyncedGroup group) {
        String groupName = group.getName();
        Log.d(TAG, format("Search contacts in group {0}.", groupName));

        String[] projection = new String[] { GroupMembership.RAW_CONTACT_ID };
        String selection = GroupMembership.GROUP_ROW_ID + "=? and "
                + GroupMembership.MIMETYPE + "=?";
        Cursor cursor = contentResolver.query(Data.CONTENT_URI, projection,
                selection, new String[] { Long.toString(group.getId()),
                        GroupMembership.CONTENT_ITEM_TYPE }, null);

        try {
            int contactsNum = cursor.getCount();
            if (!cursor.moveToFirst()) {
                Log.d(TAG, format("Group {0} is empty.", groupName));
                return Collections.emptyMap();
            }

            Log.d(TAG,
                    format("Group {0} contains {1} contacts.", groupName,
                            contactsNum));
            Map<String, SyncedContact> contacts = new HashMap<String, SyncedContact>(
                    contactsNum);
            do {
                long contactId = cursor.getLong(cursor
                        .getColumnIndexOrThrow(GroupMembership.RAW_CONTACT_ID));
                SyncedContact contact = findContact(contactId);
                if (contact == null) {
                    Log.d(TAG, format("Contact {0} is skipped.", contactId));
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
        Log.d(TAG, format("Search contact {0}.", id));

        String[] projection = new String[] { RawContacts.SYNC1,
                RawContacts.SYNC2, RawContacts.SYNC3 };
        Uri uri = ContentUris.withAppendedId(RawContacts.CONTENT_URI, id);
        Cursor cursor = contentResolver
                .query(uri, projection, null, null, null);

        try {
            if (!cursor.moveToFirst()) {
                Log.w(TAG, format("Contact {0} not found.", id));
                return null;
            }

            String username = cursor.getString(cursor
                    .getColumnIndexOrThrow(RawContacts.SYNC1));
            String version = cursor.getString(cursor
                    .getColumnIndexOrThrow(RawContacts.SYNC2));
            String unsyncedPhotoUrl = cursor.getString(cursor
                    .getColumnIndexOrThrow(RawContacts.SYNC3));

            Log.d(TAG,
                    format("Contact {0} for {1} has version {2}.", id,
                            username, version));

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

        ContentValues name = nameValues(loadedContact);
        batch.add(doInsert(StructuredName.CONTENT_ITEM_TYPE, name));

        ContentValues email = emailValues(loadedContact);
        batch.add(doInsert(Email.CONTENT_ITEM_TYPE, email));

        ContentValues phone = phoneValues(loadedContact);
        batch.add(doInsert(Phone.CONTENT_ITEM_TYPE, phone));

        ContentValues organization = organizationValues(loadedContact);
        batch.add(doInsert(Organization.CONTENT_ITEM_TYPE, organization));

        batch.add(doInsert(GroupMembership.CONTENT_ITEM_TYPE,
                GroupMembership.GROUP_ROW_ID, group.getId()));

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
     *            the synchronized contact.
     * @param loadedContact
     *            the loaded contact with new data.
     * 
     * @return the updated contact.
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

        ContentValues name = nameValues(loadedContact);
        batch.add(doUpdate(id, StructuredName.CONTENT_ITEM_TYPE, name));

        ContentValues email = emailValues(loadedContact);
        batch.add(doUpdate(id, Email.CONTENT_ITEM_TYPE, email));

        ContentValues phone = phoneValues(loadedContact);
        batch.add(doUpdate(id, Phone.CONTENT_ITEM_TYPE, phone));

        ContentValues organization = organizationValues(loadedContact);
        batch.add(doUpdate(id, Organization.CONTENT_ITEM_TYPE, organization));

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

    private static ContentValues nameValues(Contact contact) {
        ContentValues name = new ContentValues();
        name.put(StructuredName.GIVEN_NAME, contact.getFirstName());
        name.put(StructuredName.FAMILY_NAME, contact.getLastName());
        return name;
    }

    private static ContentValues emailValues(Contact contact) {
        ContentValues email = new ContentValues();
        email.put(Email.DATA, contact.getMail());
        email.put(Email.TYPE, Email.TYPE_WORK);
        return email;
    }

    private static ContentValues phoneValues(Contact contact) {
        ContentValues phone = new ContentValues();
        phone.put(Phone.NUMBER, contact.getPhone());
        phone.put(Phone.TYPE, Phone.TYPE_MOBILE);
        return phone;
    }

    private static ContentValues organizationValues(Contact contact) {
        ContentValues organization = new ContentValues();
        organization.put(Organization.OFFICE_LOCATION, contact.getLocation());
        organization.put(Organization.TYPE, Organization.TYPE_WORK);
        return organization;
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
    public void updatePhoto(Account account, SyncedContact syncedContact,
            byte[] photo) throws SyncOperationException {
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

    private Builder prepareInsert(String mime) {
        return ContentProviderOperation.newInsert(Data.CONTENT_URI)
                .withValue(Data.MIMETYPE, mime)
                .withValueBackReference(Data.RAW_CONTACT_ID, 0);
    }

    private <T> ContentProviderOperation doInsert(String mime, String key,
            T value) {
        return prepareInsert(mime).withValue(key, value).build();
    }

    private <T> ContentProviderOperation doInsert(String mime,
            ContentValues values) {
        return prepareInsert(mime).withValues(values).build();
    }

    private Builder prepareUpdate(long id, String mime) {
        String selection = Data.RAW_CONTACT_ID + "=? and " + Data.MIMETYPE
                + "=?";
        String[] selectionArgs = new String[] { Long.toString(id), mime };

        Cursor cursor = contentResolver.query(Data.CONTENT_URI, null,
                selection, selectionArgs, null);

        try {
            if (cursor.moveToFirst()) {
                Log.d(TAG, format("Update {0} for contact {1}", mime, id));
                return ContentProviderOperation.newUpdate(Data.CONTENT_URI)
                        .withSelection(selection, selectionArgs);
            } else {
                Log.d(TAG, format("Insert {0} for contact {1}", mime, id));
                return ContentProviderOperation.newInsert(Data.CONTENT_URI)
                        .withValue(Data.MIMETYPE, mime)
                        .withValue(Data.RAW_CONTACT_ID, id);
            }
        } finally {
            cursor.close();
        }
    }

    private <T> ContentProviderOperation doUpdate(long id, String mime,
            String key, T value) {
        return prepareUpdate(id, mime).withValue(key, value).build();
    }

    private ContentProviderOperation doUpdate(long id, String mime,
            ContentValues values) {
        return prepareUpdate(id, mime).withValues(values).build();
    }

}
