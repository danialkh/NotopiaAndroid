package ir.notopia.android.database;

import java.util.Date;

import androidx.room.TypeConverter;

public class DateConverter {

//    @TypeConverter
//    public static Date toDate(Long time) {
//        //2018-12-04T11:29:57.360Z
//        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",Locale.US);
//        Date date = null;
//        try {
//            date = inputFormat.parse(time.toString());
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//        return date;
//    }

    @TypeConverter
    public static Date toDate(Long timestamp) {
        return timestamp == null ? null : new Date(timestamp);
    }

    @TypeConverter
    public static Long toTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}
