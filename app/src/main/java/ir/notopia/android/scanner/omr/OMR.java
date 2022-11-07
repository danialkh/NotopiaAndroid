package ir.notopia.android.scanner.omr;

import android.content.Context;

import org.opencv.android.Utils;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;

import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import org.opencv.utils.Converters;

import java.util.Iterator;
import java.util.*;

import java.lang.Math;

import ir.notopia.android.R;

import static ir.notopia.android.scanner.omr.Util.getSource;
import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_SIMPLE;
import static org.opencv.imgproc.Imgproc.RETR_TREE;
import static org.opencv.imgproc.Imgproc.approxPolyDP;
import static org.opencv.imgproc.Imgproc.arcLength;
import static org.opencv.imgproc.Imgproc.circle;
import static org.opencv.imgproc.Imgproc.contourArea;
import static org.opencv.imgproc.Imgproc.convexHull;
import static org.opencv.imgproc.Imgproc.findContours;
import static org.opencv.imgproc.Imgproc.getPerspectiveTransform;
import static org.opencv.imgproc.Imgproc.minAreaRect;
import static org.opencv.imgproc.Imgproc.moments;
import static org.opencv.imgproc.Imgproc.warpPerspective;

public class OMR {
    public class OPTIONS{
        private Double Minimum_value;
        private Integer Minimum_col_value;
        private Integer Minimum_row_value;
        public OPTIONS(){
            Minimum_value = 0.0;
            Minimum_row_value = -1;
            Minimum_col_value = -1;
        }
    };
    private static final String TAG = "OMR CLASS";
    private Context mContext;
    private Mat source;
    private final double[] CORNER_FEATS = new double[]{0.322965313273202, 0.19188334690998524, 1.1514327482234812, 0.998754685666376};
    private final int TRANSF_SIZE = 512;
    private boolean isEven;
    private boolean isNew;
    static double alpha = 1;
    static double beta = 40;
    private boolean logging = false;

    public OMR(Mat source) {
        this.source = source;
    }

    public OMR(Mat source, boolean isNew, boolean isEven, Context context) {
        this.source = source;
        this.isEven = isEven;
        this.mContext = context;
        this.isNew = isNew;
    }


    public void setLogging(boolean logging) {
        this.logging = logging;
    }

    public Mat normalize(Mat image) throws Exception {
        Mat normalizedImage = new Mat(image.rows(), image.cols(), CvType.CV_8U, Scalar.all(0));
        Core.normalize(image, normalizedImage, 0, 255, Core.NORM_MINMAX);
        return normalizedImage;
    }

    public MatOfPoint2f get_approx_contour(MatOfPoint contour, double tol) throws Exception {
        double epsilon = tol * arcLength(new MatOfPoint2f(contour.toArray()), true);
        MatOfPoint2f approx = new MatOfPoint2f();
        approxPolyDP(new MatOfPoint2f(contour.toArray()), approx, epsilon, true);
        return approx;
    }

    public List<MatOfPoint> get_contours(Mat image) throws Exception {
        Mat im = new Mat();
        Mat blurred = new Mat();
        Imgproc.GaussianBlur(image, blurred, new Size(7, 7), 6);
        Imgproc.cvtColor(blurred, im, Imgproc.COLOR_BGR2GRAY);
        im = normalize(im);
        Imgproc.threshold(im, im, 25, 255, Imgproc.THRESH_BINARY);
        List<MatOfPoint> contours = new ArrayList<>();
        List<MatOfPoint2f> map_contours = new ArrayList<>();
        findContours(im, contours, new Mat(), RETR_TREE, CHAIN_APPROX_SIMPLE);
        Iterator<MatOfPoint> iterator = contours.iterator();

        while (iterator.hasNext()) {
            MatOfPoint contour = iterator.next();
            map_contours.add(get_approx_contour(contour, 0.01));
        }
        List<MatOfPoint> Contours = new ArrayList<>();
        for (MatOfPoint2f point : map_contours) {
            MatOfPoint new_point = new MatOfPoint(point.toArray());
            Contours.add(new_point);
        }
        return Contours;
    }

    public List<MatOfPoint> get_sorted_contours(List<MatOfPoint> contours) {
        Collections.sort(contours, new Comparator<MatOfPoint>() {
            @Override
            public int compare(MatOfPoint o1, MatOfPoint o2) {
                double[] features1 = new double[4];
                double[] features2 = new double[4];
                try {

                    features1 = get_features(new MatOfPoint(o1.toArray()));
                    features2 = get_features(new MatOfPoint(o2.toArray()));
                    double comparison1 = features_distance(CORNER_FEATS, features1);
                    double comparison2 = features_distance(CORNER_FEATS, features2);
                    if (comparison1 - comparison2 > 0)
                        return 1;
                    else if (comparison1 - comparison2 < 0)
                        return -1;
                    else
                        return 0;


                } catch (Exception e) {
                    e.printStackTrace();
                    return 0;
                }
            }
        });
        return contours;
    }

