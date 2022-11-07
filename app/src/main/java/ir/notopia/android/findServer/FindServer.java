package ir.notopia.android.findServer;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import ir.notopia.android.verification.JsonVerificationApi;
import ir.notopia.android.verification.SignInActivity;
import ir.notopia.android.verification.UserStrings;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class FindServer {

    private JsonVerificationApi jsonVerificationApi;
    private  String ServerUrl = "khali";
    Call<UserStrings> call;

    public FindServer() {

        String global_Url = "https://notopia.ir/notopiaServerList/";
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(global_Url)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        jsonVerificationApi = retrofit.create(JsonVerificationApi.class);

        Log.d("ServerList","here1");
        PostSendCodeVorod();


    }



    private void PostSendCodeVorod() {

        Log.d("ServerList","here2");


        call = jsonVerificationApi.FindServer();

        call.enqueue(new Callback<UserStrings>() {
            @Override
            public void onResponse(Call<UserStrings> call, Response<UserStrings> response) {
                if (!response.isSuccessful()) {
                    return;
                }

                UserStrings posts = response.body();
                Log.d("ServerList",posts.toString());

                Log.d("ServerList",posts.getState());
                Log.d("ServerList",posts.getStatus());


                if (posts.getState().equals("1")) {
                    Log.d("ServerList",posts.getStatus());
                    ServerUrl = posts.getStatus();

                }


                if (posts.getState().equals("0")) {
                    Log.d("ServerList",posts.getStatus());
                    ServerUrl = "Failed";

                }


            }

            @Override
            public void onFailure(Call<UserStrings> call, Throwable t) {

                ServerUrl = "Failed";
                Log.d("ServerList","Failed");
            }
        });
    }

    public Call<UserStrings> getCall() {
        return call;
    }

    public String getServerUrl() {
        return ServerUrl;
    }
}
