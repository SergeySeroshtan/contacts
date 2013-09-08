package grytsenko.contacts.app.sync;

import static java.text.MessageFormat.format;
import grytsenko.contacts.app.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Manages settings of application.
 */
public class SettingsManager {

    private static final String TAG = SettingsManager.class.getName();

    private final String syncPhotos;
    private final String syncAnywhere;
    private final String lastSyncTime;

    private final String groupCoworkers;
    private final String groupCoworkersDefault;

    private SharedPreferences preferences;
    private PackageInfo packageInfo;

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

        lastSyncTime = context.getString(R.string.lastSyncTime);

        groupCoworkers = context.getString(R.string.groupCoworkers);
        groupCoworkersDefault = context
                .getString(R.string.groupCoworkersDefault);

        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        packageInfo = getPackageInfo(context);
    }

    private static PackageInfo getPackageInfo(Context context) {
        try {
            String name = context.getPackageName();
            PackageManager manager = context.getPackageManager();
            return manager.getPackageInfo(name, 0);
        } catch (NameNotFoundException exception) {
            throw new IllegalStateException("Could not get package info.",
                    exception);
        }
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
     * Returns the time of last sync.
     */
    public long getLastSyncTime() {
        return preferences.getLong(lastSyncTime, 0);
    }

    /**
     * Sets the time of last sync to the current system time.
     */
    public void updateLastSyncTime() {
        Editor editor = preferences.edit();
        long time = System.currentTimeMillis();
        editor.putLong(lastSyncTime, time);
        editor.commit();

        Log.d(TAG,
                format("Time of last sync is {0}.", Long.toString(time)));
    }

    /**
     * Checks that application was updated since the last sync.
     * 
     * @return <code>true</code> if application was updated and
     *         <code>false</code> otherwise.
     */
    public boolean isAppUpdated() {
        long lastSyncTime = getLastSyncTime();

        return lastSyncTime < packageInfo.lastUpdateTime;
    }

}
