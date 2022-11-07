package ir.notopia.android.utils;

import android.app.Application;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import ir.notopia.android.MessageBoxActivity;
import ir.notopia.android.R;
import ir.notopia.android.database.AppRepository;
import ir.notopia.android.database.entity.MyNotification;
import ir.notopia.android.database.entity.NotificationResponse;
import ir.notopia.android.services.NotificationWebService;
import retrofit2.Call;

import static ir.notopia.android.utils.App.CHANNEL_ID;


public class NotificationUtils extends Application {
    private static final int NOTOPIA_NOTIFICATION_PENDING_INTENT_ID = 3417;
    private static final int NOTOPIA_NOTIFICATION_ID = 3430;
    private static MyNotification notifications;

    public static void pullNotificationMessage(Context context, NotificationResponse notificationResponse) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_ar_menu)
                .setLargeIcon(largeIcon(context))
                .setContentTitle(notificationResponse.getTitle())
                .setContentText(notificationResponse.getDescription())
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setContentIntent(contentIntent(context))
                .setAutoCancel(true)
                .build();

        notificationManager.notify(NOTOPIA_NOTIFICATION_ID, notification);
    }

    public static void getNewNotification(Context context) {
        NotificationWebService webService = NotificationWebService.retrofit.create(NotificationWebService.class);
        Call<MyNotification> call = webService.notifications();
        try {
            notifications = call.execute().body();

        } catch (IOException e) {
            e.printStackTrace();
        }


        NotificationResponse mNotification;
        if (notifications != null) {
            mNotification = notifications.getNotificationResponse();
            AppRepository mRepository = AppRepository.getInstance(context);
            boolean newNotifi = mRepository.addNotification(mNotification);

            if (newNotifi) pullNotificationMessage(context, mNotification);
        }

    }

    private static PendingIntent contentIntent(Context context) {
        Intent startAcicityIntent = new Intent(context, MessageBoxActivity.class);
        return PendingIntent.getActivity(
                context,
                NOTOPIA_NOTIFICATION_PENDING_INTENT_ID,
                startAcicityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static Bitmap largeIcon(Context context) {
        Resources res = context.getResources();
        Bitmap largeIcon = BitmapFactory.decodeResource(res, R.drawable.ic_ar_menu);
        return largeIcon;
    }
}
