package ir.notopia.android.calender;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.dragselectrecyclerview.DragSelectRecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ir.mirrajabi.persiancalendar.PersianCalendarView;
import ir.mirrajabi.persiancalendar.core.PersianCalendarHandler;
import ir.mirrajabi.persiancalendar.core.interfaces.OnDayLongClickedListener;
import ir.mirrajabi.persiancalendar.core.interfaces.OnMonthChangedListener;
import ir.mirrajabi.persiancalendar.core.models.CalendarEvent;
import ir.mirrajabi.persiancalendar.core.models.PersianDate;
import ir.notopia.android.MainActivity;
import ir.notopia.android.R;
import ir.notopia.android.adapter.EventsAdaptor;
import ir.notopia.android.database.AppRepository;
import ir.notopia.android.database.entity.ScanEntity;
import ir.notopia.android.scanner.opennotescanner.FullScreenViewActivity;

public class CalendarActivity extends AppCompatActivity {

    private String TAG = "CalendarActivity";
    private AppRepository mRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);


        SharedPreferences login = CalendarActivity.this.getSharedPreferences("SignedIn_PR", Context.MODE_PRIVATE);
        String userNumber = login.getString("USER_NUMBER_PR", null);

        mRepository = AppRepository.getInstance(CalendarActivity.this);

        PersianCalendarView MainPersianCalendarView = findViewById(R.id.main_calendar);
        PersianCalendarHandler MainCalendar = MainPersianCalendarView.getCalendar();
        PersianDate today = MainCalendar.getToday();
        MainCalendar.setTodayBackground(R.drawable.vc_back_item_date_today);

        TextView Lbael_month_year = findViewById(R.id.Lbael_Main_month_year);
        ImageView IVNextMonth = findViewById(R.id.IVNextMonthFilterMainCalender);
        ImageView IVLastMonth = findViewById(R.id.IVLastMonthFilterMainCalender);
        TextView TVGoToday = findViewById(R.id.TVMainGoToday);

        MainActivity.TextViweFadeOutFadeIn(TVGoToday);

        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                MainPersianCalendarView.update();
                String strMY = MainCalendar.getMonthName(today) + "  " + today.getYear();
                Lbael_month_year.setText(strMY);
            }
        }, 50);



        List<CalendarEvent> events = MainCalendar.getAllEventsForDay(today);


        int thisMounth = today.getMonth();
        int thisYear = today.getYear();
        int thisDay = today.getDayOfMonth();
        while (events.size() < 7){

            if(thisDay > 30){
                thisDay = 1;
                thisMounth++;
            }

            if(thisMounth > 12){
                thisMounth = 1;
                thisYear++;
            }

            try {
                PersianDate date = new PersianDate(thisYear,thisMounth,thisDay);
                events.addAll(MainCalendar.getAllEventsForDay(date));
            }
            catch (Exception e){

            }

            thisDay++;

        }






        RecyclerView EventsRecycleView = (DragSelectRecyclerView) findViewById(R.id.recyclerviewEvents);
        EventsAdaptor eventsAdaptor = new EventsAdaptor(events,CalendarActivity.this);
        EventsRecycleView.setLayoutManager(new GridLayoutManager(this, 1));
        EventsRecycleView.setAdapter(eventsAdaptor);

        ImageView icon_back = findViewById(R.id.icon_back);
        icon_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MainActivity.imageViweFadeOutFadeIn(icon_back);

                Intent intentBack = new Intent(CalendarActivity.this, MainActivity.class);
                intentBack.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intentBack);
            }
        });





        MainCalendar.setOnDayLongClickedListener(new OnDayLongClickedListener() {
            @Override
            public void onLongClick(PersianDate date) {

                int index = 0;
                String year = String.valueOf(date.getYear());
                String month = String.valueOf(date.getMonth());
                String day = String.valueOf(date.getDayOfMonth());


                // generate noton identifier
                String str = year + month + day + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()) + userNumber;
                byte[] data = str.getBytes();
                String NotonIdentifier = Base64.encodeToString(data, Base64.DEFAULT);

                ScanEntity mScan = new ScanEntity(NotonIdentifier, "", "1", year, month, day, "", false, "", "", 0);
                mRepository.insertScan(mScan);

                Log.d(TAG, "newScan:" + mScan.toString());


                int finalIndex = index;
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {

                                List<ScanEntity> mScans = mRepository.getScans();
                                int index = mScans.size();

                                Intent i = new Intent(CalendarActivity.this, FullScreenViewActivity.class);
                                i.putExtra("position", index);
                                i.putExtra("scan_id", getLastAddedRowId());
                                i.putParcelableArrayListExtra("title_body", (ArrayList<? extends Parcelable>) mScans);
                                CalendarActivity.this.startActivity(i);
                            }
                        },
                        50);


            }

        });


        MainCalendar.setOnMonthChangedListener(new OnMonthChangedListener() {
            @Override
            public void onChanged(PersianDate date) {
                String strMY = MainCalendar.getMonthName(date) + "  " + date.getYear();
                Lbael_month_year.setText(strMY);
            }
        });

        TVGoToday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MainActivity.TextViweFadeOutFadeIn(TVGoToday);
                MainPersianCalendarView.update();
                String strMY = MainCalendar.getMonthName(today) + "  " + today.getYear();
                Lbael_month_year.setText(strMY);
            }
        });


        IVNextMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.imageViweFadeOutFadeIn(IVNextMonth);
                MainPersianCalendarView.goToNextMonth();
            }
        });

        IVLastMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.imageViweFadeOutFadeIn(IVLastMonth);
                MainPersianCalendarView.goToPreviousMonth();
            }
        });

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
}