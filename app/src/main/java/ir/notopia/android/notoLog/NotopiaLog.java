package ir.notopia.android.notoLog;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.JsonArray;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import ir.notopia.android.findServer.FindServer;
import ir.notopia.android.verification.JsonVerificationApi;
import ir.notopia.android.verification.UserStrings;
import ir.notopia.android.verification.VeryfiMahsolActivity;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NotopiaLog {

    private static final String TAG = "NotopiaLog";
    private String mahsolCode;
    private String number;
    private String type;
    private String data;
    private String version;
    private String token;


    private boolean ServerReady = true;
    private JsonLogApi jsonLogApi;


    private boolean FindedServer = false;
    private String ServerUrl = "";

    public NotopiaLog(Context context,String version,String mahsolCode,String number, String type, String data) {
        this.version = version;
        this.number = number;
        this.type = type;
        this.data = data;
        this.mahsolCode = mahsolCode;

        SharedPreferences login = context.getSharedPreferences("SignedIn_PR", Context.MODE_PRIVATE);
        String token = login.getString("USER_Bearer_token",null);

        if(token != null){
            this.token = token;

        }

        FindServer findServer = new FindServer();
        getServerUrlAgain(findServer);
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
            this.ServerUrl = ServerUrl;
            SendToServer();
        }
        Log.d("ServetUrl:::",ServerUrl);
    }

    private void SendToServer() {

        if(FindedServer) {
            JsonArray json = new JsonArray();

            json.add(number);
            json.add(type);
            json.add(data);

            ServerReady = false;
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(ServerUrl)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            jsonLogApi = retrofit.create(JsonLogApi.class);
            PostSendLog();
        }
    }


    private void PostSendLog() {

        JSONObject obj = new JSONObject();
        try {
            obj.put("version",version);
            obj.put("number",number);
            obj.put("mahsolCode",mahsolCode);
            obj.put("type",type);
            obj.put("data",data);

            token = "Bearer " + token;

            RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"),(obj).toString());
            Call<UserStrings> call = jsonLogApi.NotopiaLog(body,token);


            Log.d(TAG,"logLog:" + " " + obj.toString());

            call.enqueue(new Callback<UserStrings>() {
                @Override
                public void onResponse(Call<UserStrings> call, Response<UserStrings> response) {
                    if (!response.isSuccessful()) {
                        return;
                    }

                    Log.d(TAG,"logLog:" + " " + mahsolCode + " " + number + " " + type + " " + data);

                    UserStrings posts = response.body();

                    Log.d(TAG,"logLog:" + posts.getState());
                    Log.d(TAG,"logLog:" + posts.getStatus());
                    ServerReady = true;

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
