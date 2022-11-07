package ir.notopia.android.database.entity;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MyNotification {

    @SerializedName("success")
    @Expose
    private boolean success;
    @SerializedName("response")
    @Expose
    private NotificationResponse notificationResponse;

    public MyNotification(boolean success, NotificationResponse notificationResponse) {
        this.success = success;
        this.notificationResponse = notificationResponse;
    }

    public MyNotification() {
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public NotificationResponse getNotificationResponse() {
        return notificationResponse;
    }

    public void setNotificationResponse(NotificationResponse notificationResponse) {
        this.notificationResponse = notificationResponse;
    }

}