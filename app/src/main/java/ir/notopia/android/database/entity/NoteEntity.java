package ir.notopia.android.database.entity;

import java.util.Date;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "notes")
public class NoteEntity {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    private int noteId;
    private String description;
    private String noteType;
    private Date date;
    private String dayId;
    private String mediaUri;
    private String text;
//    private ArrayList<String> hashTags = new ArrayList<>();


    @Ignore
    public NoteEntity(String description, String noteType, Date date, String dayId, String mediaUri,String text) {
        this.description = description;
        this.noteType = noteType;
        this.date = date;
        this.dayId = dayId;
        this.mediaUri = mediaUri;
        this.text = text;
    }

    @Ignore
    public NoteEntity() {

    }

    public NoteEntity(int noteId, String description, String noteType, Date date, String dayId, String mediaUri,String text) {

        this.noteId = noteId;
        this.description = description;
        this.noteType = noteType;
        this.date = date;
        this.dayId = dayId;
        this.mediaUri = mediaUri;
        this.text = text;
    }

    public int getNoteId() {
        return noteId;
    }

    public void setNoteId(int noteId) {
        this.noteId = noteId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNoteType() {
        return noteType;
    }

    public void setNoteType(String noteType) {
        this.noteType = noteType;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getDayId() {
        return dayId;
    }

    public void setDayId(String dayId) {
        this.dayId = dayId;
    }

    public String getMediaUri() {
        return mediaUri;
    }

    public String getText(){return text;}

    public  void setText(){this.text = text;}

    public void setMediaUri(String mediaUri) {
        this.mediaUri = mediaUri;
    }

    @Override
    public String toString() {
        return "NoteEntity{" +
                "noteId=" + noteId +
                ", description='" + description + '\'' +
                ", noteType='" + noteType + '\'' +
                ", date=" + date +
                ", dayId='" + dayId + '\'' +
                ", mediaUri='" + mediaUri + '\'' +
                ", text='" + text + '\'' +
                '}';
    }
}
