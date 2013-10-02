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
package grytsenko.contacts.app.data;

import static java.text.MessageFormat.format;
import static java.util.Collections.singletonList;
import grytsenko.contacts.api.Contact;
import grytsenko.contacts.api.MobileAppHeaders;
import grytsenko.contacts.app.R;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.springframework.http.HttpBasicAuthentication;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonHttpMessageConverter;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

/**
 * Remote repository of contacts.
 * 
 * <p>
 * We access repository through REST API.
 */
public final class ContactsRepository {

    private static final String TAG = ContactsRepository.class.getName();

    private Context context;

    /**
     * Creates a client in the given context.
     * 
     * @param context
     *            the context, where repository is used.
     */
    public ContactsRepository(Context context) {
        this.context = context;
    }

    /**
     * Gets contact of user.
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
     *             if repository is not available.
     * @throws NotAuthorizedException
     *             if user is not authorized to use repository.
     */
    public Contact getMyContact(String username, String password)
            throws NotAvailableException, NotAuthorizedException {
        Log.d(TAG, format("Get contact {0}.", username));

        URI url = buildUrl(context.getString(R.string.rest_path_my));
        return doGet(url, username, password, Contact.class);
    }

    /**
     * Gets contacts of coworkers.
     * 
     * @param username
     *            the name of user.
     * @param password
     *            the password of user.
     * 
     * @return the contacts of coworkers.
     * 
     * @throws NotAvailableException
     *             if repository is not available.
     * @throws NotAuthorizedException
     *             if user is not authorized to use repository.
     */
    public Contact[] getMyCoworkers(String username, String password)
            throws NotAvailableException, NotAuthorizedException {
        Log.d(TAG, format("Get coworkers of {0}.", username));

        URI url = buildUrl(context.getString(R.string.rest_path_coworkers));
        return doGet(url, username, password, Contact[].class);
    }

    /**
     * Downloads photo.
     * 
     * @param url
     *            the URL of photo.
     * 
     * @return the loaded photo.
     * 
     * @throws NotAvailableException
     *             if photo could not be loaded or has invalid format.
     */
    public Bitmap getPhoto(String url) throws NotAvailableException {
        if (TextUtils.isEmpty(url)) {
            throw new IllegalArgumentException("URL not defined.");
        }

        Log.d(TAG, format("Load photo from {0}.", url));
        try {
            URL validUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) validUrl
                    .openConnection();
            try {
                InputStream stream = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(stream);
                if (bitmap == null) {
                    throw new NotAvailableException("Invalid format.");
                }
                return bitmap;
            } finally {
                connection.disconnect();
            }
        } catch (MalformedURLException exception) {
            throw new NotAvailableException("Invalid URL.", exception);
        } catch (IOException exception) {
            throw new NotAvailableException("Download failed.", exception);
        }
    }

    private URI buildUrl(String path) throws NotAvailableException {
        String scheme = context.getString(R.string.rest_scheme);
        String authority = context.getString(R.string.rest_authority);
        try {
            return new URI(scheme, authority, path, null, null);
        } catch (URISyntaxException exception) {
            throw new NotAvailableException("Invalid URL.", exception);
        }
    }

    private static <T> T doGet(URI url, String username, String password,
            Class<T> responseClass) throws NotAvailableException,
            NotAuthorizedException {
        Log.d(TAG, format("Send GET request to {0}.", url.toString()));

        RestTemplate template = new RestTemplate();
        template.getMessageConverters().add(
                new MappingJacksonHttpMessageConverter());

        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(singletonList(MediaType.APPLICATION_JSON));
        headers.setAuthorization(new HttpBasicAuthentication(username, password));
        headers.set(MobileAppHeaders.Platform.HEADER_NAME,
                MobileAppHeaders.Platform.ANDROID);
        HttpEntity<?> request = new HttpEntity<Object>(headers);

        try {
            ResponseEntity<T> response = template.exchange(url, HttpMethod.GET,
                    request, responseClass);
            return response.getBody();
        } catch (HttpClientErrorException exception) {
            if (exception.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                throw new NotAuthorizedException("Invalid credentials.",
                        exception);
            }

            throw new NotAvailableException("HTTP Error.", exception);
        } catch (RestClientException exception) {
            throw new NotAvailableException("REST Error.", exception);
        } catch (Exception exception) {
            throw new NotAvailableException("Unknown error.", exception);
        }
    }

}
