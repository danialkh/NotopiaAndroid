package ir.notopia.android.scanner.opennotescanner;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.shapes.PathShape;
import android.hardware.Camera;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

import ir.hamsaa.persiandatepicker.util.PersianCalendar;
import ir.notopia.android.R;
import ir.notopia.android.scanner.opennotescanner.helpers.OpenNoteMessage;
import ir.notopia.android.scanner.opennotescanner.helpers.PreviewFrame;
import ir.notopia.android.scanner.opennotescanner.helpers.Quadrilateral;
import ir.notopia.android.scanner.opennotescanner.helpers.ScannedDocument;
import ir.notopia.android.scanner.opennotescanner.helpers.Utils;
import ir.notopia.android.scanner.opennotescanner.views.HUDCanvasView;

import static java.lang.Math.abs;

/**
 * Created by allgood on 05/03/16.
 */
public class ImageProcessor extends Handler {

    private static final String TAG = "ImageProcessor";
    private final Handler mUiHandler;
    private final OpenNoteScannerActivity mMainActivity;
    private boolean mBugRotate;
    private boolean colorMode = false;
    private boolean filterMode = true;
    private double colorGain = 1.5;       // contrast
    private double colorBias = 0;         // bright
    private int colorThresh = 110;        // threshold
    private Size mPreviewSize;
    private Point[] mPreviewPoints;
    private ResultPoint[] qrResultPoints;
    private HashMap<String, Long> pageHistory = new HashMap<>();
    private QRCodeMultiReader qrCodeMultiReader = new QRCodeMultiReader();
    private SharedPreferences qrPref;
    private SharedPreferences.Editor editor;
    private Toast myToast = null;
    private boolean onceAutoTake = true;
    private int stableCounter = 0;
    private boolean onceNuller = true;
    private String doHamedState;

    private Camera mCamera;

