package ir.notopia.android.database.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ImageAI {

    @SerializedName("barcode")
    @Expose
    private String barcode;
    @SerializedName("image")
    @Expose
    private String image;
    @SerializedName("success")
    @Expose
    private boolean success;

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    @Override
    public String toString() {
        return "ImageAI{" +
                "barcode=" + barcode +
                ", success=" + success +
                ", image='" + image + '\'' +
                '}';
    }
}
