package ir.notopia.android.services;

import ir.notopia.android.database.entity.MyNotification;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

public interface NotificationWebService {

    String BASE_URL = "https://notopia.ir/";
    String FEED_NOTIFI = "api/notification/last";


    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();


    @GET(FEED_NOTIFI)
    Call<MyNotification> notifications();


}
