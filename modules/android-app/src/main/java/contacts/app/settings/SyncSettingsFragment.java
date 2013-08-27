package contacts.app.settings;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.util.Log;
import android.widget.Toast;
import contacts.app.R;
import contacts.util.StringUtils;

/**
 * Contains settings of synchronization.
 */
public class SyncSettingsFragment extends PreferenceFragment {

    private static final String TAG = SyncSettingsFragment.class.getName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.sync_settings);

        Preference syncPhotos = findPreference(getString(R.string.syncPhotosKey));
        syncPhotos
                .setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

                    @Override
                    public boolean onPreferenceChange(Preference preference,
                            Object newValue) {
                        if (Boolean.TRUE.equals(newValue)) {
                            showToast(R.string.syncPhotosWarning);
                        }
                        return true;
                    }
                });

        Preference syncAnywhere = findPreference(getString(R.string.syncAnywhereKey));
        syncAnywhere
                .setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

                    @Override
                    public boolean onPreferenceChange(Preference preference,
                            Object newValue) {
                        if (Boolean.TRUE.equals(newValue)) {
                            showToast(R.string.syncAnywhereWarning);
                        }
                        return true;
                    }

                });

        Preference coworkers = findPreference(getString(R.string.groupCoworkersKey));
        coworkers
                .setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

                    @Override
                    public boolean onPreferenceChange(Preference preference,
                            Object newValue) {
                        return validateGroupTitle(newValue);
                    }

                });
    }

    private boolean validateGroupTitle(Object newValue) {
        boolean empty = StringUtils.isNullOrEmpty((String) newValue);
        if (empty) {
            Log.d(TAG, "Name of group could not be empty.");
            showToast(R.string.groupTitleEmpty);
        }
        return !empty;
    }

    private void showToast(int messageId) {
        Toast.makeText(getActivity(), messageId, Toast.LENGTH_SHORT).show();
    }

}
