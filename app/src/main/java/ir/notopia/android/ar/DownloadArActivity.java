package ir.notopia.android.ar;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.widget.ContentLoadingProgressBar;
import ir.notopia.android.MainActivity;
import ir.notopia.android.R;
import ir.notopia.android.database.AppRepository;
import ir.notopia.android.database.entity.ArEntity;
import ir.notopia.android.findServer.FindServer;
import ir.notopia.android.verification.EnterNumberActivity;
import ir.notopia.android.verification.JsonVerificationApi;
import ir.notopia.android.verification.SignInActivity;
import ir.notopia.android.verification.SignUpActivity;
import ir.notopia.android.verification.UserStrings;
import ir.notopia.android.verification.VerfyKhashdarActivity;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DownloadArActivity extends AppCompatActivity {

    private static DownloadFileFromURL downloadFileFromURL;

    private static int STprogress;
    private static int STfileLength;
    private static int counterDownloaded;
    private static int tedadKolFiles;
    private String token;
    private String version;

    private boolean FindedServer = false;



    private String mahsolCode;
    private int lenghtOfFile;
    private String directory = "/Notopia/Ar/";
    String arFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + directory;

    private String pathtargets = arFolder + "Targets/";
    private String pathtracker = arFolder + "Trackers/";



    String global_Url = "";

    String[] temptargets;

    private ProgressDialog pDialog;


    public static final int progress_bar_type = 0;

    RequestQueue queue;


    ArrayList<String> id_target = new ArrayList<>();
    ArrayList<String> title = new ArrayList<>();
    ArrayList<String> target = new ArrayList<>();
    ArrayList<String> data = new ArrayList<>();
    ArrayList<String> description = new ArrayList<>();
    ArrayList<String> type = new ArrayList<>();
    ArrayList<String> lat = new ArrayList<>();
    ArrayList<String> lng = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_ar);

        version = "";

        try {
            Context context = DownloadArActivity.this.getApplicationContext();
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        SharedPreferences mahsol = DownloadArActivity.this.getSharedPreferences("Mahsol_PR", Context.MODE_PRIVATE);
        mahsolCode = mahsol.getString("Mahsol_bool_PR", "");

        SharedPreferences login = DownloadArActivity.this.getSharedPreferences("SignedIn_PR", Context.MODE_PRIVATE);
        String token = login.getString("USER_Bearer_token",null);
        this.token = "Bearer " + token;


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
            Log.d("ServetUrl:::",ServerUrl);
            FindedServer = true;
            global_Url = ServerUrl + "api/Mobile/AR/";
            if(downloadFileFromURL == null) {
                downloadFileFromURL = new DownloadFileFromURL(DownloadArActivity.this);
                DownloadAndSave();
                updateUI();
            }
        }
        Log.d("ServetUrl:::",ServerUrl);
    }

    private void DownloadAndSave() {

        if(FindedServer) {
            boolean check = Permissions();

            if (check) {
                removeDirectory("Targets");
                removeDirectory("Trackers");

                if (isInternetOn()) {
                    try {

                        CreateDirectory("Targets");
                        CreateDirectory("Trackers");

                        id_target.clear();
                        title.clear();
                        target.clear();
                        data.clear();
                        description.clear();
                        type.clear();
                        lat.clear();
                        lng.clear();

                        if (!mahsolCode.equals("")) {
//                        getCatalougOnline("JkVqzqRoiM");
                            getCatalougOnline(mahsolCode);
                        } else
                            Toast.makeText(DownloadArActivity.this, "لطقا یک محصول اضافه کنید", Toast.LENGTH_SHORT).show();


                    } catch (Exception ignored) {
                    }

                } else Toast.makeText(this, "اینترنت خود را چک کنید!", Toast.LENGTH_SHORT).show();


            }
        }
        else{
            Toast.makeText(DownloadArActivity.this,"به دنبال سرور هستیم لطفا مجددا تلاش نمایید",Toast.LENGTH_SHORT).show();
        }
    }

    private void removeDirectory(String folder) {
        File dir = new File(arFolder + folder);
        if (dir.isDirectory())
        {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++)
            {
                new File(dir, children[i]).delete();
            }
        }
    }


    private boolean Permissions() {
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        // If we don't have permissions, ask user for permissions
        if (permission != PackageManager.PERMISSION_GRANTED) {
            String[] PERMISSIONS_STORAGE = {
                    Manifest.permission.CAMERA,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            };
            // int REQUEST_EXTERNAL_STORAGE = 1;

            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, 1230);
            // Toast.makeText(this, "0", Toast.LENGTH_SHORT).show();

            return false;

        }
        else {
            return true;
        }


    }

    private void CreateDirectory(String folder) {

        File dir = new File(arFolder + folder + "/");
        if (!dir.exists()) {
            try {
                dir.mkdirs();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Log.d("createDirec",arFolder + folder + "/");
    }


    private void getCatalougOnline(String ss) {

        queue = Volley.newRequestQueue(this);
        String Url = global_Url + ss + "?version=" + version;

        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest(Request.Method.GET, Url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {


                try {

//                    Toast.makeText(DownloadArActivity.this, "پاسخ سرور", Toast.LENGTH_SHORT).show();

                    String success = response.getString("success");

                    JSONArray jsonArray = response.getJSONArray("result");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);

                        String id_cat = obj.getString("_id");
                        JSONArray ARItems = obj.getJSONArray("ARItems");

                        // delete all Ars to init again
                        AppRepository mRepository = AppRepository.getInstance(DownloadArActivity.this);
                        mRepository.deleteAllArs();

                        TextView arTedadKol = findViewById(R.id.TVArTedadKol);
                        tedadKolFiles = ARItems.length() * 2;
                        arTedadKol.setText(String.valueOf(tedadKolFiles));

                        for (int j = 0; j < ARItems.length(); j++) {
                            JSONObject object = ARItems.getJSONObject(j);

                            String tempId = object.getString("_id");
                            String tempType = object.getString("type");
                            String tempTarget = tempId + ".jpg";
                            String tempTracker = "";
                            if(tempType.equals("URL"))
                                tempTracker = tempId + ".txt";
                            else
                                tempTracker = tempId + ".mp4";

                            // insert into ar database
                            mRepository.insertAr(new ArEntity(tempId,tempType,tempTarget,tempTracker));

                            id_target.add(tempId);
                            title.add(object.getString("title"));
                            target.add(object.getString("target"));
                            data.add(object.getString("data"));
                            type.add(tempType);
                            lat.add(object.getString("lat"));
                            lng.add(object.getString("lng"));
                            description.add(object.getString("description"));
                        }
                    }

                    target.addAll(data);
                    temptargets = target.toArray(new String[target.size()]);

                    if(downloadFileFromURL.getStatus() == AsyncTask.Status.PENDING){
                        downloadFileFromURL.execute(temptargets);
                    }
                }
                catch (JSONException e) {

                    Toast.makeText(DownloadArActivity.this,"ارتباط با سرور در لحضه امکان پذیر نبود",Toast.LENGTH_SHORT).show();
                    Log.d("adam:", Objects.requireNonNull(e.getMessage()));
                    e.printStackTrace();



                }


            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(DownloadArActivity.this,"ارتباط با سرور در لحضه امکان پذیر نبود",Toast.LENGTH_SHORT).show();
            }
        }) {
            /**
             * Passing some request headers
             * */
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();

                Log.d("auto",ss);
                Log.d("auto",token);


                headers.put("Authorization",token);
                return headers;
            }

        };

        queue.add(jsonArrayRequest);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {

        DownloadAndSave();

    }

    class DownloadFileFromURL extends AsyncTask<String, Integer, String> {


        String temp_path = "";
        String suffix = "";
        int fake_id_1 = 0;
        int fake_id_2 = 0;
        String temp_type;
        boolean isallowed = true;
        WeakReference<Activity> mWeakActivity;
        OutputStream output;


        public DownloadFileFromURL(Activity activity) {
            mWeakActivity = new WeakReference<Activity>(activity);
        }

        /**
         * Before starting background thread Show Progress Bar Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        /**
         * Downloading file in background thread
         */
        @Override
        protected String doInBackground(String... f_url) {
            int count;
            try {
                for (int i = 0; i < f_url.length; i++) {

                    if(isCancelled())
                        break;

                    /////////////////////  first download in targets'folder  then download in tracker's folder

                    if (i >= f_url.length / 2) {

                        if(f_url[i].charAt(0) != 'h'){
                            f_url[i] = "https://" + f_url[i];
                        }

                        Log.d("temp_path:",temp_path);
                        Log.d("this_file_url:",f_url[i]);

                        temp_path = pathtracker;
//                        suffix = ".mp4";
                        fake_id_1 = fake_id_2++;

                        temp_type = type.get(fake_id_1);
                        if (temp_type.equals("URL"))
                            suffix = ".txt";
                        else suffix = ".mp4";

                    } else {
                        fake_id_1 = i;
                        temp_path = pathtargets;
                        suffix = ".jpg";
                        temp_type = type.get(fake_id_1);

                    }

                    URL url = new URL(f_url[i]);
                    URLConnection conection = url.openConnection();
                    conection.connect();
                    // getting file length
                    lenghtOfFile = conection.getContentLength();

                    // input stream to read file - with 8k buffer
                    InputStream input = new BufferedInputStream(
                            url.openStream(), 8192);



                    System.out.println("Data::" + f_url[i]);
                    // Output stream to write file


                    ///////////////////////////////////// path and filename for download
                    if (i >= f_url.length / 2) {
                        if (temp_type.equals("URL")) {
                            isallowed = false;

                            try {
                                File gpxfile = new File(temp_path, id_target.get(fake_id_1) + suffix);
                                FileWriter writer = new FileWriter(gpxfile);
                                writer.append(f_url[i]);
                                writer.flush();
                                writer.close();
                            } catch (Exception e) {
                            }
                        } else {
                            isallowed = true;
                            output = new FileOutputStream(temp_path + id_target.get(fake_id_1) + suffix);
                        }
                    } else {
                        isallowed = true;
                        output = new FileOutputStream(temp_path + temp_type + "-" + id_target.get(fake_id_1) + suffix);

                    }

                    if (isallowed) {

                        byte[] data = new byte[1024];

                        long total = 0;

                        while ((count = input.read(data)) != -1) {
                            total += count;
                            // publishing the progress....
                            // After this onProgressUpdate will be called
                            publishProgress((int) ((total * 100) / lenghtOfFile));

                            // writing data to file
                            output.write(data, 0, count);
                        }

                        // flushing output
                        output.flush();

                        // closing streams
                        output.close();
                        input.close();
                    }


                    counterDownloaded++;
                }
            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());

            }

            return null;
        }

        /**
         * Updating progress bar
         */
        protected void onProgressUpdate(Integer... progress) {
            // setting progress percentage
//            pDialog.setProgress(progress[0]);


            Log.d("onProgressUpdate:",String.valueOf(progress[0]));

            Log.d("doProgressId:",String.valueOf(R.id.progress_ar));

            STprogress = progress[0];
            STfileLength = lenghtOfFile;

        }

        /**
         * After completing background task Dismiss the progress dialog
         **/
        @Override
        protected void onPostExecute(String file_url) {

            //Toast.makeText(DownloadArActivity.this, "فرآیند دریافت تمام شد", Toast.LENGTH_SHORT).show();

            boolean isOkArFiles = new CheckArFiles(DownloadArActivity.this).isOkArFiles();
            if(isOkArFiles) {
                Intent intentAr = new Intent(DownloadArActivity.this, ARActivity.class);
                intentAr.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                startActivity(intentAr);
            }
            else{
                Intent intentMain = new Intent(DownloadArActivity.this, MainActivity.class);
                intentMain.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intentMain);
            }

        }

    }

    public boolean isInternetOn() {
        ConnectivityManager connec = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        if (connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTED ||
                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTED) {
            return true;

        } else if (connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.DISCONNECTED || connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.DISCONNECTED) {
            return false;
        }
        return false;
    }


    @Override
    protected void onStop() {
        super.onStop();
        Log.d("logActiv","onStop");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("logActiv","onStart");
        Log.d("StartProgressId:",String.valueOf(R.id.progress_ar));


        updateUI();
    }



    public void updateUI() {

        Activity activity = DownloadArActivity.this;

        TextView arTedadDownloaded = activity.findViewById(R.id.TVArTedadDownloaded);
        TextView TVPercentDownload = activity.findViewById(R.id.TVPercentDownload);
        TextView TVSizeDownload = activity.findViewById(R.id.TVSizeDownload);
        TextView TVSizeTypeDownload = activity.findViewById(R.id.TVSizeTypeDownload);
        ContentLoadingProgressBar ProgressIndicator = activity.findViewById(R.id.progress_ar);
        TextView arTedadKol = findViewById(R.id.TVArTedadKol);


        Log.d("updateUiDowload_TKOL",String.valueOf(tedadKolFiles));
        Log.d("updateUiDowload_cuFile",String.valueOf(counterDownloaded));
        Log.d("updateUiDowload_Prog",String.valueOf(STprogress));
        Log.d("updateUiDowload_FSize",String.valueOf(STfileLength));





        arTedadKol.setText(String.valueOf(tedadKolFiles));
        TVPercentDownload.setText(STprogress + "%");
        arTedadDownloaded.setText(String.valueOf(counterDownloaded));
        int size = ((STfileLength / 1024) / 1024);
        String type;
        if (size < 1) {
            size = (STfileLength / (1024));
            type = " KB";
        } else {
            type = " MB";
        }
        TVSizeDownload.setText(String.valueOf(size));
        TVSizeTypeDownload.setText(type);
        ProgressIndicator.setProgress(STprogress);

        if(FindedServer)
            if (downloadFileFromURL.getStatus() != AsyncTask.Status.FINISHED){
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                updateUI();
                            }
                        },
                        1);
            }

    }

}