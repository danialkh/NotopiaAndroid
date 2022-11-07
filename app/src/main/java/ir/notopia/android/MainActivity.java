package ir.notopia.android;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;
import androidx.viewpager.widget.ViewPager;

import ir.hamsaa.persiandatepicker.util.PersianCalendar;
import ir.mirrajabi.persiancalendar.PersianCalendarView;
import ir.mirrajabi.persiancalendar.core.PersianCalendarHandler;
import ir.mirrajabi.persiancalendar.core.interfaces.OnDayClickedListener;
import ir.mirrajabi.persiancalendar.core.models.PersianDate;
import ir.notopia.android.adapter.TagsAdaptor;
import ir.notopia.android.ar.ARActivity;
import ir.notopia.android.ar.CheckArFiles;
import ir.notopia.android.ar.WebViewActivity;
import ir.notopia.android.calender.CalendarActivity;
import ir.notopia.android.database.AppRepository;
import ir.notopia.android.database.entity.ArEntity;
import ir.notopia.android.database.entity.AttachEntity;
import ir.notopia.android.database.entity.CategoryEntity;
import ir.notopia.android.database.entity.ScanEntity;
import ir.notopia.android.database.entity.TagAssignedEntity;
import ir.notopia.android.database.entity.TagEntity;
import ir.notopia.android.menu.AboutActivity;
import ir.notopia.android.menu.SettingActivity;
import ir.notopia.android.menu.CategoryActivity;
import ir.notopia.android.menu.EditProfileActivity;
import ir.notopia.android.menu.SupportActivity;
import ir.notopia.android.menu.TagActivity;
import ir.notopia.android.notoLog.NotopiaLog;
import ir.notopia.android.noton.SearchNotonActivity;
import ir.notopia.android.scanner.opennotescanner.FullScreenViewActivity;
import ir.notopia.android.scanner.opennotescanner.GalleryAdaptor;
import ir.notopia.android.scanner.opennotescanner.helpers.Utils;
import ir.notopia.android.utils.Constants;
import ir.notopia.android.verification.CheckSignedIn;
import ir.notopia.android.verification.SignUpActivity;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.provider.OpenableColumns;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.TransitionManager;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.dragselectrecyclerview.DragSelectRecyclerView;
import com.airbnb.lottie.LottieAnimationView;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexboxLayoutManager;
import com.google.android.flexbox.JustifyContent;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.navigation.NavigationView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import com.google.gson.JsonArray;
import com.pushpole.sdk.PushPole;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private static final int MY_PERMISSIONS_REQUEST_WRITE = 333;
    private int MY_PERMISSIONS_REQUEST_READ = 474;
    private int PICKFILE_RESULT_CODE = 1212;
    private final String TAG = MainActivity.class.getName();
    private static BottomSheetBehavior mBottomSheet;
    private static String CallFilePermiFor = "";
    private boolean exportBackup = false;
    private ImageView darkness_view;
    private static ImageView AddFloatingBtn;
    private ImageView ShelfIcon;
    private ImageView SearchIcon;
    private ImageView SelectIcon,CalendarIcon;
    private static ImageView FiltersFloatingBtn;
    private ImageView exportPdf,exportNoton,deleteSelectedNotons;
    private LottieAnimationView ArIconAnimation;
    private ImageView removeFiltersIndicator;
    private AnimatedVectorDrawable animation;
    private ConstraintLayout constraintLayout;
    private ConstraintSet show_selected_label_constraint;
    private AppRepository mRepository;
    private List<ScanEntity> mScans;
    private GalleryAdaptor galleryAdaptor;
    private RecyclerView galleryRecycleView;
    BottomSheetSliderAdaptor bottomSheetSliderAdaptor;
    ViewPager viewPager;
    DrawerLayout mDrawerLayout;

    private LinearLayout LLContentSelectNav;
    private LinearLayout LLBackSelectNav;


    private String doHamedState;
    private String FilterCategory = "All";
    private long filterStartDate = -1;
    private long filterEndDate = -1;
    private static int FilterTagId = -1;
    private static boolean FilterSelectEnable;

    private PersianCalendarView persianCalendarView;
    private PersianCalendarHandler calendar;
    private PersianDate today;

    private String version = "";

    public static void reloadTag(Activity activity,int tagId,boolean needToFilter) {
        Log.d("TagsAdaptor","reload");


        EditText ETSearchTag = activity.findViewById(R.id.ETSearchTag);
        String searchStr = ETSearchTag.getText().toString();

        TagsAdaptor tagsFilterAdaptor = new TagsAdaptor(activity,searchStr);
        // new Utils(getApplicationContext()).getFilePaths(););

        RecyclerView tagsRecycleView = (DragSelectRecyclerView) activity.findViewById(R.id.recycleViewTagFilter);
        FlexboxLayoutManager layoutManager = new FlexboxLayoutManager(activity);
        layoutManager.setFlexDirection(FlexDirection.ROW);
        layoutManager.setJustifyContent(JustifyContent.SPACE_AROUND);
        tagsRecycleView.setLayoutManager(layoutManager);



        Log.d("reloadTag:",tagId + "");



        tagsRecycleView.setAdapter(tagsFilterAdaptor);

        if(needToFilter)
            MainActivity.FilterTagId = tagId;
        else{
            MainActivity.FilterTagId = -1;
            TagsAdaptor.myPosition = -1;
        }

    }

    public static void enableSelect(Activity activity) {

        // when long press on noton programmatically click on select button
        ImageView v = activity.findViewById(R.id.select_icon_enable);
        v.performClick();

    }


    @SuppressLint("ClickableViewAccessibility")

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setNavigationViewListener();

        version = "";

        try {
            Context context = MainActivity.this.getApplicationContext();
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        // check if login needed throw user to verification activity
        new CheckSignedIn(MainActivity.this);
        new FirstCategoryInit(MainActivity.this);
        new FirstTagInit(MainActivity.this);

        PushPole.initialize(this,true);


        mRepository = AppRepository.getInstance(MainActivity.this);

        mScans = mRepository.getScans();

        FilterSelectEnable = false;


        SharedPreferences RahnamaMain = MainActivity.this.getSharedPreferences("rahnamaMain", Context.MODE_PRIVATE);
        String SawHelp = RahnamaMain.getString("SawHelp", "0");




        SharedPreferences helpScan = MainActivity.this.getSharedPreferences("helpScan", Context.MODE_PRIVATE);

        SharedPreferences defainHamed = MainActivity.this.getSharedPreferences("doHamed", Context.MODE_PRIVATE);
        defainHamed.edit().putString("doHamedState","0").apply();

        SharedPreferences mahsol = MainActivity.this.getSharedPreferences("Mahsol_PR", Context.MODE_PRIVATE);
        String mahsolCode = mahsol.getString("Mahsol_bool_PR", "");

        SharedPreferences login = MainActivity.this.getSharedPreferences("SignedIn_PR", Context.MODE_PRIVATE);
        String userNumber = login.getString("USER_NUMBER_PR", null);
        String userName = login.getString("USER_NAME_PR", null);
        String userFamily = login.getString("USER_FAMILY_PR", null);



        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView TVUserNumber = headerView.findViewById(R.id.MenuUserNumber);
        TVUserNumber.setText("+98" + userNumber);
        TextView TVUserFullName = headerView.findViewById(R.id.MenuUserFullName);
        TVUserFullName.setText(userName + " " + userFamily);

        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);

        removeFiltersIndicator = findViewById(R.id.remove_filters_indicator);
        ArIconAnimation = findViewById(R.id.ar_icon_animation);
        AddFloatingBtn = findViewById(R.id.add_floating_btn);
        FiltersFloatingBtn = findViewById(R.id.filter_floating_btn);
        ShelfIcon = findViewById(R.id.shelf_icon);
        SelectIcon = findViewById(R.id.select_icon_enable);
        CalendarIcon = findViewById(R.id.icon_calendar);
        SearchIcon = findViewById(R.id.search_icon);
        exportPdf = findViewById(R.id.exportPdf);
        exportNoton = findViewById(R.id.exportNoton);
        deleteSelectedNotons = findViewById(R.id.deleteSelectedNotons);


        constraintLayout = findViewById(R.id.mainL);
        bottomSheetSliderAdaptor = new BottomSheetSliderAdaptor(this);
        viewPager = findViewById(R.id.SliderViewPager);
        viewPager.setAdapter(bottomSheetSliderAdaptor);

        try {
            loadGallery(FilterCategory,filterStartDate,filterEndDate,FilterTagId,FilterSelectEnable);
        } catch (ParseException e) {
            e.printStackTrace();
        }



        TextView label5 = findViewById(R.id.bottomSheet_label5);
        TextView label3 = findViewById(R.id.bottomSheet_label3);
        TextView labelTag = findViewById(R.id.bottomSheet_labelTag);
        TextView label2 = findViewById(R.id.bottomSheet_label2);


        //AddFloatingBtn,ArIconAnimation,SelfIcon

        if(SawHelp.equals("0") && !(userName == null)){
            RahnamaMain.edit().putString("SawHelp","1").apply();
            View[] views = {AddFloatingBtn, FiltersFloatingBtn, ArIconAnimation, ShelfIcon};
            startShowCase(MainActivity.this, views, "ShowCaseMain15");
        }

        label5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                viewPager.setCurrentItem(0);
                changeSelectedLabelShowerSize(label5);
            }
        });
        labelTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(2);
                changeSelectedLabelShowerSize(label3);
            }
        });
        label3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                viewPager.setCurrentItem(1);
                changeSelectedLabelShowerSize(label3);
            }
        });
        label2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // GO TO PAGE DATE
                viewPager.setCurrentItem(3);
                // EXPAND DATE PAGE ITEMS TO TAGHVIM
                if(mBottomSheet.getState() == BottomSheetBehavior.STATE_EXPANDED){
                    expanding_date_layout(1);
                }
                else{
                    expanding_date_layout(0);
                }
                // RESIZE THE SHOWER PAGE SELECTED
                changeSelectedLabelShowerSize(label2);
            }
        });

        SearchIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageViweFadeOutFadeIn(SearchIcon);
                Intent i = new Intent(MainActivity.this, SearchNotonActivity.class);
                MainActivity.this.startActivity(i);
            }
        });

        ShelfIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageViweFadeOutFadeIn(ShelfIcon);
                Intent intentShelf = new Intent(MainActivity.this, ShelfsActivity.class);
                startActivity(intentShelf);


            }
        });


        exportPdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageViweFadeOutFadeIn(exportPdf);

                Log.d("exportPdf",GalleryAdaptor.selectedScans.toString());
                CallFilePermiFor = "pdf";

                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    checkFilePermissions();

                }
                else{
                    GeneratePDF(MainActivity.this);
                }




            }
        });



        exportNoton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageViweFadeOutFadeIn(exportNoton);
                exportBackup = false;

                Log.d("exportZip",GalleryAdaptor.selectedScans.toString());
                CallFilePermiFor = "export";

                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

                    checkFilePermissions();

                }
                else{
                    try {
                        Generate9ot();
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }


            }
        });


        deleteSelectedNotons.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageViweFadeOutFadeIn(deleteSelectedNotons);

                List<ScanEntity> selectedScans = GalleryAdaptor.selectedScans;

                Activity _activity = MainActivity.this;

                AlertDialog.Builder deleteConfirmBuilder;

                deleteConfirmBuilder = new AlertDialog.Builder(_activity,R.style.AlertDialogCustom);
                deleteConfirmBuilder.setTitle(_activity.getResources().getString(R.string.confirm_title_delete_noton));
                deleteConfirmBuilder.setMessage(_activity.getResources().getString(R.string.confirm_delete_notons));
                deleteConfirmBuilder.setPositiveButton(_activity.getResources().getString(R.string.answer_yes), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                        for(int i = 0;i < selectedScans.size();i++){
                            ScanEntity mScan = selectedScans.get(i);
                            deleteNoton(mScan);
                        }

                        new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                try {
                                    loadGallery(FilterCategory,filterStartDate,filterEndDate,FilterTagId,FilterSelectEnable);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        },
                        100);



                        dialog.dismiss();
                    }

                });


                deleteConfirmBuilder.setNegativeButton(_activity.getResources().getString(R.string.answer_no), new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                deleteConfirmBuilder.create().show();




            }
        });



        removeFiltersIndicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MainActivity.imageViweFadeOutFadeIn(removeFiltersIndicator);

                FilterCategory = "All";
                filterStartDate = -1;
                filterEndDate = -1;
                FilterTagId = -1;
                // if bottomSheet is on page 3 refresh tags adaptor
                if (viewPager.getCurrentItem() == 2){
                    reloadTag(MainActivity.this,0,false);
                }
                try {
                    loadGallery(FilterCategory,filterStartDate,filterEndDate,FilterTagId,FilterSelectEnable);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if(today != null){
                    persianCalendarView.update();
                    persianCalendarView.goToDate(today);
                }
            }
        });

        ArIconAnimation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageViweFadeOutFadeIn(ArIconAnimation);

                if(!mahsolCode.equals("")) {
                    boolean isOkArFiles = new CheckArFiles(MainActivity.this).isOkArFiles();
                    if(isOkArFiles) {
                        Intent intentAr = new Intent(MainActivity.this, ARActivity.class);
                        startActivity(intentAr);
                    }
                }
                else{
                    Toast.makeText(MainActivity.this,"لطفا یک محصول اضافه کنید",Toast.LENGTH_SHORT).show();
                }
            }
        });


        CalendarIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageViweFadeOutFadeIn(CalendarIcon);
                Intent i = new Intent(MainActivity.this, CalendarActivity.class);
                MainActivity.this.startActivity(i);

            }
        });


        SelectIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                imageViweFadeOutFadeIn(SelectIcon);
                LLContentSelectNav = findViewById(R.id.LLContentSelectNav);
                LLBackSelectNav = findViewById(R.id.LLBackSelectNav);


                if(FilterSelectEnable){
                    FilterSelectEnable = false;
                    AddFloatingBtn.setVisibility(View.VISIBLE);
                    FiltersFloatingBtn.setVisibility(View.VISIBLE);
                    mBottomSheet.setState(BottomSheetBehavior.STATE_HIDDEN);

                    LLContentSelectNav.setVisibility(View.GONE);
                    LLBackSelectNav.setVisibility(View.GONE);
                }
                else{
                    FilterSelectEnable = true;
                    AddFloatingBtn.setVisibility(View.GONE);
                    FiltersFloatingBtn.setVisibility(View.GONE);
                    mBottomSheet.setState(BottomSheetBehavior.STATE_HIDDEN);

                    LLContentSelectNav.setVisibility(View.VISIBLE);
                    LLBackSelectNav.setVisibility(View.VISIBLE);
                }

                try {
                    loadGallery(FilterCategory, filterStartDate, filterEndDate,FilterTagId,FilterSelectEnable);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });

        AddFloatingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageViweFadeOutFadeIn(AddFloatingBtn);

                int index = 0;
                PersianCalendar initDate = new PersianCalendar();
                String year = String.valueOf(initDate.getPersianYear());
                String month = String.valueOf(initDate.getPersianMonth());
                String day = String.valueOf(initDate.getPersianDay());


                // generate noton identifier
                String str = year + month + day + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()) + userNumber;
                byte[] data = str.getBytes();
                String NotonIdentifier = Base64.encodeToString(data, Base64.DEFAULT);

                ScanEntity mScan = new ScanEntity(NotonIdentifier,"", "1",year,month,day, "", false,"","",0);
                mRepository.insertScan(mScan);

                Log.d(TAG,"newScan:" + mScan.toString());


                int finalIndex = index;
                new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {

                        List<ScanEntity> mScans = mRepository.getScans();
                        int index = mScans.size();

                        Intent i = new Intent(MainActivity.this, FullScreenViewActivity.class);
                        i.putExtra("position",index);
                        i.putExtra("scan_id", getLastAddedRowId());
                        i.putParcelableArrayListExtra("title_body", (ArrayList<? extends Parcelable>) mScans);
                        MainActivity.this.startActivity(i);
                    }
                },
                50);

            }
        });

        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }
            @Override
            public void onPageSelected(int position) {
                int currentTextView;
                switch (position){
                    case 1:

                        loadTedadCategory();

                        ImageView dasteBandiAddIcon = findViewById(R.id.dastebandi_add_icon);
                        dasteBandiAddIcon.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                imageViweFadeOutFadeIn(dasteBandiAddIcon);
                            }
                        });

                        if(mBottomSheet.getState() == BottomSheetBehavior.STATE_EXPANDED){
                            expanding_progress_layout(1);

                            ImageView dasteBandiEditIcon = findViewById(R.id.dastebandi_edit_icon);
                            dasteBandiEditIcon.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    imageViweFadeOutFadeIn(dasteBandiEditIcon);
                                }
                            });

                            ImageView DasteBandiRemoveIcon = findViewById(R.id.dastebandi_remove_icon);
                            DasteBandiRemoveIcon.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    imageViweFadeOutFadeIn(DasteBandiRemoveIcon);
                                }
                            });
                        }
                        else{
                            expanding_progress_layout(0);
                        }
                        currentTextView = R.id.bottomSheet_label3;

                        List<CategoryEntity> mCategorys = mRepository.getCategorys();

                        for(int i = 1;i < 7;i++){
                            String strId = "back_category" + i;
                            String strIdText = "LabelDaste" + i;
                            int id = MainActivity.this.getResources().getIdentifier(strId,"id",MainActivity.this.getPackageName());
                            int idTX = getResources().getIdentifier(strIdText,"id",getPackageName());
                            int identifier = i;
                            ImageView imageView = findViewById(id);

                            int identifierText = 5 - (i - 1);
                            TextView textView = findViewById(idTX);
                            textView.setText(mCategorys.get(identifierText).getName());

                            int category = 7 - identifier;

                            if (!FilterCategory.equals("All")){
                                if(i ==  (7 - Integer.valueOf(FilterCategory))){
                                    imageView.setImageDrawable(getResources().getDrawable(R.drawable.vc_back_selected_category, getApplicationContext().getTheme()));
                                }
                            }
                            else
                                imageView.setImageDrawable(getResources().getDrawable(R.drawable.vc_back_dastebandi_item, getApplicationContext().getTheme()));

                            imageView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                    if(FilterCategory.equals(String.valueOf(category))) {
                                        try {
                                            loadGallery("All", filterStartDate, filterEndDate,FilterTagId,FilterSelectEnable);
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    else{
                                        FilterCategory = String.valueOf(category);
                                        try {
                                            loadGallery(FilterCategory, filterStartDate, filterEndDate,FilterTagId,FilterSelectEnable);
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }
                                    }

                                    Log.w("category clicked:",String.valueOf(category));
                                }
                            });
                        }



                        break;
                    case 2:
                        currentTextView = R.id.bottomSheet_labelTag;

                        MainActivity.reloadTag(MainActivity.this,0,false);


                        EditText ETSearchTag = MainActivity.this.findViewById(R.id.ETSearchTag);
                        ETSearchTag.addTextChangedListener(new TextWatcher() {
                            @Override
                            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s, int start, int before, int count) {
                                MainActivity.reloadTag(MainActivity.this,0,false);
                            }

                            @Override
                            public void afterTextChanged(Editable s) {

                            }
                        });





                        RecyclerView tagsRecycleView = findViewById(R.id.recycleViewTagFilter);
                        tagsRecycleView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                            @Override
                            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                                try {
                                    loadGallery(FilterCategory,filterStartDate,filterEndDate,FilterTagId,FilterSelectEnable);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        });



                        break;
                    case 3:

                        mBottomSheet.setState(BottomSheetBehavior.STATE_EXPANDED);
                        expanding_date_layout(1);

                        currentTextView = R.id.bottomSheet_label2;

                        persianCalendarView = findViewById(R.id.filter_calendar);
                        calendar = persianCalendarView.getCalendar();
                        today = calendar.getToday();

                        calendar.setOnDayClickedListener(new OnDayClickedListener() {
                            @SuppressLint("UseCompatLoadingForColorStateLists")
                            @Override
                            public void onClick(PersianDate date) {

                                TextView TVIndicatorEndDate = findViewById(R.id.TVIndicatorEndDate);
                                TextView TVIndicatorStartDate = findViewById(R.id.TVIndicatorStartDate);

                                @SuppressLint("SimpleDateFormat") DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
                                try {

                                    String str = (String)(date.getYear() + "/" + date.getMonth() + "/" + date.getDayOfMonth());
                                    long timeStep = ((Date)formatter.parse(str)).getTime();

                                    if(filterStartDate == -1){
                                        calendar.setSelectedDayBackground(R.drawable.vc_back_current_day_start);
                                        TVIndicatorStartDate.setText(str);
                                        filterStartDate = timeStep;

//                                        Toast.makeText(MainActivity.this,"تاریخ شروع انتخاب شد",Toast.LENGTH_SHORT).show();
                                    }
                                    else if(filterEndDate == -1){

                                        calendar.setSelectedDayBackground(R.drawable.vc_back_current_day_end);
                                        if(timeStep > filterStartDate) {
                                            TVIndicatorEndDate.setText(str);
                                            filterEndDate = timeStep;
                                        }
                                        else{
                                            Toast.makeText(MainActivity.this,"تاریخ پایان باید بعد از تاریخ شروع باشد",Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                    else{
                                        calendar.setSelectedDayBackground(R.drawable.vc_back_current_day_start);
                                        TVIndicatorStartDate.setText(str);
                                        filterStartDate = timeStep;
                                        filterEndDate = -1;

//                                        Toast.makeText(MainActivity.this,"تاریخ شروع انتخاب شد",Toast.LENGTH_SHORT).show();
                                    }

                                    loadGallery(FilterCategory,filterStartDate,filterEndDate,FilterTagId,FilterSelectEnable);

                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }

                                if(date.getDayOfMonth() == today.getDayOfMonth() && date.getDayOfWeek() == today.getDayOfWeek()){
                                    calendar.setTodayBackground(R.drawable.vc_back_current_day);
                                }
                                else{
                                    calendar.setTodayBackground(R.drawable.vc_back_item_date_today);
                                }

                            }
                        });

                        break;
                    default:

                        ImageView dashboardAddIcon2 = findViewById(R.id.dashboard_add_icon);
                        dashboardAddIcon2.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                imageViweFadeOutFadeIn(dashboardAddIcon2);
                            }
                        });

                        if(mBottomSheet.getState() == BottomSheetBehavior.STATE_EXPANDED){
                            expanding_progress_layout(1);

                            ImageView dashboardEditIcon = findViewById(R.id.dashboard_edit_icon);
                            dashboardEditIcon.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    imageViweFadeOutFadeIn(dashboardEditIcon);
                                }
                            });

                            ImageView dashboardRemoveIcon = findViewById(R.id.dashboard_remove_icon);
                            dashboardRemoveIcon.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    imageViweFadeOutFadeIn(dashboardRemoveIcon);
                                }
                            });
                        }
                        else{
                            expanding_progress_layout(0);
                        }
                        currentTextView = R.id.bottomSheet_label5;
                        break;
                }

                //change Constraint of shower selected label
                show_selected_label_constraint = new ConstraintSet();
                show_selected_label_constraint.clone(constraintLayout);
                show_selected_label_constraint.connect(R.id.ShowerLableState, ConstraintSet.START, currentTextView, ConstraintSet.START, 0);
                show_selected_label_constraint.connect(R.id.ShowerLableState, ConstraintSet.END, currentTextView, ConstraintSet.END, 0);
                show_selected_label_constraint.connect(R.id.ShowerLableState, ConstraintSet.BOTTOM, currentTextView, ConstraintSet.BOTTOM, 0);
                show_selected_label_constraint.connect(R.id.ShowerLableState, ConstraintSet.TOP, currentTextView, ConstraintSet.TOP, 0);
                TransitionManager.beginDelayedTransition(constraintLayout);
                show_selected_label_constraint.applyTo(constraintLayout);

                //change width of shower selected label
                TextView tempLabel = findViewById(currentTextView);
                changeSelectedLabelShowerSize(tempLabel);

                //make all bottom sheet textView Labels whites to black
                int[] arr = new int[]{R.id.bottomSheet_label2, R.id.bottomSheet_label3,R.id.bottomSheet_labelTag,R.id.bottomSheet_label5};
                for (int value : arr) {
                    if (value != currentTextView) {
                        TextView tempBlacker = findViewById(value);
                        if (tempBlacker.getCurrentTextColor() != Color.BLACK)
                            colorChangerTextView(tempBlacker, "#FFFFFF", "#000000");
                    }
                }
                //make current bottom sheet textView Labels black to white
                colorChangerTextView(tempLabel,"#000000","#FFFFFF");

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        FiltersFloatingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // fade out/in animation
                imageViweFadeOutFadeIn(FiltersFloatingBtn);

                // COLLAPSED/Hide bottom sheet
                if(mBottomSheet.getState() == BottomSheetBehavior.STATE_HIDDEN){
                    mBottomSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
                else if(mBottomSheet.getState() == BottomSheetBehavior.STATE_COLLAPSED){
                    mBottomSheet.setState(BottomSheetBehavior.STATE_HIDDEN);
                }
            }
        });

        View bottomsheet = findViewById(R.id.bottom_sheet);
        mBottomSheet = BottomSheetBehavior.from(bottomsheet);
        mBottomSheet.setState(BottomSheetBehavior.STATE_HIDDEN);
        moveFloatingActionBtn(-1);
        darkness_view = findViewById(R.id.img_darkness);
        mBottomSheet.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {

            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
            }
            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

                Log.w("slide:",""+slideOffset);

                runFadeKoli(slideOffset);
                moveFloatingActionBtn(slideOffset);
                expanding_date_layout(slideOffset);
                expanding_progress_layout(slideOffset);
                if (slideOffset <= 0) {
                    darkness_view.setVisibility(View.INVISIBLE);

                } else {
                    darkness_view.setVisibility(View.VISIBLE);
                }
            }
        });

        //open and close bottomsheet with click on the takht btn gray
        ImageView btn_BottomSheet_takht = findViewById(R.id.img_takhtBottomShape);
        btn_BottomSheet_takht.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // COLLAPSED/Expand bottom sheet
                if(mBottomSheet.getState() == BottomSheetBehavior.STATE_EXPANDED){
                    mBottomSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
                if(mBottomSheet.getState() == BottomSheetBehavior.STATE_COLLAPSED){
                    mBottomSheet.setState(BottomSheetBehavior.STATE_EXPANDED);
                }
            }
        });

        ImageView iv_menu = findViewById(R.id.icon_menu);
        iv_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageViweFadeOutFadeIn(iv_menu);
                mDrawerLayout.openDrawer(GravityCompat.START);


            }

        });

        //if click on darkness bottomsheet state set to STATE_COLLAPSED
        darkness_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // COLLAPSED  bottom sheet
                mBottomSheet.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

    }

    static void startShowCase(Activity activity, View[] views, String SHOWCASE_ID) {

        // Views:
        // 0) AddFloatingBtn
        // 1) FiltersFloatingBtn
        // 2) ArIconAnimation
        // 3) SelfIcon

        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(500); // half second between each showcase view

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(activity, SHOWCASE_ID);

        sequence.setConfig(config);

        sequence.addSequenceItem(views[0],"\"نوتون\" های خودت را ذخیره کن:\n" +
                "هر وقت چیزی نوشتی ازش یه \"نوتون\" بگیر!\n" +
                "برای اینکه بتونی چیزی رو که نوشتی تبدیل به \"نوتون\" کنی تا بعداً از امکاناتش استفاده کنی این دکمه را بزن و طبق راهنما گوشیت را روی صفحه نگه دار و صبر کن تا نوتوپیا با فوت و فنی که خودش بلده از صفحه عکس بگیره و \"نوتون\" جدیدی برات ایجاد کنه  \n" +
                "\"نوتون\" های جدیدت رو میتونی تو همین صفحه اصلی برنامه ببینی. ", "متوجه شدم");

        sequence.addSequenceItem(views[1],"دنبال چی هستی؟\n" +
                "هر وقت نیاز داشتی تا بین \"نوتون\" هات چیزی را پیدا کنی این دکمه رو بزن. فقط کافیه بگی دنبال چی هستی تا خیلی زود برات پیداش کنه.\n" +
                " \"نوتون\" ها بر اساس پارامترهای مختلفی مثل تاریخ، دسته بندی و نوع نوشت افزارت قابلیت فیلتر شدن دارن و نمایش \"نوتون\" ها تو گالری هم بر اساس همین فیلترها اتفاق می افته.", "متوجه شدم");

        sequence.addSequenceItem(views[2],
                "\"چو در جلوه آیی جهان دیدنیست\"\n" +
                        "هرجایی از نوشت افزارهای نوتوپیا که این علامت وجود داشت، چیزی بیش از اونچه که می بینید در اون نهفته است. برای کشف اون، این دکمه را بزن و گوشیت رو روی صفحه نگه دار.\n", "متوجه شدم");

        sequence.addSequenceItem(views[3],
                "برای مدیریت نوشت افزار های نوتوپیا از این قسمت وارد شو، اینجا می تونی انواع نوشت افزارهایی که تهیه کردی رو مشاهده و مدیریت کنی.", "متوجه شدم");

        sequence.start();
    }

    @Override
    public void onResume() {
        super.onResume();
        doHamedLoading();


        sendLog();


    }

    private void sendLog() {

        mRepository = AppRepository.getInstance(MainActivity.this);
        SharedPreferences login = MainActivity.this.getSharedPreferences("SignedIn_PR", Context.MODE_PRIVATE);
        String userNumber = login.getString("USER_NUMBER_PR", null);



        List<ScanEntity> mScans = mRepository.getScans();
        List<AttachEntity> mAttachs = mRepository.getAllAttachs();
        List<TagEntity> mTags = mRepository.getAllTags();
        List<CategoryEntity> mCategories = mRepository.getCategorys();
        List<ArEntity> mAr = mRepository.getArs();
        List<TagAssignedEntity> mAssignedTags = mRepository.getAllAssignedTags();

        SharedPreferences mahsol = MainActivity.this.getSharedPreferences("Mahsol_PR", Context.MODE_PRIVATE);
        String mahsolCode = mahsol.getString("Mahsol_bool_PR", "");

        new NotopiaLog(MainActivity.this,version,mahsolCode,userNumber,"mScans",mScans.toString());
        new NotopiaLog(MainActivity.this,version,mahsolCode,userNumber,"mAttachs",mAttachs.toString());
        new NotopiaLog(MainActivity.this,version,mahsolCode,userNumber,"mTags",mTags.toString());
        new NotopiaLog(MainActivity.this,version,mahsolCode,userNumber,"mCategories",mCategories.toString());
        new NotopiaLog(MainActivity.this,version,mahsolCode,userNumber,"mAr",mAr.toString());
        new NotopiaLog(MainActivity.this,version,mahsolCode,userNumber,"mAssignedTags",mAssignedTags.toString());

    }

    @Override
    protected void onStart() {
        super.onStart();


        // at first when app starts darkness set to invisible
        darkness_view.setVisibility(View.INVISIBLE);

        //start animation icons
        Drawable drawable_ar = ArIconAnimation.getDrawable();

        if (drawable_ar instanceof AnimatedVectorDrawableCompat) {
            AnimatedVectorDrawableCompat avd_ar = (AnimatedVectorDrawableCompat) drawable_ar;

            //  start icon animations
            avd_ar.start();

            //  repeat icon animations
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            avd_ar.start();
                        }
                    });
                }
            },0,8000);

        }
        if (drawable_ar instanceof AnimatedVectorDrawable) {
            AnimatedVectorDrawable avd_ar = (AnimatedVectorDrawable) drawable_ar;

            //  start icon animations
            avd_ar.start();
            //  repeat icon animations
            Timer timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            avd_ar.start();
                        }
                    });
                }
            },0,7000);
        }
    }


    private void expanding_date_layout(float number){

    }

    private void expanding_progress_layout(float number){
        //expanded date bottom sheet view
        if(viewPager.getCurrentItem() != 1 && viewPager.getCurrentItem() != 3 ) {
            if(number >= 0) {
                LinearLayout progress_linearLayout;

                if(viewPager.getCurrentItem() == 2){
                    progress_linearLayout = findViewById(R.id.progress_linearLayout_dastebandi);

                    ImageView dasteBandiEditIcon = findViewById(R.id.dastebandi_edit_icon);
                    dasteBandiEditIcon.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            imageViweFadeOutFadeIn(dasteBandiEditIcon);
                        }
                    });

                    ImageView dasteBandiRemoveIcon = findViewById(R.id.dastebandi_remove_icon);
                    dasteBandiRemoveIcon.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            imageViweFadeOutFadeIn(dasteBandiRemoveIcon);
                        }
                    });

                }
                else{
                    progress_linearLayout = findViewById(R.id.progress_linearLayout_dashboard);

                    ImageView dashboardBandiEditIcon = findViewById(R.id.dashboard_edit_icon);
                    dashboardBandiEditIcon.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            imageViweFadeOutFadeIn(dashboardBandiEditIcon);
                        }
                    });


                    ImageView dashboardBandiAddIcon = findViewById(R.id.dashboard_add_icon);
                    dashboardBandiAddIcon.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            imageViweFadeOutFadeIn(dashboardBandiAddIcon);
                        }
                    });

                    ImageView dashboardBandiRemoveIcon = findViewById(R.id.dashboard_remove_icon);
                    dashboardBandiRemoveIcon.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            imageViweFadeOutFadeIn(dashboardBandiRemoveIcon);
                        }
                    });
                }
                float scale = progress_linearLayout.getResources().getDisplayMetrics().density;
                progress_linearLayout.getLayoutParams().height = (int) ((46 * scale + 0.5f) * number) + (int)(17 * scale + 0.5f);
                progress_linearLayout.setLayoutParams(progress_linearLayout.getLayoutParams());
            }
        }
    }


    private void moveFloatingActionBtn(float number) {
        if(number <= 0){
            float TempNumber = 1 + number;
            float scale = AddFloatingBtn.getResources().getDisplayMetrics().density;
            ViewGroup.MarginLayoutParams add_params = (ViewGroup.MarginLayoutParams) AddFloatingBtn.getLayoutParams();
            add_params.bottomMargin = (int) ((115 * scale + 0.5f) * TempNumber) + (int) ((16 * scale + 0.5f)) ;
            AddFloatingBtn.setLayoutParams(AddFloatingBtn.getLayoutParams());

            ImageView FillterFloatingBtn = findViewById(R.id.filter_floating_btn);
            ViewGroup.MarginLayoutParams fillter_params = (ViewGroup.MarginLayoutParams) FillterFloatingBtn.getLayoutParams();
            fillter_params.bottomMargin = (int) ((115 * scale + 0.5f) * TempNumber) + (int) ((16 * scale + 0.5f)) ;
            FillterFloatingBtn.setLayoutParams(FillterFloatingBtn.getLayoutParams());
        }
    }

    //fading darkness function for bottomsheet
    private void runFadeKoli(float number) {

        if(number > 0) {
            //runFadeKoli
            darkness_view.setVisibility(View.VISIBLE);
            ObjectAnimator fadeIn = ObjectAnimator.ofFloat(darkness_view, "alpha", number);
            fadeIn.setDuration(0);
            final AnimatorSet mAnimationSet = new AnimatorSet();
            mAnimationSet.play(fadeIn);
            mAnimationSet.start();
        }
    }

    public static void imageViweFadeOutFadeIn(View tempView) {


        float FadeFirstAlphaNum  = (float) 1;
        float FadeSecondAlphaNum  = (float) 0.4;

        ObjectAnimator fadeOutAnimation = ObjectAnimator.ofFloat(tempView, "alpha", FadeFirstAlphaNum, FadeSecondAlphaNum);
        fadeOutAnimation.setDuration(200);
        final AnimatorSet mAnimationSet = new AnimatorSet();
        mAnimationSet.play(fadeOutAnimation);
        mAnimationSet.start();

        ObjectAnimator fadeInAnimation = ObjectAnimator.ofFloat(tempView, "alpha", FadeSecondAlphaNum, FadeFirstAlphaNum);
        fadeOutAnimation.setDuration(200);
        final AnimatorSet nAnimationSet = new AnimatorSet();
        nAnimationSet.play(fadeInAnimation);
        nAnimationSet.start();
    }

    public static void TextViweFadeOutFadeIn(TextView tempView) {

        float FadeFirstAlphaNum  = (float) 1;
        float FadeSecondAlphaNum  = (float) 0.4;

        ObjectAnimator fadeOutAnimation = ObjectAnimator.ofFloat(tempView, "alpha", FadeFirstAlphaNum, FadeSecondAlphaNum);
        fadeOutAnimation.setDuration(200);
        final AnimatorSet mAnimationSet = new AnimatorSet();
        mAnimationSet.play(fadeOutAnimation);
        mAnimationSet.start();

        ObjectAnimator fadeInAnimation = ObjectAnimator.ofFloat(tempView, "alpha", FadeSecondAlphaNum, FadeFirstAlphaNum);
        fadeOutAnimation.setDuration(200);
        final AnimatorSet nAnimationSet = new AnimatorSet();
        nAnimationSet.play(fadeInAnimation);
        nAnimationSet.start();
    }

    private void changeSelectedLabelShowerSize(TextView TempLabel){

        int size = TempLabel.getMeasuredWidth() + 55;
        ImageView showerPage = findViewById(R.id.ShowerLableState);
        showerPage.getLayoutParams().width = size;
        showerPage.setLayoutParams(showerPage.getLayoutParams());
    }

    private void colorChangerTextView(TextView tempLabel,String sColor,String eColor){

        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0.0f, 1.0f);
        valueAnimator.setDuration(350);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {

                float fractionAnim = (float) valueAnimator.getAnimatedValue();

                tempLabel.setTextColor(ColorUtils.blendARGB(Color.parseColor(sColor)
                        , Color.parseColor(eColor)
                        , fractionAnim));
            }
        });
        valueAnimator.start();
    }

    private void loadTedadCategory(){
        if(viewPager.getCurrentItem() == 1){
            List<CategoryEntity> mCategorys = mRepository.getCategorys();
            List<ScanEntity> mScans = mRepository.getScans();
            for(int i = 1;i < 7;i++){
                int identifier = i - 1;
                int counter = 0;
                String categoryId = String.valueOf(7 - mCategorys.get(identifier).getId());
                String strId = "LabelTedadDaste" + i;
                int id = getResources().getIdentifier(strId,"id",getPackageName());
                TextView textView = findViewById(id);

                for (int a = 0; a < mScans.size(); a++) {
                    ScanEntity obj = mScans.get(a);

                    Log.d("tededCheck_ScanCT",obj.getCategory());
                    Log.d("tededCheck_mCateId",categoryId);

                    if (obj.getCategory().equals(categoryId)) {
                        counter++;
                    }
                }
                String matn = "تعداد : " + counter;
                textView.setText(matn);

            }
        }
    }

    private void loadGallery(String category,long filterStartDate,long filterEndDate,int tagId,boolean selectEnable) throws ParseException {


        removeEmptyNotons();

        mScans = mRepository.getScans();



        loadTedadCategory();

        @SuppressLint("SimpleDateFormat") DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");

        TextView TVIndicatorCategory = findViewById(R.id.TVIndicatorCategory);
        TextView TVIndicatorStartDate = findViewById(R.id.TVIndicatorStartDate);
        TextView TVIndicatorEndDate = findViewById(R.id.TVIndicatorEndDate);
        TextView TVIndicatorTag = findViewById(R.id.TVIndicatorTag);



        LinearLayout box_filter = findViewById(R.id.box_filter_indicator);
        float scale = box_filter.getResources().getDisplayMetrics().density;
        LayoutTransition layoutTransition = box_filter.getLayoutTransition();
        int sizeInDp;
        if(filterStartDate != -1 || tagId != -1 || filterEndDate != -1 || !category.equals("All")) {
            sizeInDp = 90;
        }
        else {
            sizeInDp = 0;
        }



        if(box_filter.getLayoutParams().height > 0){

            layoutTransition.setDuration(250); // Change duration
            layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
            box_filter.getLayoutParams().height = (int) ((0 * scale + 0.5f));
            box_filter.setLayoutParams(box_filter.getLayoutParams());
            box_filter.requestLayout();

            final Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    layoutTransition.setDuration(250); // Change duration
                    layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
                    box_filter.getLayoutParams().height = (int) ((sizeInDp * scale + 0.5f));
                    box_filter.setLayoutParams(box_filter.getLayoutParams());
                    box_filter.requestLayout();
                }
            }, 250);

        }
        else{
            layoutTransition.setDuration(250); // Change duration
            layoutTransition.enableTransitionType(LayoutTransition.CHANGING);
            box_filter.getLayoutParams().height = (int) ((sizeInDp * scale + 0.5f));
            box_filter.setLayoutParams(box_filter.getLayoutParams());
            box_filter.requestLayout();
        }



        if(tagId == -1){
            // use here for ui change textView
            TVIndicatorTag.setText("-");
        }
        else{
            if(mRepository.checkTagExist(tagId)){
                TagEntity selectedTag = mRepository.getTagById(tagId);
                TVIndicatorTag.setText(selectedTag.getTagName());
            }
            else{
                TVIndicatorTag.setText("حذف شد");
                if(viewPager.getCurrentItem() == 2) {
                    reloadTag(MainActivity.this, 0, false);
                }
            }
        }


        if(filterStartDate == -1){
            TVIndicatorStartDate.setText("-");
        }
        if(filterEndDate == -1){
            TVIndicatorEndDate.setText("-");
        }


        if(category.equals("All")){
            TVIndicatorCategory.setText("همه");


            if(viewPager.getCurrentItem() == 1) {
                for (int i = 1; i < 7; i++) {
                    String strId = "back_category" + i;
                    int id = MainActivity.this.getResources().getIdentifier(strId, "id", MainActivity.this.getPackageName());
                    int identifier = i;
                    ImageView imageView = findViewById(id);
                    imageView.setImageDrawable(getResources().getDrawable(R.drawable.vc_back_dastebandi_item, getApplicationContext().getTheme()));
                }
            }

        }
        else{

            CategoryEntity categoryEntity = mRepository.getCategoryById(Integer.valueOf(category));
            TVIndicatorCategory.setText(categoryEntity.getName());

            if(viewPager.getCurrentItem() == 1) {
                for (int i = 1; i < 7; i++) {
                    String strId = "back_category" + i;
                    int id = MainActivity.this.getResources().getIdentifier(strId, "id", MainActivity.this.getPackageName());
                    int identifier = i;
                    ImageView imageView = findViewById(id);

                    if (i == (7 - Integer.valueOf(category)))
                        imageView.setImageDrawable(getResources().getDrawable(R.drawable.vc_back_selected_category, getApplicationContext().getTheme()));
                    else
                        imageView.setImageDrawable(getResources().getDrawable(R.drawable.vc_back_dastebandi_item, getApplicationContext().getTheme()));
                }
            }

        }

        FilterCategory = category;



        List<ScanEntity> tempScans = new ArrayList<>();
        ArrayList<String> ab = new ArrayList<>();

        if(true){

            for (int i = 0; i < mScans.size(); i++) {

                ScanEntity obj = mScans.get(i);
                boolean check = false;
                String str = (String)(obj.getYear() + "/" + obj.getMonth() + "/" + obj.getDay());
                long date = ((Date)formatter.parse(str)).getTime();

                Log.d("dddddd i:",String.valueOf(i));

                if (obj.getCategory().equals(category)) {
                    check = true;
                }
                else if(category.equals("All")){
                    check = true;
                }
                else{
                    check = false;
                }

                if(check){

                    if(filterStartDate != -1){
                        if(date < filterStartDate) {
                            check = false;
                        }
                    }
                }

                if(check){
                    if (filterEndDate != -1){
                        if(date > filterEndDate){
                            check = false;
                        }
                    }
                }

                // check if tag is filtered and this noton has this tag
                if(check){
                    if (tagId != -1){
                        check = notonTagSearch(obj,tagId);
                    }
                }

                Log.d(TAG + "Filter number:",String.valueOf(i));
                Log.d(TAG + "Filter Date:",str);
                Log.d(TAG + "Filter Date:",String.valueOf(date));
                Log.d(TAG + "Filter DateStart:",String.valueOf(filterStartDate));
                Log.d(TAG + "Filter DateEnd:",String.valueOf(filterEndDate));
                Log.d(TAG + "Filter Check:",String.valueOf(check));



                if(check)
                    tempScans.add(obj);

            }

        }
        else{
            tempScans = mScans;
        }

        galleryAdaptor = new GalleryAdaptor(tempScans,selectEnable,MainActivity.this);
        // new Utils(getApplicationContext()).getFilePaths(););

        galleryRecycleView = (DragSelectRecyclerView) findViewById(R.id.GalleryRecyclerview);
        galleryRecycleView.setLayoutManager(new GridLayoutManager(this, 3));
        galleryRecycleView.setAdapter(galleryAdaptor);


    }

    private void removeEmptyNotons() {

        mRepository = AppRepository.getInstance(MainActivity.this);
        List<ScanEntity> mScans = mRepository.getScans();

        boolean needRefresh = false;

        for(int i = 0;i < mScans.size();i++){
            ScanEntity mScan = mScans.get(i);

            if(mScan.getImage().equals("") && mScan.getTitle().equals("") && mScan.getText().equals("")){
                needRefresh = true;
                mRepository.deleteScan(mScan);
            }
        }


        if(needRefresh) {

            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            try {
                                loadGallery(FilterCategory, filterStartDate, filterEndDate, FilterTagId, FilterSelectEnable);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    60);
        }

    }

    private boolean notonTagSearch(ScanEntity obj, int tagId) {

        List<TagAssignedEntity> assignedTags = mRepository.getAllAssignedTagsFromNoton(obj);

        boolean checker = false;

        for (int i = 0;i < assignedTags.size();i++){

            TagAssignedEntity assignedTag = assignedTags.get(i);
            if(assignedTag.getTagId() == tagId){
                checker = true;
            }
        }

        return checker;
    }


    private void setNavigationViewListener() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }



    private void doHamedLoading(){

        SharedPreferences doHamed = MainActivity.this.getSharedPreferences("doHamed", Context.MODE_PRIVATE);
        doHamedState = doHamed.getString("doHamedState", "0");

        LinearLayout LLContentDoHamed = findViewById(R.id.LLContentDoHamed);
        ImageView DarkdoHamed = findViewById(R.id.DarkdoHamed);


        if(doHamedState.equals("1")){
            LLContentDoHamed.setVisibility(View.VISIBLE);
            DarkdoHamed.setVisibility(View.VISIBLE);

            new android.os.Handler().postDelayed(
            new Runnable() {
                public void run() {
                    doHamedLoading();
                }
            },
            500);
        }
        else{
            LLContentDoHamed.setVisibility(View.GONE);
            DarkdoHamed.setVisibility(View.GONE);
            try {
                loadGallery(FilterCategory,filterStartDate,filterEndDate,FilterTagId,FilterSelectEnable);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(CallFilePermiFor.equals("")){
            importNoton("onRequest");
        }
        else{
            if(CallFilePermiFor.equals("export")){
                try {
                    Generate9ot();
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
            else{
                GeneratePDF(MainActivity.this);
            }
        }


    }

    private void checkFilePermissions() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_WRITE);

        }

    }


    private void checkBothReadAndWritePermi(){

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){

            Uri returnUri = data.getData();

            String filePath = getFileName(returnUri);

            // change name of attached file
            String[] separated = filePath.split("\\.");
            int len = separated.length;
            String fileFormat = "";
            if(len > 0)
                fileFormat = separated[len - 1];

            Log.d(TAG,"manomige" + filePath);
            Log.d(TAG,"manomige" + fileFormat);

            if(fileFormat != null && fileFormat.equals("9ot")) {

                String fileName = "9ot-" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date())
                        + new Random().nextInt(61) + 20
                        + "." + fileFormat;

                String directory = "/Notopia/import/";

                String importFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + directory;

                CreateDirectory(importFolder);

                String finalPath = importFolder + fileName;

                MoveFile(this, filePath, finalPath,returnUri);

                try {
                    ZipManager.unzip(finalPath,importFolder);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Log.d(TAG,"manomige" + "fileFormat");




                String pathJsonFile = importFolder + "jsonFileName.json";
                JSONArray jsonObject = new JSONArray();
                try {
                    ReadFile(pathJsonFile,false,false,jsonObject);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            else{
                Toast.makeText(this,"فایل نامعتبر است",Toast.LENGTH_SHORT).show();
            }

        }
        else{

            if(CallFilePermiFor.equals("export")){
                try {
                    Generate9ot();
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
            else{
                GeneratePDF(MainActivity.this);
            }


        }
    }

    public void ReadFile (String path,Boolean notAskOnce,boolean igoneExists,JSONArray jsonObj) throws IOException, JSONException {

        if(mDrawerLayout.isDrawerOpen(GravityCompat.START)){
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }

        if(!notAskOnce){
            File yourFile = new File(path);
            FileInputStream stream = new FileInputStream(yourFile);
            String jsonStr = null;

            try {
                FileChannel fc = stream.getChannel();
                MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());

                jsonStr = Charset.defaultCharset().decode(bb).toString();
            }
            catch(Exception e){
                e.printStackTrace();
            }
            finally {
                stream.close();
            }

            Log.d(TAG,"ReadFile" + " jsonStr:" + jsonStr);

            jsonObj = new JSONArray(jsonStr);

            Log.d(TAG,"ReadFile" + " jsonObj:" + jsonObj);


            AskForDeleteRepetetiveNotons(path, jsonObj);
        }

        Log.d(TAG,"ReadFile" + " path:" + path);

        try {


            boolean checkNotonExist = false;
            for (int i = 0; i < jsonObj.length(); i++) {
                JSONObject c = jsonObj.getJSONObject(i);
                String identifier = c.getString("identifier");

                mScans = mRepository.getScans();
                for (int l = 0; l < mScans.size(); l++) {
                    if (mScans.get(l).getIdentifier().equals(identifier)) {
                        checkNotonExist = true;
                        break;
                    }
                }
            }




            // looping through All notons in json file
            for (int i = 0; i < jsonObj.length(); i++) {

                JSONObject c = jsonObj.getJSONObject(i);

                if(igoneExists) {
                    checkNotonExist = false;
                    String identifier = c.getString("identifier");

                    mScans = mRepository.getScans();
                    for (int l = 0; l < mScans.size(); l++) {
                        if (mScans.get(l).getIdentifier().equals(identifier)) {
                            checkNotonExist = true;
                            break;
                        }
                    }
                }


                if(!checkNotonExist) {
                    ScanEntity addScan = new ScanEntity(c.getString("identifier"), c.getString("image"), c.getString("category"), c.getString("year"), c.getString("month"), c.getString("day"), c.getString("qrCode"), Boolean.parseBoolean(c.getString("needEdit")), c.getString("title"), c.getString("text"), Integer.parseInt(c.getString("color")));
                    mRepository.insertScan(addScan);

                    Log.d(TAG, "newScan:" + addScan.toString());

                    Log.d(TAG, "ReadFile" + " addScan:" + addScan);

                    String attachStr = c.getString("attachs");
                    String assignTagsStr = c.getString("mAssignTags");

                    Log.d(TAG, "ReadFile" + " attachStr:" + attachStr);
                    Log.d(TAG, "ReadFile" + " attachStr:" + attachStr);


                    JSONArray jsonAttachArr = new JSONArray(attachStr);
                    JSONArray jsonAssignTagArr = new JSONArray(assignTagsStr);


                    int finalI = i;
                    JSONArray finalJsonObj = jsonObj;
                    new android.os.Handler().postDelayed(
                            new Runnable() {
                                public void run() {


                                    int dif = finalJsonObj.length() - (finalI + 1);

                                    Log.d(TAG, "ReadFile" + " finalI:" + finalI);
                                    Log.d(TAG, "ReadFile" + " jsonObj.length():" + finalJsonObj.length());


                                    addAttachAssignTagsToImportedNoton(jsonAssignTagArr, jsonAttachArr, dif);


                                }
                            },
                            50);
                }
            }

            //remove import folder and files
            String directory = "/Notopia/import/";
            String importFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + directory;

            File dir = new File(importFolder);
            if (dir.isDirectory())
            {
                String[] children = dir.list();
                for (int i = 0; i < children.length; i++)
                {
                    new File(dir, children[i]).delete();
                }
            }

            boolean finalCheckNotonExist = checkNotonExist;
            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {


                            if(!finalCheckNotonExist)
                                Toast.makeText(MainActivity.this,"نوتون ها اضافه شدند",Toast.LENGTH_SHORT).show();

                            try {
                                loadGallery(FilterCategory,filterStartDate,filterEndDate,FilterTagId,FilterSelectEnable);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                    },
            60);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void AskForDeleteRepetetiveNotons(String pathJsonFile,JSONArray jsonObj) throws JSONException {

        boolean checkNotonExist = false;

        for (int i = 0; i < jsonObj.length(); i++) {

            JSONObject c = jsonObj.getJSONObject(i);


            // check if notonExist by identifier

            String identifier = c.getString("identifier");

            mScans = mRepository.getScans();
            for (int l = 0; l < mScans.size(); l++) {
                if (mScans.get(l).getIdentifier().equals(identifier)) {
                    checkNotonExist = true;
                    break;
                }
            }
        }



        if (checkNotonExist) {
            Activity _activity = MainActivity.this;

            AlertDialog.Builder replaceConfirmBuilder;

            replaceConfirmBuilder = new AlertDialog.Builder(_activity, R.style.AlertDialogCustom);
            replaceConfirmBuilder.setTitle(_activity.getResources().getString(R.string.confirm_title_replace_noton));
            replaceConfirmBuilder.setMessage(_activity.getResources().getString(R.string.confirm_replace_notons));
            replaceConfirmBuilder.setPositiveButton(_activity.getResources().getString(R.string.answer_yes), new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {


                    for (int i = 0; i < jsonObj.length(); i++) {

                        JSONObject c = null;
                        try {
                            c = jsonObj.getJSONObject(i);
                            // check if notonExist by identifier

                            String identifier = c.getString("identifier");

                            mScans = mRepository.getScans();
                            for (int l = 0; l < mScans.size(); l++) {
                                if (mScans.get(l).getIdentifier().equals(identifier)) {
                                    ScanEntity mScan = mScans.get(l);
                                    deleteNoton(mScan);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            try {
                                ReadFile(pathJsonFile,true,true,jsonObj);
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    100);


                    dialog.dismiss();
                }

            });

            replaceConfirmBuilder.setNegativeButton(_activity.getResources().getString(R.string.answer_no), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    JsonArray jsonObject = new JsonArray();
                    try {
                        ReadFile(pathJsonFile,true,true,jsonObj);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            replaceConfirmBuilder.create().show();


        }
    }

    private void addAttachAssignTagsToImportedNoton(JSONArray jsonAssignArr,JSONArray jsonAttachArr,int dif) {


        int notonId = getLastAddedRowId() - dif;

        Log.d(TAG,"ReadFile" + " notonId:" + notonId);
        Log.d(TAG,"ReadFile" + " jsonAttachArr:" + jsonAttachArr);
        Log.d(TAG,"ReadFile" + " jsonAssignArr:" + jsonAssignArr);

        for (int a = 0; a < jsonAssignArr.length(); a++) {

            try {

                JSONObject b = jsonAssignArr.getJSONObject(a);
                String tagName = b.getString("tagName");

                List<TagEntity> mTags = mRepository.getAllTags();


                Log.d(TAG,"ReadFile" + " mTags:" + mTags.toString());
                Log.d(TAG,"ReadFile" + " tagName:" + tagName);

                boolean checkContain = false;

                for(int j = 0;j < mTags.size();j++){

                    Log.d(TAG,"ReadFile" + " tagName:" + tagName);
                    Log.d(TAG,"ReadFile" + " maromige:" + mTags.get(j).getTagName());


                    if(mTags.get(j).getTagName().equals(tagName)){
                        checkContain = true;
                        Log.d(TAG,"ReadFile" + " maromige:" + "eeeeeeeeeee");
                        break;

                    }


                }



                if(checkContain){
                    // the tag is exist

                    Log.d(TAG,"ReadFile" + " mAssign:" + "inja");
                    TagEntity mTag = mRepository.getTagByName(tagName);
                    TagAssignedEntity mAssign = new TagAssignedEntity(mTag.getTagId(),notonId);
                    mRepository.insertAssignedTag(mAssign);


                    Log.d(TAG,"ReadFile" + " mAssign:" + mAssign.toString());
                }
                else{
                    // tag doest exist
                    Log.d(TAG,"ReadFile" + " mAssign:" + "onja");

                    TagEntity newTag = new TagEntity(tagName);
                    mRepository.insertTag(newTag);

                    // because tag was not exist we insert tag then using a handle
                    // we made a delay then assign tag to noton
                    new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {

                            TagEntity mTag = mRepository.getTagByName(tagName);
                            TagAssignedEntity mAssign = new TagAssignedEntity(mTag.getTagId(),notonId);
                            mRepository.insertAssignedTag(mAssign);

                            Log.d(TAG,"ReadFile" + " mAssign:" + mAssign.toString());


                        }
                    },
                    50);


                }



            } catch (JSONException e) {
                e.printStackTrace();
            }


        }


        for (int a = 0; a < jsonAttachArr.length(); a++) {

            AttachEntity mAttach = null;
            try {

                JSONObject b = jsonAttachArr.getJSONObject(a);
                mAttach = new AttachEntity(notonId,b.getString("strUri"),b.getString("type"),b.getString("path"));
                Log.d(TAG,"ReadFile" + "added Attach: " + mAttach.toString());

                if(mAttach != null)
                    mRepository.insertAttach(mAttach);


            } catch (JSONException e) {
                e.printStackTrace();
            }


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
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case  R.id.nav_user_profile:
                Intent intentEdit = new Intent(MainActivity.this, EditProfileActivity.class);
                startActivity(intentEdit);
                return true;
            case  R.id.nav_categories:
                Intent intentCategory = new Intent(MainActivity.this, CategoryActivity.class);
                startActivity(intentCategory);
                return true;
            case  R.id.nav_tags:
                Intent intentTag = new Intent(MainActivity.this, TagActivity.class);
                startActivity(intentTag);
                return true;
            case  R.id.nav_import:
                importNoton("nav");
                return true;
            case  R.id.nav_backup:
                backupNotons();
                return true;
            case  R.id.nav_about_notopia:
                Intent intentAbout = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(intentAbout);
                return true;
            case  R.id.nav_setting:
                Intent intentSetting = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intentSetting);
                return true;
            case  R.id.nav_support:
                Intent intentSupport = new Intent(MainActivity.this, SupportActivity.class);
                startActivity(intentSupport);
                return true;
            case  R.id.nav_other_products:
                String urlMahsolat = "https://davarpour.com/mahsolat/index.html";
                Intent productsIntent = new Intent(MainActivity.this, WebViewActivity.class);
                productsIntent.putExtra(Constants.WEB_VIEW_URL,urlMahsolat);
                productsIntent.putExtra("Ordertype","main");
                MainActivity.this.startActivity(productsIntent);
                return true;
            default:
                //logout user
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void backupNotons() {

        exportBackup = true;
        List<ScanEntity> mScans = mRepository.getScans();
        GalleryAdaptor.selectedScans.clear();
        GalleryAdaptor.selectedScans.addAll(mScans);

        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            checkFilePermissions();

        }
        else{
            try {
                Generate9ot();
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }

    }

    private void importNoton(String caller) {


        String[] VideoFiles  = { "*/*"};

        CallFilePermiFor = "";

        if(!caller.equals("onRequest"))
            checkBothReadAndWritePermi();

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {

            Intent chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
            chooseFile.setType("*/*");
            chooseFile = Intent.createChooser(chooseFile, "انتخاب فایل");
            startActivityForResult(chooseFile, PICKFILE_RESULT_CODE);
        }

    }


    public int getLastAddedRowId() {

        List<ScanEntity> mScans = mRepository.getScans();
        int max = 0;
        for(int i =0;i < mScans.size();i++){
            if(max < mScans.get(i).getId()){
                max = mScans.get(i).getId();
            }
        }
        return max;
    }


    public void Generate9ot() throws IOException, JSONException {

        boolean exportAttach = true;

        SharedPreferences includeAttach = MainActivity.this.getSharedPreferences("includeAttach", Context.MODE_PRIVATE);
        String includeAttachState = includeAttach.getString("includeAttachState", "true");


        SharedPreferences includeAttachBackup = MainActivity.this.getSharedPreferences("includeBackupAttach", Context.MODE_PRIVATE);
        String includeAttachBackupState = includeAttachBackup.getString("includeAttachBackupState", "true");

        if(includeAttachBackupState.equals("false") && exportBackup == true){
            exportAttach = false;
        }

        if(includeAttachState.equals("false") && exportBackup == false){
            exportAttach = false;
        }


        String directory = "/Notopia/export/";
        String exportFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + directory;

        CreateDirectory(exportFolder);

        String zipFilename = exportFolder + "9ot-" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date())
                + new Random().nextInt(61) + 20 + ".9ot";

        int count = GalleryAdaptor.selectedScans.size();

        if(count > 0) {

            ArrayList<String> arrFiles = new ArrayList<>();
            JSONArray arrayJsonNotons = new JSONArray();

            for (int i = 0; i < count; i++) {

                ScanEntity mScan = GalleryAdaptor.selectedScans.get(i);

                // add noton images to files paths
                File imageFile = new File(mScan.getImage());
                if (imageFile.exists()) {

                    // if the string path doest exist in arrFiles array
                    if (!arrFiles.contains(mScan.getImage()))
                        arrFiles.add(mScan.getImage());
                }

                // init object for json

                JSONObject o = new JSONObject();

                o.put("category", mScan.getCategory());
                o.put("year", mScan.getYear());
                o.put("month", mScan.getMonth());
                o.put("day", mScan.getDay());
                o.put("qrCode", mScan.getQrCode());
                o.put("needEdit", String.valueOf(mScan.isNeedEdit()));
                o.put("color", String.valueOf(mScan.getColor()));
                o.put("title", mScan.getTitle());
                o.put("text", mScan.getText());
                o.put("image", mScan.getImage());
                o.put("identifier", mScan.getIdentifier());

                List<AttachEntity> mAttachs = mRepository.getAllAttachsFromNoton(mScan);

                Log.d(TAG, "generateZip" + " mAttachs:" + mAttachs.toString());

                JSONArray arrayJsonAttachs = new JSONArray();
                if(exportAttach) {
                    for (int a = 0; a < mAttachs.size(); a++) {

                        AttachEntity mAttach = mAttachs.get(a);
                        JSONObject a_o = new JSONObject();

                        a_o.put("type", mAttach.getType());
                        a_o.put("path", mAttach.getPath());
                        a_o.put("strUri", mAttach.getStrUri());

                        arrayJsonAttachs.put(a_o);

                        // add attach file to arrFiles for Zip
                        File attachFile = new File(mAttach.getPath());
                        if (attachFile.exists()) {

                            // if the string path doest exist in arrFiles array
                            if (!arrFiles.contains(mAttach.getPath()))
                                arrFiles.add(mAttach.getPath());
                        }

                        Log.d(TAG, "generateZip" + " arrFiles:" + arrFiles);

                    }
                }
                o.put("attachs", arrayJsonAttachs);


                List<TagAssignedEntity> mAssignTags = mRepository.getAllAssignedTagsFromNoton(mScan);
                JSONArray arrayJsonAssignTags = new JSONArray();

                Log.d(TAG, "generateZip" + " mAssignTags:" + mAssignTags.toString());

                for (int a = 0; a < mAssignTags.size(); a++) {

                    TagAssignedEntity mAssign = mAssignTags.get(a);
                    JSONObject a_o = new JSONObject();

                    TagEntity mTag = mRepository.getTagById(mAssign.getTagId());

                    a_o.put("tagName", mTag.getTagName());

                    arrayJsonAssignTags.put(a_o);
                }
                o.put("mAssignTags", arrayJsonAssignTags);


                arrayJsonNotons.put(o);
            }


            // convert json object to string
            String json = arrayJsonNotons.toString();

            Log.d(TAG, "generateZip" + " arrayJsonNotons:" + json);


            // at the end add the json to arrFile to zip
            // save json file in PdfFolder + jsonFileName path
            String jsonFileName = "jsonFileName.json";
            saveJsonFile(this, exportFolder, jsonFileName, json);
            arrFiles.add(exportFolder + jsonFileName);


            // define files for generate zip
            String[] files = new String[arrFiles.size()];
            for (int a = 0; a < arrFiles.size(); a++) {
                files[a] = arrFiles.get(a);
            }

            // now time to generate zip file
            ZipManager.zip(files, zipFilename);

//        Toast.makeText(getApplicationContext(),
//                zipFilename + " ذخیره شد ", Toast.LENGTH_SHORT)
//                .show();


            Intent sharingIntent = new Intent(Intent.ACTION_SEND);

            File file = new File(zipFilename);

            if (file.exists()) {
                // use file provider
                Uri uri = FileProvider.getUriForFile(this, this.getApplicationContext().getPackageName() + ".myprovider", file);
                sharingIntent.setType("*/*");
                sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);
                startActivity(Intent.createChooser(sharingIntent, "اشتراک نوتون ها"));
            }

            // now delete the json file because we dont need it any more
            File jsonFile = new File(exportFolder, jsonFileName);
            if (jsonFile.exists()) {
                jsonFile.delete();
            }

        }
        else{
            Toast.makeText(getApplicationContext(), "لطفا حداقل یک نوتون انتخاب کنید",
                    Toast.LENGTH_SHORT).show();
        }
    }


    private boolean saveJsonFile(Context context,String folderName, String fileName, String jsonString){
        try {
            File jsonFile = new File(folderName, fileName);

            if(jsonFile.exists()) {
                jsonFile.delete();
            }

            OutputStreamWriter outputStreamWriter =
                    new OutputStreamWriter(new FileOutputStream(jsonFile));

            if (jsonString != null) {
                outputStreamWriter.write(jsonString);
            }
            outputStreamWriter.close();

            return true;
        } catch (FileNotFoundException fileNotFound) {
            return false;
        } catch (IOException ioException) {
            return false;
        }

    }


    @Override
    public void onBackPressed() {

        if(mDrawerLayout.isDrawerOpen(GravityCompat.START)){
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return;
        }
        else if(FilterSelectEnable){
            FilterSelectEnable = false;
            AddFloatingBtn.setVisibility(View.VISIBLE);
            FiltersFloatingBtn.setVisibility(View.VISIBLE);
            mBottomSheet.setState(BottomSheetBehavior.STATE_HIDDEN);

            LLContentSelectNav.setVisibility(View.GONE);
            LLBackSelectNav.setVisibility(View.GONE);

            try {
                loadGallery(FilterCategory, filterStartDate, filterEndDate,FilterTagId,FilterSelectEnable);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        else{
            super.onBackPressed();
        }

    }


    public void GeneratePDF(Activity activity)
    {
        // TODO Auto-generated method stub

        String directory = "/Notopia/Pdf/";
        String PdfFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + directory;


        String filename = "pdf-" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date())
                + new Random().nextInt(61) + 20;


        easyPdf fop = new easyPdf(activity);

        if(GalleryAdaptor.selectedScans.size() > 0){
            if (fop.write(filename,GalleryAdaptor.selectedScans)) {
                Toast.makeText(getApplicationContext(),
                        PdfFolder + filename + ".pdf ذخیره شد", Toast.LENGTH_SHORT)
                        .show();
            } else {
                Toast.makeText(getApplicationContext(), "مشکل در ایجاد Pdf",
                        Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(getApplicationContext(), "لطفا حداقل یک نوتون انتخاب کنید",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteNoton(ScanEntity mScan) {

        String filePath = mScan.getImage();
        final File photoFile = new File(filePath);
        if(photoFile.exists()) {

            //now let check if this Scan image is just used in this noton
            int count = mRepository.countScansByFilePath(filePath);
            // if there is one noton that has this image so delete file
            if(count == 1) {
                photoFile.delete();
                Utils.removeImageFromGallery(filePath, this);
            }
        }

        // remove noton attachments
        List<AttachEntity> mAttachs = mRepository.getAllAttachsFromNoton(mScan);
        for(int i = 0;i < mAttachs.size();i++){

            filePath = mAttachs.get(i).getPath();
            final File attachFile = new File(filePath);
            if(attachFile.exists()) {

                //now let check if there is a noton with this attach file
                int count = mRepository.countAttachsByFilePath(filePath);
                // if there is one noton that has this attach so delete file
                if(count == 1) {
                    attachFile.delete();
                    Utils.removeImageFromGallery(filePath, this);
                }

            }
            mRepository.removeAttach(mAttachs.get(i));
        }

        mRepository.deleteScan(mScan);

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

    private void CreateDirectory(String path) {

        File dir = new File(path);
        if (!dir.exists()) {
            try {
                dir.mkdirs();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}