    public List<Point> get_template_Match(Mat template1, Mat template2, Mat template3, Mat template4, Mat image) {
        Mat outputImage1 = new Mat();
        Mat outputImage2 = new Mat();
        Mat outputImage3 = new Mat();
        Mat outputImage4 = new Mat();
        int machMethod = Imgproc.TM_CCOEFF;

        //Template matching method
        Imgproc.matchTemplate(image, template1, outputImage1, machMethod);
        Imgproc.matchTemplate(image, template2, outputImage2, machMethod);
        Imgproc.matchTemplate(image, template3, outputImage3, machMethod);
        Imgproc.matchTemplate(image, template4, outputImage4, machMethod);

        Core.MinMaxLocResult mmr1 = Core.minMaxLoc(outputImage1);
        Core.MinMaxLocResult mmr2 = Core.minMaxLoc(outputImage2);
        Core.MinMaxLocResult mmr3 = Core.minMaxLoc(outputImage3);
        Core.MinMaxLocResult mmr4 = Core.minMaxLoc(outputImage4);
        List<Point> matchLoc = new ArrayList<>();
        matchLoc.add(mmr1.maxLoc);
        matchLoc.add(mmr2.maxLoc);
        matchLoc.add(mmr3.maxLoc);
        matchLoc.add(mmr4.maxLoc);

        //Draw rectangle on result image
//        Imgproc.rectangle(image, matchLoc.get(0), new Point(matchLoc.get(0).x + template1.cols(),
//                matchLoc.get(0).y + template1.rows()), new Scalar(0, 0, 255));
//        Imgproc.rectangle(image, matchLoc.get(1), new Point(matchLoc.get(1).x + template2.cols(),
//                matchLoc.get(1).y + template2.rows()), new Scalar(0, 0, 255));
//        Imgproc.rectangle(image, matchLoc.get(2), new Point(matchLoc.get(2).x + template3.cols(),
//                matchLoc.get(2).y + template3.rows()), new Scalar(0, 0, 255));
//        Imgproc.rectangle(image, matchLoc.get(3), new Point(matchLoc.get(3).x + template4.cols(),
//                matchLoc.get(3).y + template4.rows()), new Scalar(0, 0, 255));
//        Imgcodecs.imwrite(getSource("TemplateMatching.jpg"), image);
        return matchLoc;
    }

