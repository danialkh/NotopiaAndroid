package ir.notopia.android.adapter;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.dragselectrecyclerview.DragSelectRecyclerViewAdapter;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import ir.notopia.android.MainActivity;
import ir.notopia.android.R;
import ir.notopia.android.database.AppRepository;
import ir.notopia.android.database.entity.TagEntity;
import ir.notopia.android.menu.TagActivity;

public class AddTagAdaptor extends DragSelectRecyclerViewAdapter<AddTagAdaptor.TagsViewHolder> {
    private static final String TAG = "AddTagAdaptor";
    public static int myPosition = -1;
    private List<TagEntity> mTags;
    private AppRepository mRepository;
    private Activity _activity;
    private AlertDialog.Builder builder;

    public AddTagAdaptor(Activity _activity) {
        mRepository = AppRepository.getInstance(_activity.getApplicationContext());
        mTags = mRepository.getAllTags();
        this._activity = _activity;
    }

    @NonNull
    @Override
    public TagsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_tag_edit, parent, false);
        TagsViewHolder pvh = new TagsViewHolder(v,_activity,mTags);
        return pvh;
    }

    @Override
    public void onBindViewHolder(@NonNull TagsViewHolder holder, int position) {
        TagEntity tag = mTags.get(position);
        holder.TagTextEdit.setText(tag.getTagName());

        // delete tag click
        holder.TagBtnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.imageViweFadeOutFadeIn(holder.TagBtnDelete);

                builder = new AlertDialog.Builder(_activity);
                builder.setTitle("حذف برچسب: " + tag.getTagName());

                // Set up the input
                final TextView matn = new TextView(_activity);
                matn.setPadding(0,30,40,0);
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                matn.setText("آیا مطمعن هستید که این برچسب حذف شود؟");
                builder.setView(matn);

                // Set up the buttons
                builder.setPositiveButton("بله", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        mRepository.removeTag(tag);

                        final Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                TagActivity.loadRecycleTag(_activity);
                            }
                        }, 30);

//                            Toast.makeText(CategoryActivity.this,m_Text,Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("لغو", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });


        // edit tag click
        holder.TagBtnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.imageViweFadeOutFadeIn(holder.IVTagBtnEdit);

                builder = new AlertDialog.Builder(_activity);
                builder.setTitle("ویرایش برچسب: " + tag.getTagName());

                // Set up the input
                final EditText input = new EditText(_activity);
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("ثبت", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String m_Text = input.getText().toString();

                        tag.setTagName(m_Text);
                        Log.d("checkCurrent",tag.toString());
                        mRepository.updateTag(tag);

                        final Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                TagActivity.loadRecycleTag(_activity);
                            }
                        }, 30);

//                            Toast.makeText(CategoryActivity.this,m_Text,Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("لغو", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });

        Log.d(TAG,"ClickedPos:" + String.valueOf(myPosition));
        Log.d(TAG,"thisPos:" + String.valueOf(position));



    }

    @Override
    public int getItemCount() {
        return mTags.size();
    }

    public static class TagsViewHolder extends RecyclerView.ViewHolder{

        TextView TagTextEdit;
        CardView TagBtnEdit,TagBtnDelete;
        ImageView IVTagBtnEdit,TVTagBtnDelete;

        public TagsViewHolder(@NonNull View itemView,Activity _activity,List<TagEntity> mTags) {
            super(itemView);
            TagTextEdit = itemView.findViewById(R.id.TagTextEdit);
            TagBtnEdit = itemView.findViewById(R.id.TagBtnEdit);
            TagBtnDelete = itemView.findViewById(R.id.TagBtnDelete);
            TVTagBtnDelete = itemView.findViewById(R.id.TVTagBtnDelete);
            IVTagBtnEdit = itemView.findViewById(R.id.IVTagBtnEdit);
        }
    }
}