    public ImageProcessor(Looper looper, Handler uiHandler, OpenNoteScannerActivity mainActivity) {
        super(looper);
        mUiHandler = uiHandler;
        mMainActivity = mainActivity;

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mainActivity);
        mBugRotate = sharedPref.getBoolean("bug_rotate", false);
        qrPref = mMainActivity.getApplicationContext().getSharedPreferences("QrPref", Context.MODE_PRIVATE); // 0 - for private mode
        editor = qrPref.edit();

    }

    public void handleMessage(Message msg) {


        SharedPreferences doHamed = mMainActivity.getSharedPreferences("doHamed", Context.MODE_PRIVATE);
        doHamedState = doHamed.getString("doHamedState", "0");
        LinearLayout LLContentDoHamed = mMainActivity.findViewById(R.id.LLContentDoHamed);
        Log.d("calcS doHamedState:", doHamedState);

        mMainActivity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if(doHamedState.equals("1")){
                    LLContentDoHamed.setVisibility(View.VISIBLE);
                }
                else{
                    LLContentDoHamed.setVisibility(View.GONE);
                }
            }
        });

        if (msg.obj.getClass() == OpenNoteMessage.class) {

            OpenNoteMessage obj = (OpenNoteMessage) msg.obj;

            String command = obj.getCommand();

            Log.d(TAG, "Message Received: " + command + " - " + obj.getObj().toString());

            if (command.equals("previewFrame")) {
                processPreviewFrame((PreviewFrame) obj.getObj());
            } else if (command.equals("pictureTaken")) {
                processPicture((Mat) obj.getObj());
            } else if (command.equals("colorMode")) {
                colorMode = (Boolean) obj.getObj();
            } else if (command.equals("filterMode")) {
                filterMode = (Boolean) obj.getObj();
            }
        }
    }

    private void processPreviewFrame(PreviewFrame previewFrame) {

        Result[] results = {};

        Mat frame = previewFrame.getFrame();

        try {
            results = zxing(frame);
        } catch (ChecksumException | FormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        boolean qrOk = false;
        String currentQR = null;

        if(onceNuller){
            editor.putString("QR_CODE", "empty");
            onceNuller = false;
        }


        for (Result result : results) {
            String qrText = result.getText();
            if (Utils.isMatch(qrText, "^P.. V.. S[0-9]+") && checkQR(qrText)) {
                Log.d(TAG, "QR Code valid: " + result.getText());
                qrOk = true;
                currentQR = qrText;
                qrResultPoints = result.getResultPoints();
                break;
            } else {
                Log.d(TAG, "QR Code ignored: " + result.getText());
            }
            if (qrText != null && (qrText.length() == 25 || qrText.length() == 14)) {
                editor.putString("QR_CODE", qrText);
//                Log.d(TAG, "QR Code out: " + qrCode);

            }
        }
        editor.commit();

        boolean autoMode = previewFrame.isAutoMode();
        boolean previewOnly = previewFrame.isPreviewOnly();

        if (detectPreviewDocument(frame) && ((!autoMode && !previewOnly) || (autoMode && qrOk))) {

            mMainActivity.waitSpinnerVisible();

            mMainActivity.requestPicture();

            if (qrOk) {
                pageHistory.put(currentQR, new Date().getTime() / 1000);
                Log.d(TAG, "QR Code scanned: " + currentQR);
            }
        }

        frame.release();
        mMainActivity.setImageProcessorBusy(false);

    }

    public void processPicture(Mat picture) {

        Mat img = Imgcodecs.imdecode(picture, Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
        picture.release();

        Log.d(TAG, "processPicture - imported image " + img.size().width + "x" + img.size().height);

        if (mBugRotate) {
            Core.flip(img, img, 1);
            Core.flip(img, img, 0);
        }

        ScannedDocument doc = detectDocument(img);


        // inja nemigire axo age kharab bashe
        if(doc.previewSize != null) {

            Log.d("moheme:", doc.previewSize.toString());
            Log.d("moheme:", doc.previewSize.toString());
            mMainActivity.saveDocument(doc);
            mMainActivity.shootSound();
        }
        else{
            Log.d("moheme:", "nullle");
            mMainActivity.refreshCamera();
        }

        doc.release();
        picture.release();

        mMainActivity.setImageProcessorBusy(false);
        mMainActivity.setAttemptToFocus(false);
        mMainActivity.waitSpinnerInvisible();

    }

    private ScannedDocument detectDocument(Mat inputRgba) {
        ArrayList<MatOfPoint> contours = findContours(inputRgba);

        ScannedDocument sd = new ScannedDocument(inputRgba);

        Quadrilateral quad = getQuadrilateral(contours, inputRgba.size());

        Mat doc;
        Mat filterDoc;

        if (quad != null) {

            MatOfPoint c = quad.contour;

            sd.quadrilateral = quad;
            sd.previewPoints = mPreviewPoints;
            sd.previewSize = mPreviewSize;

            doc = fourPointTransform(inputRgba, quad.points);
            filterDoc = fourPointTransform(inputRgba, quad.points);

        } else {
            doc = new Mat(inputRgba.size(), CvType.CV_8UC4);
            filterDoc =  new Mat(inputRgba.size(), CvType.CV_8UC4);
            inputRgba.copyTo(doc);
        }

        enhanceDocument(doc);
        sd.setCropped(doc);

//        Imgproc.adaptiveThreshold(doc, filterDoc, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 15, 15);
        return sd.setProcessed(filterDoc);
    }

    private boolean checkQR(String qrCode) {

        return !(pageHistory.containsKey(qrCode) &&
                pageHistory.get(qrCode) > new Date().getTime() / 1000 - 15);

    }

    private boolean detectPreviewDocument(Mat inputRgba) {

        ArrayList<MatOfPoint> contours = findContours(inputRgba);

        Quadrilateral quad = getQuadrilateral(contours, inputRgba.size());

        mPreviewPoints = null;
        mPreviewSize = inputRgba.size();

        if (quad != null) {

            Point[] rescaledPoints = new Point[4];

            double ratio = inputRgba.size().height / 500;

            for (int i = 0; i < 4; i++) {
                int x = Double.valueOf(quad.points[i].x * ratio).intValue();
                int y = Double.valueOf(quad.points[i].y * ratio).intValue();
                if (mBugRotate) {
                    rescaledPoints[(i + 2) % 4] = new Point(abs(x - mPreviewSize.width), abs(y - mPreviewSize.height));
                } else {
                    rescaledPoints[i] = new Point(x, y);
                }
            }

            mPreviewPoints = rescaledPoints;

            drawDocumentBox(mPreviewPoints, mPreviewSize);

            Log.d(TAG, quad.points[0].toString() + " , " + quad.points[1].toString() + " , " + quad.points[2].toString() + " , " + quad.points[3].toString());

            double TDFWidth = quad.points[3].x - quad.points[0].x;
            double TDFHeight = quad.points[1].y - quad.points[0].y;
            double BDFWidth = quad.points[2].x - quad.points[1].x;
            double BDFHeight = quad.points[2].y - quad.points[3].y;
            double sum = abs(TDFWidth) + abs(TDFHeight) + abs(BDFWidth) + abs(BDFHeight);

            String mQrCode = qrPref.getString("QR_CODE", null);


            Log.d("calcS topDFWidth:", String.valueOf(TDFWidth));
            Log.d("calcS topDFHeight:", String.valueOf(TDFHeight));
            Log.d("calcS bottomDFWidth:", String.valueOf(BDFWidth));
            Log.d("calcS bottomDFHeight:", String.valueOf(BDFHeight));
            Log.d("calcS DFSum:", String.valueOf(sum));

            Log.d("calcS mQrCode:", String.valueOf(mQrCode));
            Log.d("calcS onceAutoTake:", String.valueOf(onceAutoTake));

            if(OpenNoteScannerActivity.mFocused && doHamedState.equals("0"))
                if(abs(quad.points[0].x) > 30 && abs(quad.points[3].x) > 30 && abs(quad.points[0].y) > 30 && abs(quad.points[3].y) > 30) {
                    if (onceAutoTake && abs(TDFWidth) < 30 && abs(TDFHeight) < 30 && abs(BDFWidth) < 30 && abs(BDFHeight) < 30 && sum < 90) {
                        if (mQrCode != null && (mQrCode.length() == 25 || mQrCode.length() == 14)) {


                            stableCounter++;

                            if(stableCounter == 1) {
                                if (myToast != null)
                                    myToast.cancel();
                                myToast = Toast.makeText(this.mMainActivity, "لطفا گوشی را ثابت نگه دارید", Toast.LENGTH_SHORT);
                                myToast.show();
                            }

                            if(stableCounter > 1) {
                                // take picture
                                mMainActivity.requestPicture();
                                mMainActivity.waitSpinnerInvisible();
                                // prevent to take an other
                                onceAutoTake = false;

                                new android.os.Handler().postDelayed(
                                        new Runnable() {
                                            public void run() {
                                                onceAutoTake = true;
                                                stableCounter = 0;
                                                editor.putString("QR_CODE", "empty");
                                            }
                                        },
                                        2500);
                            }
                        }
                    }
                }
            return true;

        }

        mMainActivity.getHUD().clear();
        mMainActivity.invalidateHUD();

        return false;

    }


    private void drawDocumentBox(Point[] points, Size stdSize) {

        Path path = new Path();

        HUDCanvasView hud = mMainActivity.getHUD();

        // ATTENTION: axis are swapped

        float previewWidth = (float) stdSize.height;
        float previewHeight = (float) stdSize.width;

        path.moveTo(previewWidth - (float) points[0].y, (float) points[0].x);
        path.lineTo(previewWidth - (float) points[1].y, (float) points[1].x);
        path.lineTo(previewWidth - (float) points[2].y, (float) points[2].x);
        path.lineTo(previewWidth - (float) points[3].y, (float) points[3].x);
        path.close();

        PathShape newBox = new PathShape(path, previewWidth, previewHeight);

        Paint paint = new Paint();
        paint.setColor(Color.argb(50, 255, 255, 0));

        hud.clear();
        hud.addShape(newBox, paint);
        mMainActivity.invalidateHUD();


    }

    private Quadrilateral getQuadrilateral(ArrayList<MatOfPoint> contours, Size srcSize) {

        double ratio = srcSize.height / 500;
        int height = Double.valueOf(srcSize.height / ratio).intValue();
        int width = Double.valueOf(srcSize.width / ratio).intValue();
        Size size = new Size(width, height);

        for (MatOfPoint c : contours) {
            MatOfPoint2f c2f = new MatOfPoint2f(c.toArray());
            double peri = Imgproc.arcLength(c2f, true);
            MatOfPoint2f approx = new MatOfPoint2f();
            Imgproc.approxPolyDP(c2f, approx, 0.02 * peri, true);

            Point[] points = approx.toArray();

            // select biggest 4 angles polygon
            if (points.length == 4) {
                Point[] foundPoints = sortPoints(points);

                if (insideArea(foundPoints, size)) {
                    return new Quadrilateral(c, foundPoints);
                }
            }
        }

        return null;
    }

    private Point[] sortPoints(Point[] src) {

        ArrayList<Point> srcPoints = new ArrayList<>(Arrays.asList(src));

        Point[] result = {null, null, null, null};

        Comparator<Point> sumComparator = new Comparator<Point>() {
            @Override
            public int compare(Point lhs, Point rhs) {
                return Double.valueOf(lhs.y + lhs.x).compareTo(rhs.y + rhs.x);
            }
        };

        Comparator<Point> diffComparator = new Comparator<Point>() {

            @Override
            public int compare(Point lhs, Point rhs) {
                return Double.valueOf(lhs.y - lhs.x).compareTo(rhs.y - rhs.x);
            }
        };

        // top-left corner = minimal sum
        result[0] = Collections.min(srcPoints, sumComparator);

        // bottom-right corner = maximal sum
        result[2] = Collections.max(srcPoints, sumComparator);

        // top-right corner = minimal diference
        result[1] = Collections.min(srcPoints, diffComparator);

        // bottom-left corner = maximal diference
        result[3] = Collections.max(srcPoints, diffComparator);

        return result;
    }

    private boolean insideArea(Point[] rp, Size size) {

        // top-left corner = minimal sum
        // bottom-right corner = maximal sum
        // top-right corner = minimal diference
        // bottom-left corner = maximal diference

        boolean check = false;
        int width = Double.valueOf(size.width).intValue();
        int height = Double.valueOf(size.height).intValue();
        int baseMeasure = height / 4;

        int bottomPos = height - baseMeasure;
        int topPos = baseMeasure;
        int leftPos = width / 2 - baseMeasure;
        int rightPos = width / 2 + baseMeasure;


        double TDFWidth = rp[3].x - rp[0].x;
        double TDFHeight = rp[1].y - rp[0].y;
        double BDFWidth = rp[2].x - rp[1].x;
        double BDFHeight = rp[2].y - rp[3].y;

        Log.d("checkme topDFWidth:", String.valueOf(TDFWidth));
        Log.d("checkme topDFHeight:", String.valueOf(TDFHeight));
        Log.d("checkme bottomDFWidth:", String.valueOf(BDFWidth));
        Log.d("checkme bottomDFHeight:", String.valueOf(BDFHeight));



        double TWidth = rp[1].x - rp[0].x;
        double LHeight = rp[3].y - rp[1].y;
        double BWidth = rp[2].x - rp[3].x;
        double RHeight = rp[3].y - rp[1].y;
        double sum = abs(TDFWidth) + abs(TDFHeight) + abs(BDFWidth) + abs(BDFHeight);

        Log.d("loko TWidth:", String.valueOf(TWidth));
        Log.d("loko LHeight:", String.valueOf(LHeight));
        Log.d("loko BWidth:", String.valueOf(BWidth));
        Log.d("loko RHeight:", String.valueOf(RHeight));



        if(abs(rp[0].x) > 20 && abs(rp[3].x) > 20 && abs(rp[0].y) > 20 && abs(rp[3].y) > 20) {
            if (abs(TDFWidth) < 30 && abs(TDFHeight) < 30 && abs(BDFWidth) < 30 && abs(BDFHeight) < 30 && sum < 65) {
                check = true;
            }
        }


        if(check)
            if(TWidth > 200 && LHeight > 200 && BWidth > 200 && RHeight > 200){
                Log.d("loko:", "true");
                check = true;
            }
            else{
                Log.d("loko:", "false");
                check = false;
            }

//        check = true;

        return check;
    }

    private void enhanceDocument(Mat src) {
        if (colorMode && filterMode) {
            src.convertTo(src, -1, colorGain, colorBias);
            Mat mask = new Mat(src.size(), CvType.CV_8UC1);
            Imgproc.cvtColor(src, mask, Imgproc.COLOR_RGBA2GRAY);

            Mat copy = new Mat(src.size(), CvType.CV_8UC3);
            src.copyTo(copy);

//            Effect to the image for better see
//            Imgproc.adaptiveThreshold(mask, mask, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 15, 15);

            src.setTo(new Scalar(255, 255, 255));
            copy.copyTo(src, mask);

            copy.release();
            mask.release();

            // special color threshold algorithm
            colorThresh(src, colorThresh);
        } else if (!colorMode) {
            Imgproc.cvtColor(src, src, Imgproc.COLOR_RGBA2GRAY);
            if (filterMode) {
//            Effect to the image for better see
//                Imgproc.adaptiveThreshold(src, src, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY, 15, 15);
            }
        }
    }

    /**
     * When a pixel have any of its three elements above the threshold
     * value and the average of the three values are less than 80% of the
     * higher one, brings all three values to the max possible keeping
     * the relation between them, any absolute white keeps the value, all
     * others go to absolute black.
     * <p>
     * src must be a 3 channel image with 8 bits per channel
     *
     * @param src
     * @param threshold
     */
    private void colorThresh(Mat src, int threshold) {
        Size srcSize = src.size();
        int size = (int) (srcSize.height * srcSize.width) * 3;
        byte[] d = new byte[size];
        src.get(0, 0, d);

        for (int i = 0; i < size; i += 3) {

            // the "& 0xff" operations are needed to convert the signed byte to double

            // avoid unneeded work
            if ((double) (d[i] & 0xff) == 255) {
                continue;
            }

            double max = Math.max(Math.max((double) (d[i] & 0xff), (double) (d[i + 1] & 0xff)),
                    (double) (d[i + 2] & 0xff));
            double mean = ((double) (d[i] & 0xff) + (double) (d[i + 1] & 0xff)
                    + (double) (d[i + 2] & 0xff)) / 3;

            if (max > threshold && mean < max * 0.8) {
                d[i] = (byte) ((double) (d[i] & 0xff) * 255 / max);
                d[i + 1] = (byte) ((double) (d[i + 1] & 0xff) * 255 / max);
                d[i + 2] = (byte) ((double) (d[i + 2] & 0xff) * 255 / max);
            } else {
                d[i] = d[i + 1] = d[i + 2] = 0;
            }
        }
        src.put(0, 0, d);
    }

    private Mat fourPointTransform(Mat src, Point[] pts) {

        double ratio = src.size().height / 500;
        int height = Double.valueOf(src.size().height / ratio).intValue();
        int width = Double.valueOf(src.size().width / ratio).intValue();

        Point tl = pts[0];
        Point tr = pts[1];
        Point br = pts[2];
        Point bl = pts[3];

        double widthA = Math.sqrt(Math.pow(br.x - bl.x, 2) + Math.pow(br.y - bl.y, 2));
        double widthB = Math.sqrt(Math.pow(tr.x - tl.x, 2) + Math.pow(tr.y - tl.y, 2));

        double dw = Math.max(widthA, widthB) * ratio;
        int maxWidth = Double.valueOf(dw).intValue();


        double heightA = Math.sqrt(Math.pow(tr.x - br.x, 2) + Math.pow(tr.y - br.y, 2));
        double heightB = Math.sqrt(Math.pow(tl.x - bl.x, 2) + Math.pow(tl.y - bl.y, 2));

        double dh = Math.max(heightA, heightB) * ratio;
        int maxHeight = Double.valueOf(dh).intValue();

        Mat doc = new Mat(maxHeight, maxWidth, CvType.CV_8UC4);

        Mat src_mat = new Mat(4, 1, CvType.CV_32FC2);
        Mat dst_mat = new Mat(4, 1, CvType.CV_32FC2);

        src_mat.put(0, 0, tl.x * ratio, tl.y * ratio, tr.x * ratio, tr.y * ratio, br.x * ratio, br.y * ratio, bl.x * ratio, bl.y * ratio);
        dst_mat.put(0, 0, 0.0, 0.0, dw, 0.0, dw, dh, 0.0, dh);

        Mat m = Imgproc.getPerspectiveTransform(src_mat, dst_mat);

        Imgproc.warpPerspective(src, doc, m, doc.size());

        return doc;
    }

    private ArrayList<MatOfPoint> findContours(Mat src) {

        Mat grayImage = null;
        Mat cannedImage = null;
        Mat resizedImage = null;
        Mat temp = null;

        double ratio = src.size().height / 500;
        int height = Double.valueOf(src.size().height / ratio).intValue();
        int width = Double.valueOf(src.size().width / ratio).intValue();
        Size size = new Size(width, height);

        resizedImage = new Mat(size, CvType.CV_8UC4);
        grayImage = new Mat(size, CvType.CV_8UC4);
        cannedImage = new Mat(size, CvType.CV_8UC1);

        Imgproc.resize(src, resizedImage, size);
        Imgproc.cvtColor(resizedImage, grayImage, Imgproc.COLOR_RGBA2GRAY, 4);
        Imgproc.GaussianBlur(grayImage, grayImage, new Size(5, 5), 0);
        Imgproc.Canny(grayImage, cannedImage, 70, 200);


        PersianCalendar initDate = new PersianCalendar();
        String year = String.valueOf(initDate.getPersianYear());
        String month = String.valueOf(initDate.getPersianMonth());
        String day = String.valueOf(initDate.getPersianDay());
        String str = "canned:" + year + month + day + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()) + ".jpg";
        String str2 = "resizedImage:" + year + month + day + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()) + ".jpg";
        String str3 = "recImage:" + year + month + day + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()) + ".jpg";
        String str4 = "MaxRecImage:" + year + month + day + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()) + ".jpg";
