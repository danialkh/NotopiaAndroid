package ir.notopia.android;

import android.content.Context;
import android.util.Log;

import java.util.List;

import ir.notopia.android.database.AppRepository;
import ir.notopia.android.database.entity.CategoryEntity;
import ir.notopia.android.database.entity.TagEntity;

public class FirstCategoryInit {
    public FirstCategoryInit(Context context) {

        AppRepository mRepository = AppRepository.getInstance(context);
        List<TagEntity> mTags = mRepository.getAllTags();
        Log.d("CheckCategorys",mTags.toString());

        if(mTags.size() == 0) {
            for(int i = 1;i <= 6;i++){
                String matn = "دسته"  + " " + String.valueOf(i) ;
                CategoryEntity category = new CategoryEntity(matn);
                mRepository.insertCategory(category);
            }
        }

    }
}
