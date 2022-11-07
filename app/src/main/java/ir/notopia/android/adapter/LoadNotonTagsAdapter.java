package ir.notopia.android.adapter;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.dragselectrecyclerview.DragSelectRecyclerViewAdapter;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import ir.notopia.android.R;
import ir.notopia.android.database.AppRepository;
import ir.notopia.android.database.entity.ScanEntity;
import ir.notopia.android.database.entity.TagAssignedEntity;
import ir.notopia.android.database.entity.TagEntity;

public class LoadNotonTagsAdapter extends DragSelectRecyclerViewAdapter<LoadNotonTagsAdapter.ThumbViewHolder> {

    private static final String TAG = "LoadNotonTagsAdapter";
    private List<TagAssignedEntity> assignedList;
    private AppRepository mRepository;

    public LoadNotonTagsAdapter(ScanEntity mScan,Activity activity) {

        mRepository = AppRepository.getInstance(activity);
        assignedList = mRepository.getAllAssignedTagsFromNoton(mScan);

    }


    @Override
    public int getItemCount() {
        return assignedList.size();
    }


    @Override
    public void onBindViewHolder(LoadNotonTagsAdapter.ThumbViewHolder holder, int position) {
        super.onBindViewHolder(holder, position); // this line is important!
        int TagId = assignedList.get(position).getTagId();

        Log.d(TAG,"TagId:" + TagId);

        TagEntity tagEntity = mRepository.getTagById(TagId);
        Log.d(TAG,"Tag:" + tagEntity.toString());
        String tagName = tagEntity.getTagName();

        Log.d(TAG,"tagName:" + tagName);
        holder.TVTagName.setText(tagName);
    }

        @Override
    public ThumbViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_tag, parent, false);
        return new ThumbViewHolder(v);
    }

    public class ThumbViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private TextView TVTagName;

        public ThumbViewHolder(View itemView) {
            super(itemView);
            this.TVTagName = itemView.findViewById(R.id.TVTagName);
        }

        @Override
        public void onClick(View v) {
            // Forwards to the adapter's constructor callback


        }


    }
}


