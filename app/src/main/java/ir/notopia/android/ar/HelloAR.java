//================================================================================================================================
//
// Copyright (c) 2015-2019 VisionStar Information Technology (Shanghai) Co., Ltd. All Rights Reserved.
// EasyAR is the registered trademark or trademark of VisionStar Information Technology (Shanghai) Co., Ltd in China
// and other countries for the augmented reality technology developed by VisionStar Information Technology (Shanghai) Co., Ltd.
//
//================================================================================================================================

package ir.notopia.android.ar;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.opengl.GLES20;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import cn.easyar.Buffer;
import cn.easyar.CameraDevice;
import cn.easyar.CameraDeviceFocusMode;
import cn.easyar.CameraDevicePreference;
import cn.easyar.CameraDeviceSelector;
import cn.easyar.CameraDeviceType;
import cn.easyar.CameraParameters;
import cn.easyar.DelayedCallbackScheduler;
import cn.easyar.FeedbackFrameFork;
import cn.easyar.FrameFilterResult;
import cn.easyar.FunctorOfVoidFromTargetAndBool;
import cn.easyar.Image;
import cn.easyar.ImageTarget;
import cn.easyar.ImageTracker;
import cn.easyar.ImageTrackerResult;
import cn.easyar.InputFrame;
import cn.easyar.InputFrameFork;
import cn.easyar.InputFrameThrottler;
import cn.easyar.InputFrameToFeedbackFrameAdapter;
import cn.easyar.InputFrameToOutputFrameAdapter;
import cn.easyar.Matrix44F;
import cn.easyar.OutputFrame;
import cn.easyar.OutputFrameBuffer;
import cn.easyar.OutputFrameFork;
import cn.easyar.OutputFrameJoin;
import cn.easyar.Target;
import cn.easyar.TargetInstance;
import cn.easyar.TargetStatus;
import cn.easyar.Vec2F;
import cn.easyar.Vec2I;
import ir.notopia.android.utils.Constants;

import static android.content.Context.CONNECTIVITY_SERVICE;

public class HelloAR {
    private DelayedCallbackScheduler scheduler;
    private CameraDevice camera;
    private ArrayList<ImageTracker> trackers;
    private BGRenderer bgRenderer;
    private ArrayList<VideoRenderer> video_renderers;
    private VideoRenderer current_video_renderer;
    private int tracked_target = 0;
    private int active_target = 0;
    private ARVideo video = null;

    private InputFrameThrottler throttler;
    private FeedbackFrameFork feedbackFrameFork;
    private InputFrameToOutputFrameAdapter i2OAdapter;
    private InputFrameFork inputFrameFork;
    private OutputFrameJoin join;
    private OutputFrameBuffer oFrameBuffer;
    private InputFrameToFeedbackFrameAdapter i2FAdapter;
    private OutputFrameFork outputFrameFork;
    private int previousInputFrameIndex = -1;
    private byte[] imageBytes = null;


    private String directory = "/Notopia/Ar/";
    String arFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + directory;

    private String pathtargets = arFolder + "Targets/";
    private String pathtracker = arFolder + "Trackers/";
    Context context;
    Activity activity;
    String target_name;
    String id_of_tracker;
    String type, temp_tar_name, content_link, content_name;
    boolean flag_app = true;


    private ArrayList<String> FileNameTargets = new ArrayList<>();
    private ArrayList<String> id_split2 = new ArrayList<>();
    private ArrayList<String> sepratype = new ArrayList<>();
    private ArrayList<String> sepraidtarget = new ArrayList<>();



    private File[] listFile;


    String type_video = "VIDEO";
    String type_website = "URL";

    private StringBuilder temp_string;

    public HelloAR(Activity act) {
        scheduler = new DelayedCallbackScheduler();
        trackers = new ArrayList<ImageTracker>();
        activity = act;
        context = act;

        Catch_file_from_folder(new File(pathtargets));


    }

