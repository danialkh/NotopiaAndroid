package ir.notopia.android.scanner.opennotescanner;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageSize;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import ir.notopia.android.R;
import ir.notopia.android.database.entity.ScanEntity;
import ir.notopia.android.scanner.opennotescanner.helpers.Utils;
import ir.notopia.android.scanner.opennotescanner.views.TagEditorFragment;

public class FullScreenViewActivity extends AppCompatActivity {

    private Utils utils;
    private FullScreenImageAdapter mAdapter;
    private ViewPager mViewPager;

    private ImageLoader mImageLoader;
    private ImageSize mTargetSize;
    private int mMaxTexture;
    private Executor executor = Executors.newSingleThreadExecutor();

    private ScanEntity mScan;
    private List<ScanEntity> mScans;
    private int position;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        ((OpenNoteScannerApplication) getApplication()).getTracker()
//                .trackScreenView("/FullScreenViewActivity", "Full Screen Viewer");

        setContentView(R.layout.activity_fullscreen_view);


        mViewPager = (ViewPager) findViewById(R.id.pager);

        utils = new Utils(getApplicationContext());

        Intent i = getIntent();
        position = i.getIntExtra("position", 0);
//        int id = i.getIntExtra("scan_id", 0);
//        position = id - 1;
//        position = id;

        Log.d("inbaar postion:",position+ "");
//        Log.d("inbaar id:",id + "");


        // initialize Universal Image Loader
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this).build();
        mImageLoader = ImageLoader.getInstance();
        mImageLoader.init(config);

        mMaxTexture = Utils.getMaxTextureSize();
        Log.d("FullScreenViewActivity", "gl resolution: " + mMaxTexture);
        mTargetSize = new ImageSize(mMaxTexture, mMaxTexture);

        loadAdapter();

        // displaying selected image first
//        mScan = mRepository.getScan(position);

        mViewPager.setCurrentItem(position);
//        mViewPager.setCurrentItem(mScan.getId());

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Log.d("fullview", "scrolled position " + position + " offset " + positionOffset);
                Log.d("fullview", "pager " + FullScreenViewActivity.this.mViewPager.getCurrentItem());
            }

            @Override
            public void onPageSelected(int position) {
                Log.d("fullview", "selected");
                Log.d("fullview", "item" + FullScreenViewActivity.this.mViewPager.getCurrentItem());

            }

            @Override
            public void onPageScrollStateChanged(int state) {
                Log.d("fullview", "state changed");
            }

        });

    }

    private void loadAdapter() {
        mViewPager.setAdapter(null);

        ArrayList<ScanEntity> mScans = getIntent().<ScanEntity>getParcelableArrayListExtra("title_body");


        mAdapter = new FullScreenImageAdapter(FullScreenViewActivity.this,
                mScans);
        mAdapter.setImageLoader(mImageLoader);
        mAdapter.setMaxTexture(mMaxTexture, mTargetSize);
        mViewPager.setAdapter(mAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_imagepager, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_tag:
                tagImage();
                return true;
            case R.id.action_share:
                shareImage();
                return true;
//            case R.id.action_about:
//                FragmentManager fm = getSupportFragmentManager();
//                AboutFragment aboutDialog = new AboutFragment();
//                aboutDialog.show(fm, "about_view");
//                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void tagImage() {
        int item = mViewPager.getCurrentItem();
        String filePath = mAdapter.getPath(item);

        if (filePath.endsWith(".png")) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.format_not_supported);
            builder.setMessage(R.string.format_not_supported_message);
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            AlertDialog alerta = builder.create();
            alerta.show();
            return;
        }

        FragmentManager fm = getSupportFragmentManager();
        TagEditorFragment tagEditorDialog = new TagEditorFragment();

        tagEditorDialog.setFilePath(filePath);

        tagEditorDialog.setRunOnDetach(new Runnable() {
            @Override
            public void run() {
            }
        });
        tagEditorDialog.show(fm, "tageditor_view");
    }



    public void shareImage() {

        ViewPager pager = FullScreenViewActivity.this.mViewPager;
        int item = pager.getCurrentItem();

        final Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/jpg");
//        Uri uri = FileProvider.getUriForFile(getApplicationContext(), getPackageName()+".fileprovider", new File(mAdapter.getPath(item)));
        Uri uri = Uri.parse(mAdapter.getPath(item));
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        Log.d("Fullscreen", "uri " + uri);

        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_snackbar)));
    }



}
