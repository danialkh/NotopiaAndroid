package ir.notopia.android.database.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import ir.notopia.android.database.entity.ScanEntity;

@Dao
public interface ScanDao {

    @Query("SELECT * FROM scans")
    List<ScanEntity> getAll();

    @Query("SELECT * FROM scans WHERE scan_id IN (:scanIds)")
    List<ScanEntity> loadAllByIds(int[] scanIds);

    @Query("SELECT * FROM scans WHERE scan_id = :scanId ")
    ScanEntity findById(int scanId);


    @Query("SELECT * FROM scans WHERE text LIKE  '%' || :text || '%' OR title LIKE  '%' || :text || '%'")
    List<ScanEntity> findByText(String text);


    @Query("SELECT * FROM scans WHERE category = :category ")
    List<ScanEntity> findByCategory(String category);

    @Query("SELECT * FROM scans WHERE (day = :day & month = :month & year = :year) ")
    List<ScanEntity> findByDate(String day, String month, String year);

    @Query("SELECT * FROM scans WHERE (day = :day & month = :month & year = :year & category = :category) ")
    List<ScanEntity> findByDateAndCategory(String day, String month, String year, String category);

    @Query("SELECT * FROM scans WHERE image = :image ")
    ScanEntity findByImage(String image);

    @Insert
    void insertAll(ScanEntity... scan);

    @Update
    void updateScan(ScanEntity... scans);

    @Delete
    void delete(ScanEntity scan);

    @Query("DELETE FROM scans WHERE image = :filepath")
    void deleteByImage(String filepath);


    @Query("SELECT COUNT(*) FROM scans WHERE image = :path")
    int countScansByFilePath(String path);

}
