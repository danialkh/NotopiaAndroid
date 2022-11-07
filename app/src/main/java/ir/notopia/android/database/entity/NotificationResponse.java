package ir.notopia.android.database.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Date;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;


@Entity(tableName = "notifications")
public class NotificationResponse {

    @PrimaryKey(autoGenerate = false)
    @NonNull
    @SerializedName("_id")
    @Expose
    private String id;
    @ColumnInfo
    @SerializedName("active")
    @Expose
    private boolean active;
    @ColumnInfo
    @SerializedName("title")
    @Expose
    private String title;
    @ColumnInfo
    @SerializedName("for_product")
    @Expose
    private String forProduct;
    @ColumnInfo
    @SerializedName("description")
    @Expose
    private String description;
    @ColumnInfo
    @SerializedName("created_date")
    @Expose
    private Date createdDate;
    @ColumnInfo
    @SerializedName("created_user")
    @Expose
    private String createdUser;
    @ColumnInfo
    @SerializedName("createdAt")
    @Expose
    private Date createdAt;
    @ColumnInfo
    @SerializedName("updatedAt")
    @Expose
    private Date updatedAt;
    @ColumnInfo
    @SerializedName("__v")
    @Expose
    private int v;


    public NotificationResponse(@NonNull String id, boolean active, String title, String forProduct, String description, Date createdDate, String createdUser, Date createdAt, Date updatedAt, int v) {
        this.id = id;
        this.active = active;
        this.title = title;
        this.forProduct = forProduct;
        this.description = description;
        this.createdDate = createdDate;
        this.createdUser = createdUser;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.v = v;
    }

    @Ignore
    public NotificationResponse(@NonNull String id, String title, String description) {
        this.id = id;
        this.title = title;
        this.description = description;
    }

    @Ignore
    public NotificationResponse() {

    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getForProduct() {
        return forProduct;
    }

    public void setForProduct(String forProduct) {
        this.forProduct = forProduct;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getCreatedUser() {
        return createdUser;
    }

    public void setCreatedUser(String createdUser) {
        this.createdUser = createdUser;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public int getV() {
        return v;
    }

    public void setV(int v) {
        this.v = v;
    }


    @Override
    public String toString() {
        return "NotificationResponse{" +
                "id='" + id + '\'' +
                ", active=" + active +
                ", title='" + title + '\'' +
                ", forProduct='" + forProduct + '\'' +
                ", description='" + description + '\'' +
                ", createdDate=" + createdDate +
                ", createdUser='" + createdUser + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", v=" + v +
                '}';
    }
}