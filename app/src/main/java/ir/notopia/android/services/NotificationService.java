package ir.notopia.android.services;

import android.app.IntentService;
import android.content.Intent;

import androidx.annotation.Nullable;

import static ir.notopia.android.utils.NotificationUtils.getNewNotification;

public class NotificationService extends IntentService {

    public NotificationService() {
        super("NotificationService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        getNewNotification(getApplicationContext());

    }
}
