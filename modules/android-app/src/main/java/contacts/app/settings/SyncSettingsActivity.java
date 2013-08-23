package contacts.app.settings;

import android.app.Activity;
import android.os.Bundle;

/**
 * Activity, that contains settings of synchronization. Actually it simply wraps
 * an appropriate fragment.
 */
public class SyncSettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SyncSettingsFragment())
                .commit();
    }

}
