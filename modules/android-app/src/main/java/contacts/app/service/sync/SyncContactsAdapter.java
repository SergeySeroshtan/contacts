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
import contacts.app.android.R;
import contacts.app.data.NetUtils;
import contacts.app.data.NotAuthorizedException;
import contacts.app.data.NotAvailableException;
import contacts.app.data.RestClient;
import contacts.model.Contact;
import contacts.util.StringUtils;

/**
 * Synchronizes contacts.
 * 
 * <p>
 * If user cancels synchronization, then process will be safely interrupted.
 */
public class SyncContactsAdapter extends AbstractThreadedSyncAdapter {

    private static final String TAG = SyncContactsAdapter.class.getName();

    private RestClient restClient;

    private GroupsManager groupsManager;
    private ContactsManager contactsManager;

    public SyncContactsAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);

        restClient = new RestClient(context);

        groupsManager = new GroupsManager(context);
        contactsManager = new ContactsManager(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
            ContentProviderClient provider, SyncResult syncResult) {
        Log.d(TAG, "Sync started.");

        try {
            Map<String, Contact> syncedContacts = downloadContacts(account);
            Log.d(TAG, format("Loaded {0} contacts.", syncedContacts.size()));

            checkCanceled();

            String groupTitle = getContext().getString(R.string.groupCoworkers);
            long groupId = groupsManager.getGroup(account, groupTitle);
            Log.d(TAG, format("Group {0} has id {1}.", groupTitle, groupId));

            checkCanceled();

            Map<String, KnownContact> knownContacts = contactsManager
                    .getKnownContacts(account);

            createContacts(account, groupId, syncedContacts, knownContacts);
            updateContacts(account, syncedContacts, knownContacts);
            removeContacts(account, syncedContacts, knownContacts);

            Log.d(TAG, "Sync was finished.");
        } catch (CanceledException exception) {
            Log.w(TAG, "Sync was canceled.", exception);
        } catch (NotCompletedException exception) {
            Log.e(TAG, "Sync was interrupted.", exception);
        }
    }

    /**
     * Downloads contacts.
     * 
     * @param account
     *            the account of user, who performs operation.
     * 
     * @return the contacts to be synchronized.
     * 
     * @throws NotCompletedException
     *             if contacts could not be loaded.
     */
    private Map<String, Contact> downloadContacts(Account account)
            throws NotCompletedException {
        String username = account.name;
        AccountManager accountManager = AccountManager.get(getContext());
        String password = accountManager.getPassword(account);

        try {
            Contact[] contacts = restClient.getCoworkers(username, password);
            Map<String, Contact> syncedContacts = new HashMap<String, Contact>();
            for (Contact contact : contacts) {
                syncedContacts.put(contact.getUsername(), contact);
            }
            return syncedContacts;
        } catch (NotAvailableException exception) {
            throw new NotCompletedException("Service not available.", exception);
        } catch (NotAuthorizedException exception) {
            throw new NotCompletedException("Invalid credentials.", exception);
        }
    }

    /**
     * Creates new contacts.
     */
    private void createContacts(Account account, long groupId,
            Map<String, Contact> syncedContacts,
            Map<String, KnownContact> knownContacts) {
        for (Contact syncedContact : syncedContacts.values()) {
            String username = syncedContact.getUsername();
            if (knownContacts.containsKey(username)) {
                continue;
            }

            try {
                Log.d(TAG, format("Create contact for {0}.", username));
                KnownContact knownContact = contactsManager.createContact(
                        account, groupId, syncedContact);
                Log.d(TAG, format("Contact for {0} was created.", username));

                Log.d(TAG, format("Update photo for {0}.", username));
                updatePhoto(account, knownContact, syncedContact.getPhotoUrl());
            } catch (NotCompletedException exception) {
                Log.w(TAG,
                        format("Contact for {0} was not created.", username),
                        exception);
            }
        }
    }

    /**
     * Updates existing contacts, if their version differ from synchronized
     * contacts.
     */
    private void updateContacts(Account account,
            Map<String, Contact> syncedContacts,
            Map<String, KnownContact> knownContacts) {
        for (Contact syncedContact : syncedContacts.values()) {
            String username = syncedContact.getUsername();
            if (!knownContacts.containsKey(username)) {
                continue;
            }
            KnownContact knownContact = knownContacts.get(username);

            if (syncedContact.getVersion().equals(knownContact.getVersion())) {
                Log.d(TAG, format("Contact for {0} is up to date.", username));
                continue;
            }

            try {
                Log.d(TAG, format("Update contact for {0}.", username));
                contactsManager.updateContact(account, knownContact,
                        syncedContact);
                Log.d(TAG, format("Contact for {0} was updated.", username));

                Log.d(TAG, format("Update photo for {0}.", username));
                updatePhoto(account, knownContact, syncedContact.getPhotoUrl());
            } catch (NotCompletedException exception) {
                Log.w(TAG,
                        format("Contact for {0} was not updated.", username),
                        exception);
            }
        }
    }

    /**
     * Removes obsolete contacts.
     */
    private void removeContacts(Account account,
            Map<String, Contact> syncedContacts,
            Map<String, KnownContact> knownContacts) {
        for (KnownContact knownContact : knownContacts.values()) {
            String username = knownContact.getUsername();
            if (syncedContacts.containsKey(username)) {
                continue;
            }

            try {
                Log.d(TAG, format("Remove contact for {0}.", username));
                contactsManager.removeContact(account, knownContact);
                Log.d(TAG, format("Contact for {0} was removed.", username));
            } catch (NotCompletedException exception) {
                Log.w(TAG,
                        format("Contact for {0} was not removed.", username),
                        exception);
            }
        }
    }

    /**
     * Updates photo of contact.
     */
    private void updatePhoto(Account account, KnownContact knownContact,
            String photoUrl) throws NotCompletedException {
        if (StringUtils.isNullOrEmpty(photoUrl)) {
            return;
        }

        Log.d(TAG,
                format("Download photo for {0} from {1}.",
                        knownContact.getUsername(), photoUrl));
        byte[] photo = downloadPhoto(photoUrl);
        contactsManager.updateContactPhoto(account, knownContact, photo);
    }

    /**
     * Downloads photo for contact.
     * 
     * @param photoUrl
     *            the URL of photo.
     * 
     * @return the loaded photo.
     */
    private byte[] downloadPhoto(String photoUrl) throws NotCompletedException {
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
            throw new NotCompletedException("Could not download photo.",
                    exception);
        } catch (IOException exception) {
            throw new NotCompletedException("Could not convert photo.",
                    exception);
        }
    }

    /**
     * Checks, that synchronization was canceled.
     */
    private void checkCanceled() throws CanceledException {
        boolean canceled = currentThread().isInterrupted();
        if (canceled) {
            throw new CanceledException();
        }
    }

    /**
     * Thrown if synchronization was cancelled.
     */
    private static class CanceledException extends Exception {

        private static final long serialVersionUID = 1678007155060368790L;

    }

}
