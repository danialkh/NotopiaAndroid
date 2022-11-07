package ir.notopia.android.verification;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import ir.notopia.android.R;

import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.TextView;

public class ReadTermsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_read_terms);


        TextView TVTerms = findViewById(R.id.TVTerms);
        TVTerms.setText(Html.fromHtml(getString(R.string.terms)));

        CardView BTReadTerms = findViewById(R.id.BTReadTerms);
        BTReadTerms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReadTermsActivity.this.finish();
            }
        });



    }
}