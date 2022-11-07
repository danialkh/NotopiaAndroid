package ir.notopia.android.utils;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class App extends Application {
    public static final String CHANNEL_ID = "Notopia";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificatonChannels();
    }

    private void createNotificatonChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notopiaNotificationChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "مرکز پیام نوتوپیا",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notopiaNotificationChannel.setDescription("پیام های دریافتی نوتوپیا");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(notopiaNotificationChannel);
        }
    }
}
