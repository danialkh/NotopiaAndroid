package ir.notopia.android.database.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "tagsAssigned")
public class TagAssignedEntity {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    private int tagAssignedId;
    private int tagId;
    private int notonId;

    public void setTagAssignedId(int tagAssignedId) {
        this.tagAssignedId = tagAssignedId;
    }

    public void setTagId(int tagId) {
        this.tagId = tagId;
    }

    public void setNotonId(int notonId) {
        this.notonId = notonId;
    }

    public int getTagAssignedId() {
        return tagAssignedId;
    }

    public int getTagId() {
        return tagId;
    }

    public int getNotonId() {
        return notonId;
    }


    @Override
    public String toString() {
        return "TagAssignedEntity{" +
                "tagAssignedId=" + tagAssignedId +
                ", tagId=" + tagId +
                ", notonId=" + notonId +
                '}';
    }

    public TagAssignedEntity(int tagId, int notonId) {
        this.tagId = tagId;
        this.notonId = notonId;
    }

    @Ignore
    public TagAssignedEntity() {
    }




}
