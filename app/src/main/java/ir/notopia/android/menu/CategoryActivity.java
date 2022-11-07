package ir.notopia.android.menu;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import ir.notopia.android.MainActivity;
import ir.notopia.android.R;
import ir.notopia.android.database.AppRepository;
import ir.notopia.android.database.entity.CategoryEntity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class CategoryActivity extends AppCompatActivity {

    private String m_Text = "";
    private int currentCategoryId;
    private AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);


        ImageView icon_back = findViewById(R.id.icon_back);
        icon_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.imageViweFadeOutFadeIn(icon_back);
                Intent intentBack = new Intent(CategoryActivity.this, MainActivity.class);
                intentBack.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intentBack);
            }
        });

        loadCategorys();
    }

    private void loadCategorys() {

        AppRepository mRepository = AppRepository.getInstance(CategoryActivity.this);
        List<CategoryEntity> mCategorys = mRepository.getCategorys();

        Log.d("loadCategorys:",mCategorys.toString());

        for(int i = 1;i < 7;i++){

            String strId = "LYEditCategory" + i;
            String strSrc = "vc_icon_daste" + i;
            int id = this.getResources().getIdentifier(strId,"id",this.getPackageName());
            int src = this.getResources().getIdentifier(strSrc,"drawable",this.getPackageName());
            int identifier = 5 - (i - 1);
            ViewGroup view = findViewById(id);
            TextView categoryText = view.findViewById(R.id.CategoryTextEdit);
            CardView categoryBtnEdit = view.findViewById(R.id.CategoryBtnEdit);
            ViewGroup categoryView = view.findViewById(R.id.CategoryLayoutEdit);
            ImageView categoryImage = categoryView.findViewById(R.id.icon_item_layout);
            categoryImage.setImageDrawable(getResources().getDrawable(src, getApplicationContext().getTheme()));
            categoryText.setText(mCategorys.get(identifier).getName());

            ImageView IVBtn = categoryBtnEdit.findViewById(R.id.IVCategoryBtnEdit);
            categoryBtnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MainActivity.imageViweFadeOutFadeIn(IVBtn);
                    currentCategoryId = mCategorys.get(identifier).getId();
                    CategoryEntity currentCategory = mRepository.getCategoryById(currentCategoryId);

                    builder = new AlertDialog.Builder(CategoryActivity.this);
                    builder.setTitle("ویرایش دسته: " + currentCategory.getName());

                    // Set up the input
                    final EditText input = new EditText(CategoryActivity.this);
                    // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                    input.setInputType(InputType.TYPE_CLASS_TEXT);
                    builder.setView(input);

                    // Set up the buttons
                    builder.setPositiveButton("ثبت", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            m_Text = input.getText().toString();

                            currentCategory.setName(m_Text);
                            Log.d("checkCurrent",currentCategory.toString());
                            mRepository.updateCategory(currentCategory);

                            final Handler handler = new Handler(Looper.getMainLooper());
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    loadCategorys();
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

            view.setLayoutParams(view.getLayoutParams());
        }
    }
}