package ir.notopia.android.database.dao;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import ir.notopia.android.database.entity.TagEntity;

@Dao
public interface TagDao {

    @Update
    void updateTag(TagEntity... tagEntities);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertTag(TagEntity TagEntity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<TagEntity> Tags);

    @Delete
    void deleteTag(TagEntity TagEntity);

    @Query("SELECT * FROM Tags WHERE TagId = :id")
    TagEntity getTagById(int id);

    @Query("SELECT * FROM Tags WHERE TagName = :tagName")
    TagEntity getTagByName(String tagName);

    @Query("SELECT * FROM Tags order by TagId")
    List<TagEntity> getAllTags();

    @Query("DELETE FROM Tags")
    int deleteAll();

    @Query("SELECT COUNT(*) FROM Tags")
    int getCount();

    @Query("SELECT COUNT(*) FROM Tags WHERE TagId = :id")
    int checkTagExist(int id);

    @Query("SELECT * FROM Tags WHERE tagName LIKE  '%' || :searchStr || '%'")
    List<TagEntity> findByName(String searchStr);


}
