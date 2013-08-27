package grytsenko.contacts.app.service.sync;

import grytsenko.contacts.app.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Helps manage settings.
 */
public class SettingsManager {

    private final String syncPhotosKey;
    private final String syncAnywhereKey;

    private final String groupCoworkersKey;
    private final String groupCoworkersDefault;

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

        groupCoworkersKey = context.getString(R.string.groupCoworkersKey);
        groupCoworkersDefault = context
                .getString(R.string.groupCoworkersDefault);
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

    /**
     * Returns the custom title of group for coworkers.
     * 
     * @return the group title.
     */
    public String getCoworkersTitle() {
        return preferences.getString(groupCoworkersKey, groupCoworkersDefault);
    }

}
