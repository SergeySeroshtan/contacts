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
    }

    /**
     * Checks that sync of photos is enabled.
     * 
     * @return <code>true</code> if sync is enabled and <code>false</code>
     *         otherwise.
     */
    public boolean isPhotosSynced() {
        return preferences.getBoolean(syncPhotosKey, false);
    }

}
