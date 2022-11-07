package ir.notopia.android.verification;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import in.aabhasjindal.otptextview.OtpTextView;
import ir.notopia.android.MainActivity;
import ir.notopia.android.R;
import ir.notopia.android.findServer.FindServer;
import ir.notopia.android.menu.EditProfileActivity;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.gson.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class EnterNumberActivity extends AppCompatActivity {

    private JsonVerificationApi jsonVerificationApi;
    private LottieAnimationView animationView;
    private TextView EnterNumberState,TVBtnVerfyUser;
    private boolean ServerReady = true;
    private OtpTextView otpTextView;
    private boolean FindedServer = false;
    private String version = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vf_phone_number);

        version = "";

        try {
            Context context = EnterNumberActivity.this.getApplicationContext();
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        EditText ETVerifyCode = findViewById(R.id.ETVerficationCode);
        CardView BTVerifyUser = findViewById(R.id.BTVerfyUser);
        TVBtnVerfyUser = findViewById(R.id.TVBtnVerfyUser);

        TextView showTerms = findViewById(R.id.showTerms);
        CheckBox checkBoxTerms = findViewById(R.id.CheckBoxTerms);
        showTerms.setText(Html.fromHtml(getString(R.string.checkBoxMatn)));

        FindServer findServer = new FindServer();
        getServerUrlAgain(findServer);


        showTerms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MainActivity.TextViweFadeOutFadeIn(showTerms);

                Intent intent = new Intent(EnterNumberActivity.this, ReadTermsActivity.class);
                EnterNumberActivity.this.startActivity(intent);
            }
        });


        BTVerifyUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                boolean checkBoxTerm = checkBoxTerms.isChecked();

                if(checkBoxTerm) {

                    if (ServerReady) {

                        ServerReady = false;

                        EnterNumberState = findViewById(R.id.EnterNumberState);
                        EnterNumberState.setText("در انتظار پاسخ سرور");
                        animationView = findViewById(R.id.lottieAnimationSpiner);
                        animationView.setVisibility(View.VISIBLE);
                        TVBtnVerfyUser.setVisibility(View.GONE);

                        String PhoneNumber = ETVerifyCode.getText().toString();


                        if(FindedServer){
                            PostSendCodeVorod(EnterNumberActivity.this, PhoneNumber);
                        }
                        else{
                            Toast.makeText(EnterNumberActivity.this,"به دنبال سرور هستیم لطفا مجددا تلاش نمایید",Toast.LENGTH_SHORT).show();
                        }


                    }
                }
                else{
                    String matn = getResources().getString(R.string.errorTerms);
                    Toast.makeText(EnterNumberActivity.this, matn, Toast.LENGTH_SHORT).show();
                }



            }
        });
    }

    private void getServerUrlAgain(FindServer findServer) {
        String ServerUrl = findServer.getServerUrl();
        if(ServerUrl.equals("khali")) {
            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            getServerUrlAgain(findServer);
                        }
                    },
                    100);
        }
        else if(!ServerUrl.equals("Failed")){

            FindedServer = true;
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(ServerUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            jsonVerificationApi = retrofit.create(JsonVerificationApi.class);
        }
        Log.d("ServetUrl:::",ServerUrl);
    }

    private void PostSendCodeVorod(final Context context, String number) {


        JSONObject obj = new JSONObject();
        try {
            obj.put("version",version);
            obj.put("number",number);
            String key = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJudW1QaG9uZSI6IjA5MDM3ODkzMTI3Iiwicm9sZSI6ImFkbWluIiwiaWF0IjoxNjY2MzQ0NTk4fQ.OCGAQK3xG_ewM2hORVFMymb1CGatuJkBHVlFcoKM6Io";

            RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"),(obj).toString());
            Call<UserStrings> call = jsonVerificationApi.SignUpCode(body,key);

            Log.d("inja:",obj.toString());

            call.enqueue(new Callback<UserStrings>() {
                @Override
                public void onResponse(Call<UserStrings> call, Response<UserStrings> response) {
                    if (!response.isSuccessful()) {
                        return;
                    }

                    Log.d("inja:","slm");

                    Log.d("inja:",response.body().toString());

                    UserStrings posts = response.body();


                    animationView.setVisibility(View.GONE);
                    TVBtnVerfyUser.setVisibility(View.VISIBLE);

                    if (posts.getState().equals("1")) {
                        Intent intentEnterCode = new Intent(context, SignUpActivity.class);
                        intentEnterCode.putExtra("EXTRA_LOGIN_NUMBER", number);
                        context.startActivity(intentEnterCode);
                    }
                    else if (posts.getState().equals("3")) {
                        Intent intentEnterCode = new Intent(context, SignInActivity.class);
                        intentEnterCode.putExtra("EXTRA_LOGIN_NUMBER", number);
                        context.startActivity(intentEnterCode);
                    }
                    else {
                        EnterNumberState.setText(posts.getStatus());
                        ServerReady = true;
                    }
                }

                @Override
                public void onFailure(Call<UserStrings> call, Throwable t) {

                }
            });
        }
        catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        ServerReady = true;
        TextView EnterNumberState = findViewById(R.id.EnterNumberState);
        EnterNumberState.setText("شماره تلفن خود را وارد نمایید");
    }
}