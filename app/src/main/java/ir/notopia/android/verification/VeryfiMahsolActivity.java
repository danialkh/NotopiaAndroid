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
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class VeryfiMahsolActivity extends AppCompatActivity {

    JsonVerificationApi jsonVerificationApi;
    String MahsoldCode;
    private boolean FindedServer = false;

    private boolean onceBtnTab = true;

    private boolean canResend = true;
    private final Integer[] arrayTimes = {2,2,5,10,15,30};
    private int pointer = 0;
    private OtpTextView otpTextView;
    private String version = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_veryfi_mahsol);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        version = "";

        try {
            Context context = VeryfiMahsolActivity.this.getApplicationContext();
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        otpTextView = findViewById(R.id.verify_mahsol_opt);
        CardView BTVerfyMahsolCode = findViewById(R.id.BTVerfySmsMahsolCode);


        FindServer findServer = new FindServer();
        getServerUrlAgain(findServer);

        SharedPreferences login = VeryfiMahsolActivity.this.getSharedPreferences("SignedIn_PR", Context.MODE_PRIVATE);
        String userNumber = login.getString("USER_NUMBER_PR", null);
        String token = login.getString("USER_Bearer_token",null);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            MahsoldCode = extras.getString("EXTRA_MAHSOL_CODE");
        }


        TextView resendSms = findViewById(R.id.resendSmsForMahsol);
        resendSms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MainActivity.TextViweFadeOutFadeIn(resendSms);

                if(canResend && FindedServer){
                    if(arrayTimes.length > pointer) {
                        canResend = false;

                        PostResendSmsKhashdar(VeryfiMahsolActivity.this,userNumber,MahsoldCode,token);

                        new CountDownTimer(arrayTimes[pointer]*60*1000, 1000) {

                            public void onTick(long millisUntilFinished) {
                                resendSms.setText(String.valueOf(millisUntilFinished / 1000));
                                //here you can have your logic to set text to edittext
                            }

                            public void onFinish() {
                                resendSms.setText(VeryfiMahsolActivity.this.getString(R.string.resendSms));
                                canResend = true;
                            }

                        }.start();

                        pointer++;
                    }
                    else if(FindedServer){
                        Toast.makeText(VeryfiMahsolActivity.this,"به دنبال سرور هستیم لطفا مجددا تلاش نمایید",Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(VeryfiMahsolActivity.this,"نمیتوانید درخواست پیامک بیشتری بکنید",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        BTVerfyMahsolCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(onceBtnTab) {

                    onceBtnTab = false;

//                    EditText ETSmsCode = findViewById(R.id.ETMahsolCode);
//                    String smsCode = ETSmsCode.getText().toString();
                    String smsCode = otpTextView.getOTP();


                    if (userNumber != null && MahsoldCode != null) {

                        if(FindedServer) {
                            PostVerifyKhashdar(VeryfiMahsolActivity.this, userNumber, MahsoldCode, smsCode);
                        }
                        else{
                            Toast.makeText(VeryfiMahsolActivity.this,"به دنبال سرور هستیم لطفا مجددا تلاش نمایید",Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        onceBtnTab = true;
                        Toast.makeText(VeryfiMahsolActivity.this, "لطفا ابتدا ثبت نام کنید", Toast.LENGTH_SHORT).show();
                    }
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

    private void PostVerifyKhashdar(final Context context, String number, String MahsoldCode,String smsCode) {

        JSONObject obj = new JSONObject();
        try {
            obj.put("version",version);
            obj.put("number", number);
            obj.put("mahsolCode", MahsoldCode);
            obj.put("smsCode", smsCode);

            String key = "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6IjYzMDM5NDBlN2Y0YWNkMzJmODM0ZGZmOSIsInJvbGUiOiJ1c2VyIiwibnVtUGhvbmUiOiIwOTEyNTMxNDc4NSIsImlhdCI6MTY2MTIzOTU2OX0.mZZngkvfCZ7p30oeHI0R0dZXO3u9j6poL2Y2jSGC9tk";

            RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), (obj).toString());
            Call<UserStrings> call = jsonVerificationApi.VerifyMahsol(body, key);

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

                        SharedPreferences Login = VeryfiMahsolActivity.this.getSharedPreferences("Mahsol_PR", Context.MODE_PRIVATE);
                        Login.edit().putString("Mahsol_bool_PR", MahsoldCode).apply();


                        Toast.makeText(VeryfiMahsolActivity.this, posts.getStatus(), Toast.LENGTH_SHORT).show();
                        Intent intentShelf = new Intent(VeryfiMahsolActivity.this, MainActivity.class);
                        intentShelf.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intentShelf);
                    } else {
                        onceBtnTab = true;
                        Toast.makeText(VeryfiMahsolActivity.this, posts.getStatus(), Toast.LENGTH_SHORT).show();
                    }


                }

                @Override
                public void onFailure(@NotNull Call<UserStrings> call, @NotNull Throwable t) {

                }
            });
        }
        catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    private void PostResendSmsKhashdar(final Context context,String userNumber, String code,String token) {

        JSONObject obj = new JSONObject();
        try {
            obj.put("version",version);
            obj.put("number", userNumber);
            obj.put("code", code);

            token = "Bearer " + token;

            RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), (obj).toString());
            Call<UserStrings> call = jsonVerificationApi.VerifyKhashdar(body, token);

            call.enqueue(new Callback<UserStrings>() {
                @Override
                public void onResponse(@NotNull Call<UserStrings> call, @NotNull Response<UserStrings> response) {
                    if (!response.isSuccessful()) {
                        return;
                    }

                    UserStrings posts = response.body();

                    assert posts != null;

                    if (posts.getState().equals("1")) {
                        // code sended
                    } else {
                        Toast.makeText(VeryfiMahsolActivity.this, posts.getStatus(), Toast.LENGTH_SHORT).show();
                    }


                }

                @Override
                public void onFailure(@NotNull Call<UserStrings> call, @NotNull Throwable t) {

                }
            });
        }
        catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }

    @Override
    protected void onResume() {
        super.onResume();
        onceBtnTab = true;
    }
}