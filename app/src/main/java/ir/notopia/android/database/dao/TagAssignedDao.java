package ir.notopia.android.database.dao;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import ir.notopia.android.database.entity.TagAssignedEntity;

@Dao
public interface TagAssignedDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAssinedTag(TagAssignedEntity tagAssignedEntity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<TagAssignedEntity> Tags);

    @Delete
    void deleteTag(TagAssignedEntity TagAssignedEntity);

    @Query("SELECT * FROM tagsAssigned WHERE notonId = :id")
    List<TagAssignedEntity> getAssignedTagByScanId(int id);

    @Query("SELECT * FROM tagsAssigned WHERE tagId = :id")
    TagAssignedEntity getAssignedTagByTagId(int id);

    @Query("SELECT * FROM tagsAssigned")
    List<TagAssignedEntity> getAllAssignedTags();

    @Query("DELETE FROM tagsAssigned")
    int deleteAll();

    @Query("DELETE FROM tagsAssigned WHERE notonId = :id")
    void deleteAssign(int id);

    @Query("DELETE FROM tagsAssigned WHERE tagId = :id")
    void deleteAssignByTagId(int id);

    @Query("SELECT COUNT(*) FROM tagsAssigned")
    int getCount();
}