    private void loadFromImage(ImageTracker tracker, String path, String name) {//0 app  1 asset 2 absulot
        ImageTarget target = ImageTarget.createFromImageFile(path, 0, name, "", "", 1.0f);
        if (target == null) {
            Log.e("HelloAR", "target create failed or key is not correct");
            return;
        }
        tracker.loadTarget(target, scheduler, new FunctorOfVoidFromTargetAndBool() {
            @Override
            public void invoke(Target target, boolean status) {
                Log.i("HelloAR", String.format("load target (%b): %s (%d)", status, target.name(), target.runtimeID()));
            }
        });
    }

    public void recreate_context() {
        if (active_target != 0) {
            video.onLost();
            video.dispose();
            video = null;
            tracked_target = 0;
            active_target = 0;
        }
        if (bgRenderer != null) {
            bgRenderer.dispose();
            bgRenderer = null;
        }
        if (video_renderers != null) {
            for (VideoRenderer video_renderer : video_renderers) {
                video_renderer.dispose();
            }
            video_renderers = null;
        }
        current_video_renderer = null;
        previousInputFrameIndex = -1;
        bgRenderer = new BGRenderer();
        video_renderers = new ArrayList<VideoRenderer>();

        for (int k = 0; k < sepraidtarget.size(); k += 1) {
            VideoRenderer video_renderer = new VideoRenderer();
            video_renderers.add(video_renderer);
        }
    }

    public void initialize() {
        recreate_context();

        camera = CameraDeviceSelector.createCameraDevice(CameraDevicePreference.PreferObjectSensing);
        throttler = InputFrameThrottler.create();
        inputFrameFork = InputFrameFork.create(2);
        join = OutputFrameJoin.create(2);
        oFrameBuffer = OutputFrameBuffer.create();
        i2OAdapter = InputFrameToOutputFrameAdapter.create();
        i2FAdapter = InputFrameToFeedbackFrameAdapter.create();
        outputFrameFork = OutputFrameFork.create(2);

        boolean status = true;
        status &= camera.openWithPreferredType(CameraDeviceType.Back);
        camera.setSize(new Vec2I(1280, 960));
        camera.setFocusMode(CameraDeviceFocusMode.Continousauto);
        if (!status) {
            return;
        }
        ImageTracker tracker = ImageTracker.create();

        for (int i = 0; i < id_split2.size(); i++) {
//            loadFromImage(tracker, pathtargets + id_split2.get(i) + ".jpg", id_split2.get(i));
            loadFromImage(tracker, pathtargets + sepratype.get(i) + "-" + sepraidtarget.get(i) + ".jpg", sepraidtarget.get(i));

        }
        trackers.add(tracker);

        feedbackFrameFork = FeedbackFrameFork.create(trackers.size());

        camera.inputFrameSource().connect(throttler.input());
        throttler.output().connect(inputFrameFork.input());
        inputFrameFork.output(0).connect(i2OAdapter.input());
        i2OAdapter.output().connect(join.input(0));

        inputFrameFork.output(1).connect(i2FAdapter.input());
        i2FAdapter.output().connect(feedbackFrameFork.input());
        int k = 0;
        int trackerBufferRequirement = 0;
        for (ImageTracker _tracker : trackers) {
            feedbackFrameFork.output(k).connect(_tracker.feedbackFrameSink());
            _tracker.outputFrameSource().connect(join.input(k + 1));
            trackerBufferRequirement += _tracker.bufferRequirement();
            k++;
        }

        join.output().connect(outputFrameFork.input());
        outputFrameFork.output(0).connect(oFrameBuffer.input());
        outputFrameFork.output(1).connect(i2FAdapter.sideInput());
        oFrameBuffer.signalOutput().connect(throttler.signalInput());

        //CameraDevice and rendering each require an additional buffer
        camera.setBufferCapacity(throttler.bufferRequirement() + i2FAdapter.bufferRequirement() + oFrameBuffer.bufferRequirement() + trackerBufferRequirement + 2);
    }

