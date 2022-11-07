package ir.notopia.android;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import ir.mirrajabi.persiancalendar.PersianCalendarView;
import ir.mirrajabi.persiancalendar.core.PersianCalendarHandler;
import ir.mirrajabi.persiancalendar.core.interfaces.OnMonthChangedListener;
import ir.mirrajabi.persiancalendar.core.models.PersianDate;
import ir.notopia.android.database.AppRepository;
import ir.notopia.android.database.entity.CategoryEntity;

public class BottomSheetSliderAdaptor extends PagerAdapter {

    private LayoutInflater layoutInflater;
    private Context context;

    private View[] contentViews;


    public BottomSheetSliderAdaptor(Context context){
        this.context = context;
    }

    @Override
    public int getCount() {
        return 5;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }



    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        View view;
        layoutInflater = LayoutInflater.from(context);
        if(position == 1) {
            view = layoutInflater.inflate(R.layout.dastebandi_layout_mini_bottomsheet, container, false);

            AppRepository mRepository = AppRepository.getInstance(context);
            List<CategoryEntity> mCategorys = mRepository.getCategorys();

            for(int i = 1;i < 7;i++){
                String strId = "LabelDaste" + i;
                int id = context.getResources().getIdentifier(strId,"id",context.getPackageName());
                int identifier = 5 - (i - 1);
                TextView textView = view.findViewById(id);
                textView.setText(mCategorys.get(identifier).getName());
            }


        }
        else if(position == 2){
            view = layoutInflater.inflate(R.layout.hashtag_layout_mini_bottomsheet, container, false);
        }
        else if(position == 3){
            view = layoutInflater.inflate(R.layout.date_layout_mini_bottomsheet, container, false);


            TextView Lbael_month_year = view.findViewById(R.id.Lbael_month_year);
            ImageView IVLastMonth = view.findViewById(R.id.IVLastMonthFilterCalender);
            ImageView IVNextMonth = view.findViewById(R.id.IVNextMonthFilterCalender);
            TextView TVGoToday = view.findViewById(R.id.TVGoToday);

            PersianCalendarView persianCalendarView = view.findViewById(R.id.filter_calendar);
            PersianCalendarHandler calendar = persianCalendarView.getCalendar();
            PersianDate today = calendar.getToday();


            Typeface font_type = Typeface.createFromAsset(context.getAssets(), "fonts/B_Yekan.ttf");
            calendar.setTypeface(font_type);
            calendar.setTodayBackground(R.drawable.vc_back_item_date_today);
            calendar.setSelectedDayBackground(R.drawable.vc_back_current_day);


            // Lbael_month_year biar to mah va sale emroz
            String strMY = calendar.getMonthName(today) + "  " + today.getYear();
            Lbael_month_year.setText(strMY);
            Log.d("baressi",strMY);


            TVGoToday.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    MainActivity.TextViweFadeOutFadeIn(TVGoToday);
                    persianCalendarView.update();
                    String strMY = calendar.getMonthName(today) + "  " + today.getYear();
                    Lbael_month_year.setText(strMY);
                }
            });


            IVNextMonth.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.imageViweFadeOutFadeIn(IVNextMonth);
                    persianCalendarView.goToNextMonth();
                }
            });

            IVLastMonth.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.imageViweFadeOutFadeIn(IVLastMonth);
                    persianCalendarView.goToPreviousMonth();
                }
            });

            calendar.setOnMonthChangedListener(new OnMonthChangedListener() {
                @Override
                public void onChanged(PersianDate date) {
                    String strMY = calendar.getMonthName(date) + "  " + date.getYear();
                    Lbael_month_year.setText(strMY);
                }
            });

        }
        else{
            view = layoutInflater.inflate(R.layout.slide_item_container, container, false);

            SharedPreferences mahsol = context.getSharedPreferences("Mahsol_PR", Context.MODE_PRIVATE);
            String mahsolCode = mahsol.getString("Mahsol_bool_PR", "");

            if(!mahsolCode.equals("")) {
                ImageView item_viewpager6 = view.findViewById(R.id.item_viewpager6);
                TextView Label6 = view.findViewById(R.id.Label6);

                item_viewpager6.setVisibility(View.VISIBLE);
                Label6.setVisibility(View.VISIBLE);
            }
        }
        container.addView(view, 0);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }

    private void imageViweFadeOutFadeIn(ImageView tempImageView) {

        ObjectAnimator fadeOutAnimation = ObjectAnimator.ofFloat(tempImageView, "alpha", 1, 0);
        fadeOutAnimation.setDuration(150);
        final AnimatorSet mAnimationSet = new AnimatorSet();
        mAnimationSet.play(fadeOutAnimation);
        mAnimationSet.start();

        ObjectAnimator fadeInAnimation = ObjectAnimator.ofFloat(tempImageView, "alpha", 0, 1);
        fadeOutAnimation.setDuration(150);
        final AnimatorSet nAnimationSet = new AnimatorSet();
        nAnimationSet.play(fadeInAnimation);
        nAnimationSet.start();
    }

}


