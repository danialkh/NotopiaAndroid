package ir.notopia.android.database.dao;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import ir.notopia.android.database.entity.NoteEntity;

@Dao
public interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertNote(NoteEntity noteEntity);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<NoteEntity> notes);

    @Delete
    void deleteNote(NoteEntity noteEntity);

    @Query("SELECT * FROM notes WHERE noteId = :id")
    NoteEntity getNoteById(int id);

    @Query("SELECT * FROM notes WHERE dayId = :dayId ORDER BY date DESC")
    List<NoteEntity> getNotesByDayId(String dayId);

    @Query("SELECT * FROM notes ORDER BY date DESC")
    List<NoteEntity> getAll();

    @Query("DELETE FROM notes")
    int deleteAll();

    @Query("SELECT COUNT(*) FROM notes")
    int getCount();
}
