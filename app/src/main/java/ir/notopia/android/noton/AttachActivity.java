package ir.notopia.android.noton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import ir.notopia.android.R;
import ir.notopia.android.adapter.AttachAdaptor;
import ir.notopia.android.database.AppRepository;
import ir.notopia.android.database.entity.AttachEntity;
import ir.notopia.android.database.entity.ScanEntity;
import ir.notopia.android.scanner.opennotescanner.FullScreenViewActivity;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;

import com.afollestad.dragselectrecyclerview.DragSelectRecyclerView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class AttachActivity extends AppCompatActivity {


    private int PICKFILE_RESULT_CODE = 1212;
    private int MY_PERMISSIONS_REQUEST_WRITE = 606;
    private int MY_PERMISSIONS_REQUEST_READ = 707;
    private static String[] CallAttachMimFor;

    private ScanEntity mScan;
    private AppRepository mRepository;

    private String directory = "/Notopia/attachment/";
    private int whichClicked;
    private String AttachFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + directory;

    String[] gallery = {"image/png",
            "image/jpg",
            "image/jpeg"};
    String[] files  = { "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", // .ppt & .pptx
            "application/pdf","application/vnd.ms-excel"}; // pdf and xls

    String[] VideoFiles  = { "video/*"};
    String[] AudioFiles  = { "audio/x-wav","application/rtf","audio/x-wav","audio/mp3","audio/mpeg"};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attach);

        checkCreatePermissions();
        CreateDirectory();

        mRepository = AppRepository.getInstance(this);

        String notonId;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            notonId = extras.getString("notonId");
            mScan = mRepository.getScan(Integer.parseInt(notonId));
        }

        // load attachs
        LoadNotonAttach(AttachActivity.this,mScan);


        ImageView iconBack = findViewById(R.id.icon_back);
        iconBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = 0;
                List<ScanEntity> mScans = mRepository.getScans();

                //find index
                for(int i = 0;i < mScans.size();i++){
                    if(mScans.get(i).getId() == mScan.getId()){
                        index = i;
                        continue;
                    }
                }


                Intent i = new Intent(AttachActivity.this, FullScreenViewActivity.class);
                i.putExtra("position",index);
                i.putExtra("scan_id", mScan.getId());
                i.putParcelableArrayListExtra("title_body", (ArrayList<? extends Parcelable>) mScans);
                AttachActivity.this.startActivity(i);
            }
        });

        

        com.github.clans.fab.FloatingActionButton FloatingGallery = findViewById(R.id.FloatingAction_item_gallery);
        com.github.clans.fab.FloatingActionButton FloatingDoc = findViewById(R.id.FloatingAction_item_doc);
        com.github.clans.fab.FloatingActionButton FloatingAudio = findViewById(R.id.FloatingAction_item_audio);
        com.github.clans.fab.FloatingActionButton FloatingVideo = findViewById(R.id.FloatingAction_item_video);

        FloatingGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPicker(gallery,"float");
                whichClicked = 0;
            }
        });

        FloatingVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPicker(VideoFiles,"float");
                whichClicked = 1;
            }
        });

        FloatingAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPicker(AudioFiles,"float");
                whichClicked = 2;
            }
        });

        FloatingDoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPicker(files,"float");
                whichClicked = 0;
            }
        });

        
        LoadNotonAttach(this,mScan);
        
    }

    private void openPicker(String[] mimFilesType,String caller) {


        if(!caller.equals("onRequest"))
            checkCreatePermissions();

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {

            Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
            chooseFile.setType("*/*");
            chooseFile.putExtra(Intent.EXTRA_MIME_TYPES, mimFilesType);
            chooseFile = Intent.createChooser(chooseFile, "انتخاب فایل");
            startActivityForResult(chooseFile, PICKFILE_RESULT_CODE);

        }
    }

    public static void LoadNotonAttach(Activity activity, ScanEntity mScan) {



        AttachAdaptor attachAdaptor = new AttachAdaptor(activity,mScan);
        // new Utils(getApplicationContext()).getFilePaths(););

        DragSelectRecyclerView attachRecycleView = (DragSelectRecyclerView) activity.findViewById(R.id.recyclerviewAttachment);
        attachRecycleView.setLayoutManager(new GridLayoutManager(activity, 3));
        attachRecycleView.setAdapter(attachAdaptor);




    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        ArrayList<AttachmentDetail> list = attachmentManager.manipulateAttachments(getApplicationContext(),requestCode, resultCode, data); // gives you neccessary detail about attachment like uri,name,size,path and mimtype

        if(resultCode != RESULT_OK){
            return;
        }
        else{

            Uri returnUri = data.getData();

            ContentResolver cR = AttachActivity.this.getContentResolver();
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            String type = mime.getExtensionFromMimeType(cR.getType(returnUri));
            String attachType = cR.getType(returnUri);



            String filePath = getFileName(returnUri);
//            Toast.makeText(this, filePath + "", Toast.LENGTH_LONG).show();

//            Toast.makeText(this, type + "", Toast.LENGTH_LONG).show();


//             change name of attached file
            String fileFormat = type;
            String fileName = "attack-" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date())
                    + new Random().nextInt(61) + 20
                    + "." + fileFormat;

            String finalPath = AttachFolder + fileName;
//            Toast.makeText(this, finalPath + "", Toast.LENGTH_LONG).show();

            MoveFile(this, filePath, finalPath,returnUri);



            AttachEntity mAttach = new AttachEntity(mScan.getId(), Objects.requireNonNull(returnUri).toString(),attachType,finalPath);
            mRepository.insertAttach(mAttach);

            new android.os.Handler().postDelayed(
            new Runnable() {
                public void run() {
                    LoadNotonAttach(AttachActivity.this,mScan);
                }
            },
            250);


        }
    }

    private void MoveFile(Context context,String firstLoc,String finalLoc,Uri uri) {

        String TAG = "MoveFile";

        // the file to be moved or copied
        File sourceLocation = new File (firstLoc);

        // make sure your target location folder exists!
        File targetLocation = new File (finalLoc);

        // just to take note of the location sources
        Log.v(TAG, "sourceLocation: " + sourceLocation);
        Log.v(TAG, "targetLocation: " + targetLocation);

        try {

            // 1 = move the file, 2 = copy the file
            int actionChoice = 2;

            // moving the file to another directory
            if(actionChoice==1){

                if(sourceLocation.renameTo(targetLocation)){
                    Log.v(TAG, "Move file successful.");
                }else{
                    Log.v(TAG, "Move file failed.");
                }

            }

            // we will copy the file
            else{

                // make sure the target file exists

                InputStream in = context.getContentResolver().openInputStream(uri);
                OutputStream out = new FileOutputStream(targetLocation);

                // Copy the bits from instream to outstream
                byte[] buf = new byte[1024];
                int len;

                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }

                in.close();
                out.close();

                Log.v(TAG, "Copy file successful.");




            }

        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        CreateDirectory();

        openPicker(CallAttachMimFor,"onRequest");

    }



    private void checkCreatePermissions() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE);

        }


        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ);

        }

    }


    private void CreateDirectory() {

        File dir = new File(AttachFolder);
        if (!dir.exists()) {
            try {
                dir.mkdirs();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    public String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }



}