    public List<MatOfPoint> get_corners(Mat image) throws Exception {

        //Load image file
        Mat template1 = Utils.loadResource(mContext, R.drawable.tl);
//        Mat template1 = Imgcodecs.imread(getSource("tl.jpg"));

        Mat template2 = Utils.loadResource(mContext, R.drawable.tr);
//        Mat template2 = Imgcodecs.imread(getSource("tr.jpg"));

        Mat template3 = Utils.loadResource(mContext, R.drawable.bl);
//        Mat template3 = Imgcodecs.imread(getSource("bl.jpg"));

        Mat template4 = Utils.loadResource(mContext, R.drawable.br);
//        Mat template4 = Imgcodecs.imread(getSource("br.jpg"));

        List<Point> matchLoc = get_template_Match(template1, template2, template3, template4, image);

        Mat image1 = image.submat(new Rect(matchLoc.get(0), new Point(matchLoc.get(0).x + template1.cols(), matchLoc.get(0).y + template1.rows())));
        Mat image2 = image.submat(new Rect(matchLoc.get(1), new Point(matchLoc.get(1).x + template2.cols(), matchLoc.get(1).y + template2.rows())));
        Mat image3 = image.submat(new Rect(matchLoc.get(2), new Point(matchLoc.get(2).x + template3.cols(), matchLoc.get(2).y + template3.rows())));
        Mat image4 = image.submat(new Rect(matchLoc.get(3), new Point(matchLoc.get(3).x + template4.cols(), matchLoc.get(3).y + template4.rows())));
        List<MatOfPoint> image1_contours = get_contours(image1);
        List<MatOfPoint> image2_contours = get_contours(image2);
        List<MatOfPoint> image3_contours = get_contours(image3);
        List<MatOfPoint> image4_contours = get_contours(image4);
        image1_contours = get_sorted_contours(image1_contours);
        image2_contours = get_sorted_contours(image2_contours);
        image3_contours = get_sorted_contours(image3_contours);
        image4_contours = get_sorted_contours(image4_contours);

        Imgproc.drawContours(image1, image1_contours, 0, new Scalar(0, 255, 0), 3);
        Imgproc.drawContours(image2, image2_contours, 0, new Scalar(0, 255, 0), 3);
        Imgproc.drawContours(image3, image3_contours, 0, new Scalar(0, 255, 0), 3);
        Imgproc.drawContours(image4, image4_contours, 0, new Scalar(0, 255, 0), 3);
//        Imgcodecs.imwrite(getSource("image1.jpg"), image1);
//        Imgcodecs.imwrite(getSource("image2.jpg"), image2);
//        Imgcodecs.imwrite(getSource("image3.jpg"), image3);
//        Imgcodecs.imwrite(getSource("image4.jpg"), image4);

        image1.copyTo(image.submat(new Rect(matchLoc.get(0), new Point(matchLoc.get(0).x + template1.cols(), matchLoc.get(0).y + template1.rows()))));
        image2.copyTo(image.submat(new Rect(matchLoc.get(1), new Point(matchLoc.get(1).x + template2.cols(), matchLoc.get(1).y + template2.rows()))));
        image3.copyTo(image.submat(new Rect(matchLoc.get(2), new Point(matchLoc.get(2).x + template3.cols(), matchLoc.get(2).y + template3.rows()))));
        image4.copyTo(image.submat(new Rect(matchLoc.get(3), new Point(matchLoc.get(3).x + template4.cols(), matchLoc.get(3).y + template4.rows()))));
        Imgcodecs.imwrite(getSource("Corners.jpg"), image);

        //Load image file
//        Mat template5=Imgcodecs.imread(getSource("image1.jpg"));
//        Mat template6=Imgcodecs.imread(getSource("image2.jpg"));
//        Mat template7=Imgcodecs.imread(getSource("image3.jpg"));
//        Mat template8=Imgcodecs.imread(getSource("image4.jpg"));
        List<Point> matchLoc_corners = get_template_Match(image1, image2, image3, image4, image);
        List<Point> pointsOrdered1 = new ArrayList<Point>();
        List<Point> pointsOrdered2 = new ArrayList<Point>();
        List<Point> pointsOrdered3 = new ArrayList<Point>();
        List<Point> pointsOrdered4 = new ArrayList<Point>();
        pointsOrdered1.add(matchLoc_corners.get(0));
        pointsOrdered1.add(new Point(matchLoc_corners.get(0).x + image1.cols(), matchLoc_corners.get(0).y));
        pointsOrdered1.add(new Point(matchLoc_corners.get(0).x, matchLoc_corners.get(0).y + image1.rows()));
        pointsOrdered1.add(new Point(matchLoc_corners.get(0).x + image1.cols(), matchLoc_corners.get(0).y + image1.rows()));

        pointsOrdered2.add(matchLoc_corners.get(1));
        pointsOrdered2.add(new Point(matchLoc_corners.get(1).x + image2.cols(), matchLoc_corners.get(1).y));
        pointsOrdered2.add(new Point(matchLoc_corners.get(1).x, matchLoc_corners.get(1).y + image2.rows()));
        pointsOrdered2.add(new Point(matchLoc_corners.get(1).x + image2.cols(), matchLoc_corners.get(1).y + image2.rows()));

        pointsOrdered3.add(matchLoc_corners.get(2));
        pointsOrdered3.add(new Point(matchLoc_corners.get(2).x + image3.cols(), matchLoc_corners.get(2).y));
        pointsOrdered3.add(new Point(matchLoc_corners.get(2).x, matchLoc_corners.get(2).y + image3.rows()));
        pointsOrdered3.add(new Point(matchLoc_corners.get(2).x + image3.cols(), matchLoc_corners.get(2).y + image3.rows()));

        pointsOrdered4.add(matchLoc_corners.get(3));
        pointsOrdered4.add(new Point(matchLoc_corners.get(3).x + image4.cols(), matchLoc_corners.get(3).y));
        pointsOrdered4.add(new Point(matchLoc_corners.get(3).x, matchLoc_corners.get(3).y + image4.rows()));
        pointsOrdered4.add(new Point(matchLoc_corners.get(3).x + image4.cols(), matchLoc_corners.get(3).y + image4.rows()));
        MatOfPoint sourceMat1 = new MatOfPoint();
        MatOfPoint sourceMat2 = new MatOfPoint();
        MatOfPoint sourceMat3 = new MatOfPoint();
        MatOfPoint sourceMat4 = new MatOfPoint();
        sourceMat1.fromList(pointsOrdered1);
        sourceMat2.fromList(pointsOrdered2);
        sourceMat3.fromList(pointsOrdered3);
        sourceMat4.fromList(pointsOrdered4);

        List<MatOfPoint> corners = new ArrayList<>();
        corners.add(sourceMat1);
        corners.add(sourceMat2);
        corners.add(sourceMat3);
        corners.add(sourceMat4);
        return corners;
    }

