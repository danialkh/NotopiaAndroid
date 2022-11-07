package ir.notopia.android.scanner;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import ir.notopia.android.R;
import ir.notopia.android.database.AppRepository;
import ir.notopia.android.database.entity.ScanEntity;
import ir.notopia.android.scanner.liveedgedetection.activity.ScanActivity;
import ir.notopia.android.scanner.liveedgedetection.constants.ScanConstants;
import ir.notopia.android.scanner.liveedgedetection.util.ScanUtils;
import ir.notopia.android.scanner.omr.OMR;

import static ir.notopia.android.scanner.omr.Util.SCAN_DIC;
import static ir.notopia.android.scanner.omr.Util.sout;

//import ir.notopia.android.scanner.omr.old.OMR;

public class ScannerActivity extends AppCompatActivity {
    private static final int REQUEST_CODE = 101;
    private static final String TAG = ScannerActivity.class.getCanonicalName();


    private String currentPhotoPath;
    private ImageView scannedImageView;
    private View contextView;
    //    private AppDatabase mDb;
//    private Executor executor = Executors.newSingleThreadExecutor();
    private ScanEntity mScan;
    private AppRepository mRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);
        scannedImageView = findViewById(R.id.scanned_image);
        contextView = findViewById(R.id.context_view);
//        mDb = AppDatabase.getInstance(this);
        mRepository = AppRepository.getInstance(this);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            int scanId = bundle.getInt("scan_id");
            Log.i(TAG, "onCreate: " + scanId);

            mScan = mRepository.getScan(scanId);
            String category = "Category: " + mScan.getCategory();
            String date = "Date: 98/" + mScan.getMonth() + "/" + mScan.getDay();
            Uri imgUri = Uri.parse(mScan.getImage());
            scannedImageView.setImageURI(imgUri);
            Snackbar.make(contextView, category + "      " + date, Snackbar.LENGTH_INDEFINITE).show();


        } else if (OpenCVLoader.initDebug()) {
//            Toast.makeText(this, "openCv successfully loaded", Toast.LENGTH_SHORT).show();
            startScan();
        }
    }

    private void startScan() {
        Intent intent = new Intent(this, ScanActivity.class);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                if (null != data && null != data.getExtras()) {
                    String filePath = data.getExtras().getString(ScanConstants.SCANNED_RESULT);
                    Log.i("TAG_LOG", "onActivityResult: " + filePath + "image");
                    Bitmap baseBitmap = ScanUtils.decodeBitmapFromFile(filePath, ScanConstants.IMAGE_NAME);
                    File photoFile = null;
                    FileOutputStream fos = null;
                    try {
                        photoFile = createImageFile("OMR");
                        fos = new FileOutputStream(photoFile);
                        baseBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
//                        getOMRData(filePath + "image");

                    } catch (IOException ex) {
                        ex.printStackTrace();

                    } catch (Exception e) {
                        e.printStackTrace();

                    }
//                    scannedImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    scannedImageView.setImageBitmap(baseBitmap);
                    try {
                        Thread.sleep(1000);
                        getOMRData(currentPhotoPath);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                finish();
            }
        }
    }

    private void getOMRData(String img) throws Exception {

        Mat source = Imgcodecs.imread(img);
        OMR omr = new OMR(source);
        List<String> Results = omr.get_PageInfo();
        Log.i("TAG_LOG", "getOMRData: " + "Month: " + Results.get(0) + "Day: " + Results.get(1) + "Category: " + Results.get(2));

        sout("Mounth: " + Results.get(0));
        sout("Day: " + Results.get(1));
        sout("Category: " + Results.get(2));
        String category = "Category: " + Results.get(2);
        String date = "Date: 98/" + Results.get(0) + "/" + Results.get(1);
        Snackbar.make(contextView, category + "      " + date, Snackbar.LENGTH_INDEFINITE)
                .show();
//        ScanEntity scan = new ScanEntity(currentPhotoPath, Results.get(0), Results.get(1), Results.get(2));
//        executor.execute(new Runnable() {
//            @Override
//            public void run() {
//                mDb.scanDao().insertAll(scan);
//            }
//        });
//        mRepository.insertScan(scan);
////        Toast.makeText(this, "Mounth: " + Results.get(0) + " Day: " + Results.get(1) + " Category: " + Results.get(2), Toast.LENGTH_LONG).show();
//        Log.i("TAG_LOG", "getOMRData: " + scan.toString());

    }

    private File createImageFile(String name) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = name + "_JPEG_" + timeStamp + "_";
        File storageDir = new File(Environment.getExternalStorageDirectory().toString() + "/" + SCAN_DIC + "/Scans");
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
//        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".png",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        Log.e("TAG_LOG", "createImageFile: " + currentPhotoPath);
        return image;
    }

}
