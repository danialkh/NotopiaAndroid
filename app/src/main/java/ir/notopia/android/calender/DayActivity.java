package ir.notopia.android.calender;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import ir.notopia.android.EditorActivity;
import ir.notopia.android.R;
import ir.notopia.android.adapter.NotesAdapter;
import ir.notopia.android.database.AppRepository;
import ir.notopia.android.database.entity.NoteEntity;
import ir.notopia.android.utils.Constants;

import static androidx.core.content.FileProvider.getUriForFile;

public class DayActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PICTURE = 100, REQUEST_GALLERY_PICTURE = 200, REQUEST_CAMERA_VIDEO = 300, REQUEST_GALLERY_VIDEO = 400, REQUEST_AUDIO = 500;
    private static final String TAG = "Notopia";
    private static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 10;
    private String mjdn;
    private RecyclerView mNoteRecyclerView;
    private NotesAdapter mAdapter;
    private List<NoteEntity> list;
    private String mCurrentPhotoPath = null;
    private String mCurrentVideoPath = null;
    private String mCurrentAudioPath = null;
    private AppRepository mRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_day);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mRepository = AppRepository.getInstance(this);
        mNoteRecyclerView = findViewById(R.id.rVDay);


        initSpeedDial();

    }

    @Override
    protected void onResume() {
        super.onResume();
        initRecyclerView();
    }

    private void initRecyclerView() {
        mNoteRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mNoteRecyclerView.setLayoutManager(layoutManager);
        list = new ArrayList<>();
        list = mRepository.getDayNotes(mjdn);
//        Toast.makeText(this, "test" + list.size(), Toast.LENGTH_SHORT).show();
        mAdapter = new NotesAdapter(list, this);
        DividerItemDecoration itemDecor = new DividerItemDecoration(mNoteRecyclerView.getContext(), layoutManager.getOrientation());
        mNoteRecyclerView.setAdapter(mAdapter);
        mNoteRecyclerView.addItemDecoration(itemDecor);

//        mNoteRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(this,
//                mNoteRecyclerView, new RecyclerViewClickListener() {
//
//            @Override
//            public void onClick(View view, final int position) {
//                //Values are passing to activity & to fragment as well
//                Toast.makeText(DayActivity.this, "Single Click on position        :"+position,
//                        Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onLongClick(View view, int position) {
//                Toast.makeText(DayActivity.this, "Long press on position :"+position,
//                        Toast.LENGTH_LONG).show();
//            }
//
//        }));
    }

    private boolean checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {

            int permissionReadStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            int writeStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            List<String> listPermissionsNeeded = new ArrayList<>();
            if (writeStoragePermission != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (permissionReadStorage != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.CAMERA);
            }
            if (!listPermissionsNeeded.isEmpty()) {
                ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
                return false;
            }
        }
        return true;
    }

    private void initSpeedDial() {
    }

    private void addPicture() {
        final CharSequence[] items = {"دوربین", "گالری", "انصراف"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("افزودن عکس");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (items[which].equals("دوربین")) {
//                    takePicture();
//                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//                        startActivityForResult(takePictureIntent, REQUEST_CAMERA_PICTURE);
//                    }
                    dispatchTakePictureIntent();

                } else if (items[which].equals("گالری")) {
//                    selectPicture();
//                    Intent intent = new Intent(Intent.ACTION_PICK);
//                    intent.setType("image/*");
//                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(intent, REQUEST_GALLERY_PICTURE);
                } else if (items[which].equals("انصراف")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();

    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = getUriForFile(this,
                        Constants.SAVE_MEDIA_PROVIDER,
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_CAMERA_PICTURE);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
//        galleryAddPic(mCurrentPhotoPath);

        return image;
    }

    private void addVideo() {
        final CharSequence[] items = {"دوربین", "گالری", "انصراف"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("افزودن فیلم");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (items[which].equals("دوربین")) {
                    dispatchRecordVideoIntent();
//                    Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
//
//                    if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
//                        startActivityForResult(takeVideoIntent, REQUEST_CAMERA_VIDEO);
//                    }
                } else if (items[which].equals("گالری")) {
//                    selectPicture();
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("video/*");
                    startActivityForResult(intent, REQUEST_GALLERY_VIDEO);
                } else if (items[which].equals("انصراف")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();

    }

    private void dispatchRecordVideoIntent() {
        Intent RecordVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (RecordVideoIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createVideoFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = getUriForFile(this,
                        Constants.SAVE_MEDIA_PROVIDER,
                        photoFile);
                RecordVideoIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(RecordVideoIntent, REQUEST_CAMERA_VIDEO);
            }
        }
    }

    private File createVideoFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "VIDEO_" + timeStamp + "_";
        File storageDir = getExternalFilesDir("Videos");
        File video = File.createTempFile(
                imageFileName,  /* prefix */
                ".mp4",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentVideoPath = video.getAbsolutePath();
        return video;
    }

    private void addAudio() {
        Intent intent = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
        startActivityForResult(intent, REQUEST_AUDIO);
    }

    private File createAudioFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "Audio_" + timeStamp + "_";
        File storageDir = getExternalFilesDir("Audios");
        File audio = File.createTempFile(
                imageFileName,  /* prefix */
                ".mp3",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentAudioPath = audio.getAbsolutePath();

        return audio;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CAMERA_PICTURE:

                    editNote(-1, Constants.NOTE_TYPE_IMAGE, mCurrentPhotoPath);
                    Log.i(TAG, "onActivityResult: " + mCurrentPhotoPath);
                    break;
                case REQUEST_GALLERY_PICTURE:
                    Uri selectedImageUri = data.getData();

                    try {
                        saveMedia(Constants.NOTE_TYPE_IMAGE, selectedImageUri);
                        Log.i(TAG, "onActivityResult: " + mCurrentPhotoPath);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    editNote(-1, Constants.NOTE_TYPE_IMAGE, mCurrentPhotoPath);

                    break;
                case REQUEST_CAMERA_VIDEO:
                    Uri videoUri = data.getData();
                    Log.i(TAG, "onActivityResult: " + mCurrentVideoPath);
                    editNote(-1, Constants.NOTE_TYPE_VIDEO, mCurrentVideoPath);

                    break;
                case REQUEST_GALLERY_VIDEO:
                    Uri selectedVideoUri = data.getData();
                    try {
                        saveMedia(Constants.NOTE_TYPE_VIDEO, selectedVideoUri);
                        Log.i(TAG, "onActivityResult: " + mCurrentVideoPath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    editNote(-1, Constants.NOTE_TYPE_VIDEO, mCurrentVideoPath);

                    break;
                case REQUEST_AUDIO:
                    Uri recordedAudioUri = data.getData();
                    try {
                        saveMedia(Constants.NOTE_TYPE_AUDIO, recordedAudioUri);
                        Log.i(TAG, "onActivityResult: " + mCurrentAudioPath);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    editNote(-1, Constants.NOTE_TYPE_AUDIO, mCurrentAudioPath);

                    break;
                default:
                    break;

            }
        }
    }

    private void editNote(int noteId, String type, String uri) {
        Intent intentEditor = new Intent(getApplicationContext(), EditorActivity.class);
        intentEditor.putExtra(Constants.DAY_KEY_EXTRA, String.valueOf(mjdn));
        intentEditor.putExtra(Constants.NOTE_ID_EXTRA, noteId);
//        intentEditor.putExtra(Constants.NOTE_TYPE_EXTRA, Constants.NOTE_TYPE_TEXT);
        intentEditor.putExtra(Constants.NOTE_TYPE_EXTRA, type);
        intentEditor.putExtra(Constants.NOTE_URI_EXTRA, uri);

        startActivity(intentEditor);
    }

    private void saveMedia(String type, Uri uri) throws IOException {
        InputStream in = getContentResolver().openInputStream(uri);
        OutputStream out = null;
        switch (type) {
            case Constants.NOTE_TYPE_IMAGE:
                out = new FileOutputStream(createImageFile());
                break;
            case Constants.NOTE_TYPE_VIDEO:
                out = new FileOutputStream(createVideoFile());
                break;
            case Constants.NOTE_TYPE_AUDIO:
                out = new FileOutputStream(createAudioFile());
                break;
            default:

                break;
        }
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        out.close();
        Log.i(TAG, "saveMedia: " + out);
        in.close();

    }


}
