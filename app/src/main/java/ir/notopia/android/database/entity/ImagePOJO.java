package ir.notopia.android.database.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ImagePOJO {

    @SerializedName("image")
    @Expose
    private String image;

    public ImagePOJO(String image) {
        this.image = image;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

}
