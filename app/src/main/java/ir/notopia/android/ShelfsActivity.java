package ir.notopia.android;

import androidx.appcompat.app.AppCompatActivity;
import ir.notopia.android.verification.VerfyKhashdarActivity;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

public class ShelfsActivity extends AppCompatActivity {

    ImageView icon_back,add_floating_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shelfs);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        add_floating_btn = findViewById(R.id.add_floating_btn);

        SharedPreferences mahsol = ShelfsActivity.this.getSharedPreferences("Mahsol_PR", Context.MODE_PRIVATE);
        String mahsolCode = mahsol.getString("Mahsol_bool_PR", "");

        View[] views = {add_floating_btn};
        startShowCase(ShelfsActivity.this,views,"ShowCaseShelf");

        if(!mahsolCode.equals("")){
            ImageView tempMahsol = findViewById(R.id.tempSarresid);
            tempMahsol.setVisibility(View.VISIBLE);
            tempMahsol.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    MainActivity.imageViweFadeOutFadeIn(tempMahsol);

                    Intent intentMain = new Intent(ShelfsActivity.this, MainActivity.class);
                    intentMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intentMain);
                }
            });


        }


        icon_back = findViewById(R.id.icon_back);
        icon_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.imageViweFadeOutFadeIn(icon_back);
                Intent intentBack = new Intent(ShelfsActivity.this, MainActivity.class);
                intentBack.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intentBack);
            }
        });



        add_floating_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MainActivity.imageViweFadeOutFadeIn(add_floating_btn);
                if(mahsolCode.equals("")){
                    Intent intentVerifyMahsol = new Intent(ShelfsActivity.this, VerfyKhashdarActivity.class);
                    startActivity(intentVerifyMahsol);
                }
                else{
                    Toast.makeText(ShelfsActivity.this,"کاربر گرامی در حال حاضر امکان ثبت بیش از یک محصول میسر نمی باشد",Toast.LENGTH_SHORT).show();
                }

            }
        });

    }

    static void startShowCase(Activity activity, View[] views, String SHOWCASE_ID) {

        // Views:
        // 0) AddMahsolToShelf

        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(500); // half second between each showcase view

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(activity, SHOWCASE_ID);
        sequence.setConfig(config);

        sequence.addSequenceItem(views[0],"نوتوپیا داری! تو برنامه اضافه اش کن\n" +
                "برای استفاده از از ویژگی های جذاب نوشت افزاری که داری باید اونو تو برنامه فعال کنی. برای این کار باید پوشش محافظ روی کد محصول را پاک کنی و کد زیرش را اینجا وارد کنی.\n" +
                " دقت کن که تمام حروف رو دقیقا به همون شکل خودش وارد کنی، ما هم یه پیامک تایید برات میفرستیم تا اون عدد توی پیامک را تو قسمت مربوطه اش وارد کنی. \n" +
                "دیگه تمومه. حالا نوشت افزاری که داری فعال شده و میتونی تو قفسه نوشت افزارهای نوتوپیا مدیریتش کنی.  ", "متوجه شدم");

        sequence.start();
    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}