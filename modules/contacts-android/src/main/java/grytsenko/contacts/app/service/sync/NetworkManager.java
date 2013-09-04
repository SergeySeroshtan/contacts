package grytsenko.contacts.app.service.sync;

import static java.text.MessageFormat.format;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Helps manage network.
 */
public class NetworkManager {

    private static final String TAG = NetworkManager.class.getName();

    private ConnectivityManager connectivityManager;

    /**
     * Creates manager in the specified context.
     * 
     * @param context
     *            the context, where manager is used.
     */
    public NetworkManager(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context not defined.");
        }

        connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    /**
     * Checks that device is connected to network.
     * 
     * @return <code>true</code> if device is connected to network and
     *         <code>false</code> otherwise.
     */
    public boolean isConnected() {
        Log.d(TAG, "Check connection to network.");

        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

        boolean connected = checkConnected(activeNetwork);

        return connected;
    }

    /**
     * Checks that device is connected to Wi-Fi network.
     * 
     * @return <code>true</code> if device is connected to Wi-Fi network and
     *         <code>false</code> otherwise.
     */
    public boolean isConnectedToWiFi() {
        Log.d(TAG, "Check connection to Wi-Fi network.");

        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

        boolean connected = checkConnected(activeNetwork);

        return connected
                && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
    }

    /**
     * Checks that device is connected to specified network.
     */
    private boolean checkConnected(NetworkInfo network) {
        boolean connected = network != null && network.isConnected();

        if (connected) {
            String name = network.getTypeName();
            Log.d(TAG, format("Device connected to {0} network.", name));
        } else {
            Log.d(TAG, "Device is not connected to network.");
        }

        return connected;
    }

}
