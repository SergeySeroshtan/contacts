package contacts.app.service.sync;

import static java.lang.Thread.currentThread;
import static java.text.MessageFormat.format;

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
import android.util.Log;
import contacts.app.R;
import contacts.app.data.NetUtils;
import contacts.app.data.NotAuthorizedException;
import contacts.app.data.NotAvailableException;
import contacts.app.data.RestClient;
import contacts.model.Contact;
import contacts.util.StringUtils;

/**
 * Synchronizes contacts.
 */
public class SyncContactsAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = SyncContactsAdapter.class.getName();

    private RestClient restClient;

    private GroupsManager groupsManager;
    private ContactsManager contactsManager;
    private SettingsManager settingsManager;
    private NetworkManager networkManager;

    public SyncContactsAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);

        restClient = new RestClient(context);

        groupsManager = new GroupsManager(context);
        contactsManager = new ContactsManager(context);
        settingsManager = new SettingsManager(context);
        networkManager = new NetworkManager(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
            ContentProviderClient provider, SyncResult syncResult) {
        if (!isSuitableNetwork()) {
            Log.d(TAG, "Device is not connected to suitable network.");
            return;
        }

        Log.d(TAG, "Sync started.");

        try {
            /*
             * Synchronize group, which contains contacts of coworkers.
             */
            SyncedGroup groupCoworkers;
            try {
                groupCoworkers = syncGroupForCoworkers(account);
            } catch (SyncOperationException exception) {
                Log.e(TAG, "Could not sync group for coworkers.", exception);
                return;
            }

            checkCanceled();

            /*
             * Load actual contacts of coworkers.
             */
            Map<String, Contact> loadedCoworkers;
            try {
                loadedCoworkers = loadContactsOfCoworkers(account);
            } catch (NotAuthorizedException exception) {
                Log.e(TAG, "Could not access contacts of coworkers.", exception);
                return;
            } catch (NotAvailableException exception) {
                Log.e(TAG, "Could not get contacts of coworkers.", exception);
                return;
            }

            checkCanceled();

            /*
             * Synchronize contacts of coworkers.
             */
            Map<String, SyncedContact> syncedCoworkers = contactsManager
                    .allFromGroup(groupCoworkers);

            Map<String, SyncedContact> createdCoworkers = syncCreatedContacts(
                    account, groupCoworkers, loadedCoworkers, syncedCoworkers);
            Map<String, SyncedContact> updatedCoworkers = syncUpdatedContacts(
                    account, loadedCoworkers, syncedCoworkers);
            syncRemovedContacts(account, loadedCoworkers, syncedCoworkers);

            syncedCoworkers = new HashMap<String, SyncedContact>(
                    syncedCoworkers);
            syncedCoworkers.putAll(createdCoworkers);
            syncedCoworkers.putAll(updatedCoworkers);

            syncPhotos(account, syncedCoworkers);

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
    private SyncedGroup syncGroupForCoworkers(Account account)
            throws SyncOperationException {
        String groupCoworkersName = getContext().getString(
                R.string.groupCoworkersName);
        String groupCoworkersTitle = getContext().getString(
                R.string.groupCoworkersTitle);

        SyncedGroup groupCoworkers = groupsManager.findGroup(account,
                groupCoworkersName);
        if (groupCoworkers != null) {
            return groupCoworkers;
        }

        return groupsManager.createGroup(account, groupCoworkersName,
                groupCoworkersTitle);
    }

    /**
     * Loads contacts of coworkers.
     * 
     * @param account
     *            the account of user, who performs operation.
     * 
     * @return the contacts of coworkers.
     */
    private Map<String, Contact> loadContactsOfCoworkers(Account account)
            throws NotAuthorizedException, NotAvailableException {
        String username = account.name;
        AccountManager accountManager = AccountManager.get(getContext());
        String password = accountManager.getPassword(account);

        Contact[] contacts = restClient.getCoworkers(username, password);
        Map<String, Contact> loadedContacts = new HashMap<String, Contact>();
        for (Contact contact : contacts) {
            loadedContacts.put(contact.getUsername(), contact);
        }
        Log.d(TAG,
                format("Loaded {0} contacts of coworkers.",
                        loadedContacts.size()));
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
        for (SyncedContact syncedContact : syncedContacts.values()) {
            String username = syncedContact.getUsername();
            if (loadedContacts.containsKey(username)) {
                continue;
            }

            checkCanceled();

            try {
                contactsManager.removeContact(account, syncedContact);
            } catch (SyncOperationException exception) {
                Log.w(TAG,
                        format("Contact for {0} was not removed.", username),
                        exception);
            }
        }
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
            throws SyncOperationException {
        String photoUrl = syncedContact.getUnsyncedPhotoUrl();

        if (StringUtils.isNullOrEmpty(photoUrl)) {
            throw new IllegalArgumentException("URL of photo not defined.");
        }

        Log.d(TAG,
                format("Download photo for {0} from {1}.",
                        syncedContact.getUsername(), photoUrl));
        byte[] photo = loadPhoto(photoUrl);
        contactsManager.updateContactPhoto(account, syncedContact, photo);
    }

    /**
     * Loads photo for contact.
     * 
     * @param photoUrl
     *            the URL of photo.
     * 
     * @return the loaded photo.
     */
    private byte[] loadPhoto(String photoUrl) throws SyncOperationException {
        if (StringUtils.isNullOrEmpty(photoUrl)) {
            throw new IllegalArgumentException("URL is required.");
        }

        try {
            Bitmap photo = NetUtils.downloadBitmap(photoUrl);
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
            throw new SyncCanceledException();
        }
    }

    /**
     * Thrown if synchronization was cancelled.
     */
    private static class SyncCanceledException extends Exception {

        private static final long serialVersionUID = 1678007155060368790L;

    }

}