    public List<Point> get_bounding_rectByList(MatOfPoint contour) throws Exception {
        RotatedRect rect = new RotatedRect();
        rect = minAreaRect(new MatOfPoint2f(contour.toArray()));
        Point[] vertices = new Point[4];
        List<Point> points = new ArrayList<>();
        rect.points(vertices);
        for (int i = 0; i < 4; i++)
            points.add(vertices[i]);
        return points;
    }

    public Point[] get_bounding_rect(MatOfPoint contour) throws Exception {
        RotatedRect rect = new RotatedRect();
        rect = minAreaRect(new MatOfPoint2f(contour.toArray()));
        Point[] vertices = new Point[4];
        rect.points(vertices);
        return vertices;
    }

    public MatOfPoint get_convex_hull(MatOfPoint contour) throws Exception {
        MatOfInt hull = new MatOfInt();
        convexHull(contour, hull);
        MatOfPoint hullContour = hull2Points(hull, contour);
        return hullContour;
    }

    public MatOfPoint hull2Points(MatOfInt hull, MatOfPoint contour) {
        List<Integer> indexes = hull.toList();
        List<Point> points = new ArrayList<>();
        MatOfPoint point = new MatOfPoint();
        for (Integer index : indexes) {
            points.add(contour.toList().get(index));
        }
        point.fromList(points);
        return point;
    }

    public double get_contour_area_by_hull_area(MatOfPoint contour) throws Exception {
        return (contourArea(contour) / contourArea(get_convex_hull(contour)));
    }

    public double get_contour_area_by_bounding_box_area(MatOfPoint contour) throws Exception {
        return (contourArea(contour) / contourArea(new MatOfPoint(get_bounding_rect(contour))));
    }

    public double get_contour_perim_by_hull_perim(MatOfPoint contour) throws Exception {
        return (arcLength(new MatOfPoint2f(contour.toArray()), true) / arcLength(new MatOfPoint2f(get_convex_hull(contour).toArray()), true));
    }

    public double get_contour_perim_by_bounding_box_perim(MatOfPoint contour) throws Exception {
        return (arcLength(new MatOfPoint2f(contour.toArray()), true) / arcLength(new MatOfPoint2f(get_bounding_rect(contour)), true));
    }

    public double[] get_features(MatOfPoint contour) throws Exception {
        double[] features = new double[4];
        try {
            if (get_contour_area_by_hull_area(contour) != Double.POSITIVE_INFINITY || get_contour_area_by_hull_area(contour) != Double.NEGATIVE_INFINITY
                    || get_contour_area_by_bounding_box_area(contour) != Double.POSITIVE_INFINITY || get_contour_area_by_bounding_box_area(contour) != Double.NEGATIVE_INFINITY
                    || get_contour_perim_by_hull_perim(contour) != Double.POSITIVE_INFINITY || get_contour_perim_by_hull_perim(contour) != Double.NEGATIVE_INFINITY
                    || get_contour_perim_by_bounding_box_perim(contour) != Double.POSITIVE_INFINITY || get_contour_perim_by_bounding_box_perim(contour) != Double.NEGATIVE_INFINITY
            ) {
                features[0] = get_contour_area_by_hull_area(contour);
                features[1] = get_contour_area_by_bounding_box_area(contour);
                features[2] = get_contour_perim_by_hull_perim(contour);
                features[3] = get_contour_perim_by_bounding_box_perim(contour);
                //sout("(" + features[0] + ", " + features[1] + ", " + features[2] + ", " + features[3] + ")");
                return features;
            } else
                throw new ArithmeticException();
        } catch (ArithmeticException e) {
            features[0] = Double.POSITIVE_INFINITY;
            features[1] = Double.POSITIVE_INFINITY;
            features[2] = Double.POSITIVE_INFINITY;
            features[3] = Double.POSITIVE_INFINITY;
            return features;
        }
    }

    public double features_distance(double[] f1, double[] f2) throws Exception {
        double[] norm_features_array = new double[4];
        double norm_features = 0.0;
        for (int i = 0; i < 4; i++) {
            norm_features_array[i] = f1[i] - f2[i];
            norm_features += norm_features_array[i] * norm_features_array[i];
        }
        return Math.sqrt(norm_features);
    }

