package contacts.app.service.sync;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import contacts.app.R;

/**
 * Helps manage settings.
 */
public class SettingsManager {

    private final String syncPhotosKey;
    private final String syncAnywhereKey;

    private SharedPreferences preferences;

    /**
     * Creates manager in the specified context.
     * 
     * @param context
     *            the context, where manager is used.
     */
    public SettingsManager(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context not defined.");
        }

        preferences = PreferenceManager.getDefaultSharedPreferences(context);

        syncPhotosKey = context.getString(R.string.syncPhotosKey);
        syncAnywhereKey = context.getString(R.string.syncAnywhereKey);
    }

    /**
     * Checks that sync of photos is allowed.
     * 
     * @return <code>true</code> if sync is allowed and <code>false</code>
     *         otherwise.
     */
    public boolean isSyncPhotos() {
        return preferences.getBoolean(syncPhotosKey, false);
    }

    /**
     * Checks that sync can be performed in networks of any type.
     * 
     * @return <code>true</code> if sync is allowed and <code>false</code>
     *         otherwise.
     */
    public boolean isSyncAnywhere() {
        return preferences.getBoolean(syncAnywhereKey, false);
    }

}
