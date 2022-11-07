package ir.notopia.android.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import ir.notopia.android.R;
import ir.notopia.android.database.entity.NotificationResponse;

public class NotificationsAdapter extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> {
    private final List<NotificationResponse> mRespons;
    private final Context mContext;

    public NotificationsAdapter(List<NotificationResponse> mRespons, Context mContext) {
        this.mRespons = mRespons;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.list_item_notifi, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        final NotificationResponse notificationResponse = mRespons.get(i);
        viewHolder.mNotifiTitle.setText(notificationResponse.getTitle());
        viewHolder.mNotifiContent.setText(notificationResponse.getDescription());
    }

    @Override
    public int getItemCount() {
        return mRespons.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView mNotifiTitle, mNotifiContent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mNotifiTitle = itemView.findViewById(R.id.tV_notifi_title);
            mNotifiContent = itemView.findViewById(R.id.tV_nofiti_content);
        }
    }
}
