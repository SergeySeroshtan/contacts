package contacts.app.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import contacts.app.R;

/**
 * Contains settings of synchronization.
 */
public class SyncSettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.sync_settings);
    }

}