    // Default mutable arguments should be harmless here
    public void draw_point(Point point, Mat img, int radius, Scalar color) throws Exception {
        circle(img, point, radius, color, radius);
    }

    public Point get_centroid(MatOfPoint contour) throws Exception {
        Moments m = moments(contour);
        Point center = new Point(0, 0);
        center.x = m.get_m10() / m.get_m00();
        center.y = m.get_m01() / m.get_m00();
        return center;
    }

    public List<Point> order_points(List<Point> points) throws Exception {
        Collections.sort(points, new Comparator<Point>() {
            @Override
            public int compare(Point o1, Point o2) {
                int sum_x = 0, sum_y = 0;
                double angle1 = 0.0, angle2 = 0.0;
                double mean_x = 0.0, mean_y = 0.0;
                double angle_x = 0.0, angle_y = 0;

                for (int i = 0; i < points.size(); i++) {
                    sum_x += points.get(i).x;
                    sum_y += points.get(i).y;
                }
                mean_x = (double) sum_x / points.size();
                mean_y = (double) sum_y / points.size();

                angle_x = o1.x - mean_x;
                angle_y = o1.y - mean_y;
                angle1 = Math.atan2(angle_y, angle_x);

                angle_x = o2.x - mean_x;
                angle_y = o2.y - mean_y;
                angle2 = Math.atan2(angle_y, angle_x);

                if (angle1 < 0)
                    angle1 = 2 * Math.PI + angle1;

                if (angle2 < 0)
                    angle2 = 2 * Math.PI + angle2;

                if (angle1 - angle2 > 0)
                    return 1;
                else if (angle1 - angle2 < 0)
                    return -1;
                else
                    return 0;
            }
        });

        return points;
    }

    public List<Point> get_outmost_points(List<MatOfPoint> contours) throws Exception {
        List<Point> all_points = Concatenate(contours);
        return get_bounding_rectByList(new MatOfPoint(Converters.vector_Point_to_Mat(all_points)));
    }

    public List<Point> Concatenate(List<MatOfPoint> contours) throws Exception {
        List<List<Point>> results = new ArrayList<List<Point>>();
        List<Point> final_result = new ArrayList<>();
        for (MatOfPoint l : contours) {
            List<Point> result = new ArrayList<>();
            Converters.Mat_to_vector_Point(l, result);
            results.add(result);
        }
        for (int i = 0; i < results.size(); i++)
            for (int j = 0; j < results.get(i).size(); j++)
                final_result.add(results.get(i).get(j));

        return final_result;
    }


    public Mat perspective_transform(Mat img, List<Point> points) throws Exception {
        //Transform img so that points are the new corners
        List<Point> dest_points = new ArrayList<>();
        dest_points.add(new Point(0, 0));
        dest_points.add(new Point(TRANSF_SIZE, 0));
        dest_points.add(new Point(TRANSF_SIZE, TRANSF_SIZE));
        dest_points.add(new Point(0, TRANSF_SIZE));

        Mat warped = new Mat();
        MatOfPoint2f source_pers = new MatOfPoint2f(points.get(2), points.get(3), points.get(0), points.get(1));
        MatOfPoint2f dest = new MatOfPoint2f(dest_points.get(0), dest_points.get(1), dest_points.get(2), dest_points.get(3));
        Mat beforetransf = getPerspectiveTransform(source_pers, dest);
        warpPerspective(img, warped, beforetransf, new Size(TRANSF_SIZE, TRANSF_SIZE));
        return warped;
    }

    public Point sheet_coord_to_transf_coord(double x, double y) throws Exception {
        //return (new Point(Math.round(TRANSF_SIZE * x/1006), Math.round(TRANSF_SIZE * (1 - y/209))));
        return (new Point(Math.round(TRANSF_SIZE * x / 202), Math.round(TRANSF_SIZE * (1 - y / 1720))));
    }

//    public Mat get_question_patch(Mat transf, int q_number) throws Exception {
//        Point tl = sheet_coord_to_transf_coord(93, 198 - 48 * q_number);
//        Point br = sheet_coord_to_transf_coord(653, 158 - 48 * q_number);
//        return transf.submat(new Rect(tl, br));
//    }

    public Mat get_day_patch(Mat transf, int q_number) throws Exception {
        Point tl = sheet_coord_to_transf_coord(60.5, 1535 - 39 * q_number);
        Point br = sheet_coord_to_transf_coord(174.5, 1508 - 39 * q_number);
        Rect test = new Rect(tl, br);
        return transf.submat(new Rect(tl, br));
    }

