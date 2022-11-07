package ir.notopia.android.menu;

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

public interface JsonEditApi {

    @PUT("api/Mobile/editProfile")
    @Headers({ "Content-Type: application/json;charset=UTF-8"})
    Call<UserStrings> EditProfile(
            @Body RequestBody body,
            @Header("Authorization") String token
    );
}
