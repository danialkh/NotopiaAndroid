package ir.notopia.android.adapter;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.dragselectrecyclerview.DragSelectRecyclerViewAdapter;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import ir.notopia.android.MainActivity;
import ir.notopia.android.R;
import ir.notopia.android.database.AppRepository;
import ir.notopia.android.database.entity.TagEntity;

public class TagsAdaptor extends DragSelectRecyclerViewAdapter<TagsAdaptor.TagsViewHolder> {
    private static final String TAG = "TagsAdaptor";
    public static int myPosition = -1;
    private List<TagEntity> mTags;
    private AppRepository mRepository;
    private Activity _activity;
    private String searchStr;

    public TagsAdaptor(Activity _activity,String searchStr) {
        AppRepository mRepository = AppRepository.getInstance(_activity.getApplicationContext());

        this._activity = _activity;
        this.searchStr = searchStr;
        this.mTags = mRepository.getAllTagsFromNameLike(searchStr);



    }

    @NonNull
    @Override
    public TagsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_tag_filter, parent, false);
        TagsViewHolder pvh = new TagsViewHolder(v,_activity,mTags);
        return pvh;
    }

    @Override
    public void onBindViewHolder(@NonNull TagsViewHolder holder, int position) {
        TagEntity tag = mTags.get(position);
        holder.tvFilterTag.setText(tag.getTagName());

        Log.d(TAG,"ClickedPos:" + String.valueOf(myPosition));
        Log.d(TAG,"thisPos:" + String.valueOf(position));



        Drawable whiteBack = _activity.getResources().getDrawable(R.drawable.vc_back_item_hashtag,_activity.getApplicationContext().getTheme());
        Drawable yelloBack = _activity.getResources().getDrawable(R.drawable.vc_back_item_selected_hashtag,_activity.getApplicationContext().getTheme());


        if(myPosition == position){
            holder.IVback.setImageDrawable(yelloBack);
        }
        else{
            holder.IVback.setImageDrawable(whiteBack);
        }
    }

    @Override
    public int getItemCount() {
        return mTags.size();
    }

    public static class TagsViewHolder extends RecyclerView.ViewHolder{

        TextView tvFilterTag;
        ImageView IVback;

        public TagsViewHolder(@NonNull View itemView,Activity _activity,List<TagEntity> mTags) {
            super(itemView);
            tvFilterTag = itemView.findViewById(R.id.TVFilterTag);
            IVback = itemView.findViewById(R.id.back_item_tag);

//            tagsAdaptor.ids.add(id);

            IVback.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int pos = getAdapterPosition();
                    Log.d(TAG,String.valueOf(pos));
                    TagEntity tag = mTags.get(pos);

                    if(myPosition == pos){
                        // refresh and color no one
                        myPosition = -1;
                        MainActivity.reloadTag(_activity,tag.getTagId(),false);
                    }
                    else{
                        // refresh and color this pos
                        myPosition = pos;
                        MainActivity.reloadTag(_activity,tag.getTagId(),true);
                    }
                }
            });
        }
    }
}
