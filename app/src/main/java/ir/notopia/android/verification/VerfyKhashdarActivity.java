package ir.notopia.android.verification;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

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
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class VerfyKhashdarActivity extends AppCompatActivity {

    JsonVerificationApi jsonVerificationApi;
    private boolean ServerReady = true;
    private TextView EnterNumberState,TVBtnVerfyMahsolCode;
    private LottieAnimationView animationView;
    private boolean FindedServer = false;
    private String version = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verfy_khashdar);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

        version = "";

        try {
            Context context = VerfyKhashdarActivity.this.getApplicationContext();
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        TVBtnVerfyMahsolCode = findViewById(R.id.TVBtnVerfyMahsolCode);
        CardView BTVerfyMahsolCode = findViewById(R.id.BTVerfyMahsolCode);

        FindServer findServer = new FindServer();
        getServerUrlAgain(findServer);

        BTVerfyMahsolCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(ServerReady) {

                    ServerReady = false;

                    EnterNumberState = findViewById(R.id.EnterKhashdarCodeState);
                    EnterNumberState.setText("در انتظار پاسخ سرور");
                    animationView = findViewById(R.id.lottieAnimationSpinerMahsol);
                    animationView.setVisibility(View.VISIBLE);
                    TVBtnVerfyMahsolCode.setVisibility(View.GONE);

                    Log.d("inja:","here1here1");

                    EditText ETMahsolCode = findViewById(R.id.ETMahsolCode);
                    String enteredCode = ETMahsolCode.getText().toString();

                    SharedPreferences login = VerfyKhashdarActivity.this.getSharedPreferences("SignedIn_PR", Context.MODE_PRIVATE);
                    String userNumber = login.getString("USER_NUMBER_PR", null);
                    String token = login.getString("USER_Bearer_token",null);

                    Log.d("inja:",token);
                    Log.d("inja:",userNumber);
                    Log.d("inja:",enteredCode);

                    if (userNumber != null) {



                        if(FindedServer) {
                            PostVerifyKhashdar(VerfyKhashdarActivity.this, userNumber, enteredCode,token);
                        }
                        else{
                            Toast.makeText(VerfyKhashdarActivity.this,"به دنبال سرور هستیم لطفا مجددا تلاش نمایید",Toast.LENGTH_SHORT).show();
                        }


                    } else {
                        Toast.makeText(VerfyKhashdarActivity.this, "لطفا ابتدا ثبت نام کنید", Toast.LENGTH_SHORT).show();
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

    private void PostVerifyKhashdar(final Context context,String userNumber, String code,String token) {

        JSONObject obj = new JSONObject();
        try {
            obj.put("version",version);
            obj.put("number", userNumber);
            obj.put("code", code);

            token = "Bearer " + token;
            Log.d("inja:",token);

            RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), (obj).toString());
            Call<UserStrings> call = jsonVerificationApi.VerifyKhashdar(body,token);

            Log.d("inja:","here1");


            call.enqueue(new Callback<UserStrings>() {
                @Override
                public void onResponse(@NotNull Call<UserStrings> call, @NotNull Response<UserStrings> response) {
                    if (!response.isSuccessful()) {
                        return;
                    }
                    Log.d("inja:","here2");

                    UserStrings posts = response.body();

                    assert posts != null;

                    if (posts.getState().equals("1")) {

                        Intent intentMahsol = new Intent(VerfyKhashdarActivity.this, VeryfiMahsolActivity.class);
                        intentMahsol.putExtra("EXTRA_MAHSOL_CODE", code);
                        startActivity(intentMahsol);
                    } else {
                        EnterNumberState.setText(posts.getStatus());
                        animationView.setVisibility(View.GONE);
                        TVBtnVerfyMahsolCode.setVisibility(View.VISIBLE);
                        ServerReady = true;
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
        ServerReady = true;
        TextView EnterNumberState = findViewById(R.id.EnterKhashdarCodeState);
        EnterNumberState.setText("کد محصول را وارد نماید");
    }
}
