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
import grytsenko.contacts.api.Contact;

import java.util.ArrayList;
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
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Organization;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;
import android.util.Log;

/**
 * Manages sync of contacts.
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
     * Finds all contacts from specified group.
     * 
     * @param group
     *            the group where contacts are searched.
     * 
     * @return the found contacts.
     */
    public Map<String, SyncedContact> findAll(SyncedGroup group) {
        String groupUid = group.getUid();
        Log.d(TAG, format("Search contacts in group {0}.", groupUid));

        String[] projection = new String[] { GroupMembership.RAW_CONTACT_ID };
        String selection = GroupMembership.GROUP_ROW_ID + "=? and "
                + GroupMembership.MIMETYPE + "=?";
        Cursor cursor = contentResolver.query(Data.CONTENT_URI, projection,
                selection, new String[] { Long.toString(group.getId()),
                        GroupMembership.CONTENT_ITEM_TYPE }, null);

        try {
            Map<String, SyncedContact> contacts = new HashMap<String, SyncedContact>();

            if (!cursor.moveToFirst()) {
                Log.d(TAG, format("Group {0} is empty.", groupUid));
                return contacts;
            }

            Log.d(TAG,
                    format("Group {0} contains {1} contacts.", groupUid,
                            cursor.getCount()));
            do {
                long contactId = cursor.getLong(cursor
                        .getColumnIndexOrThrow(GroupMembership.RAW_CONTACT_ID));
                SyncedContact contact = findContact(contactId);
                if (contact == null) {
                    Log.d(TAG, format("Contact {0} is skipped.", contactId));
                    continue;
                }
                contacts.put(contact.getUid(), contact);
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
                RawContacts.SYNC2, RawContacts.SYNC3, RawContacts.SYNC4 };
        Uri uri = ContentUris.withAppendedId(RawContacts.CONTENT_URI, id);
        Cursor cursor = contentResolver
                .query(uri, projection, null, null, null);

        try {
            if (!cursor.moveToFirst()) {
                Log.w(TAG, format("Contact {0} not found.", id));
                return null;
            }

            String uid = cursor.getString(cursor
                    .getColumnIndexOrThrow(RawContacts.SYNC1));
            String version = cursor.getString(cursor
                    .getColumnIndexOrThrow(RawContacts.SYNC2));
            String photoUrl = cursor.getString(cursor
                    .getColumnIndexOrThrow(RawContacts.SYNC3));
            boolean photoSynced = Boolean.parseBoolean(cursor.getString(cursor
                    .getColumnIndexOrThrow(RawContacts.SYNC4)));

            Log.d(TAG, format("Found contact {0}.", uid));

            return SyncedContact
                    .create(id, uid, version, photoUrl, photoSynced);
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
     * @throws SyncException
     *             if contact could not be created.
     */
    public SyncedContact createContact(Account account, SyncedGroup group,
            Contact loadedContact) throws SyncException {
        String uid = loadedContact.getUid();
        String version = loadedContact.getVersion();
        String photoUrl = loadedContact.getPhotoUrl();
        boolean photoSynced = TextUtils.isEmpty(photoUrl);

        Log.d(TAG,
                format("Create contact {0} in group {1}.", uid, group.getUid()));

        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
        batch.add(ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
                .withValue(RawContacts.ACCOUNT_NAME, account.name)
                .withValue(RawContacts.ACCOUNT_TYPE, account.type)
                .withValue(RawContacts.SYNC1, uid)
                .withValue(RawContacts.SYNC2, version)
                .withValue(RawContacts.SYNC3, photoUrl)
                .withValue(RawContacts.SYNC4, Boolean.toString(photoSynced))
                .build());

        ContentValues name = nameValues(loadedContact);
        batch.add(doInsert(StructuredName.CONTENT_ITEM_TYPE, name));

        ContentValues email = emailValues(loadedContact);
        batch.add(doInsert(Email.CONTENT_ITEM_TYPE, email));

        ContentValues phone = phoneValues(loadedContact);
        batch.add(doInsert(Phone.CONTENT_ITEM_TYPE, phone));

        ContentValues skype = skypeValues(loadedContact);
        batch.add(doInsert(Im.CONTENT_ITEM_TYPE, skype));

        ContentValues postal = postalValues(loadedContact);
        batch.add(doInsert(StructuredPostal.CONTENT_ITEM_TYPE, postal));

        ContentValues organization = organizationValues(loadedContact);
        batch.add(doInsert(Organization.CONTENT_ITEM_TYPE, organization));

        batch.add(doInsert(GroupMembership.CONTENT_ITEM_TYPE,
                GroupMembership.GROUP_ROW_ID, group.getId()));

        try {
            ContentProviderResult[] results = contentResolver.applyBatch(
                    ContactsContract.AUTHORITY, batch);
            long id = ContentUris.parseId(results[0].uri);

            Log.d(TAG, format("Contact {0} was created.", uid));
            return SyncedContact
                    .create(id, uid, version, photoUrl, photoSynced);
        } catch (Exception exception) {
            throw new SyncException("Could not create contact.", exception);
        }
    }

    /**
     * Updates existing contact.
     * 
     * @param syncedContact
     *            the synchronized contact.
     * @param loadedContact
     *            the loaded contact with new data.
     * 
     * @return the updated contact.
     * 
     * @throws SyncException
     *             if contact could not be updated.
     */
    public SyncedContact updateContact(SyncedContact syncedContact,
            Contact loadedContact) throws SyncException {
        long id = syncedContact.getId();
        String uid = syncedContact.getUid();
        String version = loadedContact.getVersion();

        String loadedPhotoUrl = loadedContact.getPhotoUrl();
        String syncedPhotoUrl = syncedContact.getPhotoUrl();
        boolean photoUpdated = !TextUtils
                .equals(loadedPhotoUrl, syncedPhotoUrl);
        String photoUrl = photoUpdated ? loadedPhotoUrl : syncedPhotoUrl;
        boolean photoSynced = photoUpdated ? false : syncedContact
                .isPhotoSynced();

        Log.d(TAG, format("Update contact {0}.", uid));

        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();

        ContentValues name = nameValues(loadedContact);
        batch.add(doUpdate(id, StructuredName.CONTENT_ITEM_TYPE, name));

        ContentValues email = emailValues(loadedContact);
        batch.add(doUpdate(id, Email.CONTENT_ITEM_TYPE, email));

        ContentValues phone = phoneValues(loadedContact);
        batch.add(doUpdate(id, Phone.CONTENT_ITEM_TYPE, phone));

        ContentValues skype = skypeValues(loadedContact);
        batch.add(doUpdate(id, Im.CONTENT_ITEM_TYPE, skype));

        ContentValues postal = postalValues(loadedContact);
        batch.add(doUpdate(id, StructuredPostal.CONTENT_ITEM_TYPE, postal));

        ContentValues organization = organizationValues(loadedContact);
        batch.add(doUpdate(id, Organization.CONTENT_ITEM_TYPE, organization));

        Uri contactUri = ContentUris
                .withAppendedId(RawContacts.CONTENT_URI, id);
        batch.add(ContentProviderOperation.newUpdate(contactUri)
                .withValue(RawContacts.SYNC2, version)
                .withValue(RawContacts.SYNC3, photoUrl)
                .withValue(RawContacts.SYNC4, Boolean.toString(photoSynced))
                .build());

        try {
            contentResolver.applyBatch(ContactsContract.AUTHORITY, batch);
            Log.d(TAG, format("Contact {0} was updated.", uid));

            return SyncedContact
                    .create(id, uid, version, photoUrl, photoSynced);
        } catch (Exception exception) {
            throw new SyncException("Could not update photo.", exception);
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

    private static ContentValues skypeValues(Contact contact) {
        ContentValues skype = new ContentValues();
        skype.put(Im.DATA, contact.getSkype());
        skype.put(Im.PROTOCOL, Im.PROTOCOL_SKYPE);
        skype.put(Im.TYPE, Im.TYPE_OTHER);
        return skype;
    }

    private static ContentValues postalValues(Contact contact) {
        ContentValues postal = new ContentValues();
        postal.put(StructuredPostal.CITY, contact.getLocation());
        postal.put(StructuredPostal.TYPE, StructuredPostal.TYPE_WORK);
        return postal;
    }

    private static ContentValues organizationValues(Contact contact) {
        ContentValues organization = new ContentValues();
        organization.put(Organization.OFFICE_LOCATION, contact.getLocation());
        organization.put(Organization.TITLE, contact.getPosition());
        organization.put(Organization.TYPE, Organization.TYPE_WORK);
        return organization;
    }

    /**
     * Updates photo of existing contact.
     * 
     * @param syncedContact
     *            the updated contact.
     * @param photo
     *            the new photo for contact (can be <code>null</code>).
     * 
     * @throws SyncException
     *             if contact could not be updated.
     */
    public void updatePhoto(SyncedContact syncedContact, byte[] photo)
            throws SyncException {
        long id = syncedContact.getId();
        String uid = syncedContact.getUid();
        Log.d(TAG, format("Update photo of contact {0}.", uid));

        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
        batch.add(doUpdate(id, Photo.CONTENT_ITEM_TYPE, Photo.PHOTO, photo));

        Uri contactUri = ContentUris
                .withAppendedId(RawContacts.CONTENT_URI, id);
        batch.add(ContentProviderOperation.newUpdate(contactUri)
                .withValue(RawContacts.SYNC4, Boolean.toString(true)).build());

        try {
            contentResolver.applyBatch(ContactsContract.AUTHORITY, batch);
            Log.d(TAG, format("Photo of contact {0} was updated.", uid));
        } catch (Exception exception) {
            throw new SyncException("Could not update photo.", exception);
        }
    }

    /**
     * Removes existing contact.
     * 
     * @param syncedContact
     *            the removed contact.
     * 
     * @throws SyncException
     *             if contact could not be removed.
     */
    public void removeContact(SyncedContact syncedContact) throws SyncException {
        long id = syncedContact.getId();
        String uid = syncedContact.getUid();
        Log.d(TAG, format("Remove contact {0}.", uid));

        ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();
        Uri contactUri = ContentUris
                .withAppendedId(RawContacts.CONTENT_URI, id)
                .buildUpon()
                .appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER,
                        "true").build();
        batch.add(ContentProviderOperation.newDelete(contactUri).build());

        try {
            contentResolver.applyBatch(ContactsContract.AUTHORITY, batch);
            Log.d(TAG, format("Contact {0} was removed.", uid));
        } catch (Exception exception) {
            throw new SyncException("Could not remove contact.", exception);
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
                return ContentProviderOperation.newUpdate(Data.CONTENT_URI)
                        .withSelection(selection, selectionArgs);
            } else {
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
