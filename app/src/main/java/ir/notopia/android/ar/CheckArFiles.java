package ir.notopia.android.ar;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.util.List;
import java.util.Objects;

import ir.notopia.android.database.AppRepository;
import ir.notopia.android.database.entity.ArEntity;

public class CheckArFiles {

    private String directory = "/Notopia/Ar/";
    String arFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + directory;

    private String pathtargets = arFolder + "Targets/";
    private String pathtracker = arFolder + "Trackers/";

    public boolean isOkArFiles() {
        return okArFiles;
    }

    private boolean okArFiles = true;

    public CheckArFiles(Context context) {

        AppRepository mRepository = AppRepository.getInstance(context);
        List<ArEntity> mArs = mRepository.getArs();
        Log.d("CheckArFile",mArs.toString());

        if(mArs.size() != 0) {
            if (countFiles("Targets") == mArs.size() && countFiles("Trackers") == mArs.size()) {
                for (int i = 0; i < mArs.size(); i++) {
                    ArEntity mAr = mArs.get(i);
                    boolean check = CreateFileExist(mAr);
                    if (!check) {
                        okArFiles = false;
                        break;
                    }
                }
            } else {
                okArFiles = false;
            }
        }
        else{
            okArFiles = false;
        }

        if(!okArFiles){
            Intent intent = new Intent(context,DownloadArActivity.class);
            context.startActivity(intent);
        }
    }

    private int countFiles(String folder) {
        File dir = new File(arFolder + folder);
        if (dir.isDirectory()){
            return Objects.requireNonNull(dir.list()).length;
        }
        return 0;
    }


    private boolean CreateFileExist(ArEntity mAr) {


        File targetFile = new File(pathtargets ,mAr.getType() + "-" + mAr.getTarget());
        File trackerFile = new File(pathtracker,mAr.getTracker());

        Log.d("CheckArFile:Targer",targetFile.getPath());
        Log.d("CheckArFile:Tracker",trackerFile.getPath());

        if (!targetFile.exists()) {
            Log.d("CheckArFile:TargetNX",mAr.getTarget());
            return false;
        }
        if (!trackerFile.exists()) {
            Log.d("CheckArFile:TrackerNX",mAr.getTracker());
            return false;
        }
        return true;
    }
}
