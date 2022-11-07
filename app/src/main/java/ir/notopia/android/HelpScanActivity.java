package ir.notopia.android;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import ir.notopia.android.scanner.opennotescanner.OpenNoteScannerActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

public class HelpScanActivity extends AppCompatActivity {

    private String mScanId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_scan);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            this.mScanId = extras.getString("mScanId");
        }

        CardView btnGotHelp = findViewById(R.id.BtnGotHelpScan);
        btnGotHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentAi = new Intent(HelpScanActivity.this, OpenNoteScannerActivity.class);
                intentAi.putExtra("mScanId",HelpScanActivity.this.mScanId);
                startActivity(intentAi);
            }
        });

        ImageView icon_back = findViewById(R.id.icon_back);
        icon_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentBack = new Intent(HelpScanActivity.this, MainActivity.class);
                intentBack.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intentBack);
            }
        });
    }
}