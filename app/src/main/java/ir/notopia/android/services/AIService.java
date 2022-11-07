package ir.notopia.android.services;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import androidx.annotation.Nullable;
import ir.notopia.android.EditorActivity;
import ir.notopia.android.database.entity.ImageAI;
import ir.notopia.android.database.entity.ImagePOJO;
import ir.notopia.android.utils.Constants;
import retrofit2.Call;


public class AIService extends IntentService {
    public static final String TAG = "Notopia";
    private ImageAI imageAI;

    public AIService() {
        super("AIService");
    }

    public String base64Image(String imageUri) {
        Bitmap bm = BitmapFactory.decodeFile(imageUri);
//        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.test);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 60, baos);
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String fileUri = intent.getStringExtra("FILE_URI");
        Log.i(TAG, "addNote: " + fileUri);

        int firstDayOf98 = 2458563;
        AIWebService webService = AIWebService.retrofit.create(AIWebService.class);


//        JSONObject paramObject = new JSONObject();
        String base64Image = base64Image(fileUri);

//        try {
//            paramObject.put("image", base64Image);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        Log.i(TAG, "addNote: " + paramObject.toString());
//        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), paramObject.toString());

        Call<ImageAI> call = webService.ai64(new ImagePOJO(base64Image));
        try {
            imageAI = call.execute().body();
            if (imageAI != null) Log.i(TAG, "addNote: " + imageAI.toString());


        } catch (IOException e) {
            e.printStackTrace();
        }
        if (imageAI != null) {
            String image = imageAI.getImage();
            byte[] decodedString = Base64.decode(image, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            Log.i(TAG, "addNote: " + decodedByte);
            try {
                saveMedia(Uri.parse(fileUri), decodedByte);
            } catch (IOException e) {
                e.printStackTrace();
            }


            Intent intentEditor = new Intent(AIService.this, EditorActivity.class);
            if (!imageAI.getBarcode().equals("No")) {
                int day = Integer.parseInt(imageAI.getBarcode()) + firstDayOf98;
                intentEditor.putExtra(Constants.DAY_KEY_EXTRA, String.valueOf(day));
                Log.i(TAG, "onHandleIntent: " + day);
            }
            intentEditor.putExtra(Constants.NOTE_ID_EXTRA, -1);
//            intentEditor.putExtra(Constants.NOTE_TYPE_EXTRA, Constants.NOTE_TYPE_TEXT);
            intentEditor.putExtra(Constants.NOTE_TYPE_EXTRA, Constants.NOTE_TYPE_IMAGE);
            intentEditor.putExtra(Constants.NOTE_URI_EXTRA, fileUri);
            intentEditor.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intentEditor);
        }
    }

    private void saveMedia(Uri fileUri, Bitmap decodedByte) throws IOException {
        FileOutputStream out = null;
        out = new FileOutputStream(new File(fileUri.getPath()));
        decodedByte.compress(Bitmap.CompressFormat.PNG, 100, out);

    }
}