    public void dispose() {
        if (video != null) {
            video.dispose();
            video = null;
        }
        tracked_target = 0;
        active_target = 0;

        for (ImageTracker tracker : trackers) {
            tracker.dispose();
        }
        trackers.clear();
        if (video_renderers != null) {
            for (VideoRenderer video_renderer : video_renderers) {
                video_renderer.dispose();
            }
            video_renderers = null;
        }
        current_video_renderer = null;
        if (bgRenderer != null) {
            bgRenderer = null;
        }
        if (camera != null) {
            camera.dispose();
            camera = null;
        }
        if (scheduler != null) {
            scheduler.dispose();
            scheduler = null;
        }
    }

    public boolean start() {
        boolean status = true;
        if (camera != null) {
            status &= camera.start();
        } else {
            status = false;
        }
        for (ImageTracker tracker : trackers) {
            status &= tracker.start();
        }
        return status;
    }

    public void stop() {
        if (camera != null) {
            camera.stop();
        }
        for (ImageTracker tracker : trackers) {
            tracker.stop();
        }
    }

    public void render(int width, int height, int screenRotation) {
        while (scheduler.runOne()) {
        }

        GLES20.glViewport(0, 0, width, height);
        GLES20.glClearColor(0.f, 0.f, 0.f, 1.f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        OutputFrame oframe = oFrameBuffer.peek();
        if (oframe == null) {
            return;
        }
        InputFrame iframe = oframe.inputFrame();
        if (iframe == null) {
            oframe.dispose();
            return;
        }
        CameraParameters cameraParameters = iframe.cameraParameters();
        if (cameraParameters == null) {
            oframe.dispose();
            iframe.dispose();
            return;
        }
        float viewport_aspect_ratio = (float) width / (float) height;
        Matrix44F imageProjection = cameraParameters.imageProjection(viewport_aspect_ratio, screenRotation, true, false);
        Image image = iframe.image();

        try {
            if (iframe.index() != previousInputFrameIndex) {
                Buffer buffer = image.buffer();
                try {
                    if ((imageBytes == null) || (imageBytes.length != buffer.size())) {
                        imageBytes = new byte[buffer.size()];
                    }
                    buffer.copyToByteArray(imageBytes);
                    bgRenderer.upload(image.format(), image.width(), image.height(), ByteBuffer.wrap(imageBytes));
                } finally {
                    buffer.dispose();
                }
                previousInputFrameIndex = iframe.index();
            }
            bgRenderer.render(imageProjection);

            Matrix44F projectionMatrix = cameraParameters.projection(0.01f, 1000.f, viewport_aspect_ratio, screenRotation, true, false);
            for (FrameFilterResult oResult : oframe.results()) {
                if (oResult instanceof ImageTrackerResult) {
                    ImageTrackerResult result = (ImageTrackerResult) oResult;
                    ArrayList<TargetInstance> targetInstances = result.targetInstances();
                    for (TargetInstance targetInstance : targetInstances) {
                        if (targetInstance.status() == TargetStatus.Tracked) {
                            Target target = targetInstance.target();
                            int id = target.runtimeID();
                            if (active_target != 0 && active_target != id) {
//                                video = new ARVideo();
                                video.onLost();
                                video.dispose();
                                video = null;
                                tracked_target = 0;
                                active_target = 0;
                            }
                            if (tracked_target == 0) {
                                if (video == null && video_renderers.size() > 0) {
                                    target_name = target.name();

                                    if (HasTargetName(target_name)) {

                                        if (type.equals(type_video) && video_renderers.get(0).texId() != 0) {

                                            File file = new File(pathtracker + id_of_tracker + ".mp4");

                                            if (!file.exists()) {
                                                activity.runOnUiThread(new Runnable() {
                                                    public void run() {
                                                    }
                                                });
                                                video = new ARVideo();
                                            } else PlayVideo(id_of_tracker + ".mp4", 0);


                                        } else if (type.equals(type_website) && video_renderers.get(1).texId() != 0) {


                                            activity.runOnUiThread(new Runnable() {
                                                public void run() {
                                                    if (isInternetOn()) {
                                                        File file = new File(pathtracker, id_of_tracker + ".txt");
                                                        StringBuilder text = new StringBuilder();
                                                        try {
                                                            BufferedReader br = new BufferedReader(new FileReader(file));
                                                            String line;

                                                            while ((line = br.readLine()) != null) {
                                                                text.append(line);
                                                            }
                                                            br.close();
                                                            temp_string = text;
                                                        }
                                                        catch (IOException e) {
                                                            //You'll need to add proper error handling here
                                                        }
                                                        //Todo Open Url
                                                        Intent productsIntent = new Intent(context, WebViewActivity.class);
                                                        productsIntent.putExtra(Constants.WEB_VIEW_URL, temp_string.toString());
                                                        productsIntent.putExtra("Ordertype", "ar");
                                                        productsIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                                                        context.startActivity(productsIntent);
                                                    }
                                                    else
                                                        Toast.makeText(activity, "ارتباط با اینترنت متصل نیست.", Toast.LENGTH_SHORT).show();

                                                }
                                            });
                                            video = new ARVideo();

                                        }

                                    }

                                }

                                if (video != null) {

                                    video.onFound();
                                    tracked_target = id;
                                    active_target = id;
                                }
                            }
                            ImageTarget imagetarget = target instanceof ImageTarget ? (ImageTarget) (target) : null;
                            if (imagetarget != null) {
                                Vec2F scale = new Vec2F(imagetarget.scale(), imagetarget.scale() / imagetarget.aspectRatio());
                                if (current_video_renderer != null) {
                                    video.update();
                                    if (video.isRenderTextureAvailable()) {
                                        current_video_renderer.render(projectionMatrix, targetInstance.pose(), scale);
                                    }
                                }
                            }
                            target.dispose();
                        }
                        targetInstance.dispose();
                    }
                    if (targetInstances.size() == 0) {
                        if (tracked_target != 0) {
                            video.onLost();
                            tracked_target = 0;
                            if (type.equals(type_video)) {
                                active_target = 105;
                            }
                            if (type.equals(type_website)) {
                                active_target = 112;
                            }
                            RefreshStrings();

                        }
                    }
                }
                if (oResult != null) {
                    oResult.dispose();
                }
            }
        } finally {
            iframe.dispose();
            oframe.dispose();
            if (cameraParameters != null) {
                cameraParameters.dispose();
            }
            image.dispose();
        }
    }

    private void RefreshStrings() {
        id_of_tracker = "";
        temp_tar_name = "";
        type = "";
        content_link = "";
        content_name = "";
    }

    private boolean HasTargetName(String target_name) {
        boolean result = false;

        for (int i = 0; i < sepraidtarget.size(); i++) {
            if (target_name.equals(sepraidtarget.get(i))) {

                id_of_tracker = sepraidtarget.get(i);
                type = sepratype.get(i);


                result = true;
            }
        }

        return result;

    }


    private void PlayVideo(String s, int i) {
//
        video = new ARVideo();
        video.openVideoFile(pathtracker + s, video_renderers.get(i).texId(), scheduler);
        current_video_renderer = video_renderers.get(i);

    }

    private void Catch_file_from_folder(File targetfile) {
        id_split2.clear();
        FileNameTargets.clear();

        listFile = targetfile.listFiles();

        for (File value : listFile) {
            FileNameTargets.add(value.getName());

        }
        for (int i = 0; i < FileNameTargets.size(); i++) {
            String[] separated = FileNameTargets.get(i).split(".jpg");
            String[] sepratetemp = separated[0].split("-", 2);
            if (sepratetemp[0] != "")
                sepratype.add(sepratetemp[0]);
            if (sepratetemp[1] != "")
                sepraidtarget.add(sepratetemp[1]);


            id_split2.add(separated[0]);
        }

    }

    public boolean isInternetOn() {
        ConnectivityManager connec = (ConnectivityManager) activity.getSystemService(CONNECTIVITY_SERVICE);
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
}
