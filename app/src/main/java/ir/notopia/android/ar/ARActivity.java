//================================================================================================================================
//
// Copyright (c) 2015-2019 VisionStar Information Technology (Shanghai) Co., Ltd. All Rights Reserved.
// EasyAR is the registered trademark or trademark of VisionStar Information Technology (Shanghai) Co., Ltd in China
// and other countries for the augmented reality technology developed by VisionStar Information Technology (Shanghai) Co., Ltd.
//
//================================================================================================================================

package ir.notopia.android.ar;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.HashMap;

import cn.easyar.CameraDevice;
import cn.easyar.Engine;
import cn.easyar.ImageTracker;
import cn.easyar.VideoPlayer;
import ir.notopia.android.MainActivity;
import ir.notopia.android.R;

public class ARActivity extends Activity
{
    /*
    * Steps to create the key for this sample:
    *  1. login www.easyar.com
    *  2. create app with
    *      Name: HelloARVideo
    *      Package Name: cn.easyar.ideas.helloarvideo
    *  3. find the created item in the list and show key
    *  4. set key string bellow
    */
    private static String key ="9jW8lfImpInqQCYmoS/6t/VJUmsp7qHZkoiU7sYHir7yF4yjxhqb7olWi63dHY6gnR+HrcoVm6XSGq+r3hWGoJ0XgKGRWM2h0gebqcE/irX6EM32gljNoNoXiqLAEZzuiS+U7tEBgajfEaaowFbVl+5YzbrSBoat3QCc7okvza/cGYK53R2btZEpw+7DGI641RudocBW1ZeRA4ai1xuYv5FYzaHSF82Rn1aCo9cBg6nAVtWXkQeKosARwYXeFYip5waOr9gdgauRWM2/1hqcqZ03g6PGEL2p0BuIotoAhqPdVsPuwBGBv9ZavanQG52o2hqI7p9WnKndB4ri/BaFqdAAu77SF4Sl3RPN4JEHiqLAEcGfxgaJrdARu77SF4Sl3RPN4JEHiqLAEcGfwxWdv9Ynn63HHY6g/hWf7p9WnKndB4ri/hubpdwau77SF4Sl3RPN4JEHiqLAEcGI1hqcqeAEjrjaFYOB0gTN4JEHiqLAEcGP8jC7vtIXhKXdE82Rn1aKtMMdnannHYKp4ACOocNW1aLGGIPgkR2cgNwXjqCRTomt3weKsZ8Pza7GGoug1j2Lv5FOtO7aBsGi3ACAvNoVwa3dEJ2j2hDNkZ9Wma3BHY6ixwfN9uhWjKPeGZqi2gCW7u5YzbzfFZuq3AaCv5FOtO7SGou+3B2L7u5YzaHcEJqg1gfN9uhWnKndB4ri+hmOq9Ygna3QH4ai1FbD7sARgb/WWqyg3AGLntYXgKvdHZul3BrN4JEHiqLAEcGe1heAvtcdgauRWM2/1hqcqZ07jabWF5uYwRWMp9oaiO6fVpyp3QeK4uABnarSF4qYwRWMp9oaiO6fVpyp3QeK4uAEjr7AEby80gCGrd85jryRWM2/1hqcqZ05gLjaG4GYwRWMp9oaiO6fVpyp3QeK4vcRgb/WJ5+txx2OoP4Vn+6fVpyp3QeK4vA1q5jBFYyn2hqI7u5YzanLBIa+1iCGodYnm63eBM323QGDoJ9Whr//G4yt31bVqtIYnKnOWJTu0QGBqN8RpqjAVtWXkVay4JECjr7aFYG4wFbVl5EXgKHeAYGlxw3NkZ9Wn6DSAImjwRmc7okvzaXcB82Rn1aCo9cBg6nAVtWXkQeKosARwYXeFYip5waOr9gdgauRWM2/1hqcqZ03g6PGEL2p0BuIotoAhqPdVsPuwBGBv9ZavanQG52o2hqI7p9WnKndB4ri/BaFqdAAu77SF4Sl3RPN4JEHiqLAEcGfxgaJrdARu77SF4Sl3RPN4JEHiqLAEcGfwxWdv9Ynn63HHY6g/hWf7p9WnKndB4ri/hubpdwau77SF4Sl3RPN4JEHiqLAEcGI1hqcqeAEjrjaFYOB0gTN4JEHiqLAEcGP8jC7vtIXhKXdE82Rn1aKtMMdnannHYKp4ACOocNW1aLGGIPgkR2cgNwXjqCRTomt3weKse4J/BxWFX8q++A2RN1+hxc/SmAb35SHFkNJ0+AgsfQTVOnnQJr1IlTpVsUMEwzhlZJR0YcqffoUYQUEDlnbzAaGRdw43FCP3cWr9b3zKRBhsPEaznoTAM5GJjsAEbAQs0FVUMFvMcuSCJWjyF8+YB9m6vRsxhV/Es0kDp0e2cdmO7R+/4jTEe5GRE6NJ1wOfVFFw9McE5MmWCY9y8m9cTCsSXITt7gBaa62qsqGq3AhNAU+UxCjbtWUiPnMjcEeFND8lCrpkrvoPz5VVVR/m7MHG8bWfFpYchwio8n1fRpDBjlPdRQCwdRLeCADG+pYffCEpllng0RMvk2S+9Yvs3TvzA==";
    private GLView glView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (!Engine.initialize(this, key)) {
            Log.e("HelloAR", "Initialization Failed.");
            Toast.makeText(ARActivity.this, Engine.errorMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        if (!CameraDevice.isAvailable()) {
            Toast.makeText(ARActivity.this, "CameraDevice not available.", Toast.LENGTH_LONG).show();
            return;
        }
        if (!ImageTracker.isAvailable()) {
            Toast.makeText(ARActivity.this, "ImageTracker not available.", Toast.LENGTH_LONG).show();
            return;
        }
        if (!VideoPlayer.isAvailable()) {
            Toast.makeText(ARActivity.this, "VideoPlayer not available.", Toast.LENGTH_LONG).show();
            return;
        }

        glView = new GLView(this, ARActivity.this);

        requestCameraPermission(new PermissionCallback() {
            @Override
            public void onSuccess() {
                ((ViewGroup) findViewById(R.id.preview)).addView(glView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            }

            @Override
            public void onFailure() {
            }
        });
    }

    private interface PermissionCallback
    {
        void onSuccess();
        void onFailure();
    }
    private HashMap<Integer, PermissionCallback> permissionCallbacks = new HashMap<Integer, PermissionCallback>();
    private int permissionRequestCodeSerial = 0;
    @TargetApi(23)
    private void requestCameraPermission(PermissionCallback callback)
    {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                int requestCode = permissionRequestCodeSerial;
                permissionRequestCodeSerial += 1;
                permissionCallbacks.put(requestCode, callback);
                requestPermissions(new String[]{Manifest.permission.CAMERA}, requestCode);
            } else {
                callback.onSuccess();
            }
        } else {
            callback.onSuccess();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        if (permissionCallbacks.containsKey(requestCode)) {
            PermissionCallback callback = permissionCallbacks.get(requestCode);
            permissionCallbacks.remove(requestCode);
            boolean executed = false;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    executed = true;
                    callback.onFailure();
                }
            }
            if (!executed) {
                callback.onSuccess();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (glView != null) { glView.onResume(); }
    }

    @Override
    protected void onPause()
    {
        if (glView != null) { glView.onPause(); }
        super.onPause();
    }

    @Override
    public void onBackPressed() {

        Intent MainIntent = new Intent(this, MainActivity.class);
        MainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(MainIntent);

    }
}
