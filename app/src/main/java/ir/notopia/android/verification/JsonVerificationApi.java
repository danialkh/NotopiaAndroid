package ir.notopia.android.verification;

import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface JsonVerificationApi {

    @GET("ServerList.json")
    Call<UserStrings> FindServer();

    @POST("api/Mobile/")
    @Headers({ "Content-Type: application/json;charset=UTF-8"})
    Call<UserStrings> SignUpCode(
        @Body RequestBody number,
        @Header("Authorization") String token
    );


    @POST("api/Mobile/signUp")
    @Headers({ "Content-Type: application/json;charset=UTF-8"})
    Call<UserStrings> SignUpUser(
        @Body RequestBody body,
        @Header("Authorization") String token
    );

    @POST("api/Mobile/signIn")
    @Headers({ "Content-Type: application/json;charset=UTF-8"})
    Call<UserStrings> SignInUser(
        @Body RequestBody body,
        @Header("Authorization") String token
    );


    @POST("api/Mobile/verifyKhashdar")
    @Headers({ "Content-Type: application/json;charset=UTF-8"})
    Call<UserStrings> VerifyKhashdar(
            @Body RequestBody body,
            @Header("Authorization") String token
    );


    @Headers({ "Content-Type: application/json;charset=UTF-8"})
    @POST("api/Mobile/verifyMahsol")
    Call<UserStrings> VerifyMahsol(
            @Body RequestBody body,
            @Header("Authorization") String token
    );
}
