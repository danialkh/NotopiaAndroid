package ir.notopia.android;

import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import ir.notopia.android.database.AppRepository;
import ir.notopia.android.database.entity.NoteEntity;
import ir.notopia.android.utils.Constants;

public class EditorActivity extends AppCompatActivity {
    private static final String TAG = "Notopia";
    private static final int VOICE_REQUEST_CODE = 1041;
    FloatingActionButton fabVoice;
    private NoteEntity mNote = new NoteEntity();
    private TextView mTextView;
    private ImageView mImageView;
    private VideoView mVideoView;
    private ConstraintLayout constraintLayout;
    private ConstraintSet constraintSet;
    private AppRepository mRepository;
    private boolean mNewNote, mEditing;
    private Executor executor = Executors.newSingleThreadExecutor();
    private String dayKey;
    private int noteId;
    private String noteType;
    private Uri noteUri = null;
    private MediaController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mRepository = AppRepository.getInstance(this);
        constraintLayout = findViewById(R.id.activity_editor_constraint_layout);
        constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);
        init();
    }


    private void init() {
        mTextView = findViewById(R.id.note_text);
        fabVoice = findViewById(R.id.fab_voice);
        PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (activities.size() == 0) {
            fabVoice.setEnabled(false);
        }
        fabVoice.setOnClickListener(v -> {

            voiceToText();
        });
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            noteId = extras.getInt(Constants.NOTE_ID_EXTRA, -1);
            Log.i(TAG, "init: " + noteId);
            if (noteId == -1) {
                dayKey = extras.getString(Constants.DAY_KEY_EXTRA);
                noteType = extras.getString(Constants.NOTE_TYPE_EXTRA);
                noteUri = Uri.parse(extras.getString(Constants.NOTE_URI_EXTRA));
                Log.i(TAG, "onCreate: dayket:" + dayKey + ", noteID:" + noteId + ", Type:" + noteType + " NoteUri:" + noteUri);
                typeChecker(noteType);
                if (noteType.equals(Constants.NOTE_TYPE_VOICE_TEXT)) voiceToText();

                setTitle("یادداشت جدید");
                mNewNote = true;
            } else {
                setTitle("ویرایش یادداشت");
                mNote = mRepository.getNoteById(noteId);
                Log.i(TAG, "init: " + mNote.toString());
                noteUri = Uri.parse(mNote.getMediaUri());
                noteType = mNote.getNoteType();
                typeChecker(noteType);
                mTextView.setText(mNote.getDescription());

            }
        }
    }

    private void voiceToText() {
        Intent voiceIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        voiceIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        voiceIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "fa-IR");
        voiceIntent.putExtra(RecognizerIntent.EXTRA_PROMPT, "صحبت کنید");
        startActivityForResult(voiceIntent, VOICE_REQUEST_CODE);

    }

    /**
     * Handle the results from the voice recognition activity.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VOICE_REQUEST_CODE && resultCode == RESULT_OK) {
            // Populate the wordsList with the String values the recognition engine thought it heard
            final ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (!matches.isEmpty()) {
                String Query = matches.get(0);
                mTextView.append(" " + Query);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void typeChecker(String noteType) {
        switch (noteType) {
            case Constants.NOTE_TYPE_VIDEO:
                setVideo(noteUri);
                break;
            case Constants.NOTE_TYPE_IMAGE:
                setImage(noteUri);
                break;
            case Constants.NOTE_TYPE_AUDIO:
                setAudio(noteUri);
                break;
        }
    }

    private void setImage(Uri imageUri) {
        mImageView = findViewById(R.id.image_media);
        runOnUiThread(() -> {
            mImageView.setImageURI(imageUri);

            // Stuff that updates the UI

        });
        mImageView.setVisibility(View.VISIBLE);
        //                <!--app:layout_constraintTop_toBottomOf="@+id/image_media"-->

        constraintSet.connect(mTextView.getId(), ConstraintSet.TOP, mImageView.getId(), ConstraintSet.BOTTOM);

    }

    private void setAudio(Uri audioUri) {

//        Intent audioService = new Intent(this, AudioService.class);
//        audioService.setAction(AudioService.ACTION_START_AUDIO);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                        startForegroundService(audioService);
//                    } else {
//                        startService(audioService);
//                    }
//        mAudioView = findViewById(R.id.audioview);
//        mAudioView.setVisibility(View.VISIBLE);
//
////        mAudioView.setUpControls();
//        try {
//            mAudioView.setDataSource(Uri.parse("/storage/emulated/0/Android/data/ir.notopia.main/files/Audios/Audio_۲۰۱۸۱۲۱۶_۲۲۰۰۴۴_405872440.mp3"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        Button btnPlay = findViewById(R.id.btn_audio);
        btnPlay.setVisibility(View.VISIBLE);

        btnPlay.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            File file = new File(String.valueOf(audioUri));
            intent.setDataAndType(Uri.fromFile(file), "audio/*");
            startActivity(intent);

        });

//        new Handler(Looper.getMainLooper()).post(new Runnable(){
//            @Override
//            public void run() {
//                try {
//                    mAudioView.setDataSource(audioUri);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//        runOnUiThread(() -> {
//            try {
//                mAudioView.setDataSource(audioUri);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            // Stuff that updates the UI
//
//        });
        //                <!--app:layout_constraintTop_toBottomOf="@+id/image_media"-->

        constraintSet.connect(mTextView.getId(), ConstraintSet.TOP, btnPlay.getId(), ConstraintSet.BOTTOM);

    }

    private void setVideo(Uri videoUri) {
        mVideoView = findViewById(R.id.video_media);
        mVideoView.setVisibility(View.VISIBLE);
        mVideoView.setVideoURI(videoUri);
        mVideoView.setMediaController(new MediaController(this));
        mVideoView.requestFocus();
        mVideoView.start();
        Log.i(TAG, "setVideo: " + mVideoView.getDuration() + "   " + mVideoView.getResources());

//      <!--app:layout_constraintTop_toBottomOf="@+id/video_media"-->
        constraintSet.connect(mTextView.getId(), ConstraintSet.TOP, mVideoView.getId(), ConstraintSet.BOTTOM);
//        final MessageView actualResolution = findViewById(R.id.actualResolution);
        if (controller == null) {
            controller = new MediaController(EditorActivity.this);
        }
        mVideoView.setMediaController(controller);
        controller.setAnchorView(mVideoView);
        controller.setMediaPlayer(mVideoView);

        mVideoView.setOnClickListener(view -> {
            if (mVideoView.isPlaying()) return;
            mVideoView.start();
        });
        mVideoView.setOnPreparedListener(mp -> {
            Log.i(TAG, "Actual resolution: " + mp.getVideoWidth() + " x " + mp.getVideoHeight());
            ViewGroup.LayoutParams lp = mVideoView.getLayoutParams();
            float videoWidth = mp.getVideoWidth();
            float videoHeight = mp.getVideoHeight();
            float viewWidth = mVideoView.getWidth();
            lp.height = (int) (viewWidth * (videoHeight / videoWidth));
            mVideoView.setLayoutParams(lp);
            if (mVideoView.isPlaying()) return;
            mVideoView.start();
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_editor, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            saveAndReturn();
            return true;
        } else if (item.getItemId() == R.id.action_delete) {
            executor.execute(() -> {
                if (noteId != -1) {
                    deleteNote(mRepository.getNoteById(noteId));
                }
                finish();

            });
        } else if (item.getItemId() == R.id.action_save) {
            saveNote(noteId, mTextView.getText().toString(), dayKey);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        saveAndReturn();
    }

    private void saveAndReturn() {
//        mRepository.saveNote(mTextView.getText().toString());
        if (!mTextView.getText().toString().trim().isEmpty()
//                || noteUri != null
        ) {
            final CharSequence[] items = {"ذخیره", "حذف", "انصراف"};
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("");
            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (items[which].equals("ذخیره")) {
                        saveNote(noteId, mTextView.getText().toString(), dayKey);
                        finish();
                    } else if (items[which].equals("حذف")) {
                        executor.execute(() -> {
                            if (noteId != -1) {
                                deleteNote(mRepository.getNoteById(noteId));
                            }
                            finish();

                        });
                    } else if (items[which].equals("انصراف")) {
                        dialog.dismiss();
                    }
                }
            });
            builder.show();
        } else {
            finish();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
//        outState.putBoolean(EDITING_KEY, true);
        super.onSaveInstanceState(outState);
    }

    private void saveNote(final int moteID, final String noteText, String noteDayKey) {

        if (moteID == -1) {
            if (TextUtils.isEmpty(noteText.trim()) && TextUtils.isEmpty(noteUri.toString())) {
                return;
            }
            NoteEntity note = new NoteEntity(noteText.trim(), noteType, new Date(), noteDayKey, noteUri.toString(),"");
            Log.i(TAG, "saveNote: " + note.toString());

            mRepository.insertNote(note);

        } else {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    final NoteEntity note;

                    note = mRepository.getNoteById(moteID);
                    note.setDescription(noteText.trim());
                    Log.i(TAG, "saveNote: " + note.toString());

                    mRepository.insertNote(note);

                }
            });
        }
        finish();

    }

    private void deleteNote(NoteEntity note) {
        // TODO Delete file
        ContentResolver contentResolver = this.getContentResolver();
//        int delete = contentResolver.delete(Uri.parse(note.getMediaUri()), null, null);
        mRepository.deleteNote(note);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
