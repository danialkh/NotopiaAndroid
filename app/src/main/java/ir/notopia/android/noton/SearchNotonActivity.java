package ir.notopia.android.noton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import ir.notopia.android.MainActivity;
import ir.notopia.android.R;
import ir.notopia.android.database.AppRepository;
import ir.notopia.android.database.entity.ScanEntity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import com.afollestad.dragselectrecyclerview.DragSelectRecyclerView;

import java.util.List;

public class SearchNotonActivity extends AppCompatActivity {

    private AppRepository mRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_noton);

        EditText ETSearchNoton = findViewById(R.id.ETSearchNoton);
        mRepository = AppRepository.getInstance(SearchNotonActivity.this);

        ImageView icon_back = findViewById(R.id.icon_back);
        icon_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.imageViweFadeOutFadeIn(icon_back);

                Intent intentBack = new Intent(SearchNotonActivity.this, MainActivity.class);
                intentBack.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intentBack);
            }
        });

        ETSearchNoton.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String text = ETSearchNoton.getText().toString();
                List<ScanEntity> mScans = mRepository.SearchTextNotons(text);

                SearchNotonAdaptor searchNotonAdaptor = new SearchNotonAdaptor(mScans, SearchNotonActivity.this);
                // new Utils(getApplicationContext()).getFilePaths(););

                RecyclerView seachNotonRecycleView = (DragSelectRecyclerView) findViewById(R.id.searchNotonRecycleView);
                seachNotonRecycleView.setLayoutManager(new GridLayoutManager(SearchNotonActivity.this, 3));
                seachNotonRecycleView.setAdapter(searchNotonAdaptor);



                Log.d("seach:",text);
                Log.d("seach Scan:",mScans.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


    }
}