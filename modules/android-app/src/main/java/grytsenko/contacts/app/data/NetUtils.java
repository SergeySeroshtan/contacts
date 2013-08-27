package grytsenko.contacts.app.data;

import grytsenko.contacts.common.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Utilities for work with network resources.
 */
public final class NetUtils {

    /**
     * Downloads bitmap from the given location.
     * 
     * @param url
     *            the URL of bitmap.
     * 
     * @return the loaded bitmap.
     * 
     * @throws NotAvailableException
     *             if bitmap could not be loaded or has invalid format.
     */
    public static Bitmap downloadBitmap(String url)
            throws NotAvailableException {
        if (StringUtils.isNullOrEmpty(url)) {
            throw new IllegalArgumentException("URL not defined.");
        }

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

    private NetUtils() {
    }

}
