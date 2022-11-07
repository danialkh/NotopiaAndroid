package ir.notopia.android.scanner.opennotescanner;

// based on http://android-er.blogspot.com.br/2012/07/gridview-loading-photos-from-sd-card.html

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.afollestad.dragselectrecyclerview.DragSelectRecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import ir.notopia.android.R;
import ir.notopia.android.database.AppRepository;
import ir.notopia.android.database.entity.ScanEntity;


public class GalleryGridActivity extends AppCompatActivity{

    private static final String TAG = "GalleryGridActivity";
    GalleryAdaptor myThumbAdapter;
    private MenuItem mShare;
    private MenuItem mTag;
    private MenuItem mDelete;
    private DragSelectRecyclerView recyclerView;
    private AlertDialog.Builder deleteConfirmBuilder;


    private SharedPreferences mSharedPref;
    private AppRepository mRepository;
    //    private AppDatabase mDb;
    private Executor executor = Executors.newSingleThreadExecutor();
    //    private ScansAdapter mAdapter;
    //    private RecyclerView rv;
    private List<ScanEntity> mScans;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRepository = AppRepository.getInstance(this);
        mScans = mRepository.getScans();

        Log.i(TAG, "onCreate mScans: " + mScans.size());


        mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);

//        ((OpenNoteScannerApplication) getApplication()).getTracker()
//                .trackScreenView("/GalleryGridActivity", "Gallery");

        setContentView(R.layout.activity_gallery);

        ActionBar actionBar = getSupportActionBar();

        assert actionBar != null;
        actionBar.setDisplayShowHomeEnabled(true);

        actionBar.setTitle(null);
        actionBar.setDisplayHomeAsUpEnabled(true);
//        actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_24dp);





        ArrayList<String> ab = new ArrayList<>();
        for (int i = 0; i < mScans.size(); i++) {
            ab.add(mScans.get(i).getImage());
        }
//        myThumbAdapter = new ThumbAdapter(this, ab );
        myThumbAdapter =  new GalleryAdaptor(mScans,false,this);
        // new Utils(getApplicationContext()).getFilePaths(););

        recyclerView = (DragSelectRecyclerView) findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 4));
        recyclerView.setAdapter(myThumbAdapter);

//        deleteConfirmBuilder = new AlertDialog.Builder(this);
//
//        deleteConfirmBuilder.setTitle(getString(R.string.confirm_title));
//        deleteConfirmBuilder.setMessage(getString(R.string.confirm_delete_multiple_text));
//
//        deleteConfirmBuilder.setPositiveButton(getString(R.string.answer_yes), new DialogInterface.OnClickListener() {
//
//            public void onClick(DialogInterface dialog, int which) {
////                mRepository.deleteScan();
////                deleteImage();
//                dialog.dismiss();
//            }

//        });

//        deleteConfirmBuilder.setNegativeButton(getString(R.string.answer_no), new DialogInterface.OnClickListener() {
//
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//            }
//        });

    }

    private void reloadAdapter() {
        recyclerView.setAdapter(null);
        mScans = mRepository.getScans();

        ArrayList<String> ab = new ArrayList<>();
        for (int i = 0; i < mScans.size(); i++) {
            ab.add(mScans.get(i).getImage());
        }

        myThumbAdapter = new GalleryAdaptor(mScans,false,this);

        recyclerView.setAdapter(myThumbAdapter);
        recyclerView.invalidate();

    }

    @Override
    public void onResume() {
        super.onResume();
        reloadAdapter();
    }
//
//    private void deleteImage() {
//        for (String filePath : myThumbAdapter.getSelectedFiles()) {
//            final File photoFile = new File(filePath);
//            if (photoFile.delete()) {
//                Utils.removeImageFromGallery(filePath, this);
//                Log.d(TAG, "Removed file: " + filePath);
//                mRepository.deleteScanByImage(filePath);
//            }
//
//        }
//
//        reloadAdapter();
//
//    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_gallery, menu);
//
//        mShare = menu.findItem(R.id.action_share);
//        mShare.setVisible(false);
//
//        mTag = menu.findItem(R.id.action_tag);
//        // mTag.setVisible(false);
//
//        mDelete = menu.findItem(R.id.action_delete);
//        mDelete.setVisible(false);
//
//        invalidateOptionsMenu();
//
//        return true;
//    }



//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        switch (id) {
//            case android.R.id.home:
//                finish();
//                break;
//            case R.id.action_share:
//                shareImages();
//                return true;
//            case R.id.action_tag:
//                break;
//            case R.id.action_delete:
//                deleteConfirmBuilder.create().show();
//                return true;
////            case R.id.action_about:
////                FragmentManager fm = getSupportFragmentManager();
////                AboutFragment aboutDialog = new AboutFragment();
////                aboutDialog.show(fm, "about_view");
////                break;
//            default:
//                break;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }



//    public void shareImages() {
//        ArrayList<String> selectedFiles = myThumbAdapter.getSelectedFiles();
//
//        if (selectedFiles.size() == 1) {
//            /* Only one scanned document selected: ACTION_SEND intent */
//            final Intent shareIntent = new Intent(Intent.ACTION_SEND);
//            shareIntent.setType("image/jpg");
//
//            Uri uri = FileProvider.getUriForFile(getApplicationContext(), getPackageName() + ".fileprovider", new File(selectedFiles.get(0)));
//            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
//            Log.d("GalleryGridActivity", "uri " + uri);
//
//            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_snackbar)));
//        } else {
//            ArrayList<Uri> filesUris = new ArrayList<>();
//            for (String i : myThumbAdapter.getSelectedFiles()) {
//                Uri uri = FileProvider.getUriForFile(getApplicationContext(), getPackageName() + ".fileprovider", new File(i));
//                filesUris.add(uri);
//                Log.d("GalleryGridActivity", "uri " + uri);
//            }
//
//            final Intent shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
//            shareIntent.setType("image/jpg");
//
//            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, filesUris);
//
//            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_snackbar)));
//        }
//    }


}
