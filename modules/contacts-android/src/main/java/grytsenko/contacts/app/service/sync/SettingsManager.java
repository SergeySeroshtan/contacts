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

    private final String syncPhotos;
    private final String syncAnywhere;
    private final String syncTimestamp;

    private final String groupCoworkers;
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

        syncPhotos = context.getString(R.string.syncPhotos);
        syncAnywhere = context.getString(R.string.syncAnywhere);

        syncTimestamp = context.getString(R.string.syncTimestamp);

        groupCoworkers = context.getString(R.string.groupCoworkers);
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
        return preferences.getBoolean(syncPhotos, false);
    }

    /**
     * Checks that sync can be performed in networks of any type.
     * 
     * @return <code>true</code> if sync is allowed and <code>false</code>
     *         otherwise.
     */
    public boolean isSyncAnywhere() {
        return preferences.getBoolean(syncAnywhere, false);
    }

    /**
     * Returns the custom title of group for coworkers.
     * 
     * @return the group title.
     */
    public String getCoworkersTitle() {
        return preferences.getString(groupCoworkers, groupCoworkersDefault);
    }

    /**
     * Returns the timestamp of last sync.
     */
    public long getLastSyncTimestamp() {
        return preferences.getLong(syncTimestamp, 0);
    }

    /**
     * Sets the timestamp of last sync to the current system time.
     */
    public void updateLastSyncTimestamp() {
        Editor editor = preferences.edit();
        long timestamp = System.currentTimeMillis();
        editor.putLong(syncTimestamp, timestamp);
        editor.commit();

        Log.d(TAG,
                format("Timestamp of last sync is {0}.",
                        Long.toString(timestamp)));
    }

}
