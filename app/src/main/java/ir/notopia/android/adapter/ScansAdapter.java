package ir.notopia.android.adapter;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ir.notopia.android.R;
import ir.notopia.android.database.AppRepository;
import ir.notopia.android.database.entity.ScanEntity;

public class ScansAdapter extends RecyclerView.Adapter<ScansAdapter.ScanViewHolder> {
    private static final String TAG = "ScanAdapter";
    private List<ScanEntity> mScans;
    private OnScanListener mOnScanListener;
    private AppRepository mRepository;

    public ScansAdapter(List<ScanEntity> scans, OnScanListener onScanListener, AppRepository repository) {
        this.mScans = scans;
        this.mOnScanListener = onScanListener;
        this.mRepository = repository;
    }

    @NonNull
    @Override
    public ScanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.scan_list, parent, false);
        ScanViewHolder pvh = new ScanViewHolder(v, mOnScanListener);
        return pvh;
    }

    @Override
    public void onBindViewHolder(@NonNull ScanViewHolder holder, int position) {
        String category = "Category: " + mScans.get(position).getCategory();
        String date = "Date: 98/" + mScans.get(position).getMonth() + "/" + mScans.get(position).getDay();
        Uri photoUri = Uri.parse(mScans.get(position).getImage());
        holder.scanCategory.setText(category);
        holder.scanDate.setText(date);
        holder.scanPhoto.setImageURI(photoUri);
    }

    @Override
    public int getItemCount() {
        return mScans.size();
    }

    public void removeItem(int position) {
        Log.i(TAG, "removeItem: " + position + mScans.get(position).getId());
        ScanEntity mScan = mScans.get(position);
        mRepository.deleteScan(mScan);
        mScans.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(ScanEntity item, int position) {
        mScans.add(position, item);
        notifyItemInserted(position);
    }

    public interface OnScanListener {
        void onScanClick(int position);
    }

    public static class ScanViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        CardView cv;
        TextView scanCategory;
        TextView scanDate;
        ImageView scanPhoto;
        OnScanListener onScanListener;

        public ScanViewHolder(@NonNull View itemView, OnScanListener onScanListener) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.cv);
            scanCategory = (TextView) itemView.findViewById(R.id.scan_category);
            scanDate = (TextView) itemView.findViewById(R.id.scan_date);
            scanPhoto = (ImageView) itemView.findViewById(R.id.scan_photo);
            this.onScanListener = onScanListener;
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            onScanListener.onScanClick(getAdapterPosition());
        }
    }

}
