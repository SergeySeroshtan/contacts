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
 * Notifies user about status of synchronization.
 */
public class StatusService extends IntentService {

    /**
     * The name of service.
     */
    private static final String SERVICE_NAME = StatusService.class.getName();

    /**
     * Notifies user about status of synchronization.
     */
    public static final String ACTION_NOTIFY_STATUS = SERVICE_NAME
            + ".ACTION_NOTIFY_STATUS";
    /**
     * Contains identifier of string that contains status message.
     */
    public static final String STATUS_MESSAGE = "STATUS_MESSAGE";

    /**
     * Cancels status notification.
     */
    private static final String ACTION_CANCEL_STATUS = SERVICE_NAME
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
        if (ACTION_NOTIFY_STATUS.equals(action)) {
            int messageId = intent.getIntExtra(STATUS_MESSAGE,
                    R.string.sync_internal_error);
            notifyStatus(manager, messageId);
        } else if (ACTION_CANCEL_STATUS.equals(action)) {
            cancelStatus(manager);
        }
    }

    private void notifyStatus(NotificationManager manager, int messageId) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                this);
        builder.setContentTitle(getString(R.string.app_name));
        builder.setContentText(getString(messageId));
        builder.setSmallIcon(R.drawable.ic_main);

        Intent cancelIntent = new Intent(this, StatusService.class);
        cancelIntent.setAction(ACTION_CANCEL_STATUS);
        PendingIntent contentIntent = PendingIntent.getService(this, 0,
                cancelIntent, 0);
        builder.setContentIntent(contentIntent);

        manager.notify(STATUS_NOFITICATION, builder.getNotification());
    }

    private void cancelStatus(NotificationManager manager) {
        manager.cancel(STATUS_NOFITICATION);
    }

}
