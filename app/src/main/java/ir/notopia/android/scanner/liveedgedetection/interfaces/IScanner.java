package ir.notopia.android.scanner.liveedgedetection.interfaces;

import android.graphics.Bitmap;

import ir.notopia.android.scanner.liveedgedetection.enums.ScanHint;

/**
 * Interface between activity and surface view
 */

public interface IScanner {
    void displayHint(ScanHint scanHint);

    void onPictureClicked(Bitmap bitmap);
}