    public Mat get_month_patch(Mat transf, int q_number) throws Exception {
        Point tl = new Point();
        Point br = new Point();
        if (q_number <= 2) {
            tl = sheet_coord_to_transf_coord(146.5, 823 - 39 * q_number);
            br = sheet_coord_to_transf_coord(176.5, 796 - 39 * q_number);
        } else if (q_number >= 3 && q_number <= 5) {
            tl = sheet_coord_to_transf_coord(146.5, 682 - 39 * (q_number - 3));
            br = sheet_coord_to_transf_coord(176.5, 652 - 39 * (q_number - 3));
        } else if (q_number >= 6 && q_number <= 8) {
            tl = sheet_coord_to_transf_coord(146.5, 541 - 39 * (q_number - 6));
            br = sheet_coord_to_transf_coord(176.5, 514 - 39 * (q_number - 6));
        } else {
            tl = sheet_coord_to_transf_coord(146.5, 400 - 39 * (q_number - 9));
            br = sheet_coord_to_transf_coord(176.5, 373 - 39 * (q_number - 9));
        }
        return transf.submat(new Rect(tl, br));
    }

    public Mat get_category_patch(Mat transf, int q_number) throws Exception {
        Point tl = sheet_coord_to_transf_coord(60.5, 195 - 53 * q_number);
        Point br = sheet_coord_to_transf_coord(140.5, 158 - 53 * q_number);
        return transf.submat(new Rect(tl, br));
    }

//    public Mat[] get_question_patches(Mat transf) throws Exception {
//        Mat[] results = new Mat[4];
//        for(int i = 0; i < 4; i++)
//            results[i] = get_question_patch(transf, i);
//        return results;
//    }

    public Mat[] get_day_patches(Mat transf) throws Exception {
        Mat[] results = new Mat[16];
        for (int i = 0; i < 16; i++)
            results[i] = get_day_patch(transf, i);
        return results;
    }

    public Mat[] get_month_patches(Mat transf) throws Exception {
        Mat[] results = new Mat[12];
        for (int i = 0; i < 12; i++)
            results[i] = get_month_patch(transf, i);
        return results;
    }

    public Mat[] get_category_patches(Mat transf) throws Exception {
        Mat[] results = new Mat[3];
        for (int i = 0; i < 3; i++)
            results[i] = get_category_patch(transf, i);
        return results;
    }

    public Mat[] get_alternative_day_patches(Mat day_patch) throws Exception {
        Mat[] day_results = new Mat[2];
        Point point0 = new Point();
        Point point1 = new Point();
        for (int i = 0; i < 2; i++) {
            point0 = sheet_coord_to_transf_coord(84 * i, 0);
            point1 = sheet_coord_to_transf_coord(30 + 84 * i, 0);
            day_results[i] = day_patch.submat(0, day_patch.rows(), (int) point0.x, (int) point1.x);
        }
        return day_results;
    }

    public Mat[] get_alternative_month_patches(Mat month_patch) throws Exception {
        Mat[] Month_results = new Mat[1];
        Point point0 = new Point();
        Point point1 = new Point();
        for (int i = 0; i < 1; i++) {
            point0 = sheet_coord_to_transf_coord(i, 0);
            point1 = sheet_coord_to_transf_coord(30 + i, 0);
            if (point1.x > month_patch.size().width)
                point1.x = month_patch.size().width;

            Month_results[i] = month_patch.submat(0, month_patch.rows(), (int) point0.x, (int) point1.x);
        }
        return Month_results;
    }

    public Mat[] get_alternative_category_patches(Mat category_patch) throws Exception {
        Mat[] Category_results = new Mat[2];
        Point point0 = new Point();
        Point point1 = new Point();
        for (int i = 0; i < 2; i++) {
            point0 = sheet_coord_to_transf_coord(50 * i, 0);
            point1 = sheet_coord_to_transf_coord(30 + 50 * i, 0);
            if (point1.x > category_patch.size().width)
                point1.x = category_patch.size().width;

            Category_results[i] = category_patch.submat(0, category_patch.rows(), (int) point0.x, (int) point1.x);
        }
        return Category_results;
    }

    public void draw_marked_day_alternative(Mat day_patch, int index) throws Exception {
        Point c = sheet_coord_to_transf_coord(30 * (3 * index + .5), 30 / 2);
        draw_point(new Point(c.x, TRANSF_SIZE - c.y), day_patch, 2, new Scalar(255, 255, 0));
    }

    public void draw_marked_month_alternative(Mat month_patch, int index) throws Exception {
        Point c = sheet_coord_to_transf_coord(30 * (2 * index + .4), 30 / 2);
        draw_point(new Point(c.x, TRANSF_SIZE - c.y), month_patch, 2, new Scalar(255, 0, 255));
    }

