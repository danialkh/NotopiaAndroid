package ir.notopia.android.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.dragselectrecyclerview.DragSelectRecyclerViewAdapter;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageSize;

import java.io.File;
import java.util.List;

import ir.notopia.android.R;
import ir.notopia.android.database.AppRepository;
import ir.notopia.android.database.entity.AttachEntity;
import ir.notopia.android.database.entity.ScanEntity;
import ir.notopia.android.noton.AttachActivity;
import ir.notopia.android.scanner.opennotescanner.helpers.Utils;

public class AttachAdaptor extends DragSelectRecyclerViewAdapter<AttachAdaptor.ThumbViewHolder> {

    private static final String TAG = "Attach Adaptor";

    private ImageLoader mImageLoader;
    private ImageSize mTargetSize;
    private List<AttachEntity> mAttachs;
    private ScanEntity mScan;
    private Activity _activity;
    private boolean SelectEnable;
    private AppRepository mRepository;


    // Constructor takes click listener callback
    public AttachAdaptor(Activity activity,ScanEntity mScan) {
        super();


        this.mScan = mScan;
        this._activity = activity;


        this.mRepository = AppRepository.getInstance(activity);
        mAttachs = mRepository.getAllAttachsFromNoton(mScan);

    }
    @Override
    public AttachAdaptor.ThumbViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.attact_item, parent, false);
        return new AttachAdaptor.ThumbViewHolder(v);
    }

    @Override
    public void onBindViewHolder(AttachAdaptor.ThumbViewHolder holder, int position) {
        super.onBindViewHolder(holder, position); // this line is important!

        AttachEntity mAttach = mAttachs.get(position);
        String filename = mScan.getImage();
        Log.i(TAG, "onBindViewHolder: " + mAttach.getType());

        switch (mAttach.getType()) {
            case "image/jpeg":
                holder.image.setImageDrawable(_activity.getResources().getDrawable(R.drawable.icon_gallery));
                break;
            case "audio/mpeg":
                holder.image.setImageDrawable(_activity.getResources().getDrawable(R.drawable.ic_audio));
                break;
            case "video/mp4":
                holder.image.setImageDrawable(_activity.getResources().getDrawable(R.drawable.ic_video));
                break;
            default:
                holder.image.setImageDrawable(_activity.getResources().getDrawable(R.drawable.ic_document));
                break;
        }

        holder.IVdelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder deleteConfirmBuilder;

                deleteConfirmBuilder = new AlertDialog.Builder(_activity,R.style.AlertDialogCustom);
                deleteConfirmBuilder.setTitle("حذف پیوست");
                deleteConfirmBuilder.setMessage("آیا مطمعن هستید میخواهید این پیوست را حذف کنید");
                deleteConfirmBuilder.setPositiveButton("بله", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {


                        removeAttachFile(mAttach);
                        mRepository.removeAttach(mAttach);


                        final Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                AttachActivity.LoadNotonAttach(_activity,mScan);
                            }
                        }, 50);



                        dialog.dismiss();
                    }

                    private void removeAttachFile(AttachEntity mAttach) {

                        String filePath = mAttach.getPath();
                        File attachFile = new File(filePath);

                        if(attachFile.exists()) {

                            //now let check if there is a noton with this attach file
                            int count = mRepository.countAttachsByFilePath(filePath);
                            // if there is one noton that has this attach so delete file
                            if(count == 1) {
                                attachFile.delete();
                                Utils.removeImageFromGallery(filePath, _activity);
                            }

                        }
                    }

                });


                deleteConfirmBuilder.setNegativeButton("لغو", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                deleteConfirmBuilder.create().show();


            }
        });





    }



    @Override
    public int getItemCount() {
        return mAttachs.size();
    }


    public class ThumbViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        public final ImageView image;
        private ImageView IVdelete;
        public ThumbViewHolder(View itemView) {
            super(itemView);
            this.image = itemView.findViewById(R.id.attach_image);

//            this.image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            // this.image.setPadding(8, 8, 8, 8);
            this.itemView.setOnClickListener(this);

            IVdelete = itemView.findViewById(R.id.IVBackDeleteAttach);
        }

        @Override
        public void onClick(View v) {
            // Forwards to the adapter's constructor callback

//            ScanEntity scan = mScans.get(index);
//            Intent i = new Intent(context, FullScreenViewActivity.class);
//            i.putExtra("position", index);
//            i.putExtra("scan_id", scan.getId());
//            i.putParcelableArrayListExtra("title_body", (ArrayList<? extends Parcelable>) mScans);
//            context.startActivity(i);


            int index = getAdapterPosition();
            String path = mAttachs.get(index).getPath();
            String strUri = mAttachs.get(index).getStrUri();
            openFile(path,strUri,_activity);

        }


    }




    private void openFile(String path,String strUri,Activity _activity) {

        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            String url = path;

            File file = new File(url);

            if (file.exists()) {

                // use file provider
                Uri uri = FileProvider.getUriForFile(_activity, _activity.getApplicationContext().getPackageName() + ".myprovider", file);

                Log.d(TAG, "Uri" + uri.toString());


                if (url.toString().contains(".doc") || url.toString().contains(".docx")) {
                    // Word document
                    intent.setDataAndType(uri, "application/msword");
                } else if (url.toString().contains(".pdf")) {
                    // PDF file
                    intent.setDataAndType(uri, "application/pdf");
                } else if (url.toString().contains(".ppt") || url.toString().contains(".pptx")) {
                    // Powerpoint file
                    intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
                } else if (url.toString().contains(".xls") || url.toString().contains(".xlsx")) {
                    // Excel file
                    intent.setDataAndType(uri, "application/vnd.ms-excel");
                } else if (url.toString().contains(".zip")) {
                    // ZIP file
                    intent.setDataAndType(uri, "application/zip");
                } else if (url.toString().contains(".rar")) {
                    // RAR file
                    intent.setDataAndType(uri, "application/x-rar-compressed");
                } else if (url.toString().contains(".rtf")) {
                    // RTF file
                    intent.setDataAndType(uri, "application/rtf");
                } else if (url.toString().contains(".wav") || url.toString().contains(".mp3")) {
                    // WAV audio file
                    intent.setDataAndType(uri, "audio/x-wav");
                } else if (url.toString().contains(".gif")) {
                    // GIF file
                    intent.setDataAndType(uri, "image/gif");
                } else if (url.toString().contains(".jpg") || url.toString().contains(".jpeg") || url.toString().contains(".png")) {
                    // JPG file
                    intent.setDataAndType(uri, "image/jpeg");
                } else if (url.toString().contains(".txt")) {
                    // Text file
                    intent.setDataAndType(uri, "text/plain");
                } else if (url.toString().contains(".3gp") || url.toString().contains(".mpg") ||
                        url.toString().contains(".mpeg") || url.toString().contains(".mpe") || url.toString().contains(".mp4") || url.toString().contains(".avi")) {
                    // Video files
                    intent.setDataAndType(uri, "video/*");
                } else {
                    intent.setDataAndType(uri, "*/*");
                }

                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                _activity.startActivity(intent);
            }
            else{
                Toast.makeText(_activity, "فایل پیوست یافت نشد", Toast.LENGTH_SHORT).show();
            }
        }
        catch (ActivityNotFoundException e) {
            Toast.makeText(_activity, "هیچ نرم افزاری جهت مشاهده پیوست یافت نشد", Toast.LENGTH_SHORT).show();
        }

    }
}
