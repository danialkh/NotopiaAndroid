package ir.notopia.android.services;


import android.app.job.JobParameters;
import android.app.job.JobService;

//import com.firebase.jobdispatcher.JobParameters;
//import com.firebase.jobdispatcher.JobService;

public class NotificationJobService extends JobService {
    private static final String TAG = "NotifiyJobService";

//    private static AsyncTask mBackgroundTask;

    @Override
    public boolean onStartJob(JobParameters job) {
//        Log.i(TAG, "Job start ");
////        Context context = NotificationJobService.this;
//        Intent intent = new Intent(this, NotificationService.class);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            startForegroundService(intent);
//        } else {
//            startService(intent);
//        }
        return true;
    }


    @Override
    public boolean onStopJob(JobParameters job) {
//        if (mBackgroundTask != null) mBackgroundTask.cancel(true);
        return true;
    }


}
