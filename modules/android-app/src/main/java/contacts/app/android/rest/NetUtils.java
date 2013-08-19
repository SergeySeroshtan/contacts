package contacts.app.android.rest;

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
     * @param location
     *            the location of bitmap, that is represented with URL.
     * 
     * @return the loaded bitmap.
     * 
     * @throws NotAvailableException
     *             if bitmap could not be loaded or has invalid format.
     */
    public static Bitmap downloadBitmap(String location)
            throws NotAvailableException {
        try {
            URL url = new URL(location);
            HttpURLConnection connection = (HttpURLConnection) url
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
