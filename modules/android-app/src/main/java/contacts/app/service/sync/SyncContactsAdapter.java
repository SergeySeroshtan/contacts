package contacts.app.service.sync;

import static java.lang.Thread.currentThread;
import static java.text.MessageFormat.format;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;

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
    private ContactsManager contactsManager;

    public SyncContactsAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);

        restClient = new RestClient(context);
        contactsManager = new ContactsManager(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
            ContentProviderClient provider, SyncResult syncResult) {
        Log.d(TAG, "Sync started.");

        try {
            List<Contact> contacts = downloadContacts(account);
            Log.d(TAG, format("Loaded {0} contacts.", contacts.size()));

            checkCanceled();

            String groupTitle = getContext().getString(R.string.groupCoworkers);
            long groupId = contactsManager.getGroup(account, groupTitle);
            Log.d(TAG, format("Group {0} has id {1}.", groupTitle, groupId));

            checkCanceled();

            syncContacts(account, groupId, contacts);

            Log.d(TAG, "Sync was finished.");
        } catch (CanceledException exception) {
            Log.w(TAG, "Sync was canceled.", exception);
        } catch (NotCompletedException exception) {
            Log.e(TAG, "Sync was interrupted.", exception);
        }
    }

    /**
     * Downloads list of contacts for synchronization.
     * 
     * @param account
     *            the account of user, who performs operation.
     * 
     * @return the list of contacts.
     * 
     * @throws NotCompletedException
     *             if contacts could not be loaded.
     */
    private List<Contact> downloadContacts(Account account)
            throws NotCompletedException {
        String username = account.name;
        AccountManager accountManager = AccountManager.get(getContext());
        String password = accountManager.getPassword(account);

        try {
            return restClient.getCoworkers(username, password);
        } catch (NotAvailableException exception) {
            throw new NotCompletedException("Service not available.", exception);
        } catch (NotAuthorizedException exception) {
            throw new NotCompletedException("Invalid credentials.", exception);
        }
    }

    /**
     * Synchronizes contacts.
     * 
     * <p>
     * FIXME Ignores contacts of users, that already exist.
     */
    private void syncContacts(Account account, long groupId,
            List<Contact> contacts) throws CanceledException {
        Set<String> existingContacts = contactsManager
                .getExistingContacts(account);

        for (Contact contact : contacts) {
            String userName = contact.getUserName();
            Log.d(TAG, format("Sync contact for {0}.", userName));

            checkCanceled();

            if (existingContacts.contains(userName)) {
                Log.d(TAG, format("Contact for {0} already exists.", userName));
                continue;
            }

            try {
                byte[] photo = downloadPhoto(contact);
                contactsManager.createContact(account, groupId, contact, photo);
            } catch (NotCompletedException exception) {
                Log.w(TAG, format("Contact for {0} skipped.", userName));
            }
        }
    }

    /**
     * Downloads photo for contact.
     * 
     * @param contact
     *            the data of contact.
     * 
     * @return the photo for contact or <code>null</code> if it could not be
     *         loaded.
     */
    private byte[] downloadPhoto(Contact contact) {
        Log.d(TAG, format("Download photo for {0}.", contact.getUserName()));

        String photoUrl = contact.getPhotoUrl();
        if (StringUtils.isNullOrEmpty(photoUrl)) {
            Log.d(TAG, "Location of photo is not defined.");
            return null;
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
        } catch (NotAvailableException e) {
            Log.d(TAG, "Could not download photo.");
        } catch (IOException exception) {
            Log.d(TAG, "Could not convert photo.");
        }

        return null;
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
