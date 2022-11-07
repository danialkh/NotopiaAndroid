package ir.notopia.android.database.entity;


import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "Attachs")
public class AttachEntity {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    private int attachId;
    private String type;
    private String path;
    private String strUri;
    private int notonId;


    public String getStrUri() {
        return strUri;
    }

    public void setStrUri(String strUri) {
        this.strUri = strUri;
    }

    public void setAttachId(int attachId) {
        this.attachId = attachId;
    }

    public void setNotonId(int notonId) {
        this.notonId = notonId;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getAttachId() {
        return attachId;
    }

    public int getNotonId() {
        return notonId;
    }

    public String getType() {
        return type;
    }

    public String getPath() {
        return path;
    }


    @Override
    public String toString() {
        return "AttachEntity{" +
                "attachId=" + attachId +
                ", type='" + type + '\'' +
                ", path='" + path + '\'' +
                ", strUri='" + strUri + '\'' +
                ", notonId=" + notonId +
                '}';
    }

    public AttachEntity(int notonId, String strUri, String type, String path) {

        this.notonId = notonId;
        this.type = type;
        this.path = path;
        this.strUri = strUri;
    }

    @Ignore
    public AttachEntity() {
    }

}
