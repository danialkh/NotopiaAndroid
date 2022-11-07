package ir.notopia.android.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import ir.notopia.android.database.entity.AttachEntity;

@Dao
public interface AttachDao {

    
    
    @Update
    void updateAttach(AttachEntity... AttachEntities);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAttach(AttachEntity AttachEntity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<AttachEntity> Attachs);

    @Delete
    void deleteAttach(AttachEntity AttachEntity);

    @Query("SELECT * FROM Attachs WHERE AttachId = :id")
    AttachEntity getAttachById(int id);

    @Query("SELECT * FROM Attachs order by AttachId")
    List<AttachEntity> getAllAttachs();

    @Query("DELETE FROM Attachs")
    int deleteAll();

    @Query("SELECT COUNT(*) FROM Attachs")
    int getCount();

    @Query("SELECT COUNT(*) FROM Attachs WHERE AttachId = :id")
    int checkAttachExist(int id);

    @Query("SELECT * FROM Attachs WHERE notonId = :id")
    List<AttachEntity> getAttachsByNotonId(int id);


    @Query("SELECT COUNT(*) FROM Attachs WHERE path = :path")
    int countAttachsByFilePath(String path);
}
