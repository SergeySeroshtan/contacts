package contacts.app.android.repository;

import static java.text.MessageFormat.format;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.util.Log;
import contacts.app.android.R;
import contacts.app.android.rest.AuthorizationException;
import contacts.app.android.rest.NetworkException;
import contacts.app.android.rest.RestClient;
import contacts.model.Contact;

/**
 * Remote repository that provides access through REST.
 */
public class ContactsRepositoryRest implements ContactsRepository {

    private static final String TAG = ContactsRepositoryRest.class.getName();

    private static final String JSON_USER_NAME = "userName";
    private static final String JSON_FIRST_NAME = "firstName";
    private static final String JSON_LAST_NAME = "lastName";
    private static final String JSON_PHONE = "phone";
    private static final String JSON_MAIL = "mail";
    private static final String JSON_LOCATION = "location";

    private Context context;
    private AccountManager accountManager;

    private RestClient restClient;

    public ContactsRepositoryRest(Context context) {
        this.context = context;
        this.accountManager = AccountManager.get(context);

        this.restClient = new RestClient();
    }

    public List<Contact> findByOffice(Account account)
            throws AuthorizationException, NetworkException {
        String username = account.name;

        Log.d(TAG, format("Find by office for {0}.", username));

        URI uri = resolveUri(context.getString(R.string.restPathSearchContacts));
        String content = restClient.doGet(username,
                accountManager.getPassword(account), uri);

        try {
            return parseContacts(content);
        } catch (JSONException exception) {
            throw new NetworkException("Invalid data format.", exception);
        }
    }

    /**
     * Parses JSON and creates a list of contacts.
     */
    private List<Contact> parseContacts(String content) throws JSONException {
        JSONArray jsonContacts = new JSONArray(content);
        List<Contact> contacts = new ArrayList<Contact>();
        for (int i = 0; i < jsonContacts.length(); ++i) {
            JSONObject jsonContact = jsonContacts.getJSONObject(i);
            contacts.add(parseContact(jsonContact));
        }
        return contacts;
    }

    private URI resolveUri(String path) throws NetworkException {
        try {
            return new URI(context.getString(R.string.restScheme),
                    context.getString(R.string.restAuthority), path, null, null);
        } catch (URISyntaxException exception) {
            throw new NetworkException("Invalid URI.", exception);
        }
    }

    /**
     * Creates a contact from JSON object.
     */
    private static Contact parseContact(JSONObject json) throws JSONException {
        Contact contact = new Contact();

        contact.setUserName(json.getString(JSON_USER_NAME));
        contact.setFirstName(json.getString(JSON_FIRST_NAME));
        contact.setLastName(json.getString(JSON_LAST_NAME));
        contact.setPhone(json.getString(JSON_PHONE));
        contact.setMail(json.getString(JSON_MAIL));
        contact.setLastName(json.getString(JSON_LOCATION));

        return contact;
    }

}
