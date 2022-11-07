package ir.notopia.android.scanner.omr;

import android.os.Environment;
import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;

import java.io.File;
import java.util.Collections;
import java.util.List;

import static org.opencv.imgcodecs.Imgcodecs.imwrite;

public class Util {

    public static final String SCAN_DIC = "Notopia";
    public static String SOURCE_FOLDER = System.getProperty("user.dir") + "/sources/";
    public static String TARGET_FOLDER = System.getProperty("user.dir") + "/target/";

    public static String getSource(String name) {
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

    public static String getOutput(String name) {
        return TARGET_FOLDER + name;
    }

    public static void write2File(Mat source, String name) {
        imwrite(getOutput(name), source);
    }

    public static void sout(String str) {
        Log.i("SOUT_LOG", str);
//        System.out.println(str);
    }

    public static void sortTopLeft2BottomRight(List<MatOfPoint> points) {
        // top-left to right-bottom sort
        Collections.sort(points, (e1, e2) -> {

            Point o1 = new Point(e1.get(0, 0));
            Point o2 = new Point(e2.get(0, 0));

            return o1.y > o2.y ? 1 : -1;
        });
    }

    public static void sortLeft2Right(List<MatOfPoint> points) {
        // left to right sort
        Collections.sort(points, (e1, e2) -> {

            Point o1 = new Point(e1.get(0, 0));
            Point o2 = new Point(e2.get(0, 0));

            return o1.x > o2.x ? 1 : -1;
        });
    }
}
