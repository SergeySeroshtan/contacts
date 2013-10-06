/**
 * Copyright (C) 2013 Anton Grytsenko (anthony.grytsenko@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grytsenko.contacts.app.service;

import grytsenko.contacts.app.R;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

/**
 * Manages notifications about status of synchronization.
 */
public class StatusService extends IntentService {

    /**
     * The name of service.
     */
    private static final String SERVICE_NAME = StatusService.class.getName();

    /**
     * Notifies user that sync completed.
     */
    public static final String NOTIFY_COMPLETED = SERVICE_NAME
            + ".NOTIFY_COMPLETED";
    /**
     * Notifies user that he is not authorized.
     */
    public static final String NOTIFY_USER_NOT_AUTHORIZED = SERVICE_NAME
            + ".NOTIFY_USER_NOT_AUTHORIZED";
    /**
     * Notifies user that server not available.
     */
    public static final String NOTIFY_DATA_NOT_AVAILABLE = SERVICE_NAME
            + ".NOTIFY_DATA_NOT_AVAILABLE";
    /**
     * Notifies user that sync failed due to internal error.
     */
    public static final String NOTIFY_INTERNAL_ERROR = SERVICE_NAME
            + ".NOTIFY_INTERNAL_ERROR";

    /**
     * Cancels status notification.
     */
    private static final String CANCEL_STATUS = SERVICE_NAME
            + ".ACTION_CANCEL_STATUS";
    /**
     * Identifier for status notification.
     */
    private static final int STATUS_NOFITICATION = 0;

    /**
     * Creates service.
     */
    public StatusService() {
        super(SERVICE_NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        String action = intent.getAction();
        if (NOTIFY_COMPLETED.equals(action)) {
            notify(manager, R.string.sync_completed);
        } else if (NOTIFY_USER_NOT_AUTHORIZED.equals(action)) {
            notify(manager, R.string.sync_user_not_authorized);
        } else if (NOTIFY_DATA_NOT_AVAILABLE.equals(action)) {
            notify(manager, R.string.sync_data_not_available);
        } else if (NOTIFY_INTERNAL_ERROR.equals(action)) {
            notify(manager, R.string.sync_internal_error);
        } else if (CANCEL_STATUS.equals(action)) {
            cancel(manager);
        }
    }

    private void notify(NotificationManager manager, int textId) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                this);
        builder.setContentTitle(getString(R.string.app_name));
        builder.setContentText(getString(textId));
        builder.setSmallIcon(R.drawable.ic_main);

        Intent cancelIntent = new Intent(this, StatusService.class);
        cancelIntent.setAction(CANCEL_STATUS);
        PendingIntent contentIntent = PendingIntent.getService(this, 0,
                cancelIntent, 0);
        builder.setContentIntent(contentIntent);

        manager.notify(STATUS_NOFITICATION, builder.getNotification());
    }

    private void cancel(NotificationManager manager) {
        manager.cancel(STATUS_NOFITICATION);
    }

}
