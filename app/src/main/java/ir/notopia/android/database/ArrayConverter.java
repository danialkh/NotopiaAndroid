package ir.notopia.android.database;

import androidx.room.TypeConverter;

import java.util.ArrayList;

public class ArrayConverter {
    @TypeConverter
    public String fromArray(ArrayList<String> strings) {
        String string = "";
        for (String s : strings) string += (s + ",");

        return string;
    }

    @TypeConverter
    public ArrayList<String> toArray(String concatenatedStrings) {
        ArrayList<String> myStrings = new ArrayList<>();

        for (String s : concatenatedStrings.split(",")) myStrings.add(s);

        return myStrings;
    }
}
