package ir.notopia.android.ar;

import androidx.appcompat.app.AppCompatActivity;
import ir.notopia.android.MainActivity;
import ir.notopia.android.R;
import ir.notopia.android.utils.Constants;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewActivity extends AppCompatActivity {
    private WebView myWebView;
    private String Ordertype;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        myWebView = (WebView) findViewById(R.id.webview);
        // Configure related browser settings
        myWebView.getSettings().setLoadsImagesAutomatically(true);
        myWebView.getSettings().setJavaScriptEnabled(true);
        myWebView.getSettings().setUseWideViewPort(true);
        myWebView.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        // Configure the client to use when opening URLs
        myWebView.setWebViewClient(new WebViewClient());
        // Load the initial URL
        if (getIntent().hasExtra(Constants.WEB_VIEW_URL)) {
            String url = getIntent().getStringExtra(Constants.WEB_VIEW_URL);
            Ordertype = getIntent().getStringExtra("Ordertype");
            myWebView.loadUrl(url);
        }
    }

    @Override
    public void onBackPressed() {
        if(Ordertype.equals("ar")) {
            Intent arIntent = new Intent(this, ARActivity.class);
            startActivity(arIntent);
        }
        else{
            Intent MainIntent = new Intent(this, MainActivity.class);
            MainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(MainIntent);
        }
    }
}
