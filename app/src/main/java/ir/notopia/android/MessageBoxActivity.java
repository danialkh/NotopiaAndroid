package ir.notopia.android;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import ir.notopia.android.adapter.NotificationsAdapter;
import ir.notopia.android.database.AppRepository;
import ir.notopia.android.database.entity.NotificationResponse;

public class MessageBoxActivity extends AppCompatActivity {
    public static AppRepository mRepository;
    private RecyclerView mNotifiRecyclerView;
    private NotificationsAdapter mAdapter;
    private List<NotificationResponse> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_box);
        Toolbar toolbar = findViewById(R.id.notopia_toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

            getSupportActionBar().setTitle(R.string.messages);
//        toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_action_back));
//            toolbar.setNavigationOnClickListener(v -> onBackPressed());
        }
        mRepository = AppRepository.getInstance(this);
        initRecyclerView();
    }

    private void initRecyclerView() {
        mNotifiRecyclerView = findViewById(R.id.nofications_recyclerview);
        mNotifiRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mNotifiRecyclerView.setLayoutManager(layoutManager);
        list = new ArrayList<>();
        list = mRepository.getAllNotificatios();
        mAdapter = new NotificationsAdapter(list, this);
        mNotifiRecyclerView.setAdapter(mAdapter);
    }

}