//        Imgcodecs.imwrite(getSource(str), cannedImage);
//        Imgcodecs.imwrite(getSource(str2), resizedImage);


        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();

        Imgproc.findContours(cannedImage, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);


        Log.d("findedContours:",contours.toString());






        hierarchy.release();

        Collections.sort(contours, new Comparator<MatOfPoint>() {

            @Override
            public int compare(MatOfPoint lhs, MatOfPoint rhs) {
                return Double.valueOf(Imgproc.contourArea(rhs)).compareTo(Imgproc.contourArea(lhs));
            }
        });


        Point[] MaxPoints = null;

        temp = new Mat(size, CvType.CV_8UC4);
        int counter =  0;
        double max = 0;
        for(MatOfPoint c : contours){
            counter++;
            MatOfPoint2f c2f = new MatOfPoint2f(c.toArray());
            double peri = Imgproc.arcLength(c2f, true);
            MatOfPoint2f approx = new MatOfPoint2f();
            Imgproc.approxPolyDP(c2f, approx, 0.1 * peri, true);

            Point[] points = approx.toArray();
            points = sortPoints(points);


            // top-left corner = minimal sum
            // top-right corner = minimal diference
            // bottom-right corner = maximal sum
            // bottom-left corner = maximal diference

            if(points.length == 4) {
                Log.d("foundedFromCanny" + counter, points.toString());

                if(abs(points[0].x - points[2].x) + abs(points[0].y - points[2].y) > max){
                    max = abs(points[0].x - points[2].x) + abs(points[0].y - points[2].y);
                    MaxPoints = points;
                }


                Imgproc.rectangle(temp,points[0],points[2],new Scalar(0,255,0));

                for (Point a : points) {
                    Log.d("foundedFromCanny" + counter, a.toString());
                }
            }

        }

