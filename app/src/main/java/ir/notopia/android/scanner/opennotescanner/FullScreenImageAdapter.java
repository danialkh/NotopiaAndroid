package ir.notopia.android.scanner.opennotescanner;

/*
 * based on code originally at http://www.androidhive.info/2013/09/android-fullscreen-image-slider-with-swipe-and-pinch-zoom-gestures/
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager.widget.PagerAdapter;

import com.afollestad.dragselectrecyclerview.DragSelectRecyclerView;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.ortiz.touchview.TouchImageView;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import ir.hamsaa.persiandatepicker.Listener;
import ir.hamsaa.persiandatepicker.PersianDatePickerDialog;
import ir.hamsaa.persiandatepicker.util.PersianCalendar;
import ir.notopia.android.HelpScanActivity;
import ir.notopia.android.MainActivity;
import ir.notopia.android.R;
import ir.notopia.android.adapter.LoadNotonTagsAdapter;
import ir.notopia.android.database.AppRepository;
import ir.notopia.android.database.entity.AttachEntity;
import ir.notopia.android.database.entity.CategoryEntity;
import ir.notopia.android.database.entity.ScanEntity;
import ir.notopia.android.database.entity.TagAssignedEntity;
import ir.notopia.android.database.entity.TagEntity;
import ir.notopia.android.noton.AttachActivity;
import ir.notopia.android.scanner.opennotescanner.helpers.Utils;

public class FullScreenImageAdapter extends PagerAdapter{

    private static final String TAG = "FullScreenImageAdapter";
    private Activity _activity;
    private ArrayList<String> _imagePaths;
    private int maxTexture;
    private ImageLoader mImageLoader;
    private ImageSize mTargetSize;
    private Executor executor = Executors.newSingleThreadExecutor();
    private List<ScanEntity> mScans;
    private AppRepository mRepository;
    private PersianCalendar initDate;
    private TextView docDate;
    private ImageView docRotate;
    private ImageView docSave;
    private ImageView docDelete;
    private ImageView picDelete;
    private ImageView iconAttach;
    private EditText notonText,notonTitle;
    private String date;
    private Spinner docCategory;
    private String mCategory;
    private String mDay;
    private String mMonth;
    private String mYear;
    private String mQrCode;
    private PersianDatePickerDialog picker;
    private float rotate = 0;

    //tags recycleView
    DragSelectRecyclerView assignedTagsRecycleView;

    // constructor
    public FullScreenImageAdapter(Activity activity,
                                  List<ScanEntity> mScans) {
        this._activity = activity;
        this.mScans = mScans;
        mRepository = AppRepository.getInstance(_activity);


    }


    private static double Log(double n, double base) {
        return Math.log(n) / Math.log(base);
    }

    @Override
    public int getCount() {
        return this.mScans.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        View layoutImageNoton;
        TouchImageView imgDisplay;
        rotate = 0;
        LayoutInflater inflater = (LayoutInflater) _activity
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewLayout = inflater.inflate(R.layout.layout_fullscreen_image, container,
                false);
        ScanEntity mScan = mScans.get(position);
        Log.d(TAG, "mScan: " + mScan.toString());
        mCategory = mScan.getCategory();
        mYear = mScan.getYear();
        mMonth = mScan.getMonth();
        mDay = mScan.getDay();
        date = mYear + "/" + mMonth + "/" + mDay;
        Log.d(TAG, "mScan: " + date);

        layoutImageNoton = viewLayout.findViewById(R.id.layoutImageNoton);
        imgDisplay =  viewLayout.findViewById(R.id.imgDisplay);
        docCategory = viewLayout.findViewById(R.id.doc_category);
        docDate = viewLayout.findViewById(R.id.doc_date_test);
        docRotate = viewLayout.findViewById(R.id.doc_rotate);
        docSave = viewLayout.findViewById(R.id.doc_save);
        docDelete = viewLayout.findViewById(R.id.doc_delete);
        picDelete = viewLayout.findViewById(R.id.IVBackDeletePic);
        iconAttach = viewLayout.findViewById(R.id.iconAttach);

        notonText = viewLayout.findViewById(R.id.notonText);
        notonTitle = viewLayout.findViewById(R.id.notonTitle);

        notonTitle.setText(mScan.getTitle());
        notonText.setText(mScan.getText());

        // load the tags that assing to this noton
        loadNotonTag(mScan,viewLayout);

        SharedPreferences helpScan = _activity.getSharedPreferences("helpScan", Context.MODE_PRIVATE);
        String helpScanState = helpScan.getString("helpScanState", "true");

        SharedPreferences mahsol = _activity.getSharedPreferences("Mahsol_PR", Context.MODE_PRIVATE);
        String mahsolCode = mahsol.getString("Mahsol_bool_PR", "");

        List<CategoryEntity> mCategorys = mRepository.getCategorys();
        String[] categories = {
                mCategorys.get(5).getName(),
                mCategorys.get(4).getName(),
                mCategorys.get(3).getName(),
                mCategorys.get(2).getName(),
                mCategorys.get(1).getName(),
                mCategorys.get(0).getName()
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(_activity,
                android.R.layout.simple_spinner_item, categories);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        docCategory.setAdapter(adapter);

        docCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
                // TODO Auto-generated method stub
                String selCat = docCategory.getItemAtPosition(arg2).toString();
                int index = -1;
                for (int i=0;i < categories.length;i++) {
                    if (categories[i].equals(selCat)) {
                        index = i;
                        break;
                    }
                }
                index++;
                mScan.setCategory(String.valueOf(index));
                mRepository.updateScan(mScan);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

        int index = Integer.parseInt(mCategory) - 1;

        docCategory.setSelection(index);
        docDate.setText(date);
        Log.d(TAG, "instantiateItem: position " + position + " scanId " + mScan.getId());
        initDate = new PersianCalendar();
        docDate.setOnClickListener(v -> {
            showCalendar(mScan, position, viewLayout);
        });


        String imagePath = mScan.getImage();
//        docRotate.setOnClickListener(v ->
//        {
//            rotate = rotate == 270 ? 0 : rotate + 90;
//            imgDisplay.setRotation(rotate);
//            imgDisplay.setScaleType(ImageView.ScaleType.FIT_CENTER);
//
//
//        });
//        docSave.setOnClickListener(v ->
//        {
//            rotateImageByUri(imagePath, rotate);
//
//
//        });

        LinearLayout addPicToNoton = viewLayout.findViewById(R.id.addPicToNoton);

        File file = new File(imagePath);
        if(!file.exists()){
            layoutImageNoton.setVisibility(View.GONE);
            addPicToNoton.setVisibility(View.VISIBLE);
        }
        else{

            addPicToNoton.setVisibility(View.GONE);
            layoutImageNoton.setVisibility(View.VISIBLE);
            mImageLoader.displayImage("file:///" + imagePath, imgDisplay, mTargetSize);

            Log.d("AddNotonActivity"," inja" + imagePath);
            Log.d("AddNotonActivity"," inja" + mTargetSize);
        }

        addPicToNoton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ImageView IMAddPic = viewLayout.findViewById(R.id.IMAddPic);
                TextView TVAddPic = viewLayout.findViewById(R.id.TVAddPic);

                MainActivity.imageViweFadeOutFadeIn(IMAddPic);
                MainActivity.imageViweFadeOutFadeIn(TVAddPic);


                if(!mahsolCode.equals("")) {

                    Intent intentAi;

                    if(helpScanState.equals("true")){
                        intentAi = new Intent(_activity, HelpScanActivity.class);
                    }
                    else{
                        intentAi = new Intent(_activity, OpenNoteScannerActivity.class);
                    }

                    Log.d("mScanId in AddNoton:",String.valueOf(mScan.getId()));
                    intentAi.putExtra("mScanId",String.valueOf(mScan.getId()));
                    _activity.startActivity(intentAi);
                    _activity.finish();

                }
                else{
                    Toast.makeText(_activity,"لطفا یک محصول اضافه کنید",Toast.LENGTH_SHORT).show();
                }
            }
        });




        notonText.addTextChangedListener(new TextWatcher() {



            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                EditText temp = viewLayout.findViewById(R.id.notonText);
                String text = temp.getText().toString();
                mScan.setText(text);
                mRepository.updateScan(mScan);


                Log.d("mahaaa:",text);
                Log.d("mahaaa Scan:",mScan.toString());

            }

            @Override
            public void afterTextChanged(Editable s) {


            }
        });

        notonTitle.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                EditText temp = viewLayout.findViewById(R.id.notonTitle);
                String text = temp.getText().toString();
                mScan.setTitle(text);
                mRepository.updateScan(mScan);
            }

            @Override
            public void afterTextChanged(Editable s) {


            }
        });


        // initialize tags from database
        boolean[] selectedLanguage;
        ArrayList<Integer> langList = new ArrayList<>();
        List<TagEntity> TagList = mRepository.getAllTags();
        List<TagAssignedEntity> TagAssignList = mRepository.getAllAssignedTagsFromNoton(mScan);
        String[] langArray = new String[TagList.size()];
        selectedLanguage = new boolean[langArray.length];


        for(int i = 0;i < TagAssignList.size();i++){
            int TagId = TagAssignList.get(i).getTagId();

            index = -1;
            for (int a = 0; a < TagList.size(); a++) {
                if (TagList.get(a).getTagId() == TagId) {
                    index = a;
                }
            }

            if(index != -1){
                langList.add(index);
                selectedLanguage[index] = true;
            }

        }

        for(int i = 0;i < TagList.size();i++){
            langArray[i] = TagList.get(i).getTagName();
        }


        TextView iconColor = viewLayout.findViewById(R.id.iconNotonColor);
        String[] items = {"پیش فرض","قرمز","سبز","آبی","صورتی"};
        int checkedItem = mScan.getColor();
        String colorName = items[checkedItem];
        iconColor.setText(colorName);

        iconColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(_activity);
                alertDialog.setTitle("انتخاب رنگ");

                alertDialog.setSingleChoiceItems(items, checkedItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // update database for change color of noton
                        mScan.setColor(which);
                        mRepository.updateScan(mScan);

                        // change the textView color name noton
                        String colorName = items[which];
                        iconColor.setText(colorName);

                    }
                });
                AlertDialog alert = alertDialog.create();
                alert.setCanceledOnTouchOutside(false);
                alert.show();


            }
        });



        iconAttach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.imageViweFadeOutFadeIn(iconAttach);
                Intent intentAttach = new Intent(_activity, AttachActivity.class);
                intentAttach.putExtra("notonId", String.valueOf(mScan.getId()));
                _activity.startActivity(intentAttach);
            }
        });



        ImageView iconTag = viewLayout.findViewById(R.id.iconTag);
            // initialize selected language array


        iconTag.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            // Initialize alert dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(_activity);

            // set title
            builder.setTitle("انتخاب برچسب");

            // set dialog non cancelable
            builder.setCancelable(false);

            builder.setMultiChoiceItems(langArray, selectedLanguage, new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                    // check condition
                    if (b) {
                        // when checkbox selected
                        // Add position  in lang list
                        langList.add(i);
                        // Sort array list
                        Collections.sort(langList);
                    } else {
                        // when checkbox unselected
                        // Remove position from langList

                        // find the index of value i for remove from list [because of unselected]
                        int index = -1;
                        for (int a = 0; a < langList.size(); a++) {
                            if (langList.get(a).equals(i)) {
                                index = a;
                            }
                        }

                        if(index != -1)
                            langList.remove(index);
                    }
                }
            });

            builder.setPositiveButton("ثبت", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // Initialize string builder
                    StringBuilder stringBuilder = new StringBuilder();
                    // use for loop


                    mRepository.removeAssignedTags(mScan);

                    for (int j = 0; j < langList.size(); j++) {
                        // concat array value

                        int currentTagId = TagList.get(langList.get(j)).getTagId();
                        TagAssignedEntity tagAssignedEntity = new TagAssignedEntity(currentTagId,mScan.getId());
                        mRepository.insertAssignedTag(tagAssignedEntity);

                        // check condition
                        if (j != langList.size() - 1) {
                            // When j value  not equal
                            // to lang list size - 1
                            // add comma
                            stringBuilder.append(", ");
                        }
                    }

                    final Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            loadNotonTag(mScan,viewLayout);
                        }
                    }, 150);


                    // set text on textView
                    //textView.setText(stringBuilder.toString());
                }
            });

            builder.setNegativeButton("لغو", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // dismiss dialog
                    dialogInterface.dismiss();
                }
            });
            // show dialog
            builder.show();
        }
    });

    picDelete.setOnClickListener(v -> {

        AlertDialog.Builder deleteConfirmBuilder;

        deleteConfirmBuilder = new AlertDialog.Builder(_activity,R.style.AlertDialogCustom);
        deleteConfirmBuilder.setTitle(_activity.getResources().getString(R.string.confirm_title));
        deleteConfirmBuilder.setMessage(_activity.getResources().getString(R.string.confirm_delete_pic));
        deleteConfirmBuilder.setPositiveButton(_activity.getResources().getString(R.string.answer_yes), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                deletePic(imagePath,addPicToNoton,layoutImageNoton,mScan);
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


    });


    docDelete.setOnClickListener(v -> {

        AlertDialog.Builder deleteConfirmBuilder;

        deleteConfirmBuilder = new AlertDialog.Builder(_activity,R.style.AlertDialogCustom);
        deleteConfirmBuilder.setTitle(_activity.getResources().getString(R.string.confirm_title_delete_noton));
        deleteConfirmBuilder.setMessage(_activity.getResources().getString(R.string.confirm_delete_noton));
        deleteConfirmBuilder.setPositiveButton(_activity.getResources().getString(R.string.answer_yes), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                deleteNoton(mScan);
                Intent intentGallery = new Intent(_activity, MainActivity.class);
                _activity.startActivity(intentGallery);
                _activity.finish();
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


    });

        container.addView(viewLayout);

        return viewLayout;
    }

    private void deletePic(String filePath,LinearLayout addPicToNoton,View layoutImageNoton,ScanEntity mScan) {

        final File photoFile = new File(filePath);
        if(photoFile.exists()) {
            photoFile.delete();
            Utils.removeImageFromGallery(filePath, _activity);
        }
        mScan.setImage("");
        mScan.setNeedEdit(false);
        mRepository.updateScan(mScan);

        layoutImageNoton.setVisibility(View.GONE);
        addPicToNoton.setVisibility(View.VISIBLE);
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
                Utils.removeImageFromGallery(filePath, _activity);
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
                    Utils.removeImageFromGallery(filePath, _activity);
                }

            }
            mRepository.removeAttach(mAttachs.get(i));
        }

        mRepository.deleteScan(mScan);
    }


    private void showCalendar(ScanEntity scan, int pos, View viewLayout) {
        Typeface typeface = Typeface.createFromAsset(_activity.getAssets(), "fonts/B_Yekan.ttf");

        initDate.setPersianDate(Integer.parseInt(scan.getYear()), Integer.parseInt(scan.getMonth()), Integer.parseInt(scan.getDay()));
        Log.d(TAG, "instantiateItem: position " + pos + " in date scanID: " + scan.getId());
        Log.d(TAG, "instantiateItem:" + scan.getId());


        Log.d(TAG,"manoMige:"+scan.toString());

        PersianCalendar persianCalendar = new PersianCalendar();
        int thisYear =  persianCalendar.getPersianYear();

        picker = new PersianDatePickerDialog(_activity)
                .setPositiveButtonString("باشه")
                .setNegativeButton("بیخیال")
                .setTodayButton("امروز")
                .setTodayButtonVisible(true)
                .setMinYear(thisYear - 5)
                .setMaxYear(thisYear + 5)
//                .setMaxYear(PersianDatePickerDialog.THIS_YEAR)
                .setInitDate(initDate)
                .setActionTextColor(Color.GRAY)
                .setTypeFace(typeface)
                .setTitleType(PersianDatePickerDialog.WEEKDAY_DAY_MONTH_YEAR)
                .setShowInBottomSheet(false)
                .setListener(new Listener() {
                    @Override
                    public void onDateSelected(PersianCalendar persianCalendar) {
                        Log.d(TAG, "instantiateItem: position " + pos + " in date scanID: " + scan.getId());
                        TextView myTvDate = viewLayout.findViewById(R.id.doc_date_test);
                        mDay = "" + persianCalendar.getPersianDay();
                        scan.setDay(mDay);
                        mMonth = "" + persianCalendar.getPersianMonth();
                        scan.setMonth(mMonth);
                        mYear = "" + persianCalendar.getPersianYear();
                        scan.setYear(mYear);
                        scan.setNeedEdit(false);
                        date = mYear + "/" + mMonth + "/" + mDay;
//                        Toast.makeText(_activity, date, Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "scan: " + scan);
                        mRepository.updateScan(scan);
                        myTvDate.setText(date);
                    }

                    @Override
                    public void onDismissed() {

                    }
                });


        picker.show();
    }

    public String getPath(int position) {
        return mScans.get(position).getImage();
//        return _imagePaths.get(position);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((RelativeLayout) object);
    }

    public void setMaxTexture(int maxTexture, ImageSize targetSize) {
        this.maxTexture = maxTexture;
        mTargetSize = targetSize;
    }

    public void setImageLoader(ImageLoader imageLoader) {
        mImageLoader = imageLoader;
    }



    private void rotateImageByUri(String filePath, float rotate) {
        Uri uri = Uri.parse("file:///" + filePath);
        try {
            Bitmap bitmap = null;
            bitmap = MediaStore.Images.Media.getBitmap(_activity.getContentResolver(), uri);
            Matrix matrix = new Matrix();
            matrix.postRotate(rotate);
            Bitmap bmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            //learn content provider for more info
            OutputStream os = _activity.getContentResolver().openOutputStream(uri);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, os);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private void loadNotonTag(ScanEntity mScan,View viewLayout){

        Log.d("loadNotonTag","loadNotonTag");

        LoadNotonTagsAdapter assignedAdaptor = new LoadNotonTagsAdapter(mScan,_activity);
        // new Utils(getApplicationContext()).getFilePaths(););
        assignedTagsRecycleView = (DragSelectRecyclerView) viewLayout.findViewById(R.id.notonTagsRecycleView);
        LinearLayoutManager horizontalLayoutManager
                = new LinearLayoutManager(_activity, LinearLayoutManager.HORIZONTAL, false);
        assignedTagsRecycleView.setLayoutManager(horizontalLayoutManager);
        assignedTagsRecycleView.setAdapter(assignedAdaptor);

    }

}
