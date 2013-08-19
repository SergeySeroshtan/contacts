package contacts.app.data;

import static java.text.MessageFormat.format;
import static java.util.Collections.singletonList;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpBasicAuthentication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import android.content.Context;
import android.util.Log;
import contacts.app.android.R;
import contacts.model.Contact;

/**
 * Client to access REST services for contacts.
 */
public final class RestClient {

    private static final String TAG = RestClient.class.getName();

    private Context context;

    /**
     * Creates a client in the given context.
     */
    public RestClient(Context context) {
        this.context = context;
    }

    /**
     * Gets the contact of user.
     * 
     * <p>
     * Can be used to check credentials.
     * 
     * @param username
     *            the name of user.
     * @param password
     *            the password of user.
     * 
     * @return the contact of user.
     * 
     * @throws NotAvailableException
     *             if service is not available.
     * @throws NotAuthorizedException
     *             if user is not authorized.
     */
    public Contact getMy(String username, String password)
            throws NotAvailableException, NotAuthorizedException {
        Log.d(TAG, format("Get contact for {0}.", username));

        URI url = buildUrl(context.getString(R.string.restPathMyContact));
        return doGet(url, username, password, Contact.class);
    }

    /**
     * Gets contacts of all people from the one office with user.
     * 
     * @param username
     *            the name of user.
     * @param password
     *            the password of user.
     * 
     * @return the contacts of people, including contact for user.
     * 
     * @throws NotAvailableException
     *             if service is not available.
     * @throws NotAuthorizedException
     *             if user is not authorized.
     */
    public List<Contact> getCoworkers(String username, String password)
            throws NotAvailableException, NotAuthorizedException {
        Log.d(TAG, format("Find by office for {0}.", username));

        URI url = buildUrl(context.getString(R.string.restPathSearchContacts));
        Contact[] contacts = doGet(url, username, password, Contact[].class);
        return Arrays.asList(contacts);
    }

    private URI buildUrl(String path) throws NotAvailableException {
        String scheme = context.getString(R.string.restScheme);
        String authority = context.getString(R.string.restAuthority);
        try {
            return new URI(scheme, authority, path, null, null);
        } catch (URISyntaxException exception) {
            throw new NotAvailableException("Invalid URL.", exception);
        }
    }

    public static <T> T doGet(URI url, String username, String password,
            Class<T> responseClass) throws NotAvailableException,
            NotAuthorizedException {
        Log.d(TAG, format("Send GET request to {0}.", url.toString()));

        RestTemplate template = new RestTemplate();
        template.getMessageConverters().add(
                new MappingJacksonHttpMessageConverter());

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(singletonList(MediaType.APPLICATION_JSON));
        headers.setAuthorization(new HttpBasicAuthentication(username, password));
        HttpEntity<?> request = new HttpEntity<Object>(headers);

        try {
            ResponseEntity<T> response = template.exchange(url, HttpMethod.GET,
                    request, responseClass);
            if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new NotAuthorizedException("Invalid credentials.");
            }

            return response.getBody();
        } catch (RestClientException exception) {
            throw new NotAvailableException("REST-service is not available.",
                    exception);
        }
    }

}
