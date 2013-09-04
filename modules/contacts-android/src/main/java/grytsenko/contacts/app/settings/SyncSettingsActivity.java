package grytsenko.contacts.app.settings;

import grytsenko.contacts.app.R;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.text.TextUtils;
import android.widget.Toast;

/**
 * Contains settings of synchronization.
 * 
 * <p>
 * We use activity instead of fragment to provide backward compatibility.
 */
public class SyncSettingsActivity extends PreferenceActivity {

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        boolean empty = TextUtils.isEmpty((String) newValue);
        if (empty) {
            showToast(R.string.groupTitleEmpty);
        }
        return !empty;
    }

    private void showToast(int messageId) {
        Toast.makeText(this, messageId, Toast.LENGTH_SHORT).show();
    }

}
