package ir.notopia.android.notoLog;

import java.util.List;

import ir.notopia.android.verification.UserStrings;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;

public interface JsonLogApi {

    @POST("api/Mobile/log")
    @Headers({ "Content-Type: application/json;charset=UTF-8"})
    Call<UserStrings> NotopiaLog(
            @Body RequestBody body,
            @Header("Authorization") String token
    );


}