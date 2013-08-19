package contacts.app.service.account;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Service that allows user to create new account.
 */
public class SignInService extends Service {

    private static final String TAG = SignInService.class.getName();

    private BasicAutheticator autheticator;

    @Override
    public void onCreate() {
        super.onCreate();

        autheticator = new BasicAutheticator(this);

        Log.d(TAG, "Service created.");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return autheticator.getIBinder();
    }

}
