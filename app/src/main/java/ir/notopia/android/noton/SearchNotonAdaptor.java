package ir.notopia.android.noton;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.dragselectrecyclerview.DragSelectRecyclerViewAdapter;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageSize;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;
import ir.notopia.android.R;
import ir.notopia.android.database.AppRepository;
import ir.notopia.android.database.entity.ScanEntity;
import ir.notopia.android.scanner.opennotescanner.FullScreenViewActivity;

public class SearchNotonAdaptor extends DragSelectRecyclerViewAdapter<SearchNotonAdaptor.ThumbViewHolder> {
    private static final String TAG = "Gallery Adaptor";

    private ImageLoader mImageLoader;
    private ImageSize mTargetSize;
    private List<ScanEntity> mScans;
    List<ScanEntity> itemList;
    private Context context;


    // Constructor takes click listener callback
    public SearchNotonAdaptor(List<ScanEntity> tempMScans, Context activity) {
        super();
        mScans = tempMScans;
        context = activity;
        this.itemList = tempMScans;

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(activity).build();
        mImageLoader = ImageLoader.getInstance();
        mImageLoader.init(config);
        mTargetSize = new ImageSize(220, 220); // result Bitmap will be fit to this size

    }
    @Override
    public SearchNotonAdaptor.ThumbViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.galley_item_new, parent, false);
        return new SearchNotonAdaptor.ThumbViewHolder(v);
    }

    @Override
    public void onBindViewHolder(SearchNotonAdaptor.ThumbViewHolder holder, int position) {
        super.onBindViewHolder(holder, position); // this line is important!

        ScanEntity mScan = itemList.get(position);
        String filename = mScan.getImage();
        Log.i(TAG, "onBindViewHolder: " + filename);

        switch (mScan.getColor()) {
            case 0:
                holder.notonText.setBackgroundColor(context.getResources().getColor(R.color.notonDefaultColor));
                break;
            case 1:
                holder.notonText.setBackgroundColor(context.getResources().getColor(R.color.notonRed));
                break;
            case 2:
                holder.notonText.setBackgroundColor(context.getResources().getColor(R.color.notonGreen));
                break;
            case 3:
                holder.notonText.setBackgroundColor(context.getResources().getColor(R.color.notonBlue));
                break;
            case 4:
                holder.notonText.setBackgroundColor(context.getResources().getColor(R.color.notonPink));
                break;
        }

        AppRepository mRepository = AppRepository.getInstance(context);
        ImageView hasAttach = holder.itemView.findViewById(R.id.IVhasAttach);
        int countAttach = mRepository.getAllAttachsFromNoton(mScan).size();
        if(countAttach > 0){
            hasAttach.setVisibility(View.VISIBLE);
        }
        else{
            hasAttach.setVisibility(View.GONE);
        }

        if (!filename.equals(holder.filename)) {

            // remove previous image
//                holder.image.setImageBitmap(null);
            Log.i(TAG, "onBindViewHolder: " + filename);
            String matn;
            // Load image, decode it to Bitmap and return Bitmap to callback
            File file = new File(filename);
            if(!file.exists()){
                holder.colorSquare.setVisibility(View.GONE);
                holder.notonText.setLines(Integer.MAX_VALUE);

                matn = "<br><br><big><b>" + itemList.get(position).getTitle() + "</b></big><br>" + itemList.get(position).getText();
            }
            else{

                mImageLoader.displayImage("file:///" + filename, holder.image, mTargetSize);
                matn = "<big><b>" + itemList.get(position).getTitle() + "</b></big>";
            }
            mImageLoader.displayImage("file:///" + filename, holder.image, mTargetSize);

            float scale = context.getApplicationContext().getResources().getDisplayMetrics().density;
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();
            int margin6 = (int) ((6 * scale + 0.5f));
            int margin4 = (int) ((4 * scale + 0.5f));
            int margin2 = (int) ((2 * scale + 0.5f));
            if((position + 1)% 3 == 0 && position != 0){
                //sotone akhar
                params.setMargins(margin2,margin6,margin6,0);
            }
            else if((position + 2) % 3 == 0){
                //sotone vasat
                params.setMargins(margin4,margin6,margin4,0);
            }
            else {
                //sotone aval
                params.setMargins(margin6,margin6,margin2,0);
            }
            holder.itemView.setLayoutParams(holder.itemView.getLayoutParams());


            TextView TVItemGallerydate = holder.itemView.findViewById(R.id.TVItemGallerydate);
            ImageView IVItemGalleryId = holder.itemView.findViewById(R.id.IVItemGalleryId);
            int intCategory = Integer.parseInt(itemList.get(position).getCategory());

            Log.d("intCategory:",String.valueOf(intCategory));

            switch (intCategory){
                case 1:
                    IVItemGalleryId.setImageDrawable(context.getResources().getDrawable(R.drawable.vc_icon_daste1));
                    break;
                case 2:
                    IVItemGalleryId.setImageDrawable(context.getResources().getDrawable(R.drawable.vc_icon_daste2));
                    break;
                case 3:
                    IVItemGalleryId.setImageDrawable(context.getResources().getDrawable(R.drawable.vc_icon_daste3));
                    break;
                case 4:
                    IVItemGalleryId.setImageDrawable(context.getResources().getDrawable(R.drawable.vc_icon_daste4));
                    break;
                case 5:
                    IVItemGalleryId.setImageDrawable(context.getResources().getDrawable(R.drawable.vc_icon_daste5));
                    break;
                case 6:
                    IVItemGalleryId.setImageDrawable(context.getResources().getDrawable(R.drawable.vc_icon_daste6));
                    break;
                default:
                    IVItemGalleryId.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_error_category));
                    break;
            }

            TVItemGallerydate.setText(itemList.get(position).getMonth() + " / " + itemList.get(position).getDay());


            //holder load text of noton
            holder.notonText.setText(Html.fromHtml(matn));


            // holder.image.setImageBitmap(decodeSampledBitmapFromUri(filename, 220, 220));
            Log.d(TAG, "Gallery Error: " + itemList.get(position).isNeedEdit());
            if (itemList.get(position).isNeedEdit()) {
                holder.error.setVisibility(View.VISIBLE);
//                    holder.colorSquare.setBackgroundResource(R.color.red);

            } else {
                holder.error.setVisibility(View.GONE);
//                    holder.colorSquare.setBackgroundResource(R.color.colorPrimary);
            }
            holder.filename = filename;
        }

    }



    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public ArrayList<String> getSelectedFiles() {

        ArrayList<String> selection = new ArrayList<>();

        for (Integer i : getSelectedIndices()) {
            selection.add(itemList.get(i).getImage());
        }

        return selection;
    }


    public class ThumbViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        public final ImageView image;
        public ImageView error;
        public View colorSquare;
        public String filename;
        private TextView notonText;

        public ThumbViewHolder(View itemView) {
            super(itemView);
            this.image = itemView.findViewById(R.id.gallery_image);
            this.error = itemView.findViewById(R.id.gallery_image_error);
            this.notonText = itemView.findViewById(R.id.notonTextView);
            this.colorSquare = itemView.findViewById(R.id.colorSquare);

            this.image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            // this.image.setPadding(8, 8, 8, 8);
            this.itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            // Forwards to the adapter's constructor callback
            int index = getAdapterPosition();
            ScanEntity scan = mScans.get(index);
            Intent i = new Intent(context, FullScreenViewActivity.class);
            i.putExtra("position", index);
            i.putExtra("scan_id", scan.getId());
            i.putParcelableArrayListExtra("title_body", (ArrayList<? extends Parcelable>) mScans);
            context.startActivity(i);

        }


    }
}
