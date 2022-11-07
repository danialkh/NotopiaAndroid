package ir.notopia.android.database.dao;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import ir.notopia.android.database.entity.CategoryEntity;

@Dao
public interface CategoryDao {

    @Update
    void updateCategory(CategoryEntity... categoryEntity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCategory(CategoryEntity CategoryEntity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<CategoryEntity> Categorys);

    @Query("SELECT * FROM CategoryTable ORDER BY id DESC")
    List<CategoryEntity> getAll();

    @Query("SELECT * FROM CategoryTable WHERE id = :ctgId")
    CategoryEntity getCategoryById(Integer ctgId);

    @Query("DELETE FROM CategoryTable")
    int deleteAll();

}