    public void draw_marked_category_alternative(Mat category_patch, int index) throws Exception {
        Point c = sheet_coord_to_transf_coord(30 * (2.5 * index + 0.5), 30 / 2);
        draw_point(new Point(c.x, TRANSF_SIZE - c.y), category_patch, 2, new Scalar(0, 255, 255));
    }

    public List<Double> get_marked_alternative(Mat[] alternative_patches) throws Exception {

        List<Double> before_means = new ArrayList<>();
        for (int i = 0; i < alternative_patches.length; i++) {
            MatOfDouble mean = new MatOfDouble();
            MatOfDouble sigma = new MatOfDouble();

            Core.meanStdDev(alternative_patches[i], mean, sigma);
            before_means.add(mean.get(0, 0)[0]);
        }

        return before_means;
    }

    public OPTIONS get_alt_index(List<List<Double>> before_means, int patch_size)
    {
//        List<List<Double>> means = new ArrayList<>();
//        for(int i = 0; i < before_means.size(); i++) {
//            List<Double> mean = new ArrayList<>();
//            mean = before_means.get(i);
//            Collections.sort(mean);
//            means.add(mean);
//        }
        List<OPTIONS> options = new ArrayList<>();
        for(Integer i = 0; i < patch_size; i++) {
            OPTIONS option = new OPTIONS();
            Integer min_index = -1;
            Double min_value = 255.0;
           for(Integer j = 0; j < before_means.size(); j++)
           {
              if(before_means.get(j).get(i) < min_value) {
                  min_value = before_means.get(j).get(i);
                  min_index = j;
              }
           }
           option.Minimum_col_value = i;
           option.Minimum_row_value = min_index;
           option.Minimum_value = min_value;
           options.add(option);
        }

        if(patch_size > 1) {
            if (options.get(0).Minimum_value < options.get(1).Minimum_value) {
                if(options.get(0).Minimum_value / options.get(1).Minimum_value < 0.8)
                    return options.get(0);
                else
                    return new OPTIONS();
            }
            else if (options.get(0).Minimum_value > options.get(1).Minimum_value){
                if(options.get(1).Minimum_value / options.get(0).Minimum_value < 0.8)
                    return options.get(1);
                else
                    return new OPTIONS();
            }
        }
        else {
            if(options.get(0).Minimum_value / 255 < 0.7)
                return options.get(0);
            else
                return new OPTIONS();
        }
        return new OPTIONS();
    }
    private int minAndis(List<Double> before_means) {
        Double min = before_means.get(0);
        int andis = 0;
        for (int i = 1; i < before_means.size(); i++) {
            if (min > before_means.get(i)) {
                min = before_means.get(i);
                andis = i;
            }
        }
        return andis;
    }