//        Imgcodecs.imwrite(getSource(str3), temp);



        if(MaxPoints != null) {

            temp = new Mat(size, CvType.CV_8UC4);
            Imgproc.rectangle(temp, MaxPoints[0], MaxPoints[2], new Scalar(0, 255, 0));
//            Imgcodecs.imwrite(getSource(str4), temp);



            MatOfPoint c = new MatOfPoint(MaxPoints);

            ArrayList<MatOfPoint> a = new ArrayList<MatOfPoint>();
            a.add(c);
            contours = a;

        }








        Log.d("yanniiiii:",contours.toString());



        resizedImage.release();
        grayImage.release();
        cannedImage.release();

        return contours;
    }

    public Result[] zxing(Mat inputImage) throws ChecksumException, FormatException {

        int w = inputImage.width();
        int h = inputImage.height();

        Mat southEast;

//        if (mBugRotate) {
//            southEast = inputImage.submat(h - h / 4, h, 0, w / 2 - h / 4);
//        } else {
//            southEast = inputImage.submat(0, h / 4, w / 2 + h / 4, w);
//        }
        southEast = inputImage.submat(0, h, 0, w);

        Bitmap bMap = Bitmap.createBitmap(southEast.width(), southEast.height(), Bitmap.Config.ARGB_8888);
        org.opencv.android.Utils.matToBitmap(southEast, bMap);
        southEast.release();
        int[] intArray = new int[bMap.getWidth() * bMap.getHeight()];
        //copy pixel data from the Bitmap into the 'intArray' array
        bMap.getPixels(intArray, 0, bMap.getWidth(), 0, 0, bMap.getWidth(), bMap.getHeight());

        LuminanceSource source = new RGBLuminanceSource(bMap.getWidth(), bMap.getHeight(), intArray);

        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        Result[] results = {};
        try {
            results = qrCodeMultiReader.decodeMultiple(bitmap);
        } catch (NotFoundException e) {
        }

        return results;

    }

    public static String getSource(String name) {

        String SCAN_DIC = "Notopia";
        File storageDir = new File(Environment.getExternalStorageDirectory().toString() + "/" + SCAN_DIC + "/debug");
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        File image = null;
        image = new File(storageDir, name);
//        try {
//            image = File.createTempFile(
//                    name,  /* prefix */
//                    ".jpg",         /* suffix */
//                    storageDir      /* directory */
//            );
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        return image.getAbsolutePath();
//        return SOURCE_FOLDER + name;
    }

    public void setBugRotate(boolean bugRotate) {
        mBugRotate = bugRotate;
    }
}
