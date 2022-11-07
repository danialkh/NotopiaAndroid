package ir.notopia.android.menu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import ir.notopia.android.MainActivity;
import ir.notopia.android.R;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;

public class SettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        SwitchCompat switchCompat = findViewById(R.id.btnHelpScanSwitch);
        SwitchCompat switchAttach = findViewById(R.id.btnIncludeAttachSwitch);
        SwitchCompat switchAttachBackup = findViewById(R.id.btnIncludeAttachBackupSwitch);


        SharedPreferences includeAttach = SettingActivity.this.getSharedPreferences("includeAttach", Context.MODE_PRIVATE);
        String includeAttachState = includeAttach.getString("includeAttachState", "true");


        SharedPreferences includeAttachBackup = SettingActivity.this.getSharedPreferences("includeBackupAttach", Context.MODE_PRIVATE);
        String includeAttachBackupState = includeAttachBackup.getString("includeAttachBackupState", "true");

        SharedPreferences helpScan = SettingActivity.this.getSharedPreferences("helpScan", Context.MODE_PRIVATE);
        String helpScanState = helpScan.getString("helpScanState", "true");




        if(includeAttachBackupState.equals("true")){
            switchAttachBackup.setChecked(true);
        }
        else{
            switchAttachBackup.setChecked(false);
        }

        if(includeAttachState.equals("true")){
            switchAttach.setChecked(true);
        }
        else{
            switchAttach.setChecked(false);
        }




        if(helpScanState.equals("true")){
            switchCompat.setChecked(true);
        }
        else{
            switchCompat.setChecked(false);
        }



        switchAttachBackup.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String state = String.valueOf(isChecked);
                includeAttachBackup.edit().putString("includeAttachBackupState",state).apply();
            }
        });


        switchAttach.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String state = String.valueOf(isChecked);
                includeAttach.edit().putString("includeAttachState",state).apply();
            }
        });


        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String state = String.valueOf(isChecked);
                helpScan.edit().putString("helpScanState",state).apply();
            }
        });






        ImageView icon_back = findViewById(R.id.icon_back);
        icon_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.imageViweFadeOutFadeIn(icon_back);
                Intent intentBack = new Intent(SettingActivity.this, MainActivity.class);
                intentBack.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intentBack);
            }
        });
    }
}