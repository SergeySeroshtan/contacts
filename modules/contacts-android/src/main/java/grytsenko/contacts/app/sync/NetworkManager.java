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
package grytsenko.contacts.app.sync;

import static java.text.MessageFormat.format;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

/**
 * Manages status of network.
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
