package grytsenko.contacts.app.service.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Service for synchronization of contacts.
 */
public class SyncContactsService extends Service {

    private static final String TAG = SyncContactsService.class.getName();

    private SyncContactsAdapter adapter;

    @Override
    public void onCreate() {
        super.onCreate();

        adapter = new SyncContactsAdapter(this, true);

        Log.d(TAG, "Service created.");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return adapter.getSyncAdapterBinder();
    }

}
