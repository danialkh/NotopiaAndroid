package ir.notopia.android.services;

import ir.notopia.android.database.entity.ImageAI;
import ir.notopia.android.database.entity.ImagePOJO;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AIWebService {
    String AI_BASE_URL = "http://ai.notopia.ir/";
    String FEED_AI_64 = "api/vision64";
    String FEED_AI = "api/vision";

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(AI_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    @POST(FEED_AI_64)
    Call<ImageAI> ai64(@Body ImagePOJO body);
}
