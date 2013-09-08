package grytsenko.contacts.app.service.sync;

import static java.lang.Thread.currentThread;
import static java.text.MessageFormat.format;
import grytsenko.contacts.api.Contact;
import grytsenko.contacts.app.R;
import grytsenko.contacts.app.data.ContactsRepository;
import grytsenko.contacts.app.data.NotAuthorizedException;
import grytsenko.contacts.app.data.NotAvailableException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

/**
 * Synchronizes contacts.
 */
public class SyncContactsAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = SyncContactsAdapter.class.getName();

    private ContactsRepository contactsRepository;

    private GroupsManager groupsManager;
    private ContactsManager contactsManager;
    private SettingsManager settingsManager;
    private NetworkManager networkManager;

    public SyncContactsAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);

        contactsRepository = new ContactsRepository(context);

        groupsManager = new GroupsManager(context);
        contactsManager = new ContactsManager(context);
        settingsManager = new SettingsManager(context);
        networkManager = new NetworkManager(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
            ContentProviderClient provider, SyncResult syncResult) {
        if (!isSuitableNetwork()) {
            Log.d(TAG, "Unsuitable network.");
            return;
        }

        Log.d(TAG, "Sync started.");

        try {
            Log.d(TAG, "Sync contacts of coworkers.");

            Log.d(TAG, "Sync group.");
            SyncedGroup groupCoworkers;
            try {
                groupCoworkers = syncCoworkersGroup(account);
            } catch (SyncOperationException exception) {
                Log.e(TAG, "Could not sync group.", exception);
                return;
            }

            checkCanceled();

            Log.d(TAG, "Load contacts.");
            Map<String, Contact> loadedCoworkers;
            try {
                loadedCoworkers = loadCoworkersContacts(account);
                Log.d(TAG,
                        format("Loaded {0} contacts.", loadedCoworkers.size()));
            } catch (NotAuthorizedException exception) {
                Log.e(TAG, "Access denied.", exception);
                return;
            } catch (NotAvailableException exception) {
                Log.e(TAG, "Contacts not available.", exception);
                return;
            }

            checkCanceled();

            Log.d(TAG, "Search contacts in address book.");
            Map<String, SyncedContact> syncedCoworkers = contactsManager
                    .allFromGroup(groupCoworkers);
            Log.d(TAG, format("Found {0} contacts.", syncedCoworkers.size()));

            Log.d(TAG, "Sync contacts.");
            Map<String, SyncedContact> createdCoworkers = syncCreatedContacts(
                    account, groupCoworkers, loadedCoworkers, syncedCoworkers);
            Map<String, SyncedContact> updatedCoworkers = syncUpdatedContacts(
                    account, loadedCoworkers, syncedCoworkers);
            syncRemovedContacts(account, loadedCoworkers, syncedCoworkers);

            Log.d(TAG, "Sync photos.");
            syncedCoworkers = new HashMap<String, SyncedContact>(
                    syncedCoworkers);
            syncedCoworkers.putAll(createdCoworkers);
            syncedCoworkers.putAll(updatedCoworkers);

            syncPhotos(account, syncedCoworkers);

            Log.d(TAG, "Update timestamp.");
            settingsManager.updateLastSyncTimestamp();

            Log.d(TAG, "Sync finished.");
        } catch (SyncCanceledException exception) {
            Log.w(TAG, "Sync canceled.", exception);
        }
    }

    /**
     * Synchronizes group for contacts of coworkers.
     * 
     * @param account
     *            the account of user, who performs operation.
     * 
     * @return the synchronized group.
     * 
     * @throws SyncOperationException
     *             the group could not be synchronized.
     */
    private SyncedGroup syncCoworkersGroup(Account account)
            throws SyncOperationException {
        String name = getContext().getString(R.string.groupCoworkersName);
        String title = settingsManager.getCoworkersTitle();

        SyncedGroup group = groupsManager.findGroup(account, name);
        if (group != null) {
            if (!title.equals(group.getTitle())) {
                return groupsManager.updateTitle(group, title);
            }

            return group;
        }

        return groupsManager.createGroup(account, name, title);
    }

    /**
     * Loads contacts of coworkers.
     * 
     * @param account
     *            the account of user, who performs operation.
     * 
     * @return the contacts of coworkers.
     */
    private Map<String, Contact> loadCoworkersContacts(Account account)
            throws NotAuthorizedException, NotAvailableException,
            SyncCanceledException {
        if (!isSuitableNetwork()) {
            throw new SyncCanceledException("Unsuitable network.");
        }

        String username = account.name;
        AccountManager accountManager = AccountManager.get(getContext());
        String password = accountManager.getPassword(account);

        Contact[] contacts = contactsRepository.getCoworkersContacts(username,
                password);
        Map<String, Contact> loadedContacts = new HashMap<String, Contact>();
        for (Contact contact : contacts) {
            loadedContacts.put(contact.getUsername(), contact);
        }
        return loadedContacts;
    }

    /**
     * Creates new contacts.
     */
    private Map<String, SyncedContact> syncCreatedContacts(Account account,
            SyncedGroup group, Map<String, Contact> loadedContacts,
            Map<String, SyncedContact> syncedContacts)
            throws SyncCanceledException {
        Map<String, SyncedContact> createdContacts = new HashMap<String, SyncedContact>();

        for (Contact loadedContact : loadedContacts.values()) {
            String username = loadedContact.getUsername();
            if (syncedContacts.containsKey(username)) {
                continue;
            }

            checkCanceled();

            try {
                SyncedContact createdContact = contactsManager.createContact(
                        account, group, loadedContact);
                createdContacts.put(createdContact.getUsername(),
                        createdContact);
            } catch (SyncOperationException exception) {
                Log.w(TAG,
                        format("Contact for {0} was not created.", username),
                        exception);
            }
        }

        Log.d(TAG, format("Created {0} contacts.", createdContacts.size()));
        return createdContacts;
    }

    /**
     * Updates existing contacts, if their version differ from synchronized
     * contacts.
     */
    private Map<String, SyncedContact> syncUpdatedContacts(Account account,
            Map<String, Contact> loadedContacts,
            Map<String, SyncedContact> syncedContacts)
            throws SyncCanceledException {
        Map<String, SyncedContact> updatedContacts = new HashMap<String, SyncedContact>();

        for (Contact loadedContact : loadedContacts.values()) {
            String username = loadedContact.getUsername();
            if (!syncedContacts.containsKey(username)) {
                continue;
            }
            SyncedContact syncedContact = syncedContacts.get(username);

            checkCanceled();

            if (loadedContact.getVersion().equals(syncedContact.getVersion())) {
                Log.d(TAG, format("Contact for {0} is up to date.", username));
                continue;
            }

            try {
                SyncedContact updatedContact = contactsManager.updateContact(
                        account, syncedContact, loadedContact);
                updatedContacts.put(updatedContact.getUsername(),
                        updatedContact);
            } catch (SyncOperationException exception) {
                Log.w(TAG,
                        format("Contact for {0} was not updated.", username),
                        exception);
            }
        }

        Log.d(TAG, format("Updated {0} contacts.", updatedContacts.size()));
        return updatedContacts;
    }

    /**
     * Removes obsolete contacts.
     */
    private void syncRemovedContacts(Account account,
            Map<String, Contact> loadedContacts,
            Map<String, SyncedContact> syncedContacts)
            throws SyncCanceledException {
        int removedContactsNum = 0;

        for (SyncedContact syncedContact : syncedContacts.values()) {
            String username = syncedContact.getUsername();
            if (loadedContacts.containsKey(username)) {
                continue;
            }

            checkCanceled();

            try {
                contactsManager.removeContact(account, syncedContact);
                ++removedContactsNum;
            } catch (SyncOperationException exception) {
                Log.w(TAG,
                        format("Contact for {0} was not removed.", username),
                        exception);
            }
        }

        Log.d(TAG, format("Removed {0} contacts.", removedContactsNum));
    }

    /**
     * Synchronizes photos.
     */
    private void syncPhotos(Account account,
            Map<String, SyncedContact> syncedContacts)
            throws SyncCanceledException {
        if (!settingsManager.isSyncPhotos()) {
            Log.d(TAG, "Sync of photos is disabled.");
            return;
        }

        for (SyncedContact syncedContact : syncedContacts.values()) {
            if (syncedContact.isPhotoSynced()) {
                continue;
            }

            checkCanceled();

            try {
                syncPhoto(account, syncedContact);
            } catch (SyncOperationException exception) {
                Log.w(TAG,
                        format("Photo for {0} was not updated.",
                                syncedContact.getUsername()), exception);
            }
        }
    }

    /**
     * Updates photo of contact.
     */
    private void syncPhoto(Account account, SyncedContact syncedContact)
            throws SyncOperationException, SyncCanceledException {
        String photoUrl = syncedContact.getUnsyncedPhotoUrl();

        if (TextUtils.isEmpty(photoUrl)) {
            throw new IllegalArgumentException("URL of photo not defined.");
        }

        Log.d(TAG,
                format("Download photo for {0} from {1}.",
                        syncedContact.getUsername(), photoUrl));
        byte[] photo = loadPhoto(photoUrl);
        contactsManager.updatePhoto(account, syncedContact, photo);
    }

    /**
     * Loads photo for contact.
     * 
     * @param photoUrl
     *            the URL of photo.
     * 
     * @return the loaded photo.
     */
    private byte[] loadPhoto(String photoUrl) throws SyncOperationException,
            SyncCanceledException {
        if (TextUtils.isEmpty(photoUrl)) {
            throw new IllegalArgumentException("URL is required.");
        }

        if (!isSuitableNetwork()) {
            throw new SyncCanceledException("Unsuitable network.");
        }

        try {
            Bitmap photo = contactsRepository.getPhoto(photoUrl);
            ByteArrayOutputStream compressStream = new ByteArrayOutputStream();

            try {
                photo.compress(CompressFormat.PNG, 100, compressStream);
                return compressStream.toByteArray();
            } finally {
                compressStream.close();
            }
        } catch (NotAvailableException exception) {
            throw new SyncOperationException("Could not download photo.",
                    exception);
        } catch (IOException exception) {
            throw new SyncOperationException("Could not convert photo.",
                    exception);
        }
    }

    /**
     * Checks that network is suitable.
     * 
     * @return <code>true</code> if network is suitable and <code>false</code>
     *         otherwise.
     */
    public boolean isSuitableNetwork() {
        if (settingsManager.isSyncAnywhere()) {
            return networkManager.isConnected();
        }

        return networkManager.isConnectedToWiFi();
    }

    /**
     * Checks, that synchronization was canceled.
     */
    private void checkCanceled() throws SyncCanceledException {
        boolean canceled = currentThread().isInterrupted();
        if (canceled) {
            throw new SyncCanceledException("Sync interrupted.");
        }
    }

    /**
     * Thrown if synchronization was cancelled.
     */
    private static class SyncCanceledException extends Exception {

        private static final long serialVersionUID = 1678007155060368790L;

        public SyncCanceledException(String message) {
            super(message);
        }

    }

}
