package ir.notopia.android.database.dao;


import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import ir.notopia.android.database.entity.NotificationResponse;

@Dao
public interface ResponseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertResponse(NotificationResponse... notificationResponse);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<NotificationResponse> respons);

    @Delete
    void deleteResponse(NotificationResponse notificationResponse);

    @Query("SELECT * FROM notifications WHERE id = :id")
    NotificationResponse getResponseById(String id);

    @Query("SELECT * FROM notifications ORDER BY updatedAt DESC ")
    List<NotificationResponse> getAll();
//    List<NotificationResponse> getAll();

    @Query("DELETE FROM notifications")
    int deleteAll();

    @Query("SELECT COUNT(*) FROM notifications")
    int getCount();

    @Query("SELECT COUNT(*) FROM notifications WHERE active = 1 ")
    int getNewCount();
}
