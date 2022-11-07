package ir.notopia.android.menu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import ir.notopia.android.MainActivity;
import ir.notopia.android.R;
import ir.notopia.android.findServer.FindServer;
import ir.notopia.android.verification.UserStrings;
import ir.notopia.android.verification.VerfyKhashdarActivity;
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
import android.widget.ImageView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.List;

public class EditProfileActivity extends AppCompatActivity {

    private EditText ETEditName,ETEditFamily;
    private JsonEditApi jsonEditApi;

    private boolean FindedServer = false;
    private String version = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        version = "";

        try {
            Context context = EditProfileActivity.this.getApplicationContext();
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        SharedPreferences login = EditProfileActivity.this.getSharedPreferences("SignedIn_PR", Context.MODE_PRIVATE);
        String userNumber = login.getString("USER_NUMBER_PR", null);
        String userName = login.getString("USER_NAME_PR", null);
        String userFamily = login.getString("USER_FAMILY_PR", null);
        String token = login.getString("USER_Bearer_token",null);


        FindServer findServer = new FindServer();
        getServerUrlAgain(findServer);


        ETEditName = findViewById(R.id.ETEditName);
        ETEditFamily = findViewById(R.id.ETEditFamily);

        ETEditName.setText(userName);
        ETEditFamily.setText(userFamily);

        CardView bTEditProfile = findViewById(R.id.BTEditProfile);

        bTEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String name = ETEditName.getText().toString();
                String family = ETEditFamily.getText().toString();

                if(FindedServer) {
                    PostEditProfile(EditProfileActivity.this, userNumber, name, family, token);
                }
                else{
                    Toast.makeText(EditProfileActivity.this,"به دنبال سرور هستیم لطفا مجددا تلاش نمایید",Toast.LENGTH_SHORT).show();
                }
            }
        });



        ImageView icon_back = findViewById(R.id.icon_back);
        icon_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.imageViweFadeOutFadeIn(icon_back);
                Intent intentBack = new Intent(EditProfileActivity.this, MainActivity.class);
                intentBack.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intentBack);
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

            jsonEditApi = retrofit.create(JsonEditApi.class);
        }
        Log.d("ServetUrl:::",ServerUrl);
    }

    private void PostEditProfile(Context context, String userNumber, String name, String family,String token) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("version",version);
            obj.put("number", userNumber);
            obj.put("name", name);
            obj.put("family", family);

            Log.d("inja","here");

            token = "Bearer " + token;

            Log.d("edit",token);

            RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), (obj).toString());
            Call<UserStrings> call = jsonEditApi.EditProfile(body,token);

            Log.d("inja","here2");

            call.enqueue(new Callback<UserStrings>() {
                @Override
                public void onResponse(Call<UserStrings> call, Response<UserStrings> response) {
                    if (!response.isSuccessful()) {
                        return;
                    }

                    Log.d("inja","here3");

                    UserStrings posts = response.body();

                    if (posts.getState().equals("1")) {

                        SharedPreferences Login = EditProfileActivity.this.getSharedPreferences("SignedIn_PR", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = Login.edit();
                        editor.putString("USER_NUMBER_PR", userNumber);
                        editor.putString("USER_NAME_PR", name);
                        editor.putString("USER_FAMILY_PR", family).apply();

                        Intent intentMain = new Intent(context, MainActivity.class);
                        intentMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intentMain);
                    } else {
                        Toast.makeText(context, posts.getStatus(), Toast.LENGTH_SHORT).show();
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


}