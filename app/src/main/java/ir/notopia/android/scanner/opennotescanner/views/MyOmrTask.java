package ir.notopia.android.scanner.opennotescanner.views;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.util.ArrayList;
import java.util.List;

import ir.hamsaa.persiandatepicker.util.PersianCalendar;
import ir.notopia.android.database.AppRepository;
import ir.notopia.android.database.entity.ScanEntity;
import ir.notopia.android.scanner.omr.OMR;
import ir.notopia.android.scanner.omr.QrCode;
import ir.notopia.android.scanner.opennotescanner.FullScreenViewActivity;

public class MyOmrTask extends AsyncTask<Void, Void, Void> {

    private static final String TAG = "OpenNoteScannerActivity";
    private Context mContext;
    private AppRepository mRepository;
    private String mImage;
    private String mScanId;
    private Mat source;
    private String[] categories = {"1", "2", "3", "4", "5", "6"};
    private String mCategory;
    private String mDay;
    private String mMonth;
    private String mYear;
    private final String mQrCode;
    private SharedPreferences doScan;
    private  SharedPreferences.Editor editor;

    public MyOmrTask(Context context, AppRepository mRepository,String mScanId, String oriImage, String image,Mat source, String qrCode) {
        this.mRepository = mRepository;
        mContext = context;
        mImage = image;
        this.source = Imgcodecs.imread(oriImage);
        this.mQrCode = qrCode;
        this.mScanId = mScanId;

        doScan = mContext.getSharedPreferences("doHamed", Context.MODE_PRIVATE);
        editor = doScan.edit();

        Log.d("mScanId in OMRTaask:",mScanId);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... voids) {

        editor.putString("doHamedState","1").apply();

        Log.d("calcS doHamedState:", "OMRTK:1");

        Log.d("doInBackground: " , mQrCode);
        QrCode qrCode = new QrCode(mQrCode);
        qrCode.calc();

        Log.d("calcS doHamedState:", "source:" + source);

        OMR omr = new OMR(source,qrCode.isNew(), qrCode.isEven(), mContext);
        List<String> Results;
        try {
            Results = omr.get_PageInfo();
            Log.i("TAG_LOG", "getOMRData: " + " Month: " + Results.get(0) + " Day: " + Results.get(1) + " Category: " + Results.get(2) + " QrCode: " + mQrCode);

            mMonth = Results.get(0);
            mDay = Results.get(1);
            mCategory = Results.get(2);

            PersianCalendar persianCalendar = new PersianCalendar();
            mYear = String.valueOf(persianCalendar.getPersianYear());

            String date = mYear + "/" + Results.get(0) + "/" + Results.get(1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onPostExecute(Void result) {

        Log.d(TAG, "onPostExecute: " + mCategory + mYear + mMonth + mDay);

        Log.d(TAG, "onPostExecute start: " + mRepository.getScans().size());

        ScanEntity mScan;
        int lsTedadScans = mRepository.getScans().size();

        mScan = mRepository.getScan(Integer.valueOf(mScanId));
        mScan.setCategory(mCategory);
        mScan.setQrCode(mQrCode);
        mScan.setImage(mImage);

        mScan.setYear(mYear);
        mScan.setMonth(mMonth);
        mScan.setDay(mDay);

        if (mCategory.equals("0") || mYear.equals("1398") || mMonth.equals("0") || mDay.equals("0")) {
            mScan.setNeedEdit(true);
        }
        else {
            mScan.setNeedEdit(false);
        }
        mRepository.updateScan(mScan);
        CheckScanFinished(mImage,mScan,lsTedadScans);

    }

    private void CheckScanFinished(String image, ScanEntity mScan,int lsTedadScans) {


        Log.d("CheckScanFinished:",mScan.toString());
        Log.d("CheckScanFinished2:",mRepository.getScan(mScan.getId()).toString());


        if(!mRepository.getScan(mScan.getId()).toString().equals(mScan.toString())){
            new android.os.Handler().postDelayed(
            new Runnable() {
                public void run() {
                    CheckScanFinished(image,mScan,lsTedadScans);
                }
            },
            400);
        }
        else {
            editor.putString("doHamedState","0").apply();
            Intent i = new Intent(mContext, FullScreenViewActivity.class);
            List<ScanEntity> mScans = mRepository.getScans();
            int index = 0;
            for(int a = 0;a < mScans.size();a++){
                if(mScan.getId() == mScans.get(a).getId()){
                       index = a;
                       continue;
                }
            }
            i.putExtra("position", index);
            i.putExtra("scan_id", mScan.getId());
            i.putParcelableArrayListExtra("title_body", (ArrayList<? extends Parcelable>) mScans);
            mContext.startActivity(i);
        }

    }
}
