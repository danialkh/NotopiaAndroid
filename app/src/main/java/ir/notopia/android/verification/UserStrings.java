package ir.notopia.android.verification;

import com.google.gson.annotations.SerializedName;

public class UserStrings {

    @SerializedName("state")
    private String state;

    @SerializedName("status")
    private String status;

    @SerializedName("name")
    private String name;

    @SerializedName("family")
    private String family;

    @SerializedName("BearerToken")
    private String BearerToken;


    public String getBearerToken() {
        return BearerToken;
    }

    public String getState() {
        return state;
    }

    public String getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

    public String getFamily() {
        return family;
    }
}