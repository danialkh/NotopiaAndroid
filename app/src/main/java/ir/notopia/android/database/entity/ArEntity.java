package ir.notopia.android.database.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "ArTable")
public class ArEntity {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    private int id;
    private String arId;
    private String type;
    private String target;
    private String tracker;

    public ArEntity(String arId,String type, String target, String tracker) {
        this.arId = arId;
        this.type = type;
        this.target = target;
        this.tracker = tracker;
    }


    public String getArId() {
        return arId;
    }

    public void setArId(String arId) {
        this.arId = arId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getTracker() {
        return tracker;
    }

    public void setTracker(String tracker) {
        this.tracker = tracker;
    }

    @Override
    public String toString() {
        return "ArEntity{" +
                "ArId=" + arId +
                ", type='" + type + '\'' +
                ", target='" + target + '\'' +
                ", tracker=" + tracker +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
