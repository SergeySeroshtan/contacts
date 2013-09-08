package grytsenko.contacts.app.service.sync;

import static java.text.MessageFormat.format;
import grytsenko.contacts.app.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Helps manage settings.
 */
public class SettingsManager {

    private static final String TAG = SettingsManager.class.getName();

    private final String syncPhotosKey;
    private final String syncAnywhereKey;

    private final String syncTimestampKey;

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

        syncPhotosKey = context.getString(R.string.syncPhotosKey);
        syncAnywhereKey = context.getString(R.string.syncAnywhereKey);

        syncTimestampKey = context.getString(R.string.syncTimestampKey);

        groupCoworkersKey = context.getString(R.string.groupCoworkersKey);
        groupCoworkersDefault = context
                .getString(R.string.groupCoworkersDefault);

        preferences = PreferenceManager.getDefaultSharedPreferences(context);
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

    /**
     * Returns the timestamp of last sync.
     */
    public long getLastSyncTimestamp() {
        return preferences.getLong(syncTimestampKey, 0);
    }

    /**
     * Sets the timestamp of last sync to the current system time.
     */
    public void updateLastSyncTimestamp() {
        Editor editor = preferences.edit();
        long timestamp = System.currentTimeMillis();
        editor.putLong(syncTimestampKey, timestamp);
        editor.commit();

        Log.d(TAG,
                format("Timestamp of last sync is {0}.",
                        Long.toString(timestamp)));
    }

}
