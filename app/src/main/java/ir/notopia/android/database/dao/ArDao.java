package ir.notopia.android.database.dao;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import ir.notopia.android.database.entity.ArEntity;

@Dao
public interface ArDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAr(ArEntity ArEntity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ArEntity> Ars);
    
    @Query("SELECT * FROM ArTable ORDER BY tracker DESC")
    List<ArEntity> getAll();

    @Query("DELETE FROM ArTable")
    int deleteAll();

}