    public String get_letter(int alt_index) throws Exception {
        if (alt_index != -1)
            return Integer.toString(alt_index + 1);
        else
            return "N/A";
    }
    public Mat shadow_removal(Mat img) throws Exception {
        Mat result = new Mat();
        Mat result_norm = new Mat();
        List<Mat> splite_image = new ArrayList<>();
        List<Mat> result_planes = new ArrayList<>();
        List<Mat> result_norm_planes = new ArrayList<>();
        Core.split(img, splite_image);
        for(int i = 0; i < splite_image.size(); i++) {
            Mat dilated = new Mat();
            Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(25, 25));
            Imgproc.dilate(splite_image.get(i), dilated, kernel);
            Imgproc.medianBlur(dilated, dilated, 25);
            Mat diff_img = new Mat();
            Core.absdiff(splite_image.get(i), dilated, diff_img);
            Core.subtract(new MatOfDouble(255), diff_img, diff_img);
            Mat norm_img = new Mat();
            Core.normalize(diff_img, norm_img, 0, 255, Core.NORM_MINMAX);
            result_planes.add(diff_img);
            result_norm_planes.add(norm_img);
        }
        Core.merge(result_planes, result);
        Core.merge(result_norm_planes, result_norm);
        return result_norm;
    }

    public List<String> get_PageInfo() throws Exception {
        /*Run the full pipeline:

        - Load image
        - Convert to grayscale
        - Filter out high frequencies with a Gaussian kernel
        - Apply threshold
        - Find contours
        - Find corners among all contours
        - Find 'outmost' points of all corners
        - Apply perpsective transform to get a bird's eye view
        - Scan each line for the marked answer*/

        setLogging(true);
        //Mat destination = new Mat(source.rows(), source.cols(), source.type());
        source.convertTo(source, -1, alpha, beta);
        Mat Resized_source = new Mat();
        if(isNew) {
            if (isEven) {
                source = source.submat((int) (0.08 * source.rows()), (int) (0.88 * source.rows()), (int) (0.8 * source.cols()), source.cols());

            } else {
                source = source.submat((int) (0.08 * source.rows()), (int) (0.88 * source.rows()), 5, (int) (0.2 * source.cols()));
            }
        }
        else{
            if (isEven) {
                source = source.submat((int) (0.1 * source.rows()), (int) (0.9 * source.rows()), (int) (0.8 * source.cols()), source.cols());
            } else {
                source = source.submat((int) (0.1 * source.rows()), (int) (0.9 * source.rows()), 5, (int) (0.2 * source.cols()));
            }
        }
        Imgcodecs.imwrite(getSource("croped_source.jpg"), source);
        Imgproc.resize(source, Resized_source, new Size(250, 1394));
        Mat Shadow_Removed = new Mat();
        Shadow_Removed = shadow_removal(Resized_source);
        Imgcodecs.imwrite(getSource("Shadow_Removed.jpg"), Shadow_Removed);
        List<MatOfPoint> corners = get_corners(Shadow_Removed);
        List<Point> outmost = order_points(get_outmost_points(corners));

        Mat transf = perspective_transform(Shadow_Removed, outmost);
        Imgcodecs.imwrite(getSource("transf.jpg"), transf);
        Imgproc.threshold(transf, transf, 200, 255, Imgproc.THRESH_BINARY);
        Imgcodecs.imwrite(getSource("transf_threshold.jpg"), transf);
//        List<String> day_answers = new ArrayList<>();
//        List<String> month_answers = new ArrayList<>();
//        List<String> category_answers = new ArrayList<>();

        List<List<Double>> patches = new LinkedList<>();
        for (int i = 0; i < get_day_patches(transf).length; i++)
            patches.add(get_marked_alternative(get_alternative_day_patches(get_day_patches(transf)[i])));

        OPTIONS day_alt_index = get_alt_index(patches, 2);
//        if (day_alt_index != -1)
//            draw_marked_day_alternative(get_day_patches(transf)[i], day_alt_index);
//        day_answers.add(get_letter(day_alt_index.Minimum_col_value));
        patches.clear();

        for (int i = 0; i < get_month_patches(transf).length; i++)
            patches.add(get_marked_alternative(get_alternative_month_patches(get_month_patches(transf)[i])));

        OPTIONS month_alt_index = get_alt_index(patches, 1);
//        if (month_alt_index != -1)
//            draw_marked_day_alternative(get_month_patches(transf)[i], month_alt_index);
//        month_answers.add(get_letter(month_alt_index.Minimum_col_value));
        patches.clear();

        for (int i = 0; i < get_category_patches(transf).length; i++)
            patches.add(get_marked_alternative(get_alternative_category_patches(get_category_patches(transf)[i])));

        OPTIONS category_alt_index = get_alt_index(patches, 2);
//        if (category_alt_index != -1)
//            draw_marked_category_alternative(get_day_patches(transf)[i], category_alt_index);
//        category_answers.add(get_letter(category_alt_index.Minimum_col_value));
        patches.clear();


        List<String> Results = new ArrayList<>();
        String day = "", month = "", category_name = "";

        if (!get_letter(day_alt_index.Minimum_col_value).equalsIgnoreCase("N/A")) {
            if (get_letter(day_alt_index.Minimum_col_value).equalsIgnoreCase("1"))
                day = Integer.toString((day_alt_index.Minimum_row_value * 2) + 2);
            else if (get_letter(day_alt_index.Minimum_col_value).equalsIgnoreCase("2"))
                day = Integer.toString((day_alt_index.Minimum_row_value * 2) + 1);
        } else
            day = Integer.toString(0);

        if (!get_letter(month_alt_index.Minimum_col_value).equalsIgnoreCase("N/A")) {
            month = Integer.toString(month_alt_index.Minimum_row_value + 1);
        } else
            month = Integer.toString(0);

        if (!get_letter(category_alt_index.Minimum_col_value).equalsIgnoreCase("N/A")) {
            if (get_letter(category_alt_index.Minimum_col_value).equalsIgnoreCase("1"))
                category_name = Integer.toString((category_alt_index.Minimum_row_value * 2) + 2);
            else if (get_letter(category_alt_index.Minimum_col_value).equalsIgnoreCase("2"))
                category_name = Integer.toString((category_alt_index.Minimum_row_value * 2) + 1);
        } else
            category_name = Integer.toString(0);

        Imgcodecs.imwrite(getSource("Output.jpg"), transf);
        Results.add(month);
        Results.add(day);
        Results.add(category_name);
        return Results;
    }
}
