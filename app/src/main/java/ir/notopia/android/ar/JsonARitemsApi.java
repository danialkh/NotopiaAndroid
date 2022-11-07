package ir.notopia.android.ar;

import ir.notopia.android.verification.UserStrings;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface JsonARitemsApi {

    @Headers({ "Content-Type: application/json;charset=UTF-8"})
    @POST("api/Mobile/verifyMahsol")
    Call<UserStrings> getJson(
            @Body RequestBody body,
            @Header("Authorization") String token
    );


}
