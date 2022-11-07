package ir.notopia.android.verification;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import in.aabhasjindal.otptextview.OtpTextView;
import ir.notopia.android.MainActivity;
import ir.notopia.android.R;
import ir.notopia.android.ShelfsActivity;
import ir.notopia.android.findServer.FindServer;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class SignUpActivity extends AppCompatActivity {

    EditText ETSignUpName, ETSignUpFamily;
    CardView BTVerfyCode;
    private JsonVerificationApi jsonVerificationApi;
    private String imei;
    public static final int PERMISSION_READ_STATE = 58;

    private boolean FindedServer = false;

    private boolean onceBtnTab = true;
    private boolean canResend = true;
    private final Integer[] arrayTimes = {2, 2, 5, 10, 15, 30};
    private int pointer = 0;
    private OtpTextView otpTextView;

    private String version = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vf_signup);

        version = "";

        try {
            Context context = SignUpActivity.this.getApplicationContext();
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        imei = Settings.Secure.getString(
                SignUpActivity.this.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        String number;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            number = extras.getString("EXTRA_LOGIN_NUMBER");

            otpTextView = findViewById(R.id.SignUpOpt);
            ETSignUpName = findViewById(R.id.ETSignUpName);
            ETSignUpFamily = findViewById(R.id.ETSignUpFamily);
            BTVerfyCode = findViewById(R.id.BTVerfySignUpCode);
            TextView resendSms = findViewById(R.id.resendSmsForSignUp);

            FindServer findServer = new FindServer();
            getServerUrlAgain(findServer);

            resendSms.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    MainActivity.TextViweFadeOutFadeIn(resendSms);

                    if(canResend && FindedServer){
                        if(arrayTimes.length > pointer) {
                            canResend = false;

                            PostSendCodeVorod(SignUpActivity.this,number);

                            new CountDownTimer(arrayTimes[pointer]*60*1000, 1000) {

                                public void onTick(long millisUntilFinished) {
                                    resendSms.setText(String.valueOf(millisUntilFinished / 1000));
                                    //here you can have your logic to set text to edittext
                                }

                                public void onFinish() {
                                    resendSms.setText(SignUpActivity.this.getString(R.string.resendSms));
                                    canResend = true;
                                }

                            }.start();

                            pointer++;
                        }
                        else if(!FindedServer){
                            Toast.makeText(SignUpActivity.this,"به دنبال سرور هستیم لطفا مجددا تلاش نمایید",Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(SignUpActivity.this,"نمیتوانید درخواست پیامک بیشتری بکنید",Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });


            BTVerfyCode.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("HardwareIds")
                @Override
                public void onClick(View v) {

                    if(onceBtnTab) {

                        onceBtnTab = false;

                        String enteredCode = otpTextView.getOTP();
                        String enteredName = ETSignUpName.getText().toString();
                        String enteredFamily = ETSignUpFamily.getText().toString();

                        if (!enteredCode.equals("") && !enteredName.equals("") && !enteredFamily.equals("")) {



                            if(FindedServer){
                                PostSignUpUser(SignUpActivity.this, number,imei, enteredCode, enteredName, enteredFamily);
                            }
                            else{
                                Toast.makeText(SignUpActivity.this,"به دنبال سرور هستیم لطفا مجددا تلاش نمایید",Toast.LENGTH_SHORT).show();
                            }

                        }
                        else {
                            onceBtnTab = true;
                            Toast.makeText(SignUpActivity.this, "لطفا همه اطلاعات رو وارد نمایید", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
        }
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

    private void PostSignUpUser(final Context context,String number,String imei,String code,String name,String family) {

        JSONObject obj = new JSONObject();
        try {
            obj.put("version",version);
            obj.put("number", number);
            obj.put("imei", imei);
            obj.put("code", code);
            obj.put("name", name);
            obj.put("family", family);

            String key = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJudW1QaG9uZSI6IjA5MDM3ODkzMTI3Iiwicm9sZSI6ImFkbWluIiwiaWF0IjoxNjY2MzQ0NTk4fQ.OCGAQK3xG_ewM2hORVFMymb1CGatuJkBHVlFcoKM6Io";

            RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), (obj).toString());
            Call<UserStrings> call = jsonVerificationApi.SignUpUser(body,key);


            call.enqueue(new Callback<UserStrings>() {
                @Override
                public void onResponse(@NotNull Call<UserStrings> call, @NotNull Response<UserStrings> response) {
                    if (!response.isSuccessful()) {
                        return;
                    }

                    UserStrings posts = response.body();

                    //                String str = posts.getState() + "\n" + posts.getStatus();
                    //                Toast.makeText(SignUpActivity.this,str,Toast.LENGTH_SHORT).show();
                    assert posts != null;
                    if (posts.getState().equals("1")) {

                        //                    Toast.makeText(context,"Yeap",Toast.LENGTH_SHORT).show();
                        SharedPreferences Login = SignUpActivity.this.getSharedPreferences("SignedIn_PR", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = Login.edit();
                        editor.putString("USER_NUMBER_PR", number);
                        editor.putString("USER_NAME_PR", name);
                        editor.putString("USER_FAMILY_PR", family).apply();

                        String token = posts.getBearerToken();
                        editor.putString("USER_Bearer_token",token).apply();


                        Intent intent = new Intent(context, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    } else {
                        onceBtnTab = true;
                        Toast.makeText(SignUpActivity.this, posts.getStatus(), Toast.LENGTH_SHORT).show();
                    }


                }

                @Override
                public void onFailure(@NotNull Call<UserStrings> call, @NotNull Throwable t) {
                    Log.d("retrofitEror", "SignUp:" + t.getMessage());
                }
            });
        }
        catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void PostSendCodeVorod(final Context context, String number) {

        JSONObject obj = new JSONObject();
        try {
            obj.put("version",version);
            obj.put("number", number);

            String key = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJudW1QaG9uZSI6IjA5MDM3ODkzMTI3Iiwicm9sZSI6ImFkbWluIiwiaWF0IjoxNjY2MzQ0NTk4fQ.OCGAQK3xG_ewM2hORVFMymb1CGatuJkBHVlFcoKM6Io";

            RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), (obj).toString());
            Call<UserStrings> call = jsonVerificationApi.SignUpCode(body,key);

            call.enqueue(new Callback<UserStrings>() {
                @Override
                public void onResponse(Call<UserStrings> call, Response<UserStrings> response) {
                    if (!response.isSuccessful()) {
                        return;
                    }

                    UserStrings posts = response.body();

                    if (posts.getState().equals("1") || posts.getState().equals("3")) {
                        // code mojadadan ersal shod
                    } else {
                        Toast.makeText(SignUpActivity.this, posts.getStatus(), Toast.LENGTH_SHORT).show();
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
        onceBtnTab = true;
    }
}
