package ir.notopia.android.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import ir.notopia.android.database.dao.ArDao;
import ir.notopia.android.database.dao.AttachDao;
import ir.notopia.android.database.dao.CategoryDao;
import ir.notopia.android.database.dao.NoteDao;
import ir.notopia.android.database.dao.ResponseDao;
import ir.notopia.android.database.dao.ScanDao;
import ir.notopia.android.database.dao.TagAssignedDao;
import ir.notopia.android.database.dao.TagDao;
import ir.notopia.android.database.entity.ArEntity;
import ir.notopia.android.database.entity.AttachEntity;
import ir.notopia.android.database.entity.CategoryEntity;
import ir.notopia.android.database.entity.NoteEntity;
import ir.notopia.android.database.entity.NotificationResponse;
import ir.notopia.android.database.entity.ScanEntity;
import ir.notopia.android.database.entity.TagAssignedEntity;
import ir.notopia.android.database.entity.TagEntity;

@Database(entities = {NotificationResponse.class, NoteEntity.class, ScanEntity.class, ArEntity.class, CategoryEntity.class, TagEntity.class, TagAssignedEntity.class, AttachEntity.class}, version = 1, exportSchema = false)
@TypeConverters({DateConverter.class,ArrayConverter.class})
public abstract class AppDatabase extends RoomDatabase {
    public static final String DATABASE_NAME = "Database.db";
    public static final Object LOCK = new Object();
    private static volatile AppDatabase instance;

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (LOCK) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, DATABASE_NAME)
                            .allowMainThreadQueries()
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }

    public abstract ResponseDao responseDao();

    public abstract NoteDao noteDao();

    public abstract ScanDao scanDao();

    public abstract ArDao arDao();

    public abstract CategoryDao CategoryDao();

    public abstract TagDao TagDao();

    public abstract TagAssignedDao TagAssignedDao();

    public abstract AttachDao AttachDao();

}
