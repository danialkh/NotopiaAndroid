package ir.notopia.android.menu;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import ir.notopia.android.MainActivity;
import ir.notopia.android.R;
import ir.notopia.android.adapter.AddTagAdaptor;
import ir.notopia.android.database.AppRepository;
import ir.notopia.android.database.entity.TagEntity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.afollestad.dragselectrecyclerview.DragSelectRecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class TagActivity extends AppCompatActivity {

    private AlertDialog.Builder builder;
    AppRepository mRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tag);
        loadRecycleTag(TagActivity.this);

        mRepository = AppRepository.getInstance(TagActivity.this.getApplicationContext());
        FloatingActionButton add_tag_floating_btn = findViewById(R.id.add_tag_floating_btn);


        ImageView icon_back = findViewById(R.id.icon_back);
        icon_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.imageViweFadeOutFadeIn(icon_back);
                Intent intentBack = new Intent(TagActivity.this, MainActivity.class);
                intentBack.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intentBack);
            }
        });

        add_tag_floating_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                builder = new AlertDialog.Builder(TagActivity.this);
                builder.setTitle("ایجاد برچسب جدید:");

                // Set up the input
                final EditText input = new EditText(TagActivity.this);
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("ثبت", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String m_Text = input.getText().toString();
                        TagEntity tag = new TagEntity(m_Text);

                        Log.d("checkCurrent",tag.toString());
                        mRepository.insertTag(tag);

                        final Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                loadRecycleTag(TagActivity.this);
                            }
                        }, 30);
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





    }

    public static void loadRecycleTag(Activity activity) {


        AddTagAdaptor addTagAdapator = new AddTagAdaptor(activity);

        DragSelectRecyclerView galleryRecycleView = (DragSelectRecyclerView) activity.findViewById(R.id.AddTagRecyclerview);
        galleryRecycleView.setLayoutManager(new GridLayoutManager(activity, 1));
        galleryRecycleView.setAdapter(addTagAdapator);


    }
}