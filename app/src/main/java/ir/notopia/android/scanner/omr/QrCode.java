package ir.notopia.android.scanner.omr;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.io.FileNotFoundException;
import java.io.InputStream;

import ir.notopia.android.verification.VerifyPageDecoder;

public class QrCode {

    private static final String TAG = "QrCode";
    private final String mImage;
    private String contents;

    private String MahsolType;
    private String MahsolZirno;
    private String MahsolYear;
    private String MahsolSeri;
    private String MahsolVizhegi;
    private String pageNumber;
    private boolean isEven;
    private boolean isNew;
    private Context mContext;

    public QrCode(String image, Context mContext) {
        this.mImage = image;
        this.mContext = mContext;
    }

    public QrCode(String qrText) {
        this.contents = qrText;
        this.mImage = null;
    }

    public boolean isEven() {
        return isEven;
    }

    public boolean isNew() {
        return isNew;
    }

    public String getContents() {
        return contents;
    }

    public String getMahsolYear() {
        return MahsolYear;
    }

    public String getMahsolType() {
        return MahsolType;
    }

    public String getMahsolVizhegi() {
        return MahsolVizhegi;
    }

    public String getPageNumber() {
        return pageNumber;
    }

    public String getMahsolZirno() { return MahsolZirno; }

    public String getMahsolSeri() { return MahsolSeri; }

    public void checkQr() {
        Uri uri = Uri.parse("file://" + mImage);
        Bitmap bMap = null;
//        try {
//            bMap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), uri);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        InputStream inputStream = null;
        try {
            inputStream = mContext.getContentResolver().openInputStream(uri);
            bMap = BitmapFactory.decodeStream(inputStream);
//            Log.e(TAG, "uri is not a bitmap," + uri.toString());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
//        Bitmap bMap = BitmapFactory.decodeStream(image);
        if (bMap != null) {

            int[] intArray = new int[bMap.getWidth() * bMap.getHeight()];
//copy pixel data from the Bitmap into the 'intArray' array
            bMap.getPixels(intArray, 0, bMap.getWidth(), 0, 0, bMap.getWidth(), bMap.getHeight());
            Log.d(TAG, "QrCode: " + bMap.getWidth() + " " + bMap.getHeight());
            LuminanceSource source = new RGBLuminanceSource(bMap.getWidth(), bMap.getHeight(), intArray);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            Reader reader = new MultiFormatReader();
            Result result = null;

            try {
                result = reader.decode(bitmap);
                contents = result.getText();
                Log.i("Result", contents);
            }
            catch (NotFoundException e) {
                Log.d(TAG, "Code Not Found");
                e.printStackTrace();
            } catch (FormatException | ChecksumException e) {
                e.printStackTrace();
            }
            if (result != null) {
                contents = result.getText();


                if(contents.length() == 25) {

                    //String contents = "https://9ot.ir/c/2OhcqZzo";
                    contents = contents.substring(17, 25);
                    contents = VerifyPageDecoder.DecodePageCode(contents);
                    //String contents = "0101001001002";

                    MahsolType = contents.substring(0, 2);
                    MahsolZirno = contents.substring(2, 4);
                    MahsolYear = contents.substring(4, 5);
                    MahsolSeri = contents.substring(5, 7);
                    MahsolVizhegi = contents.substring(7, 10);
                    pageNumber = contents.substring(10, 13);
                    if(Integer.parseInt(pageNumber) % 2 == 0)
                        isEven = false;
                    else
                        isEven = true;

                    isNew = true;
                }
                else{
                    //        String qrOutPut="99p01m01n004z2";
                    //        System.out.println(qrOutPut.substring(0,2));
                    //returns 99 year or product series
                    MahsolYear = contents.substring(0, 2);
                    //        System.out.println(qrOutPut.substring(2,5));
                    //returns P01 page type
                    MahsolType = contents.substring(2, 5);
                    //        System.out.println(qrOutPut.substring(5,8));
                    //returns m01 product type
                    MahsolVizhegi = contents.substring(5, 8);
                    //        System.out.println(qrOutPut.substring(8,12));
                    //returns n004 page number
                    pageNumber = contents.substring(8, 12);
                    //        System.out.println(qrOutPut.substring(12,14));

                    //returns z1 odd or Z2 even
                    String oddEven = contents.substring(12, 14);
                    if (oddEven.equals("z1")) {
                        //oddEven = "odd";
                        isEven = false;
                    } else if (oddEven.equals("z2")) {
                        //oddEven = "even";
                        isEven = true;
                    }

                    isNew = false;
                }
            }
        }
    }

    public void calc(){
        if (contents != null) {

            if(contents.length() == 25) {



                //String contents = "https://9ot.ir/c/2OhcqZzo";
                contents = contents.substring(17, 25);
                contents = VerifyPageDecoder.DecodePageCode(contents);
                //String contents = "0101001001002";
                MahsolType = contents.substring(0, 2);
                MahsolZirno = contents.substring(2, 4);
                MahsolYear = contents.substring(4, 5);
                MahsolSeri = contents.substring(5, 7);
                MahsolVizhegi = contents.substring(7, 10);
                pageNumber = contents.substring(10, 13);
                if(Integer.parseInt(pageNumber) % 2 == 0)
                    isEven = false;
                else
                    isEven = true;

                isNew = true;
            }
            else {

                //        String qrOutPut="99p01m01n004z2";
                //        System.out.println(qrOutPut.substring(0,2));
                //returns 99 year or product series
                MahsolYear = contents.substring(0, 2);
                //        System.out.println(qrOutPut.substring(2,5));
                //returns P01 page type
                MahsolType = contents.substring(2, 5);
                //        System.out.println(qrOutPut.substring(5,8));
                //returns m01 product type
                MahsolVizhegi = contents.substring(5, 8);
                //        System.out.println(qrOutPut.substring(8,12));
                //returns n004 page number
                pageNumber = contents.substring(8, 12);
                //        System.out.println(qrOutPut.substring(12,14));

                MahsolZirno = "01";
                MahsolSeri = "01";


                //returns z1 odd or Z2 even
                String oddEven = contents.substring(12, 14);
                if (oddEven.equals("z1")) {
//                oddEven = "odd";
                    isEven = false;
                } else if (oddEven.equals("z2")) {
//                oddEven = "even";
                    isEven = true;
                }

                isNew = false;
            }
        }
    }
}
